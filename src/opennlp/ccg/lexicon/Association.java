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
 * An association between structures of different strata/layers. In the
 * morphological stratum/layer, there may exist a form, a tone, or a caps. In
 * the lexicogrammatical stratum/layer, there may exist grammatical functions,
 * grammatical features, and a lexical term. In the rhetoricosemantic
 * stratum/layer, there may exist a named entity and/or a named entity class.
 * Each structure of the association is an associate.
 * 
 * An association may include only a form associate or form and tone associates.
 * In such cases, the association is said to be formal.
 * 
 * An associate canon contains not only formal associates, but also
 * lexicogrammatical and rhetoricosemantic ones. This includes currently
 * grammatical functions, a lexical term, a supertag, and an entity class.
 * 
 * An association has the boundaries of a grammatical unit. It may therefore
 * correspond to a series of written words such as "in front" in
 * "in front of the building". Typically, because the tokenizer uses space for
 * words, it is common to represent "in front" as "in_front" both in the
 * association base and in the character sequence to be parsed.
 * 
 * For efficient storage and equality checking, Associations are interned by
 * factory methods.
 *
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.21 $, $Date: 2009/12/16 22:39:20 $
 */
abstract public class Association implements Serializable, Comparable<Association> {

	private static final List<Pair<String, String>> NO_ASSOCIATES = new ArrayList<Pair<String, String>>();
	private static final long serialVersionUID = 1L;

	/** Returns the surface form. */
	abstract public String getForm();

	/** Returns the pitch accent. */
	abstract public String getTone();

	/** Returns the list of extra attribute-value pairs. */
	abstract protected List<Pair<String, String>> getAssociates();

	/**
	 * Returns the value of the attribute with the given name, or null if none.
	 * The attribute names Tokenizer.WORD_ATTR, ..., Tokenizer.SEM_CLASS_ATTR
	 * may be used to retrieve the form, ..., semantic class.
	 */
	abstract public String getAssociateValue(String formalAttributeName);

	/** Returns the stem. */
	abstract public String getTerm();

	/** Returns the part of speech. */
	abstract public String getFunctions();

	/** Returns the supertag. */
	abstract public String getSupertag();

	/** Returns the semantic class. */
	abstract public String getEntityClass();

	/**
	 * @return non-canonical associates
	 */
	public final List<Pair<String, String>> getNonCanonicalAssociates() {
		List<Pair<String, String>> associates = getAssociates();
		return associates != null ? associates : NO_ASSOCIATES;
	}

	/**
	 * @return tone and non-canonical associates
	 */
	public final List<Pair<String, String>> getToneAndNonCanonicalAssociates() {
		List<Pair<String, String>> associates = getAssociates();
		String tone = getTone();
		if (associates == null && tone == null) {
			return NO_ASSOCIATES;
		} else if (associates == null) {
			associates = new ArrayList<Pair<String, String>>(1);
			associates.add(new Pair<String, String>(Tokenizer.TONE_ASSOCIATE, tone));
			return associates;
		} else if (tone == null) {
			return associates;
		} else {
			associates = new ArrayList<Pair<String, String>>(associates);
			associates.add(new Pair<String, String>(Tokenizer.TONE_ASSOCIATE, tone));
			return associates;
		}
	}

	/**
	 * The known associate keys
	 */
	private final static Set<String> knownAssociateKeys = initKnownAssciateKeys();

	@SuppressWarnings("unchecked")
	private final static Set<String> initKnownAssciateKeys() {
		Set<String> knownAssociateKeys = new THashSet(new TObjectIdentityHashingStrategy());
		String[] names = { Tokenizer.FORM_ASSOCIATE, Tokenizer.TONE_ASSOCIATE,
				Tokenizer.CAPS_ASSOCIATE, Tokenizer.TERM_ASSOCIATE, Tokenizer.FUNCTIONS_ASSOCIATE,
				Tokenizer.SUPERTAG_ASSOCIATE, Tokenizer.ENTITY_CLASS_ASSOCIATE,
				Tokenizer.ENTITY_ASSOCIATE };
		for (int i = 0; i < names.length; i++) {
			knownAssociateKeys.add(names[i]);
		}
		return knownAssociateKeys;
	}

	/**
	 * Checks whether an associate key is known
	 * 
	 * @param associateKey the associate key
	 * @return <code>true</code> if the associate key is known and
	 *         <code>false</code> otherwise
	 */
	public final static boolean checkAssociateKeyKnown(String associateKey) {
		return knownAssociateKeys.contains(associateKey.intern());
	}

