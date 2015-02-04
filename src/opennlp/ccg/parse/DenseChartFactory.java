package opennlp.ccg.parse;

/**
 * A factory of dense charts.
 * 
 * @author Daniel Couto-Vale
 */
public class DenseChartFactory implements ChartFactory {

	@Override
	public Chart makeChart(int size) {
		return new DenseChart(size);
	}

}
