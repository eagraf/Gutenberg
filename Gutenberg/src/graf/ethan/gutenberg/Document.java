package graf.ethan.gutenberg;

import java.io.File;

public class Document {
	
	public File file;
	
	private PdfDictionary catalog;
	private PdfDictionary pageTree;
	private PdfDictionary trailer;
	
	public boolean linearized = false;
	
	public Document(File f) {
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
