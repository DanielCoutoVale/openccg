package opennlp.ccg.parse;

/**
 * A factory of sparse charts.
 * 
 * @author Daniel Couto-Vale
 */
public class SparseChartFactory implements ChartFactory {

	@Override
	public Chart makeChart(int size) {
		return new SparseChart(size);
	}

}
