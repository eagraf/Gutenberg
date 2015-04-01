package graf.ethan.gutenberg.pdf;

import java.io.File;
import java.util.ArrayList;

public class PdfDocument {
	
	public File file;
	
	private PdfDictionary catalog;
	private PdfDictionary pageTree;
	private PdfDictionary trailer;
	
	public boolean linearized = false;
	
	private ArrayList<PdfDictionary> pages;
	
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
	
	@SuppressWarnings("unchecked")
	public void setPageTree(PdfDictionary pageTree) {
		this.pageTree = pageTree;
		pages = new ArrayList<>();
		getPages((ArrayList<PdfObjectReference>) pageTree.get("Kids"));
	}
	
	@SuppressWarnings("unchecked")
	private void getPages(ArrayList<PdfObjectReference> object) {
		for(int i = 0; i < object.size(); i ++) {
			String type = (String) ((PdfDictionary) pageTree.crossScanner.getObject(object.get(i))).get("Type");
			if(type.equals("Pages")) {
				getPages((ArrayList<PdfObjectReference>) ((PdfDictionary) pageTree.crossScanner.getObject(object.get(i))).get("Kids"));
			}
			else if(type.equals("Page")) {
				this.pages.add(((PdfDictionary) pageTree.crossScanner.getObject(object.get(i))));
			}
		}
	}
	
	public PdfDictionary getTrailer() {
		return trailer;
	}
	
	public void setTrailer(PdfDictionary trailer) {
		this.trailer = trailer;
	}
	
	public ArrayList<PdfDictionary> getPages() {
		return this.pages;
	}
}
