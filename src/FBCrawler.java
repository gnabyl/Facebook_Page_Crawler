import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

class FBCrawler {
	String hostUrl, targetUrl, username, password;
	WebDriver browser = new ChromeDriver();
	List<WebElement> listPosts;
	ArrayList<CrawlerResult> result = new ArrayList<>();
	String outputFolder;

	String url;
	String splited[];
	boolean found;
		

	public void setBrowser(WebDriver browser) {
		this.browser = browser;		
	}

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
	}

	public void doCrawling() {				
		do {
			getListPosts();
			getPostsID();
			getPostsCommentsCount();
			loadMore();
		} while (getSumOfComments() < 10000);		
		outputData();
		browser.close();	
	}

	private void outputData() {
		FileWriter fw = null;
		try {
			
			fw = new FileWriter(new File(outputFolder + "/" + targetUrl.substring(targetUrl.lastIndexOf('/') + 1)) + ".csv");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int getSumOfComments() {
		int res = 0;
		for (CrawlerResult r : result) {
			if (r.getID().equals("cantgetid"))
				continue;
			res += r.getCommentsCount();			
		}
		return res;
	}

	private void login() {
		browser.findElement(By.id("email")).sendKeys(username);
		browser.findElement(By.id("pass")).sendKeys(password);
		browser.findElement(By.id("loginbutton")).click();
	}

	private void getListPosts() {		
		listPosts = browser.findElements(By.cssSelector("._1xnd > ._4-u2._4-u8"));
	}

	private void loadMore() {
		JavascriptExecutor jex = (JavascriptExecutor) browser;
		jex.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

	private void getPostsID() {
		result.clear();
		for (WebElement element : listPosts) {
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

	private void getPostsCommentsCount() {
		List<WebElement> footer_comment;
		String text_comment;
		System.out.println("Get Count\nPhase 1");
		for (int i = 0; i < listPosts.size(); i++) {
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
		}		

		for (int i = 0; i < listPosts.size(); i++) {
			if (result.get(i).getCommentsCount() == 0) {
				System.out.println(i);
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
			}
		}
	}

}