	/**
	 * @return <code>true</code> if all associates are formal and
	 *         <code>false</code> otherwise
	 */
	public final boolean isFormal() {
		return getForm() != null && getTerm() == null && getFunctions() == null
				&& getSupertag() == null && getEntityClass() == null;
	}

	// comparator for attr-val pairs
	private final static Comparator<Pair<String, String>> attrValComparator = new Comparator<Pair<String, String>>() {
		public int compare(Pair<String, String> p1, Pair<String, String> p2) {
			return p1.a.compareTo(p2.a);
		}
	};

	/** Sorts attr-val pairs by attr name. */
	public final static void sortAttrValPairs(List<Pair<String, String>> pairs) {
		Collections.sort(pairs, attrValComparator);
	}

	/** Returns a hash code for this word. */
	public final int hashCode() {
		int hc = System.identityHashCode(getForm());
		hc = 31 * hc + System.identityHashCode(getTone());
		for (Pair<String, String> pair : getNonCanonicalAssociates()) {
			hc = 31 * hc + System.identityHashCode(pair.a);
			hc = 31 * hc + System.identityHashCode(pair.b);
		}
		hc = 31 * hc + System.identityHashCode(getTerm());
		hc = 31 * hc + System.identityHashCode(getFunctions());
		hc = 31 * hc + System.identityHashCode(getSupertag());
		hc = 31 * hc + System.identityHashCode(getEntityClass());
		return hc;
	}

	/** Returns whether this word equals the given object. */
	public final boolean equals(Object o) {
		if (this == o)
			return true;
		// nb: can use ==, since constructor interns all factors
		if (!(o instanceof Association))
			return false;
		Association word = (Association) o;
		boolean sameFields = getForm() == word.getForm() && getTone() == word.getTone()
				&& getTerm() == word.getTerm() && getFunctions() == word.getFunctions()
				&& getSupertag() == word.getSupertag() && getEntityClass() == word.getEntityClass();
		if (!sameFields)
			return false;
		List<Pair<String, String>> pairs = getAssociates();
		List<Pair<String, String>> wordPairs = word.getAssociates();
		if (pairs == null && wordPairs == null)
			return true;
		if (pairs == null || wordPairs == null)
			return false;
		if (pairs.size() != wordPairs.size())
			return false;
		for (int i = 0; i < pairs.size(); i++) {
			if (!pairs.get(i).equals(wordPairs.get(i)))
				return false;
		}
		return true;
	}

	/** Returns an int representing lexicographic sorting. */
	public final int compareTo(Association association) {
		if (this == association)
			return 0;
		int cmp = 0;
		cmp = compare(getForm(), association.getForm());
		if (cmp != 0)
			return cmp;
		cmp = compare(getTone(), association.getTone());
		if (cmp != 0)
			return cmp;
		cmp = compare(getTerm(), association.getTerm());
		if (cmp != 0)
			return cmp;
		cmp = compare(getFunctions(), association.getFunctions());
		if (cmp != 0)
			return cmp;
		cmp = compare(getSupertag(), association.getSupertag());
		if (cmp != 0)
			return cmp;
		cmp = compare(getEntityClass(), association.getEntityClass());
		if (cmp != 0)
			return cmp;
		List<Pair<String, String>> pairs = getAssociates();
		List<Pair<String, String>> wordPairs = association.getAssociates();
		if (pairs == null && wordPairs == null)
			return 0;
		if (pairs == null)
			return -1;
		if (wordPairs == null)
			return 1;
		if (pairs.size() < wordPairs.size())
			return -1;
		if (pairs.size() > wordPairs.size())
			return 1;
		for (int i = 0; i < pairs.size(); i++) {
			Pair<String, String> p = pairs.get(i);
			Pair<String, String> wp = wordPairs.get(i);
			cmp = p.a.compareTo(wp.a);
			if (cmp != 0)
				return cmp;
			cmp = p.b.compareTo(wp.b);
			if (cmp != 0)
				return cmp;
		}
		return 0;
	}

	// compares strings, accounting for nulls
	private final int compare(String s1, String s2) {
		if (s1 == null && s2 == null)
			return 0;
		if (s1 == null)
			return -1;
		if (s2 == null)
			return 1;
		return s1.compareTo(s2);
	}

