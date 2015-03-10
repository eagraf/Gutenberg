package graf.ethan.gutenberg.xref;

import java.util.ArrayList;

import graf.ethan.gutenberg.pdf.PdfObjectReference;

public class XrefList extends Xref {
	
	private ArrayList<Xref> Xrefs;
	
	public XrefList() {
		Xrefs = new ArrayList<Xref>();
	}
	
	public void addXref(Xref xref) {
		Xrefs.add(xref);
	}

	@Override
	public Object getObject(PdfObjectReference reference) {
		for(int i = 0; i < Xrefs.size(); i ++) {
			for(int j = 0; j < Xrefs.get(i).xrefSections.size(); j ++) {
				if(reference.objectNumber >= Xrefs.get(i).xrefSections.get(j).startNum && 
						reference.objectNumber < Xrefs.get(i).xrefSections.get(j).startNum + Xrefs.get(i).xrefSections.get(j).length) {
					return Xrefs.get(i).getObject(reference);
				}
			}
		}
		return null;
	}

	@Override
	public long getObjectPosition(PdfObjectReference reference) {
		for(int i = 0; i < Xrefs.size(); i ++) {
			for(int j = 0; j < Xrefs.get(i).xrefSections.size(); j ++) {
				if(reference.objectNumber >= Xrefs.get(i).xrefSections.get(j).startNum && 
						reference.objectNumber < Xrefs.get(i).xrefSections.get(j).startNum + Xrefs.get(i).xrefSections.get(j).length) {
					return Xrefs.get(i).getObjectPosition(reference);
				}
			}
		}
		return -1;
	}

}
