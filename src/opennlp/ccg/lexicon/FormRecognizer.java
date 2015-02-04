package opennlp.ccg.lexicon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import opennlp.ccg.parse.Chart;
import opennlp.ccg.parse.ChartCompleter;
import opennlp.ccg.parse.ChartCompleterFactory;
import opennlp.ccg.parse.ChartFactory;
import opennlp.ccg.parse.SparseChartFactory;
import opennlp.ccg.synsem.Category;
import opennlp.ccg.synsem.Symbol;
import opennlp.ccg.synsem.SymbolHash;
import opennlp.ccg.unify.UnifyControl;

/**
 * A word recognizer that implements a holistic approach to word recognition. It uses an automaton
 * for recognising words.
 * 
 * @author Daniel Couto-Vale
 */
public class FormRecognizer {

	/**
	 * Map from character to word recognizers
	 */
	private final Map<Character, FormRecognizer> recognizerMap;

	/**
	 * The chart factory 
	 */
	private final ChartFactory chartFactory;

	/**
	 * The chart completer factory 
	 */
	private final ChartCompleterFactory chartCompleterFactory;

	/**
	 * The types map
	 */
	private Map<Character, List<Association>> musterMap;

	/**
	 * The length of the forms stored in this map
	 */
	private int length;

	/**
	 * A lexicon
	 */
	private Lexicon lexicon;


	/**
	 * Constructor
	 */
	public FormRecognizer(Lexicon lexicon, ChartCompleterFactory chartCompleterFactory) {
		this(lexicon, chartCompleterFactory, new SparseChartFactory(), 1);
	}

	/**
	 * Constructor
	 * 
	 * @param chartFactory the chart factory
	 */
	public FormRecognizer(Lexicon lexicon, ChartCompleterFactory chartCompleterFactory, ChartFactory chartFactory) {
		this(lexicon, chartCompleterFactory, chartFactory, 1);
	}

	/**
	 * Constructor
	 * 
	 * @param chartFactory the chart factory
	 * @param length the length of the words stored at this level
	 */
	private FormRecognizer(Lexicon lexicon, ChartCompleterFactory chartCompleterFactory, ChartFactory chartFactory, int length) {
		this.lexicon = lexicon;
		this.chartCompleterFactory = chartCompleterFactory;
		recognizerMap = new HashMap<Character, FormRecognizer>();
		this.chartFactory = chartFactory;
		this.length = length;
	}

	/**
	 * Adds a word type to the word recognizer
	 *  
	 * @param pattern the pattern to add
	 * @param sort the type of the word
	 */
	public final void addAssociation(Association association) {
		addAssociation(association, 0);
	}

	/**
	 * Adds a word type to the word recognizer
	 *  
	 * @param form the pattern to add
	 * @param sort the type of the word
	 * @param index the index of the pattern
	 */
	public final void addAssociation(Association association, int index) {
		String form = association.getForm();
		if (index + 1 < form.length()) {
			char ch = form.charAt(index);
			FormRecognizer recognizer = recognizerMap.get(ch);
			if (recognizer == null) {
				recognizer = new FormRecognizer(lexicon, chartCompleterFactory, chartFactory, length + 1);
				recognizerMap.put(ch, recognizer);
			}
			recognizer.addAssociation(association, index + 1);
		} else {
			char ch = form.charAt(index);
			if (musterMap == null) {
				musterMap = new HashMap<Character, List<Association>>();
			}
			List<Association> types = musterMap.get(ch);
			if (types == null) {
				types = new LinkedList<Association>();
				musterMap.put(ch, types);
			}
			types.add(association);
		}
	}

	/**
	 * Recognizes words in a wording
	 * 
	 * @param characterSequence the wording
	 * @return a chart with recognized words
	 * @throws IOException when a chart document cannot be created
	 */
	public final Chart recognizeForms(String characterSequence) throws IOException {
		UnifyControl.startUnifySequence();
		Chart chart = chartFactory.makeChart(characterSequence.length());
		ChartCompleter chartCompleter = chartCompleterFactory.makeChartCompleter(chart);
		for (int index = 0; index < characterSequence.length(); index++) {
			recognizeForms(characterSequence, index, chartCompleter);
		}
		return chart;
	}

	/**
	 * Recognizes words.
	 * 
	 * @param wording the wording where to recognize words
	 * @param index the index of where to recognize words
	 * @param typeList the types of the recognized words
	 */
	private final void recognizeForms(String wording, int index, ChartCompleter chartCompleter) {
		char ch = wording.charAt(index);
		if (musterMap != null) {
			List<Association> musters = musterMap.get(ch);
			if (musters != null) {
				List<SymbolHash> symbolHashes = new ArrayList<SymbolHash>(musters.size());
				for (Association muster : musters) {
					try {
						symbolHashes.add(lexicon.recognizeMuster(muster));
					} catch (LexException e) {
						e.printStackTrace();
					}
				}
				for (SymbolHash symbolHash : symbolHashes) {
					for (Symbol symbol : symbolHash.getSignsSorted()) {
						int x1 = index - length + 1;
						int x2 = index;
						Category category = symbol.getCategory();
						UnifyControl.reindex(category);
						chartCompleter.annotateForm(x1, x2, symbol);
					}
				}
			}
		}
		if (index + 1 < wording.length()) {
			FormRecognizer recognizer = recognizerMap.get(ch);
			if (recognizer != null) {
				recognizer.recognizeForms(wording, index + 1, chartCompleter);
			}
		}
	}

	@Override
	public final String toString() {
		StringBuffer buffer = new StringBuffer();
		appendString(buffer, "");
		return buffer.toString();
	}

	/**
	 * Appends a string representing this subtree.
	 * 
	 * @param buffer the string buffer
	 * @param indent the indentation
	 */
	public final void appendString(StringBuffer buffer, String indent) {
		if (musterMap != null) {
			for (Character ch : musterMap.keySet()) {
				buffer.append(indent + "'" + ch + "' " + length + ":");
				List<Association> association = musterMap.get(ch);
				buffer.append(association.size());
				buffer.append("\n");
			}
		}
		for (Character ch : recognizerMap.keySet()) {
			buffer.append(indent + "'" + ch + "' " + length + "\n");
			FormRecognizer recognizer = recognizerMap.get(ch);
			recognizer.appendString(buffer, indent + " ");
		}
	}

}
