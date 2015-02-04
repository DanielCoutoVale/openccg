package opennlp.ccg.parse;

/**
 * A chart factory.
 * 
 * @author Daniel Couto-Vale
 */
public interface ChartFactory {

	/**
	 * Makes a chart.
	 * 
	 * @param size the size of the chart
	 * @return the chart
	 */
	public Chart makeChart(int size);

}
