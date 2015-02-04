package opennlp.ccg.parse;

/**
 * A chart completer factory.
 * 
 * @author Daniel Couto-Vale
 */
public interface ChartCompleterFactory {

	/**
	 * Makes a chart completer
	 * 
	 * @param chart the chart to complete
	 * @return the chart completer factory
	 */
	public ChartCompleter makeChartCompleter(Chart chart);

}
