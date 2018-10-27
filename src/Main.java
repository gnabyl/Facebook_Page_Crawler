import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	static String username, password, inputFile;
	static Scanner sc = new Scanner(System.in);
	static ExecutorService threadPool;
	
	public static void main(String args[]) {
		System.out.println("Username: ");
		username = sc.nextLine();
		System.out.println("Password: ");
		password = sc.nextLine();
		System.out.println("Number of threads: ");
		int nThreads = sc.nextInt();
		threadPool = Executors.newFixedThreadPool(nThreads);
		
		String outputFolder = ".";
		inputFile = "./fanpage_list.txt";
		if (args.length != 0) {
			inputFile = args[0];
			outputFolder  = args[1];
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.lastIndexOf('/') == line.length() - 1)
					line = line.substring(0, line.length() - 1);
				threadPool.submit(new CrawlingTask(username, password, "https://www.facebook.com", line, outputFolder));
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
