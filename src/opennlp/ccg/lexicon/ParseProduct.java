package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.List;

import opennlp.ccg.parse.ChartCompleter;
import opennlp.ccg.parse.ScoredSymbol;
import opennlp.ccg.synsem.Symbol;

/**
 * The product of an automatic linguistic analysis.
 *
 * @author Daniel Couto-Vale
 */
public class ParseProduct {

	/**
	 * The duration of parse
	 */
	private int parseTime = 0;

	/**
	 * The duration of form recognition
	 */
	private int lexTime = 0;

	/**
	 * The duration of chart construction
	 */
	private int chartTime = 0;

	/**
	 * The duration of supertag unpacking
	 */
	private int unpackingTime = 0;

	/**
	 * The chart
	 */
	private ChartCompleter chartCompleter = null;

	/**
	 * The symbols
	 */
	private List<Symbol> symbols = new ArrayList<Symbol>();

	/**
	 * The symbol scores
	 */
	private List<Double> scores = new ArrayList<Double>();

	/**
	 * @return the lexTime
	 */
	public final int getLexTime() {
		return lexTime;
	}

	/**
	 * @param lexTime the lexTime to set
	 */
	public final void setLexTime(int lexTime) {
		this.lexTime = lexTime;
	}

	/**
	 * @return the parseTime
	 */
	public final int getParseTime() {
		return parseTime;
	}

	/**
	 * @param parseTime the parseTime to set
	 */
	public final void setParseTime(int parseTime) {
		this.parseTime = parseTime;
	}

	/**
	 * @return the chartTime
	 */
	public final int getChartTime() {
		return chartTime;
	}

	/**
	 * @param chartTime the chartTime to set
	 */
	public final void setChartTime(int chartTime) {
		this.chartTime = chartTime;
	}

	/**
	 * @return the unpackingTime
	 */
	public final int getUnpackingTime() {
		return unpackingTime;
	}

	/**
	 * @param unpackingTime the unpackingTime to set
	 */
	public final void setUnpackingTime(int unpackingTime) {
		this.unpackingTime = unpackingTime;
	}

	/**
	 * @return the chart
	 */
	public final ChartCompleter getChartCompleter() {
		return chartCompleter;
	}

	/**
	 * @param chartCompleter the chart to set
	 */
	public final void setChartCompleter(ChartCompleter chartCompleter) {
		this.chartCompleter = chartCompleter;
	}

	/**
	 * @param scoredSymbols the scored symbols
	 */
	public final void setScoredSymbols(List<ScoredSymbol> scoredSymbols) {
		List<Symbol> symbols = new ArrayList<Symbol>();
		List<Double> scores = new ArrayList<Double>();
		for (ScoredSymbol scoredSymbol : scoredSymbols) {
			symbols.add(scoredSymbol.getSymbol());
			scores.add(scoredSymbol.getScore());
		}
		setSymbols(symbols);
		setScores(scores);
	}

	/**
	 * @return the result
	 */
	public final List<Symbol> getSymbols() {
		return symbols;
	}

	/**
	 * @param symbols the result to set
	 */
	public final void setSymbols(List<Symbol> symbols) {
		this.symbols = symbols;
	}

	/**
	 * @return the scores
	 */
	public final List<Double> getScores() {
		return scores;
	}

	/**
	 * @param scores the scores to set
	 */
	public final void setScores(List<Double> scores) {
		this.scores = scores;
	}

}
