package graf.ethan.gutenberg.filter;

public abstract class Filter {
	
	public abstract int read();
	
	public abstract long skip(long n);
	
	public abstract void reset();

	public abstract boolean finished();
	
	public abstract void close();
}
