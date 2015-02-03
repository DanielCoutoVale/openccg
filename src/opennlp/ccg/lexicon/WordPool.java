package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import opennlp.ccg.util.Pair;

public class WordPool {

	// factory methods
	
	/** The word factory to use. */
	protected static WordFactory wordFactory = new FullWordFactory();

	/** Creates a core surface word from the given one, removing all attrs in the given set. */
	public static synchronized Word createCoreSurfaceWord(Word word, Set<String> attrsSet) {
	    String form = word.getForm();
	    String accent = word.getPitchAccent();
	    if (accent != null && attrsSet.contains(Tokenizer.TONE_ASSOCIATE)) accent = null;
	    List<Pair<String,String>> pairs = word.getFormalAttributes(); 
	    if (pairs != null) {
	        pairs = new ArrayList<Pair<String,String>>(pairs); 
	        Iterator<Pair<String,String>> pairsIt = pairs.iterator(); 
	        while (pairsIt.hasNext()) {
	            Pair<String,String> pair = pairsIt.next(); 
	            if (attrsSet.contains(pair.a)) { pairsIt.remove(); }
	        }
	        return createWord(form, accent, pairs, null, null, null, null);
	    }
	    else {
	        return createWordDirectly(form, accent, null, null, null, null, null);
	    }
	}

	/** Creates a full word from the given surface one, adding the given stem, POS and semantic class. */
	public static synchronized Word createFullWord(Word word, String stem, String POS, String supertag, String semClass) {
	    stem = (stem != null) ? stem.intern() : null; 
	    POS = (POS != null) ? POS.intern() : null;
	    supertag = (supertag != null) ? supertag.intern() : null;
	    semClass = (semClass != null) ? semClass.intern() : null; 
	    return createWordDirectly(word.getForm(), word.getPitchAccent(), word.getFormalAttributes(), stem, POS, supertag, semClass);
	}

	/** Creates a full word from the given surface one, 
	    adding the second (full) given word's stem, POS and semantic class, 
	    as well as the second word's additional attr-val pairs, 
	    plus the given supertag. */
	public static synchronized Word createFullWord(Word word, Word word2, String supertag) {
	    boolean mixedAttrs = false;
	    List<Pair<String,String>> pairs = word.getFormalAttributes(); 
	    List<Pair<String,String>> pairs2 = word2.getFormalAttributes(); 
	    if (pairs == null && pairs2 != null) { pairs = pairs2; }
	    else if (pairs2 != null) {
	        mixedAttrs = true;
	        pairs = new ArrayList<Pair<String,String>>(pairs); 
	        for (int i = 0; i < pairs2.size(); i++) {
	            if (!pairs.contains(pairs2.get(i))) {
	                pairs.add(pairs2.get(i)); 
	            }
	        }
	    }
	    if (mixedAttrs) { 
	        return createWord(
	            word.getForm(), word.getPitchAccent(), pairs, 
	            word2.getStem(), word2.getPOS(), supertag, word2.getSemClass()
	        );
	    }
	    else {
	        supertag = (supertag != null) ? supertag.intern() : null;
	        return createWordDirectly(
	            word.getForm(), word.getPitchAccent(), pairs, 
	            word2.getStem(), word2.getPOS(), supertag, word2.getSemClass()
	        );
	    }
	}

	/** Creates a surface word from the given one, removing the stem, POS, supertag and semantic class. */
	public static synchronized Word createSurfaceWord(Word word) {
	    return createWordDirectly(word.getForm(), word.getPitchAccent(), word.getFormalAttributes(), null, null, null, null);
	}

	/** Creates a surface word from the given one, removing the stem, POS, supertag and semantic class, 
	    and replacing the form with the given one. */
	public static synchronized Word createSurfaceWord(Word word, String form) {
	    form = (form != null) ? form.intern() : null; 
	    return createWordDirectly(form, word.getPitchAccent(), word.getFormalAttributes(), null, null, null, null);
	}

	/** Creates a surface word from the given one, removing the stem, POS, supertag and semantic class, 
	    and replacing the form with the semantic class, uppercased. */
	public static synchronized Word createSurfaceWordUsingSemClass(Word word) {
	    String form = word.getSemClass().toUpperCase().intern();
	    return createWordDirectly(form, word.getPitchAccent(), word.getFormalAttributes(), null, null, null, null);
	}

	// NB: could try different factory methods for concrete words, but 
	//     it's unclear whether it makes much difference
	// protected static WordFactory wordFactory = new FactorChainWord.Factory();
	
