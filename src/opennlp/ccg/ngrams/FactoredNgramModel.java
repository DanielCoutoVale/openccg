///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004-5 University of Edinburgh (Michael White)
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//////////////////////////////////////////////////////////////////////////////

package opennlp.ccg.ngrams;

import opennlp.ccg.lexicon.*;
import opennlp.ccg.util.*;

import java.util.*;
import java.io.*;

/**
 * A scorer for a factored n-gram backoff model. The file format is the one
 * generated by the SRILM version 1.4.1 fngram-count tool. Only static backoff
 * orders are supported at present, with the most distant parent variable
 * dropped at each backoff point. Unknown words/factors are mapped to
 * &lt;unk&gt; if the latter is present in the model.
 *
 * @author Michael White
 * @version $Revision: 1.22 $, $Date: 2011/10/11 03:29:42 $
 */
public class FactoredNgramModel extends NgramScorer {
	/** Map for caching and reusing individual models by filename. */
	public static Map<String, FactoredNgramModel> modelCache = new HashMap<String, FactoredNgramModel>();

	/** The variable to predict. */
	public final ModelVariable child;

	/** The parent variables to condition on, backing off from the end. */
	public final ModelVariable[] parents;

	/** A variable in a factored n-gram model. */
	public class ModelVariable {
		/** The variable name. */
		public final String name;
		/** The (absolute value of the) position in the history. */
		public final int position;

		/** Makes a model variable from a string such as "W" or "W(-1)". */
		public ModelVariable(String str) {
			int leftparen = str.indexOf("(");
			if (leftparen > 0) {
				name = str.substring(0, leftparen).intern();
				int rightparen = str.indexOf(")");
				position = Math.abs(Integer.parseInt(str.substring(leftparen + 1, rightparen)));
			} else {
				name = str.intern();
				position = 0;
			}
		}
	}

	/**
	 * Loads a factored n-gram model for the given child variable, with the
	 * given parent variables, from the file with the given name, in the SRILM
	 * format. The flag for using sem classes is defaulted to true.
	 */
	public FactoredNgramModel(String child, String parents[], String filename) throws IOException {
		this(child, parents, filename, true);
	}

	/**
	 * Loads a factored n-gram model for the given child variable, with the
	 * given parent variables, from the file with the given name, in the SRILM
	 * format, and with the given flag for using sem classes.
	 */
	public FactoredNgramModel(String child, String parents[], String filename, boolean useSemClasses)
			throws IOException {
		this.useEntityClasses = useSemClasses;
		this.child = new ModelVariable(child);
		this.parents = new ModelVariable[parents.length];
		order = 1;
		for (int i = 0; i < parents.length; i++) {
			this.parents[i] = new ModelVariable(parents[i]);
			order = Math.max(order, this.parents[i].position + 1);
		}
		this.numNgrams = new int[(int) Math.pow(2, parents.length)];
		// check cache
		FactoredNgramModel cachedModel = modelCache.get(filename);
		if (cachedModel != null) {
			// share trie etc.
			numNgrams = cachedModel.numNgrams;
			openVocab = cachedModel.openVocab;
			trieMapRoot = cachedModel.trieMapRoot;
			cachedLogProbs = cachedModel.cachedLogProbs;
		}
		// otherwise load model
		else {
			Reader in = new BufferedReader(new FileReader(filename));
			readModel(in);
			modelCache.put(filename, this);
		}
	}

	/**
	 * Returns a list of feature keys for the ngram starting at the given index
	 * in wordsToScore and with the given order, using the keys in keysList
	 * after setting them appropriately with setKeysToNgram; returns null if
	 * this operation does not succeed normally. With factored models, factor
	 * keys are sequenced as two string keys.
	 */
	protected List<String> ngram(int i, int order) {
		boolean ok = setKeysToNgram(i, order);
		if (!ok)
			return null;
		featureKeysList.clear();
		for (int j = 0; j < keysList.size(); j++) {
			Object key = keysList.get(j);
			if (key instanceof String) {
				featureKeysList.add(Tokenizer.FORM_ASSOCIATE);
				featureKeysList.add((String) key);
			} else if (!(key instanceof FactorKey)) {
				throw new RuntimeException("Factor keys expected here!  key: " + key);
			} else {
				FactorKey fkey = (FactorKey) key;
				featureKeysList.add(fkey.factor);
				featureKeysList.add(fkey.val);
			}
		}
		return featureKeysList;
	}

