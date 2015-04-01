package graf.ethan.gutenberg.filter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class CCITTFaxDecoder {
	
	public static int EOL = 0x1001;
	
	public static final int[] WHITETERMINATOR = {
		//0-3
		0x135, 0x47, 0x17, 0x18,
		//4-7
		0x1B, 0x1C, 0x1E, 0x1F,
		//8-11
		0x33, 0x34, 0x27, 0x28,
		//12-15
		0x48, 0x43, 0x74, 0x75,
		//16-19
		0x6A, 0x6B, 0xA7, 0x8C,
		//20-23
		0x88, 0x97, 0x83, 0x84,
		//24-27
		0xA8, 0xAB, 0x93, 0xA4,
		//28-31
		0x98, 0x102, 0x103, 0x11A,
		//32-35
		0x11B, 0x112, 0x113, 0x114,
		//36-39
		0x115, 0x116, 0x117, 0x128,
		//40-43
		0x129, 0x12A, 0x12B, 0x12C,
		//44-47
		0x12D, 0x104, 0x105, 0x10A,
		//48-51
		0x10B, 0x152, 0x153, 0x154,
		//52-55
		0x155, 0x124, 0x125, 0x158,
		//56-59
		0x159, 0x15A, 0x15B, 0x14A,
		//60-63
		0x14B, 0x132, 0x133, 0x134
	};
	
	public static final int[] BLACKTERMINATOR = {
		//0-3
		0x437, 0xA, 0x7, 0x6,
		//4-7
		0xB, 0x13, 0x12, 0x23,
		//8-11
		0x45, 0x44, 0x84, 0x85,
		//12-15
		0x87, 0x104, 0x107, 0x218,
		//16-19
		0x417, 0x418, 0x408, 0x867,
		//20-23
		0x868, 0x86C, 0x837, 0x828,
		//24-27
		0x817, 0x818, 0x10CA, 0x10CB,
		//28-31
		0x10CC, 0x10CD, 0x1068, 0x1069,
		//32-35
		0x106A, 0x106B, 0x10D2, 0x10D3,
		//36-39
		0x10D4, 0x10D5, 0x10D6, 0x10D7,
		//40-43
		0x106C, 0x106D, 0x10DA, 0x10DB,
		//44-47
		0x1054, 0x1055, 0x1056, 0x1057,
		//48-51
		0x1064, 0x1065, 0x1052, 0x1053,
		//52-55
		0x1024, 0x1037, 0x1038, 0x1027,
		//56-59
		0x1028, 0x1058, 0x1059, 0x102B,
		//60-63
		0x102C, 0x105A, 0x1066, 0x1067
	};
	
	public static final int[] WHITEMAKEUP = {
		//64, 128, 192, 256
		0x3B, 0x32, 0x57, 0xB7,
		//320, 384, 448, 512 
		0x136, 0x137, 0x164, 0x165,
		//576, 640, 704, 768 
		0x168, 0x167, 0x2CC, 0x2CD,
		//832, 896, 960, 1024
		0x2D2, 0x2D3, 0x2D4, 0x2D5,
		//1088, 1152, 1216, 1280
		0x2D6, 0x2D7, 0x2D8, 0x2D9,
		//1344, 1408, 1472, 1536
		0x2DA, 0x2DB, 0x298, 0x299,
		//1600, 1664, 1728, 1792
		0x29A, 0x58, 0x29B, 0x808,
		//1856, 1920, 1984, 2048
		0x80C, 0x80D, 0x1012, 0x1013,
		//2112, 2176, 2240, 2304
		0x1014, 0x1015, 0x1016, 0x1017,
		//2368, 2432, 2496, 2560
		0x101C, 0x101D, 0x101E, 0x101F
	};
	
	public static final int[] BLACKMAKEUP = {
		//64, 128, 192, 256
		0x40F, 0x10C8, 0x10C9, 0x105B,
		//320, 384, 448, 512 
		0x1033, 0x1034, 0x1035, 0x206C,
		//576, 640, 704, 768 
		0x206D, 0x204A, 0x204B, 0x204C,
		//832, 896, 960, 1024
		0x204D, 0x2072, 0x2073, 0x2074,
		//1088, 1152, 1216, 1280
		0x2075, 0x2076, 0x2077, 0x2052,
		//1344, 1408, 1472, 1536
		0x2053, 0x2054, 0x2055, 0x205A,
		//1600, 1664, 1728, 1792
		0x205B, 0x205C, 0x29B, 0x808,
		//1856, 1920, 1984, 2048
		0x80C, 0x80D, 0x1012, 0x1013,
		//2112, 2176, 2240, 2304
		0x1014, 0x1015, 0x1016, 0x1017,
		//2368, 2432, 2496, 2560
		0x101C, 0x101D, 0x101E, 0x101F
	};
	
	private static final int PASS = 0x11;
	private static final int HORIZONTAL = 0x9;
	private static final int VERTICAL0 = 0x3;
	private static final int VERTICAL1 = 0xB;
	private static final int VERTICAL2 = 0x43;
	private static final int VERTICAL3 = 0x83;
	private static final int VERTICALM1 = 0xA;
	private static final int VERTICALM2 = 0x42;
	private static final int VERTICALM3 = 0x82;
	
	
	private CCITTFaxDecode filter;
	
	private int curr = 0;
	private int offset = 8;
	
	public int rows;
	public int columns;
	
	private BufferedImage img;
	
	int[] referenceLine;
	int[] currentLine;
	
	boolean white = true;
	
	int a0;
	int a1;
	int b1;
	int b2;
	
	public FileWriter writer;
	
	public CCITTFaxDecoder(CCITTFaxDecode filter, int rows, int columns) throws IOException {
		this.filter = filter;
		this.rows = rows;
		this.columns = columns;
		this.img = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_ARGB);
		
		try {
			writer = new FileWriter(new File("C:\\Users\\Ethan\\Desktop\\Gutenberg\\PDF Test\\output.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		writer.write("COLUMNS: " + columns + " ");
		writer.write("ROWS: " + rows);
		
		currentLine = new int[columns + 1];
		referenceLine = new int[columns + 1];
	}
	
	public void decodeT62D() throws IOException {
		a0 = 0;
		a1 = columns;
		b1 = columns;
		b2 = columns;
		Arrays.fill(referenceLine, 1);
		
		for(int y = 0; y < rows; y ++) {
			writer.write("\nLine " + y + " ");
			scanLine2D();
			
			for(int x = 0; x < columns; x ++) {
				img.setRGB(x, y, currentLine[x]);
			}
			
			referenceLine = currentLine;
		}
		
	}
	
	public void scanLine2D() throws IOException {
		int count = 0;
		white = true;
		while(count < columns) {
			int code = getCodingMode();
			writer.write("Coding Mode: " + code + " ");
			scanReferenceLine(count);
			switch(code) {
				case 4:
					//Pass Mode
					a1 = b2;
					break;
				case 5:
					//Horizontal Mode
					a1 = a0 + scanRun();
					fillRun();
					white = !white;
					count += a1 - a0;
					a0 = a1;
					a1 = a0 + scanRun();
					break;
				case 0:
				case 1:
				case 2:
				case 3:
				case -1:
				case -2:
				case -3:
					if(b1 == columns) {
						a1 = b1;
					}
					else {
						a1 = b1 + code;
					}
					break;
				default:
					a1 = 0;
					break;
			}
			fillRun();
			count += a1 - a0;
			a0 = a1;
			if(code != 4) {
				white = !white;
			}
		}
		a0 = 0;
	}
	
	public void scanReferenceLine(int pos) throws IOException {
		boolean done = false;
		int loc = pos + 1;
		//Get b1
		int val = white ? 1 : 0;
		while(!done) {	
			try {
				//If it is a changing element of opposite color.
				if(referenceLine[loc] != val && referenceLine[loc-1] == val) {
					b1 = loc;
					done = true;
				}
			}
			catch(ArrayIndexOutOfBoundsException e) {
				b1 = columns;
				done = true;
			}
			loc ++;
		}
		done = false;
		while(!done) {
			try {
				if(referenceLine[loc] == val) {
					b2 = loc;
					done = true;
				}
			}
			catch(ArrayIndexOutOfBoundsException e) {
				b2 = columns;
				done = true;
			}
			loc ++;
		}
		
		writer.write("Scan Reference Line: " + b1 + " ," + b2 + " ");
	}
	
	/*
	 * Fill a section of the current line.
	 */
	public void fillRun() throws IOException {
		writer.write("a0: " + a0 + " a1: " + a1 + " ");
		if(white) {
			for(int x = a0; x < a1 && x < columns; x ++) {
				currentLine[x] = 1;
			}
			writer.write("White Run: " + (a1 - a0) + " ");
		}
		else {
			for(int x = a0; x < a1 && x < columns; x ++) {
				currentLine[x] = 0;
			}
			writer.write("Black Run: " + (a1 - a0) + " ");
		}
	}
	
	/*
	 * Return the length of the next run composed of makeup and terminating words.
	 */
	public int scanRun() throws IOException {
		int nextWord = nextWord();
		if(nextWord >= 64) {
			int res = nextWord;
			writer.write("Makeup: " + nextWord + " ");
			while(true) {	
				nextWord = nextWord();
				if(nextWord >= 64) {
					res += nextWord;
					writer.write("Makeup: " + nextWord + " ");
				}
				else {
					res += nextWord;
					writer.write("Terminator: " + nextWord + " ");
					return res;
				}
			}
		}
		writer.write("Terminator: " + nextWord + " ");
		return nextWord;
	}
	
	/*
	 * Get the next bit from the current byte.
	 */
	public int nextBit() {
		if(offset == 8) {
			curr = filter.nextByte();
			offset = 0;
		}
		int res = (curr >> (7-offset)) & 1;
		offset ++;
		try {
			writer.write(res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	/*
	 * Return the next word.
	 */
	public int nextWord() {
		if(white) {
			return getWhiteWord();
		}
		return getBlackWord();
	}
	
	/*
	 * Get the next white code word. Returns 0-63 for terminating words and multiples of 64 for markup words.
	 */
	public int getWhiteWord() {
		int hex = 1;
		while(true) {
			hex *= 2;
			hex += nextBit();
			for(int i = 0; i < WHITETERMINATOR.length; i++) {
				if(hex == WHITETERMINATOR[i]) {
					return i;
				}
			}
			for(int i = 0; i < WHITEMAKEUP.length; i++) {
				if(hex == WHITEMAKEUP[i]) {
					return (i + 1) * 64;
				}
			}
		}
	}
	
	/*
	 * Get the next black code word. Returns 0-63 for terminating words and multiples of 64 for markup words.
	 */
	public int getBlackWord() {
		int hex = 1;
		while(true) {
			hex *= 2;
			hex += nextBit();
			for(int i = 0; i < BLACKTERMINATOR.length; i++) {
				if(hex == BLACKTERMINATOR[i]) {
					return i;
				}
			}
			for(int i = 0; i < BLACKMAKEUP.length; i++) {
				if(hex == BLACKMAKEUP[i]) {
					return (i + 1) * 64;
				}
			}
		}
	}
	
	public int getCodingMode() {
		int hex = 1;
		while(true) {
			hex *= 2;
			hex += nextBit();
			switch(hex) {
				case PASS:
					return 4;
				case HORIZONTAL:
					return 5;
				case VERTICAL0:
					return 0;
				case VERTICAL1:
					return 1;
				case VERTICAL2:
					return 2;
				case VERTICAL3:
					return 3;
				case VERTICALM1:
					return -1;
				case VERTICALM2:
					return -2;
				case VERTICALM3:
					return -3;
			}
		}
	}
	
	public BufferedImage getImage() {
		return img;
	}
}
