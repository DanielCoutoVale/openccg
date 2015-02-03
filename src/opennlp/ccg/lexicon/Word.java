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

package opennlp.ccg.lexicon;

import opennlp.ccg.util.*;

import java.io.*;
import java.util.*;

import gnu.trove.*;

/**
 * A Word object may either be a surface word or a full word.
 * 
 * A surface word holds a surface form, an optional pitch accent, and an 
 * optional list of extra attribute-value pairs.
 * 
 * A full word additionally contains a stem, part of speech, supertag and semantic class.
 * 
 * A word may be a multiword consisting of multiple orthographic words, 
 * typically separated by underscores in the surface form.
 * 
 * For efficient storage and equality checking, Word objects are interned by 
 * the factory methods of the configured WordFactory.
 *
 * @author      Michael White
 * @version     $Revision: 1.21 $, $Date: 2009/12/16 22:39:20 $
 */
abstract public class Word implements Serializable, Comparable<Word> {

	private static final List<Pair<String, String>> EMPTY_PAIR_LIST = new ArrayList<Pair<String, String>>();
	private static final long serialVersionUID = 1L;

	/** Returns the surface form. */
    abstract public String getForm();
    
    /** Returns the pitch accent. */
    abstract public String getPitchAccent();
    /** Returns the list of extra attribute-value pairs. */
    abstract protected List<Pair<String,String>> getFormalAttributes();

    /** Returns the value of the attribute with the given name, or null if none. 
    The attribute names Tokenizer.WORD_ATTR, ..., Tokenizer.SEM_CLASS_ATTR 
    may be used to retrieve the form, ..., semantic class. */
    abstract public String getFormalAttributeValue(String formalAttributeName);
    
    /** Returns the stem. */
    abstract public String getStem();
    
    /** Returns the part of speech. */
    abstract public String getPOS();
    
    /** Returns the supertag. */
    abstract public String getSupertag();
    
    /** Returns the semantic class. */
    abstract public String getSemClass();
    
    // empty iterator
    private final static List<Pair<String,String>> emptyPairs = new ArrayList<Pair<String,String>>(0);

    /** Returns an iterator over the extra attribute-value pairs. */
    public final List<Pair<String,String>> getFormalAttributesProtected() { 
        List<Pair<String,String>> pairs = getFormalAttributes();
        return pairs != null ? pairs : emptyPairs;
    } 
    
    /** Returns an iterator over the surface attribute-value pairs, including the pitch accent (if any). */
    public List<Pair<String,String>> getSurfaceAttrValPairs() {
        List<Pair<String,String>> pairs = getFormalAttributes(); String pitchAccent = getPitchAccent();
        if (pairs == null && pitchAccent == null) return EMPTY_PAIR_LIST; 
        else if (pairs == null) { 
            List<Pair<String,String>> retval = new ArrayList<Pair<String,String>>(1); 
            retval.add(new Pair<String,String>(Tokenizer.TONE_ASSOCIATE, pitchAccent));
            return retval;
        }
        else if (pitchAccent == null) return pairs;
        else {
            List<Pair<String,String>> retval = new ArrayList<Pair<String,String>>(pairs);
            retval.add(new Pair<String,String>(Tokenizer.TONE_ASSOCIATE, pitchAccent));
            return retval;
        }
    }
    
    // the known attr names
    private static Set<String> knownAttrs = initKnownAttrs(); 
    @SuppressWarnings("unchecked")
	private static Set<String> initKnownAttrs() {
        Set<String> knownAttrs = new THashSet(new TObjectIdentityHashingStrategy());
        String[] names = {
            Tokenizer.WORD_ASSOCIATE, Tokenizer.TONE_ASSOCIATE, 
            Tokenizer.TERM_ASSOCIATE, Tokenizer.FUNCTIONS_ASSOCIATE, 
            Tokenizer.SUPERTAG_ASSOCIATE, Tokenizer.ENTITY_CLASS_ASSOCIATE
        };
        for (int i = 0; i < names.length; i++) { knownAttrs.add(names[i]); }
        return knownAttrs;
    }
    
