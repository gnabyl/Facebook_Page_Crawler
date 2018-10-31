import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

class CustomCondition {
	public static ExpectedCondition<Boolean> customCondition() {
		return new ExpectedCondition<Boolean>() {

			@Override
			public Boolean apply(WebDriver browser) {
				JavascriptExecutor jex = (JavascriptExecutor) browser;
				return (Boolean) jex.executeScript("return ((document.documentElement.scrollHeight - document.documentElement.scrollTop - document.documentElement.clientHeight) > 100);");			
			}
			
		};
		
	}
}


class FBCrawler {
	String hostUrl, targetUrl, username, password;
	WebDriver browser = new ChromeDriver();
	List<WebElement> listPosts = new ArrayList<>();
	ArrayList<CrawlerResult> result = new ArrayList<>();
	String outputFolder;	


	public FBCrawler(String hostUrl, String targetUrl, String username, String password, String outputFolder) {
		this.hostUrl = hostUrl;
		this.targetUrl = targetUrl;
		this.username = username;
		this.password = password;
		this.outputFolder = outputFolder;
	}

	private void navigate(String url) {	
		browser.navigate().to(url);
	}
	
	public void startCrawler() {
		navigate(hostUrl);
		login();
		navigate(targetUrl + "/posts");
		new Actions(browser).sendKeys(Keys.ESCAPE).build().perform();
		try {
			Alert alertBox = browser.switchTo().alert();
			alertBox.dismiss();
		} catch (NoAlertPresentException e) {
			System.out.println("Alert passed");
		}
	}

	public void doCrawling() {
		int total = 0, currentPostCursor = 0, newPostCount;
		do {
			newPostCount = getListPosts();
			getPostsID(currentPostCursor);
			total += getPostsCommentsCount(currentPostCursor);
			loadMore();
			currentPostCursor += newPostCount;
			System.out.println(total + " " + listPosts.size());
		} while (total < 10000);		
		outputData();
		browser.close();	
	}

	private void outputData() {
		try {			
			FileWriter fw = new FileWriter(new File(outputFolder + "/" + targetUrl.substring(targetUrl.lastIndexOf('/') + 1)) + ".csv");
			int currentCount = 0;
			for (CrawlerResult r : result) {
				if (r.getID().equals("cantgetid"))
					continue;
				fw.write(r.getID() + "," + r.getCommentsCount() + "\n");
				currentCount += r.getCommentsCount();
				if (currentCount >= 10000)
					break;
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void login() {
		browser.findElement(By.id("email")).sendKeys(username);
		browser.findElement(By.id("pass")).sendKeys(password);
		browser.findElement(By.id("loginbutton")).click();
	}

	private int getListPosts() {	
		List<WebElement> newBLocks = browser.findElements(By.cssSelector("._1xnd"));
		List<WebElement> newPosts = newBLocks.get(newBLocks.size() - 1).findElements(By.cssSelector("._1xnd > ._4-u2 ._4-u8"));
		listPosts.addAll(newPosts);
		return newPosts.size();
	}

	private void loadMore() {
		JavascriptExecutor jex = (JavascriptExecutor) browser;
		jex.executeScript("window.scroll(0, document.documentElement.scrollHeight)");
		WebDriverWait wait = new WebDriverWait(browser, 10000);
		try {
			wait.until(CustomCondition.customCondition());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getPostsID(int currentCursor) {
		String url;
		String splited[];
		boolean found;
		
		for (int i = currentCursor; i < listPosts.size(); i ++) {
			WebElement element = listPosts.get(i);
			CrawlerResult currentResult = new CrawlerResult();
			List<WebElement> idSelector = element.findElements(By.cssSelector("._5pcq"));
			if (idSelector.size() == 0)
				currentResult.setID("cantgetid");
			else {
				url = idSelector.get(0).getAttribute("href");
				splited = url.split("/");
				found = false;
				for (int j = 0; j < splited.length; j++) {
					if (splited[j].matches("[0-9]+")) {
						currentResult.setID(splited[j]);
						found = true;
						break;
					}
				}
				if (!found)
					currentResult.setID(url.substring(url.lastIndexOf('/') + 1,
							url.indexOf('?') - (url.charAt(url.indexOf('?') - 1) == '/' ? 1 : 0)));
			}
			result.add(currentResult);
		}
	}

	private int getPostsCommentsCount(int currentCursor) {
		int total = 0;
		List<WebElement> footer_comment;
		String text_comment;
		for (int i = currentCursor; i < listPosts.size(); i++) {
			footer_comment = listPosts.get(i).findElements(By.cssSelector(".fcg.UFIPagerCount"));						
			if ((footer_comment.size() == 0) || (result.get(i).getID().equals("cantgetid"))) {
				result.get(i).setCommentsCount(0);
				continue;
			}
			text_comment = footer_comment.get(footer_comment.size() - 1).getAttribute("innerText");			
			if (text_comment.equals(""))
				result.get(i).setCommentsCount(0);
			else
				result.get(i).setCommentsCount(Integer.parseInt(text_comment
						.substring(text_comment.indexOf("of ") + 3, text_comment.length()).replace(",", "")));
			total += result.get(i).getCommentsCount();
		}		

		for (int i = 0; i < listPosts.size(); i++) {
			if (result.get(i).getCommentsCount() == 0) {
				try {
					text_comment = listPosts.get(i).findElement(By.cssSelector("._ipm")).getAttribute("innerText");
					if (!text_comment.contains("Comments")) {
						result.get(i).setCommentsCount(0);
						continue;
					}
					result.get(i).setCommentsCount(Integer.parseInt(text_comment.substring(0, text_comment.indexOf(" "))));
				} catch (NoSuchElementException e) {
					result.get(i).setCommentsCount(0);
				}
				total += result.get(i).getCommentsCount();
			}
		}
		return total;
	}

}