	/**
	 * Checks whether the morpho-(phono-/grapho-)logical associate keys of the current association
	 * intersect with the given associate keys.
	 * 
	 * WARNING: The current implementation only garantees matches for morphological associates.
	 * However, non morphological associates might exist among the associate keys, what is not
	 * checked against. In addition, the form is not checked for, but only the coarticulated
	 * associates as long as the form is not accidentally specified among the non canonical
	 * associates.
	 * 
	 * @param associateKeys the associate keys to check for intersection
	 * @return <code>true</code> if there is intersection and <code>false</code> otherwise.
	 */
	public final boolean intersectsAssociateKeys(Set<String> associateKeys) {
		if (getTone() != null && associateKeys.contains(Tokenizer.TONE_ASSOCIATE)) {
			return true;
		}
		for (Pair<String, String> nonCanonicalAssociate : getNonCanonicalAssociates()) {
			if (associateKeys.contains(nonCanonicalAssociate.a)) {
				return true;
			}
		}
		return false;
	}

	/** Returns a hash code for this word's restriction to a surface word. */
	public int surfaceWordHashCode() {
		int hc = System.identityHashCode(getForm());
		hc = 31 * hc + System.identityHashCode(getTone());
		for (Pair<String, String> pair : getNonCanonicalAssociates()) {
			hc = 31 * hc + System.identityHashCode(pair.a);
			hc = 31 * hc + System.identityHashCode(pair.b);
		}
		return hc;
	}

	/**
	 * Returns whether this word and the given object have equal restrictions to
	 * surface words.
	 */
	public final boolean formallyEquals(Object obj) {
		if (this == obj)
			return true;
		// nb: can use ==, since constructor interns all factors
		if (!(obj instanceof Association))
			return false;
		Association word = (Association) obj;
		boolean sameFields = getForm() == word.getForm() && getTone() == word.getTone();
		if (!sameFields)
			return false;
		List<Pair<String, String>> pairs = getAssociates();
		List<Pair<String, String>> wordPairs = word.getAssociates();
		if (pairs == null && wordPairs == null)
			return true;
		if (pairs == null || wordPairs == null)
			return false;
		if (pairs.size() != wordPairs.size())
			return false;
		for (int i = 0; i < pairs.size(); i++) {
			if (!pairs.get(i).equals(wordPairs.get(i)))
				return false;
		}
		return true;
	}

	/** Returns canonical version of deserialized word. */
	public final Object readResolve() throws ObjectStreamException {
		return AssociationPool.createWord(getForm(), getTone(), getTerm(), getFunctions(),
				getSupertag(), getEntityClass(), getAssociates());
	}

	@Override
	public final String toString() {
		StringBuffer sb = new StringBuffer();
		if (getForm() != null)
			sb.append(getForm());
		if (getTone() != null)
			sb.append('_').append(getTone());
		for (Pair<String, String> pair : getNonCanonicalAssociates()) {
			sb.append('_').append(pair.b);
		}
		if (getTerm() != null && getTerm() != getForm())
			sb.append('_').append(getTerm());
		if (getFunctions() != null)
			sb.append('_').append(getFunctions());
		if (getSupertag() != null)
			sb.append('_').append(getSupertag());
		if (getEntityClass() != null)
			sb.append('_').append(getEntityClass());
		if (sb.length() == 0)
			sb.append((String) null);
		return sb.toString();
	}

	/** Tests serialization. */
	public final static void main(String[] argv) throws IOException, ClassNotFoundException {
		// create words
		Association w = AssociationPool.createWord("ran");
		Association fw = AssociationPool.createFullWord(w, "run", "VBD", "s\\np", "MOTION");
		Association wb = AssociationPool.createWordWithAttrs(w, AssociationPool.createWord("B", "L"));
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
		Association w2 = (Association) in.readObject();
		System.out.println(w2);
		System.out.print("Reading fw2: ");
		Association fw2 = (Association) in.readObject();
		System.out.println(fw2);
		System.out.print("Reading wb2: ");
		Association wb2 = (Association) in.readObject();
		System.out.println(wb2);
		in.close();
		// test identity (and thus readResolve)
		System.out.println("w == w2?: " + (w == w2));
		System.out.println("fw == fw2?: " + (fw == fw2));
		System.out.println("wb == wb2?: " + (wb == wb2));
	}
}
