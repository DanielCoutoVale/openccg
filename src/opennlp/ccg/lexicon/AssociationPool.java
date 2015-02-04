package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import opennlp.ccg.util.Pair;

/**
 * A pool of associations that interns the associations for efficiency reasons.
 * 
 * @author Daniel Couto-Vale
 */
public class AssociationPool {

	/**
	 * The association factory
	 */
	protected static AssociationFactory factory = new AssociateCanonFactory();

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Content-containing Association Factory Methods

	/**
	 * Creates an association of a muster with grammatical and semantic content.
	 * 
	 * @param muster the muster
	 * @param term the term
	 * @param functions the functions
	 * @param supertag the supertag
	 * @param entityClass the entity class
	 * @return the content-containing association
	 */
	public static synchronized Association createContainer(Association muster, String term,
			String functions, String supertag, String entityClass) {
		term = (term != null) ? term.intern() : null;
		functions = (functions != null) ? functions.intern() : null;
		supertag = (supertag != null) ? supertag.intern() : null;
		entityClass = (entityClass != null) ? entityClass.intern() : null;
		return factory.create(muster.getForm(), muster.getTone(), term, functions, supertag,
				entityClass, muster.getAssociates());
	}

	/**
	 * Creates an association of a muster with grammatical and semantic content
	 * from the container plus the given supertag.
	 * 
	 * @param muster the muster
	 * @param container the container
	 * @param supertag the supertag
	 * @return the content-containing association
	 */
	public static synchronized Association createContainer(Association muster,
			Association container, String supertag) {
		boolean mixedAssociates = false;
		List<Pair<String, String>> musterAssociates = muster.getAssociates();
		List<Pair<String, String>> containerAssociates = container.getAssociates();
		if (musterAssociates == null && containerAssociates != null) {
			musterAssociates = containerAssociates;
		} else if (containerAssociates != null) {
			mixedAssociates = true;
			musterAssociates = new ArrayList<Pair<String, String>>(musterAssociates);
			for (int i = 0; i < containerAssociates.size(); i++) {
				if (!musterAssociates.contains(containerAssociates.get(i))) {
					musterAssociates.add(containerAssociates.get(i));
				}
			}
		}
		if (mixedAssociates) {
			return createAssociation(muster.getForm(), muster.getTone(), container.getTerm(),
					container.getFunctions(), supertag, container.getEntityClass(),
					musterAssociates);
		} else {
			supertag = (supertag != null) ? supertag.intern() : null;
			return factory.create(muster.getForm(), muster.getTone(), container.getTerm(),
					container.getFunctions(), supertag, container.getEntityClass(),
					musterAssociates);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Contentless Association Factory Methods

	/**
	 * Creates a muster for an association by returning an association without
	 * lexicogrammatical and rhetoricosemantic associates.
	 * 
	 * WARNING: non-morphological non-canonical associates are not removed
	 * 
	 * @param association the base association
	 * @return the muster
	 */
	public static synchronized Association createMuster(Association association) {
		return factory.create(association.getForm(), association.getTone(), null, null, null, null,
				association.getAssociates());
	}

	/**
	 * Creates a muster with a substitute form.
	 * 
	 * WARNING: non-morphological non-canonical associates are not removed
	 * 
	 * @param association the association
	 * @param substituteForm the substitute form
	 * @return the muster
	 */
	public static synchronized Association createMuster(Association association,
			String substituteForm) {
		substituteForm = (substituteForm != null) ? substituteForm.intern() : null;
		return factory.create(substituteForm, association.getTone(), null, null, null, null,
				association.getAssociates());
	}

	/**
	 * Creates a muster that consists of a form associate.
	 * 
	 * @param form the form
	 * @return the muster
	 */
	public static synchronized Association createMuster(String form) {
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

	/**
	 * Reduces a muster by removing the specified nonassociates (coarticulated
	 * associates).
	 * 
	 * @param muster the muster to reduce
	 * @param nonassociateKeys the nonassociates to remove
	 * @return the reduced association
	 */
	public static synchronized Association reduceMuster(Association muster,
			Set<String> nonassociateKeys) {
		String form = muster.getForm();
		String tone = muster.getTone();
		if (tone != null && nonassociateKeys.contains(Tokenizer.TONE_ASSOCIATE)) {
			tone = null;
		}
		List<Pair<String, String>> associates = muster.getAssociates();
		if (associates != null) {
			associates = new ArrayList<Pair<String, String>>(associates);
			Iterator<Pair<String, String>> associateIterator = associates.iterator();
			while (associateIterator.hasNext()) {
				Pair<String, String> associate = associateIterator.next();
				if (nonassociateKeys.contains(associate.a)) {
					associateIterator.remove();
				}
			}
			return createAssociation(form, tone, null, null, null, null, associates);
		} else {
			return factory.create(form, tone, null, null, null, null, null);
		}
	}

	/**
	 * Creates an association with the specified associates.
	 * 
	 * @param form the form
	 * @param tone the tone
	 * @param term the term
	 * @param functions the functions
	 * @param supertag the supertag
	 * @param entityClass the entityClass
	 * @param associates other associates
	 * @return the association
	 */
	public static synchronized Association createAssociation(String form, String tone, String term,
			String functions, String supertag, String entityClass,
			List<Pair<String, String>> associates) {
		// normalize factors
		form = (form != null) ? form.intern() : null;
		tone = (tone != null) ? tone.intern() : null;
		if (associates != null) {
			if (associates.isEmpty())
				associates = null;
			else {
				associates = new ArrayList<Pair<String, String>>(associates);
				Association.sortAttrValPairs(associates);
				for (int i = 0; i < associates.size(); i++) {
					Pair<String, String> p = associates.get(i);
					String attr = p.a.intern();
					String val = (p.b != null) ? p.b.intern() : null;
					associates.set(i, new Pair<String, String>(attr, val));
				}
			}
		}
		term = (term != null) ? term.intern() : null;
		functions = (functions != null) ? functions.intern() : null;
		supertag = (supertag != null) ? supertag.intern() : null;
		entityClass = (entityClass != null) ? entityClass.intern() : null;
		return factory.create(form, tone, term, functions, supertag, entityClass, associates);
	}

	/**
	 * Creates an association based on a given one, but with another form.
	 * 
	 * @param baseAssociation the base association
	 * @param substituteForm the substitute form
	 * @return the association
	 */
	public static synchronized Association createAssociation(Association baseAssociation,
			String substituteForm) {
		if (substituteForm != null) {
			substituteForm = substituteForm.intern();
		}
		return factory.create(substituteForm, baseAssociation.getTone(), baseAssociation.getTerm(),
				baseAssociation.getFunctions(), baseAssociation.getSupertag(),
				baseAssociation.getEntityClass(), baseAssociation.getAssociates());
	}

	/**
	 * Creates an association based on a given one with the associates of a
	 * muster with the exception of the form.
	 * 
	 * @param baseAssociation the base association
	 * @param substituteMuster the substitute muster
	 * @return the association
	 */
	public static synchronized Association createAssociation(Association baseAssociation,
			Association substituteMuster) {
		String tone = baseAssociation.getTone();
		if (tone == null) {
			tone = substituteMuster.getTone();
		}
		boolean mixedAssociates = false;
		List<Pair<String, String>> associates = baseAssociation.getAssociates();
		List<Pair<String, String>> substituteAssociates = substituteMuster.getAssociates();
		if (associates == null && substituteAssociates != null) {
			associates = substituteAssociates;
		} else if (substituteAssociates != null) {
			mixedAssociates = true;
			associates = new ArrayList<Pair<String, String>>(associates);
			for (int i = 0; i < substituteAssociates.size(); i++) {
				if (!associates.contains(substituteAssociates.get(i))) {
					associates.add(substituteAssociates.get(i));
				}
			}
		}
		// get rest
		String form = baseAssociation.getForm();
		String term = baseAssociation.getTerm();
		String functions = baseAssociation.getFunctions();
		String supertag = baseAssociation.getSupertag();
		String entityClass = baseAssociation.getEntityClass();
		// with mixed attrs, need to normalize
		if (mixedAssociates)
			return createAssociation(form, tone, term, functions, supertag, entityClass, associates);
		else
			return factory.create(form, tone, term, functions, supertag, entityClass, associates);
	}

}
