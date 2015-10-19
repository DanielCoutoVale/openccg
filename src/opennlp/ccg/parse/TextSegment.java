package opennlp.ccg.parse;

/**
 * A segment delimited by first and last positions
 * 
 * @author Daniel Couto-Vale <daniel.couto-vale@ifaar.rwth-aachen.de>
 */
public class TextSegment {

	/**
	 * The first position of the segment.
	 */
	private final int first;

	/**
	 * The last position of the segment.
	 */
	private final int last;

	/**
	 * Constructor
	 * 
	 * @param first the first position of the segment
	 * @param last the last position of the segment
	 */
	public TextSegment(int first, int last) {
		this.first = first;
		this.last = last;
	}

	/**
	 * @return the first
	 */
	public int getFirst() {
		return first;
	}

	/**
	 * @return the last
	 */
	public int getLast() {
		return last;
	}
	
}
