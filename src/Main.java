import java.util.Scanner;

public class Main {
	
	static String username;
	static String password;
	static Scanner sc = new Scanner(System.in);
	
	public static void main(String args[]) {
		System.out.println("Username: ");
		username = sc.nextLine();
		System.out.println("Password: ");
		password = sc.nextLine();
		
		String outputFolder = ".";
		if (args.length != 0)
			outputFolder  = args[0];
		
		
		FBCrawler crawler_tuyetcollection = new FBCrawler("https://www.facebook.com", "https://www.facebook.com/We.Are.vOzer", username, password, outputFolder);
		crawler_tuyetcollection.startCrawler();
		crawler_tuyetcollection.doCrawling();
		
	}
}
