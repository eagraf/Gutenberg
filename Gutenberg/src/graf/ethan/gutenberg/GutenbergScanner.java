package graf.ethan.gutenberg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class GutenbergScanner {
	
	public FileReader fileReader;
	public BufferedReader bufferedReader;
	
	public GutenbergScanner(File f) {
		try {
			fileReader = new FileReader(f);
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found");
			e.printStackTrace();
		}
		bufferedReader = new BufferedReader(fileReader);
	}
	
	public void getVersion() {
		
	}
	
	public void firstPass() {
		
	}

}
