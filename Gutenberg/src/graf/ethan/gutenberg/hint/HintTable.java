package graf.ethan.gutenberg.hint;

public class HintTable {
	
	//Page Offset Hint Table Header Entries
	//The least number of objects in a page (Including the object itself).
	public int pageOffset1;
	//The location of the first page's page object.
	public long pageOffset2;
	//The number of bits needed to represent the difference between the greatest and least number of objects in a page.
	public int pageOffset3;
	//The least length of a page in bytes.
	public long pageOffset4;
	//The number of bits needed to represent the difference between the greatest and least length of a page.
	public int pageOffset5;
	//The least offset of the start of any content stream, relative to the beginning of its page.
	public long pageOffset6;
	//The number of bits needed to represent the difference between the greatest and least offset to the start of a content stream.
	public int pageOffset7;
	//The least content stream length.
	public long pageOffset8;
	//The number of bits needed to represent the difference between the greatest and least content stream length.
	public int pageOffset9;
	//The number of bits needed to represent the greatest number of shared object references.
	public int pageOffset10;
	//The number of bits needed to represent the numerically greatest shared object identifier used by the pages.
	public int pageOffset11;
	//The number of bits needed to represent the numerator of the fractional position for each shared object reference.
	public int pageOffset12;
	//The denominator of the fractional position for each shared object reference.
	public int pageOffset13;
	
	//Shared Object Hint Table Header Entries
	//The object number of the first object in the shared object section.
	public int sharedObject1;
	//The location of the first object in the shared object section.
	public int sharedObject2;
	//The number of shared object entries for the first page.
	public int sharedObject3;
	//The number of shared object entries for the shared objects section, including the number of shared object entries in the first page.
	public int sharedObject4;
	//The number of bits needed to represent the greatest number of objects in a shared object group.
	public int sharedObject5;
	//The least length of a shared object group in bytes.
	public long sharedObject6;
	//The number of bits needed to represent the difference between the greatest and least length of a shared object group, in bytes.
	public int sharedObject7;
	
	//Thumbnail Hint Table Header Entries
	//The object number of the first thumbnail image.
	public int thumbnail1;
	//The location of the first thumbnail image.
	public int thumbnail2;
	//The number of pages that have thumbnail image.
	public int thumbnail3;
	//The number of bits needed to represent the greatest number of consecutive pages that do not have a thumbnail image.
	public int thumbnail4;
	//The least length of a thumbnail image in bytes.
	public int thumbnail5;
	//The number of bits needed to represent the difference betweent he greatest and least length of a thumbnail image.
	public int thumbnail6;
	//The least number of objects in a thumbnail image. 
	public int thumbnail7;
	//The number of bits needed to represent the difference between the greatest and least number of objects ina thumbnail image.
	public int thumbnail8;
	//The object number of the first object in the thumbnail shared objects section.
	public int thumbnail9;
	//The locationf of the first object in the thimbnail shared objects section.
	public int thumbnail10;
	//The number of thumbnail shared objects.
	public int thumbnail11;
	//The number of theumnail shared objects section in bytes.
	public int thumbnail12;
	
	//Generic Hint Tables
	public GenericHintTable outline;
	public GenericHintTable threadInformation;
	public GenericHintTable namedDestination;
	public GenericHintTable pageLabel;
	public GenericHintTable informationDictionary;
	
	//Extended Generic Hint Tables
	//Logical Structure (Extended Generic).
	
	
	//Interactive Form (Extended Generic).
	
	
	//Renditions Name Tree (Extended Generic).
	
	
	//Embedded File Stream (Extended Generic).
	
	
	public PageHintTable offsets[];
}
