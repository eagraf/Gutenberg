package graf.ethan.gutenberg;

public class XrefSection {
	public int startNum;
	public long startPos;
	public int length;

	public XrefSection(int startNum, int length, long startPos) {
		this.startNum = startNum;
		this.startPos = startPos;
		this.length = length;
	}
	
	public XrefSection(int startNum, int length) {
		this.startNum = startNum;
		this.length = length;
	}
}
