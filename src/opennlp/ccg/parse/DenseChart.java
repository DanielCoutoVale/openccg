package opennlp.ccg.parse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * The implementation of a dense chart as a (n x n) table.
 * 
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Michael White
 * @author Daniel Couto-Vale
 */
public class DenseChart implements Chart, Serializable {

	/**
	 * Generated serial version
	 */
	private static final long serialVersionUID = -55772431506718482L;

	/**
	 * The data structure.
	 */
	private final Cell[][] table;

	/**
	 * Constructor
	 * 
	 * @param size the size of the table
	 */
	public DenseChart(int size) {
		table = new Cell[size][size];
	}

	/**
	 * Constructor
	 * 
	 * @param chartFile the chart file
	 * @throws IOException when there is a problem with reading the file
	 * @throws FileNotFoundException when the file is not found
	 */
	public DenseChart(File chartFile) throws FileNotFoundException , IOException {
		FileInputStream fis = new FileInputStream(chartFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);
		try {
			table = (Cell[][]) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw (RuntimeException) new RuntimeException().initCause(e);
		} finally {
			ois.close();
			bis.close();
			fis.close();
		}
	}

	@Override
	public final Cell getForm(int end, int hops) {
		return table[end][hops];
	}

	@Override
	public final void setForm(int end, int hops, Cell form) {
		table[end][hops] = form;
	}

	@Override
	public final int getSize() {
		return table.length;
	}

}