	/** Creates a surface word with the given form. */
	public static synchronized Word createWord(String form) { 
	    form = (form != null) ? form.intern() : null; 
	    return wordFactory.create(form);
	}

	/** Creates a (surface or full) word with the given attribute name and value.
	    The attribute names Tokenizer.WORD_ATTR, ..., Tokenizer.SEM_CLASS_ATTR 
	    may be used for the form, ..., semantic class. */
	public static synchronized Word createWord(String attributeName, String attributeValue) {
	    attributeName = attributeName.intern();
	    attributeValue = (attributeValue != null) ? attributeValue.intern() : null; 
	    return wordFactory.create(attributeName, attributeValue);
	}

	/** Creates a (surface or full) word. */
	public static synchronized Word createWord(
	    String form, String pitchAccent, List<Pair<String,String>> attrValPairs, 
	    String stem, String POS, String supertag, String semClass 
	) {
	    // normalize factors
	    form = (form != null) ? form.intern() : null; 
	    pitchAccent = (pitchAccent != null) ? pitchAccent.intern() : null;
	    if (attrValPairs != null) {
	        if (attrValPairs.isEmpty()) attrValPairs = null;
	        else {
	            attrValPairs = new ArrayList<Pair<String,String>>(attrValPairs);
	            Word.sortAttrValPairs(attrValPairs);
	            for (int i = 0; i < attrValPairs.size(); i++) {
	                Pair<String,String> p = attrValPairs.get(i);
	                String attr = p.a.intern();
	                String val = (p.b != null) ? p.b.intern() : null;
	                attrValPairs.set(i, new Pair<String,String>(attr, val));
	            }
	        }
	    }
	    stem = (stem != null) ? stem.intern() : null; 
	    POS = (POS != null) ? POS.intern() : null;
	    supertag = (supertag != null) ? supertag.intern() : null;
	    semClass = (semClass != null) ? semClass.intern() : null; 
	    // create word
	    return createWordDirectly(form, pitchAccent, attrValPairs, stem, POS, supertag, semClass);
	}

	/** Creates a (surface or full) word from the given one, replacing the word form with the given one. */
	public static synchronized Word createWord(Word word, String form) {
	    if (form != null) form = form.intern();
	    return createWordDirectly(
	        form, word.getPitchAccent(), word.getFormalAttributes(), 
	        word.getStem(), word.getPOS(), word.getSupertag(), word.getSemClass()
	    );
	}

	/** Creates a (surface or full) word directly, from the given canonical factors. */
	public static synchronized Word createWordDirectly(
	    String form, String pitchAccent, List<Pair<String,String>> attrValPairs, 
	    String stem, String POS, String supertag, String semClass 
	) {
	    return wordFactory.create(form, pitchAccent, attrValPairs, stem, POS, supertag, semClass);
	}

	/** Creates a (surface or full) word from the given one, 
	    replacing the form and stem with the semantic class, uppercased. */
	public static synchronized Word createWordUsingSemClass(Word word) {
	    String form = word.getSemClass().toUpperCase().intern();
	    String stem = form;
	    return createWordDirectly(
	        form, word.getPitchAccent(), word.getFormalAttributes(), 
	        stem, word.getPOS(), word.getSupertag(), word.getSemClass()
	    );
	}

	/** Creates a (surface or full) word from the given surface one, adding the 
	    second word's additional attr-val pairs. */
	public static synchronized Word createWordWithAttrs(Word word, Word word2) {
	    // get accent
	    String accent = word.getPitchAccent();
	    if (accent == null) accent = word2.getPitchAccent();
	    // get attrs
	    boolean mixedAttrs = false;
	    List<Pair<String,String>> pairs = word.getFormalAttributes();
	    List<Pair<String,String>> pairs2 = word2.getFormalAttributes(); 
	    if (pairs == null && pairs2 != null) { pairs = pairs2; }
	    else if (pairs2 != null) {
	        mixedAttrs = true;
	        pairs = new ArrayList<Pair<String,String>>(pairs); 
	        for (int i = 0; i < pairs2.size(); i++) {
	            if (!pairs.contains(pairs2.get(i))) {
	                pairs.add(pairs2.get(i)); 
	            }
	        }
	    }
	    // get rest
	    String form = word.getForm(); String stem = word.getStem(); 
	    String POS = word.getPOS(); String supertag = word.getSupertag(); String semClass = word.getSemClass(); 
	    // with mixed attrs, need to normalize
	    if (mixedAttrs) 
	        return createWord(form, accent, pairs, stem, POS, supertag, semClass);
	    else 
	        return createWordDirectly(form, accent, pairs, stem, POS, supertag, semClass);
	}

}
