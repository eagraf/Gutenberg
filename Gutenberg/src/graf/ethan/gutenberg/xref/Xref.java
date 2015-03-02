package graf.ethan.gutenberg.xref;

import java.util.ArrayList;

import graf.ethan.gutenberg.pdf.PdfObjectReference;

public abstract class Xref {
	
	public ArrayList<XrefSection> xrefs;
	
	public abstract Object getObject(PdfObjectReference reference);
	
	public abstract long getObjectPosition(PdfObjectReference reference);
}
