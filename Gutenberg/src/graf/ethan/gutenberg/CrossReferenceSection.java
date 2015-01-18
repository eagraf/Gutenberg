package graf.ethan.gutenberg;

public class CrossReferenceSection {
	public int startNum;
	public long startPos;
	public int length;

	public CrossReferenceSection(int startNum, int length, long startPos) {
		this.startNum = startNum;
		this.startPos = startPos;
		this.length = length;
	}
}
