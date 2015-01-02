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
    	
    	File f = new File("C:\\Users\\Ethan\\Desktop\\PDF Test\\FileScannerTest.txt");
    	
    	FileScanner scanner = new FileScanner(f);
    	
    	System.out.println(scanner.nextLine());
    	System.out.println("Line Number: " + scanner.getLineNumber());
    	
    	scanner.setPosition(18);
    	
    	System.out.println(scanner.nextInt());
    	System.out.println(scanner.nextLong());
    	System.out.println(scanner.nextShort());
    	System.out.println(scanner.nextFloat());
    	
    	System.out.println(scanner.next());
    	System.out.println(scanner.next());
    	System.out.println(scanner.next());
    	
    	scanner.setPosition(0);

    	System.out.println(scanner.next());
    } 
 
} 