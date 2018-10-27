
class CrawlingTask implements Runnable {
	
	String username, password, hostUrl, targetUrl, outputFolder;
	
	public CrawlingTask(String username, String password, String hostUrl, String targetUrl, String outputFolder) {
		this.username = username;
		this.password = password;
		this.hostUrl = hostUrl;
		this.targetUrl = targetUrl;
		this.outputFolder = outputFolder;
	}

	@Override
	public void run() {
		FBCrawler crawler = new FBCrawler(hostUrl, targetUrl, username, password, outputFolder);
		crawler.startCrawler();
		crawler.doCrawling();
	}
	
	

}
