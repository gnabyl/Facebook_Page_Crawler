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
		if (args.length < 5) {
			System.out.println("Not enough arguments!");
			return;
		}
			
		String outputFolder = ".";
		inputFile = "./fanpage_list.txt";
		int nThreads = 4;
		if (args.length != 0) {
			inputFile = args[0];
			outputFolder  = args[1];
			username = args[2];
			password = args[3];
			nThreads = Integer.parseInt(args[4]);
		}
		
		System.out.println("Start crawling program with:\n"
				+ "Input: " + inputFile + "\n"
						+ "Output: " + outputFolder + "\n"
								+ "nThreads: " + nThreads);
		
		threadPool = Executors.newFixedThreadPool(nThreads);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.lastIndexOf('/') == line.length() - 1)
					line = line.substring(0, line.length() - 1);
				threadPool.submit(new CrawlingTask(username, password, "https://www.facebook.com", line, outputFolder));
			}
			threadPool.shutdown();
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
