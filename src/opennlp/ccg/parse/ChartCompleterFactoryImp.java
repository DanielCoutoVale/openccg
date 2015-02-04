package opennlp.ccg.parse;

import opennlp.ccg.grammar.RuleGroup;

/**
 * An implementation of a chart completer factory.
 * 
 * @author Daniel Couto-Vale
 */
public class ChartCompleterFactoryImp implements ChartCompleterFactory {

	/**
	 * The rules to apply
	 */
	private final RuleGroup rules;

	/**
	 * The configuration of the chart completer
	 */
	private final ChartCompleterConfig config;

	/**
	 * Constructor
	 * 
	 * @param rules the rules to apply
	 * @param config the configuration of the chart completer
	 */
	public ChartCompleterFactoryImp(RuleGroup rules, ChartCompleterConfig config) {
		this.rules = rules;
		this.config = config;
	}

	@Override
	public ChartCompleter makeChartCompleter(Chart chart) {
		return new ChartCompleterImp(rules, chart,
				new ChartCompleterConfig(config));
	}

}