	/**
	 * Returns the log prob of the ngram starting at the given index in
	 * wordsToScore and with the given order, with backoff. If using sem
	 * classes, then words with replacement sem classes are mapped to words with
	 * the sem class replacing the form and stem. Any remaining unknown
	 * words/factors are mapped to &lt;unk&gt;, if the latter is present in the
	 * model.
	 */
	// extracts factor keys from full words according to parents list,
	// then determines log prob from the list of factor keys
	protected float logProbFromNgram(int i, int order) {
		// skip initial start tag
		if (i == 0 && order == 1 && ((Association) wordsToScore.get(0)).getForm() == "<s>")
			return 0;
		// set up factor keys
		keysList.clear();
		int i0 = i + order - 1; // index of current word
		// determine last available parent, if full context not available
		int lastParentIndex = parents.length - 1;
		if (this.order > order) {
			for (int j = 0; j < parents.length; j++) {
				if (parents[j].position >= order) {
					lastParentIndex = j - 1;
					break;
				}
			}
		}
		// go through parents in reverse order,
		// extracting and adding factor keys
		for (int j = lastParentIndex; j >= 0; j--) {
			int pos_j = i0 - parents[j].position;
			if (pos_j < i)
				continue; // skip if pos_j past i
			Association w = (Association) wordsToScore.get(pos_j);
			keysList.add(makeFactorKey(w, parents[j].name));
		}
		// add factor key for child
		Association current = (Association) wordsToScore.get(i0);
		keysList.add(makeFactorKey(current, child.name));
		if (debugScore) {
			System.out.print("logp( " + keysList.get(keysList.size() - 1) + " | ");
			for (int j = keysList.size() - 2; j >= 0; j--) {
				System.out.print(keysList.get(j) + " ");
			}
			System.out.print(") = ");
		}
		// calc log prob from factor keys
		float retval = logProb(0, keysList.size());
		// NB: workaround for apparent bug in SRILM 1.4.1 fngram-count tool,
		// whereby prob for </s> does not use higher-order contexts for
		// factors other than W: just use zero
		if (current.getForm() == "</s>" && child.name != "W")
			retval = 0;
		if (debugScore)
			System.out.println("" + retval);
		return retval;
	}

	// makes a factor key from the given word by extracting
	// the attribute with the given name, where
	// the delimiter tokens are treated as a special case,
	// and the attr val is adjusted if using sem classes
	private Object makeFactorKey(Association w, String attr) {
		// special cases for <s> and </s>: just return
		// a word with this form, regardless of the attr
		String form = w.getForm();
		if (form == "<s>" || form == "</s>") {
			return FactorKey.getKey(attr, form);
		}
		// get val for this attr
		String val = w.getAssociateValue(attr);
		// check for sem class replacement for form or stem
		if (attr == Tokenizer.FORM_ASSOCIATE || attr == Tokenizer.TERM_ASSOCIATE) {
			String scr = semClassReplacement(w);
			if (scr != null)
				val = scr;
		}
		// make factor key
		Object retval = FactorKey.getKey(attr, val);
		// check for unknown val
		if (openVocab && trieMapRoot.getChild(retval) == null) {
			val = "<unk>";
			retval = FactorKey.getKey(attr, val);
		}
		// return
		return retval;
	}

