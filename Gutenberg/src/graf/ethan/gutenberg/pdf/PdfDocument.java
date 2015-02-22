package graf.ethan.gutenberg.pdf;

import java.io.File;

public class PdfDocument {
	
	public File file;
	
	private PdfDictionary catalog;
	private PdfDictionary pageTree;
	private PdfDictionary trailer;
	
	public boolean linearized = false;
	
	public PdfDocument(File f) {
		this.file = f;
	}
	
	public PdfDictionary getCatalog() {
		return catalog;
	}
	
	public void setCatalog(PdfDictionary catalog) {
		this.catalog = catalog;
	}
	
	public PdfDictionary getPageTree() {
		return pageTree;
	}
	
	public void setPageTree(PdfDictionary pageTree) {
		this.pageTree = pageTree;
	}
	
	public PdfDictionary getTrailer() {
		return trailer;
	}
	
	public void setTrailer(PdfDictionary trailer) {
		this.trailer = trailer;
	}
}