    /** Returns whether the given attr is a known one (vs an extra one). */
    public static boolean isKnownAttr(String attr) {
        return knownAttrs.contains(attr.intern());
    }
    
    
    /** Returns true if the form is non-null, while the stem, part of speech, supertag and semantic class are null. */
    public boolean isSurfaceWord() {
        return getForm() != null && getStem() == null && getPOS() == null && getSupertag() == null && getSemClass() == null;
    }

    // NB: could try different factory methods for concrete words, but 
    //     it's unclear whether it makes much difference
    // protected static WordFactory wordFactory = new FactorChainWord.Factory();
    
    // comparator for attr-val pairs
    private static Comparator<Pair<String,String>> attrValComparator = new Comparator<Pair<String,String>>() {
        public int compare(Pair<String,String> p1, Pair<String,String> p2) {
            return p1.a.compareTo(p2.a);
        }
    };
    
    /** Sorts attr-val pairs by attr name. */
    public static void sortAttrValPairs(List<Pair<String,String>> pairs) {
        Collections.sort(pairs, attrValComparator);
    }
    
    /** Returns a hash code for this word. */
    public int hashCode() {
        int hc = System.identityHashCode(getForm());
        hc = 31*hc + System.identityHashCode(getPitchAccent());
        for (Pair<String,String> pair : getFormalAttributesProtected()) {
            hc = 31*hc + System.identityHashCode(pair.a);
            hc = 31*hc + System.identityHashCode(pair.b);
        }
        hc = 31*hc + System.identityHashCode(getStem()); 
        hc = 31*hc + System.identityHashCode(getPOS()); 
        hc = 31*hc + System.identityHashCode(getSupertag()); 
        hc = 31*hc + System.identityHashCode(getSemClass()); 
        return hc;
    }
    
