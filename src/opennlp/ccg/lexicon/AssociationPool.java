package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import opennlp.ccg.util.Pair;

public class AssociationPool {

	// factory methods

	/** The word factory to use. */
	protected static AssociationFactory factory = new AssociateCanonFactory();

	/**
	 * Creates a core surface word from the given one, removing all attrs in the
	 * given set.
	 */
	public static synchronized Association createCoreSurfaceWord(Association association,
			Set<String> nonassociateKeys) {
		String form = association.getForm();
		String tone = association.getTone();
		if (tone != null && nonassociateKeys.contains(Tokenizer.TONE_ASSOCIATE)) {
			tone = null;
		}
		List<Pair<String, String>> associates = association.getAssociates();
		if (associates != null) {
			associates = new ArrayList<Pair<String, String>>(associates);
			Iterator<Pair<String, String>> associateIterator = associates.iterator();
			while (associateIterator.hasNext()) {
				Pair<String, String> associate = associateIterator.next();
				if (nonassociateKeys.contains(associate.a)) {
					associateIterator.remove();
				}
			}
			return createWord(form, tone, null, null, null, null, associates);
		} else {
			return factory.create(form, tone, null, null, null, null, null);
		}
	}

	/**
	 * Creates a full word from the given surface one, adding the given stem,
	 * POS and semantic class.
	 */
	public static synchronized Association createFullWord(Association word, String stem,
			String POS, String supertag, String semClass) {
		stem = (stem != null) ? stem.intern() : null;
		POS = (POS != null) ? POS.intern() : null;
		supertag = (supertag != null) ? supertag.intern() : null;
		semClass = (semClass != null) ? semClass.intern() : null;
		return factory.create(word.getForm(), word.getTone(), stem, POS, supertag,
				semClass, word.getAssociates());
	}

	/**
	 * Creates a full word from the given surface one, adding the second (full)
	 * given word's stem, POS and semantic class, as well as the second word's
	 * additional attr-val pairs, plus the given supertag.
	 */
	public static synchronized Association createFullWord(Association word, Association word2,
			String supertag) {
		boolean mixedAttrs = false;
		List<Pair<String, String>> pairs = word.getAssociates();
		List<Pair<String, String>> pairs2 = word2.getAssociates();
		if (pairs == null && pairs2 != null) {
			pairs = pairs2;
		} else if (pairs2 != null) {
			mixedAttrs = true;
			pairs = new ArrayList<Pair<String, String>>(pairs);
			for (int i = 0; i < pairs2.size(); i++) {
				if (!pairs.contains(pairs2.get(i))) {
					pairs.add(pairs2.get(i));
				}
			}
		}
		if (mixedAttrs) {
			return createWord(word.getForm(), word.getTone(), word2.getTerm(), word2.getFunctions(),
					supertag, word2.getEntityClass(), pairs);
		} else {
			supertag = (supertag != null) ? supertag.intern() : null;
			return factory.create(word.getForm(), word.getTone(), word2.getTerm(), word2.getFunctions(),
					supertag, word2.getEntityClass(), pairs);
		}
	}

	/**
	 * Creates a surface word from the given one, removing the stem, POS,
	 * supertag and semantic class.
	 */
	public static synchronized Association createSurfaceWord(Association word) {
		return factory.create(word.getForm(), word.getTone(), null, null, null,
				null, word.getAssociates());
	}

	/**
	 * Creates a surface word from the given one, removing the stem, POS,
	 * supertag and semantic class, and replacing the form with the given one.
	 */
	public static synchronized Association createSurfaceWord(Association word, String form) {
		form = (form != null) ? form.intern() : null;
		return factory.create(form, word.getTone(), null, null, null, null,
				word.getAssociates());
	}

	/**
	 * Creates a surface word from the given one, removing the stem, POS,
	 * supertag and semantic class, and replacing the form with the semantic
	 * class, uppercased.
	 */
	public static synchronized Association createSurfaceWordUsingSemClass(Association word) {
		String form = word.getEntityClass().toUpperCase().intern();
		return factory.create(form, word.getTone(), null, null, null, null,
				word.getAssociates());
	}

