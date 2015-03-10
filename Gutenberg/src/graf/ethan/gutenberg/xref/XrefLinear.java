package graf.ethan.gutenberg.xref;

import graf.ethan.gutenberg.pdf.PdfObjectReference;

public class XrefLinear extends Xref{
	
	public Xref xRef1;
	public Xref xRef2;
	
	@Override
	public Object getObject(PdfObjectReference reference) {
		for(int i = 0; i < xRef2.xrefSections.size(); i ++) {
			if(reference.objectNumber > xRef2.xrefSections.get(i).startNum && reference.objectNumber < xRef2.xrefSections.get(i).startNum + xRef2.xrefSections.get(i).length) {
				return xRef2.getObject(reference);
			}
		}
		return xRef1.getObject(reference);
	}
		
	@Override
	public long getObjectPosition(PdfObjectReference reference) {
		for(int i = 0; i < xRef2.xrefSections.size(); i ++) {
			if(reference.objectNumber > xRef2.xrefSections.get(i).startNum && reference.objectNumber < xRef2.xrefSections.get(i).startNum + xRef2.xrefSections.get(i).length) {
				return xRef2.getObjectPosition(reference);
			}
		}
		return xRef1.getObjectPosition(reference);
	}
}
