package graf.ethan.gutenberg.filter;

public abstract class Predictor {

	public abstract int nextComponent();
	
	public abstract boolean finished();
	
	public abstract long skip(long n);
	
	public abstract void reset();
}