	// NB: could try different factory methods for concrete words, but
	// it's unclear whether it makes much difference
	// protected static WordFactory wordFactory = new FactorChainWord.Factory();

	/** Creates a surface word with the given form. */
	public static synchronized Association createWord(String form) {
		form = (form != null) ? form.intern() : null;
		return factory.create(form);
	}

	/**
	 * Creates a (surface or full) word with the given attribute name and value.
	 * The attribute names Tokenizer.WORD_ATTR, ..., Tokenizer.SEM_CLASS_ATTR
	 * may be used for the form, ..., semantic class.
	 */
	public static synchronized Association createWord(String attributeName, String attributeValue) {
		attributeName = attributeName.intern();
		attributeValue = (attributeValue != null) ? attributeValue.intern() : null;
		return factory.create(attributeName, attributeValue);
	}

	/** Creates a (surface or full) word. */
	public static synchronized Association createWord(String form, String tone,
			String term, String functions, String supertag, String entityClass,
			List<Pair<String, String>> attrValPairs) {
		// normalize factors
		form = (form != null) ? form.intern() : null;
		tone = (tone != null) ? tone.intern() : null;
		if (attrValPairs != null) {
			if (attrValPairs.isEmpty())
				attrValPairs = null;
			else {
				attrValPairs = new ArrayList<Pair<String, String>>(attrValPairs);
				Association.sortAttrValPairs(attrValPairs);
				for (int i = 0; i < attrValPairs.size(); i++) {
					Pair<String, String> p = attrValPairs.get(i);
					String attr = p.a.intern();
					String val = (p.b != null) ? p.b.intern() : null;
					attrValPairs.set(i, new Pair<String, String>(attr, val));
				}
			}
		}
		term = (term != null) ? term.intern() : null;
		functions = (functions != null) ? functions.intern() : null;
		supertag = (supertag != null) ? supertag.intern() : null;
		entityClass = (entityClass != null) ? entityClass.intern() : null;
		return factory.create(form, tone, term, functions, supertag, entityClass, attrValPairs);
	}

	/**
	 * Creates a (surface or full) word from the given one, replacing the word
	 * form with the given one.
	 */
	public static synchronized Association createWord(Association word, String form) {
		if (form != null)
			form = form.intern();
		return factory.create(form, word.getTone(), word.getTerm(), word.getFunctions(),
				word.getSupertag(), word.getEntityClass(), word.getAssociates());
	}

	/**
	 * Creates a (surface or full) word from the given one, replacing the form
	 * and stem with the semantic class, uppercased.
	 */
	public static synchronized Association createWordUsingSemClass(Association word) {
		String form = word.getEntityClass().toUpperCase().intern();
		String stem = form;
		return factory.create(form, word.getTone(), stem, word.getFunctions(),
				word.getSupertag(), word.getEntityClass(), word.getAssociates());
	}

	/**
	 * Creates a (surface or full) word from the given surface one, adding the
	 * second word's additional attr-val pairs.
	 */
	public static synchronized Association createWordWithAttrs(Association word, Association word2) {
		// get accent
		String accent = word.getTone();
		if (accent == null)
			accent = word2.getTone();
		// get attrs
		boolean mixedAttrs = false;
		List<Pair<String, String>> pairs = word.getAssociates();
		List<Pair<String, String>> pairs2 = word2.getAssociates();
		if (pairs == null && pairs2 != null) {
			pairs = pairs2;
		} else if (pairs2 != null) {
			mixedAttrs = true;
			pairs = new ArrayList<Pair<String, String>>(pairs);
			for (int i = 0; i < pairs2.size(); i++) {
				if (!pairs.contains(pairs2.get(i))) {
					pairs.add(pairs2.get(i));
				}
			}
		}
		// get rest
		String form = word.getForm();
		String stem = word.getTerm();
		String POS = word.getFunctions();
		String supertag = word.getSupertag();
		String semClass = word.getEntityClass();
		// with mixed attrs, need to normalize
		if (mixedAttrs)
			return createWord(form, accent, stem, POS, supertag, semClass, pairs);
		else
			return factory.create(form, accent, stem, POS, supertag, semClass, pairs);
	}

}