	// reads in model
	private void readModel(Reader in) throws IOException {
		// setup
		// Tokenizer wordTokenizer = (Grammar.theGrammar != null)
		// ? Grammar.theGrammar.lexicon.tokenizer
		// : new DefaultTokenizer();
		StreamTokenizer tokenizer = initTokenizer(in);
		String[] tokens = new String[parents.length + 3];
		Object[] factorKeys = new Object[parents.length + 1];
		boolean foundData = false;
		int numParents = -1;
		int parentsInt = -1;
		List<Object> currentPrefix = new ArrayList<Object>();
		List<Object> currentKeys = null;
		List<TrieMap<Object, NgramFloats>> currentChildren = null;
		// loop through lines
		while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
			// read line into tokens
			readLine(tokenizer, tokens);
			// check for blank line
			if (tokens[0] == null)
				continue;
			// check for initial delimiter
			if (tokens[0].equals("\\data\\")) {
				foundData = true;
				continue;
			}
			if (!foundData)
				continue;
			// read header line
			if (tokens[0].equals("ngram")) {
				int equalPos = tokens[1].indexOf("=");
				int n = Integer.decode(tokens[1].substring(0, equalPos)).intValue();
				int total = Integer.parseInt(tokens[1].substring(equalPos + 1));
				numNgrams[n] = total;
				// init children, keys lists
				if (currentChildren == null) {
					currentChildren = new ArrayList<TrieMap<Object, NgramFloats>>(total);
					currentKeys = new ArrayList<Object>(total);
				}
				// calc totals (not actually used anymore)
				if (n == numNgrams.length - 1) {
					@SuppressWarnings("unused")
					int totalNgrams = 0;
					for (int i = 0; i < numNgrams.length; i++) {
						totalNgrams += numNgrams[i];
					}
					// System.out.println("totalNgrams: " + totalNgrams);
				}
				continue;
			}
			// check for final delimiter
			if (tokens[0].equals("\\end\\")) {
				addTrieMapChildren(currentPrefix, currentKeys, currentChildren);
				break;
			}
			// read line starting new parents context
			int gramsPos = -1;
			if (tokens[0].startsWith("\\") && (gramsPos = tokens[0].indexOf("-grams:")) > 0) {
				// add current children
				addTrieMapChildren(currentPrefix, currentKeys, currentChildren);
				// update num parents
				// System.out.println(tokens[0]);
				parentsInt = Integer.decode(tokens[0].substring(1, gramsPos)).intValue();
				numParents = numParents(parentsInt);
				continue;
			}
			if (numParents < 0)
				continue;
			// current order is num parents plus one for child var
			int currentOrder = numParents + 1;
			// parse a line of the form
			// <prob> <p1> <p2> ... <pN> <c> [<bow>]
			// i.e. a log prob, followed by N vars, a child var, and an optional
			// back off weight
			// NB: unlike the ARPA format, here the <bow> here is
			// associated with the backoff of <p1> <p2> ... <pN>
			// rather than context consisting of <p1> <p2> ... <pN> <c>
			// read logprob
			float logprob = Float.parseFloat(tokens[0]);
			// unescape, intern factor keys
			for (int i = 1; i < currentOrder + 1; i++) {
				String attr = (i == currentOrder) ? child.name
						: parents[(currentOrder - i) - 1].name;
				String val = tokens[i];
				int hyphenPos = val.indexOf('-');
				if (hyphenPos > 0) {
					String attrCheck = val.substring(0, hyphenPos).intern();
					if (attr != attrCheck) {
						System.err.println("Warning: expected attr " + attr + " rather than "
								+ attrCheck + " in " + tokens[i]);
					}
					val = val.substring(hyphenPos + 1);
				}
				val = DefaultTokenizer.unescape(val);
				if (val != null)
					val = val.intern();
				factorKeys[i - 1] = FactorKey.getKey(attr, val);
			}
			// check prefix
			boolean samePrefix = (currentPrefix.size() == currentOrder - 1);
			for (int i = 0; samePrefix && i < currentOrder - 1; i++) {
				if (factorKeys[i] != currentPrefix.get(i))
					samePrefix = false;
			}
			// if changed, add current children, reset prefix
			if (!samePrefix) {
				addTrieMapChildren(currentPrefix, currentKeys, currentChildren);
				for (int i = 0; i < currentOrder - 1; i++) {
					currentPrefix.add(factorKeys[i]);
				}
			}
			Object key = factorKeys[currentOrder - 1];
			currentKeys.add(key);
			currentChildren.add(new TrieMap<Object, NgramFloats>(new NgramFloats(logprob, 0)));
			// read back-off weight, if present
			if (tokens[currentOrder + 1] != null) {
				float bow = Float.parseFloat(tokens[currentOrder + 1]);
				// add to prefix node
				TrieMap<Object, NgramFloats> prefixNode = trieMapRoot
						.findChildFromList(currentPrefix);
				NgramFloats nfloats = prefixNode.data;
				if (nfloats != null)
					nfloats.bow = bow;
				else
					prefixNode.data = new NgramFloats(0, bow);
			}
		}
		// set openVocab according to presence of child <unk>
		Object unkKey = FactorKey.getKey(child.name, "<unk>");
		openVocab = (trieMapRoot.getChild(unkKey) != null);
	}

	// returns the number of parents present in the parentsInt spec
	private int numParents(int parentsInt) {
		int retval = 0;
		for (int i = 0; i < parents.length; i++) {
			if ((parentsInt & 1) != 0)
				retval++;
			parentsInt = parentsInt >> 1;
		}
		return retval;
	}

	/** Test loading and scoring. */
	// NB: This produces the same scores as the SRILM fngram tool when both
	// <s> and </s> tags are used, except that ...
	// NB: There is a workaround for an apparent bug in SRILM 1.4.1 fngram-count
	// tool,
	// whereby prob for </s> does not use higher-order contexts for
	// factors other than W, and thus a log prob of zero is just used instead.
	public static void main(String[] args) throws IOException {

		String usage = "Usage: java opennlp.ccg.ngrams.FactoredNgramModel <child> <parents> <lmfile> <tokens>";

		if (args.length > 0 && args[0].equals("-h")) {
			System.out.println(usage);
			System.exit(0);
		}

		String child = args[0];
		String[] parents = args[1].split("\\s+");
		String lmfile = args[2];
		String tokens = args[3];

		System.out.println("Loading n-gram model from: " + lmfile);
		FactoredNgramModel lm = new FactoredNgramModel(child, parents, lmfile, true);
		System.out.println("child var: " + lm.child.name);
		for (int i = 0; i < lm.parents.length; i++) {
			System.out.println("parent var: (" + lm.parents[i].name + "," + lm.parents[i].position
					+ ")");
		}
		System.out.println("order: " + lm.order);
		System.out.println("openVocab: " + lm.openVocab);
		System.out.println();
		// System.out.println("trie map: ");
		// System.out.println(lm.trieMapRoot.toString());
		// System.out.println();

		Tokenizer tokenizer = new DefaultTokenizer();
		List<Association> words = tokenizer.tokenize(tokens, true);
		System.out.println("scoring: ");
		for (int i = 0; i < words.size(); i++) {
			System.out.println(words.get(i).toString());
		}
		System.out.println();
		lm.debugScore = true;
		lm.setWordsToScore(words, true);
		lm.prepareToScoreWords();
		double logprob = lm.logprob();
		double score = convertToProb(logprob);
		System.out.println();
		System.out.println("score: " + score);
		System.out.println("logprob: " + logprob);
		System.out.println("ppl: " + NgramScorer.convertToPPL(logprob / (words.size() - 1)));
	}
}
