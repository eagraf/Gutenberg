package graf.ethan.gutenberg;

import java.io.*;

public class GutenbergCore {
 
    public static void main(String[] args) throws IOException {
    	/*
        File f=new File("C:\\Users\\Ethan\\Desktop\\PDF Test\\minimal.pdf");
 
        OutputStream oos = new FileOutputStream("C:\\Users\\Ethan\\Desktop\\PDF Test\\output.txt");
 
        byte[] buf = new byte[8192];
 
        InputStream is = new FileInputStream(f);
 
        int c = 0;
 
        while ((c = is.read(buf, 0, buf.length)) > 0) {
            oos.write(buf, 0, c);
            oos.flush();
        } 
 
        oos.close();
        System.out.println("stop");
        is.close();*/
    	
    	File f = new File("C:\\Users\\Ethan\\Desktop\\PDF Test\\PdfScannerTest2.txt");
    	
    	FileScanner scanner = new FileScanner(f);
    	PdfScanner pdfScanner = new PdfScanner(scanner);
    	
    	try {
			System.out.println(pdfScanner.scanString());
		} catch (NotCorrectPdfTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
			System.out.println(pdfScanner.scanName());
		} catch (NotCorrectPdfTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
} 