    /** Returns whether this word equals the given object. */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        // nb: can use ==, since constructor interns all factors
        if (!(obj instanceof Word)) return false;
        Word word = (Word) obj;
        boolean sameFields =
            getForm() == word.getForm() && 
            getPitchAccent() == word.getPitchAccent() &&
            getStem() == word.getStem() && 
            getPOS() == word.getPOS() && 
            getSupertag() == word.getSupertag() && 
            getSemClass() == word.getSemClass();
        if (!sameFields) return false;
        List<Pair<String,String>> pairs = getFormalAttributes();
        List<Pair<String,String>> wordPairs = word.getFormalAttributes();
        if (pairs == null && wordPairs == null) return true;
        if (pairs == null || wordPairs == null) return false;
        if (pairs.size() != wordPairs.size()) return false;
        for (int i = 0; i < pairs.size(); i++) {
            if (!pairs.get(i).equals(wordPairs.get(i))) return false;
        }
        return true;
    }
    
    /** Returns an int representing lexicographic sorting. */
    public int compareTo(Word word) {
    	if (this == word) return 0;
    	int cmp = 0;
    	cmp = compare(getForm(), word.getForm());
    	if (cmp != 0) return cmp;
    	cmp = compare(getPitchAccent(), word.getPitchAccent());
    	if (cmp != 0) return cmp;
    	cmp = compare(getStem(), word.getStem());
    	if (cmp != 0) return cmp;
    	cmp = compare(getPOS(), word.getPOS());
    	if (cmp != 0) return cmp;
    	cmp = compare(getSupertag(), word.getSupertag());
    	if (cmp != 0) return cmp;
    	cmp = compare(getSemClass(), word.getSemClass());
    	if (cmp != 0) return cmp;
        List<Pair<String,String>> pairs = getFormalAttributes();
        List<Pair<String,String>> wordPairs = word.getFormalAttributes();
        if (pairs == null && wordPairs == null) return 0;
        if (pairs == null) return -1;
        if (wordPairs == null) return 1;
        if (pairs.size() < wordPairs.size()) return -1;
        if (pairs.size() > wordPairs.size()) return 1;
        for (int i = 0; i < pairs.size(); i++) {
        	Pair<String,String> p = pairs.get(i);
        	Pair<String,String> wp = wordPairs.get(i);
        	cmp = p.a.compareTo(wp.a);
        	if (cmp != 0) return cmp;
        	cmp = p.b.compareTo(wp.b);
        	if (cmp != 0) return cmp;
        }
    	return 0;
    }
    
    // compares strings, accounting for nulls
    private int compare(String s1, String s2) {
    	if (s1 == null && s2 == null) return 0;
    	if (s1 == null) return -1;
    	if (s2 == null) return 1;
    	return s1.compareTo(s2);
    }
    
    /** Returns whether this word's surface attributes intersect with the given ones. */
    public boolean attrsIntersect(Set<String> attrsSet) {
        if (getPitchAccent() != null && attrsSet.contains(Tokenizer.TONE_ASSOCIATE))
            return true;
        for (Pair<String,String> pair : getFormalAttributesProtected()) {
            if (attrsSet.contains(pair.a)) return true;
        }
        return false;
    }
    
    /** Returns a hash code for this word's restriction to a surface word. */
    public int surfaceWordHashCode() {
        int hc = System.identityHashCode(getForm());
        hc = 31*hc + System.identityHashCode(getPitchAccent());
        for (Pair<String,String> pair : getFormalAttributesProtected()) {
            hc = 31*hc + System.identityHashCode(pair.a);
            hc = 31*hc + System.identityHashCode(pair.b);
        }
        return hc;
    }
    
    /** Returns whether this word and the given object have equal restrictions to surface words. */
    public boolean surfaceWordEquals(Object obj) {
        if (this == obj) return true;
        // nb: can use ==, since constructor interns all factors
        if (!(obj instanceof Word)) return false;
        Word word = (Word) obj;
        boolean sameFields =
            getForm() == word.getForm() && 
            getPitchAccent() == word.getPitchAccent();
        if (!sameFields) return false;
        List<Pair<String,String>> pairs = getFormalAttributes();
        List<Pair<String,String>> wordPairs = word.getFormalAttributes();
        if (pairs == null && wordPairs == null) return true;
        if (pairs == null || wordPairs == null) return false;
        if (pairs.size() != wordPairs.size()) return false;
        for (int i = 0; i < pairs.size(); i++) {
            if (!pairs.get(i).equals(wordPairs.get(i))) return false;
        }
        return true;
    }
    
    /** Returns canonical version of deserialized word. */
    public Object readResolve() throws ObjectStreamException {
    	return WordPool.createWord(getForm(), getPitchAccent(), getFormalAttributes(), getStem(), getPOS(), getSupertag(), getSemClass());
    }
    
    /** Shows non-trivial fields separated by underscores. */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (getForm() != null) sb.append(getForm());
        if (getPitchAccent() != null) sb.append('_').append(getPitchAccent());
        for (Pair<String,String> pair : getFormalAttributesProtected()) {
            sb.append('_').append(pair.b);
        }
        if (getStem() != null && getStem() != getForm()) sb.append('_').append(getStem());
        if (getPOS() != null) sb.append('_').append(getPOS());
        if (getSupertag() != null) sb.append('_').append(getSupertag());
        if (getSemClass() != null) sb.append('_').append(getSemClass());
        if (sb.length() == 0) sb.append((String)null);
        return sb.toString();
    }
    
    /** Tests serialization. */
    public static void main(String[] argv) throws IOException, ClassNotFoundException {
    	// create words
    	Word w = WordPool.createWord("ran");
    	Word fw = WordPool.createFullWord(w, "run", "VBD", "s\\np", "MOTION");
    	Word wb = WordPool.createWordWithAttrs(w, WordPool.createWord("B", "L"));
    	// write to tmp.out
    	String filename = "tmp.ser";
    	ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
    	System.out.println("Writing w: " + w);
    	out.writeObject(w);
    	System.out.println("Writing fw: " + fw);
    	out.writeObject(fw);
    	System.out.println("Writing wb: " + wb);
    	out.writeObject(wb);
    	out.close();
    	// read from tmp.out
    	ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
    	System.out.print("Reading w2: ");
    	Word w2 = (Word) in.readObject();
    	System.out.println(w2);
    	System.out.print("Reading fw2: ");
    	Word fw2 = (Word) in.readObject();
    	System.out.println(fw2);
    	System.out.print("Reading wb2: ");
    	Word wb2 = (Word) in.readObject();
    	System.out.println(wb2);
    	in.close();
    	// test identity (and thus readResolve)
    	System.out.println("w == w2?: " + (w == w2));
    	System.out.println("fw == fw2?: " + (fw == fw2));
    	System.out.println("wb == wb2?: " + (wb == wb2));
    }
}

