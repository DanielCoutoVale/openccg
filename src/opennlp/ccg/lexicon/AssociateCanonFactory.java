package opennlp.ccg.lexicon;

import java.util.ArrayList;
import java.util.List;

import opennlp.ccg.util.Interner;
import opennlp.ccg.util.Pair;

/**
 * A factory of associate canons.
 * 
 * @author Daniel Couto-Vale
 */
public class AssociateCanonFactory implements WordFactory {

	// reusable word, for looking up already interned ones
	private final AssociateCanon association = new AssociateCanon(null, null, null, null, null,
			null, null);

	/**
	 * Sets the associates of the reusable association.
	 * 
	 * @param form the form
	 * @param tone the tone
	 * @param term the term
	 * @param functions the functions
	 * @param supertag the supertag
	 * @param entityClass the entity classes
	 * @param associates the associates
	 */
	private final void updateAssociates(String form, String tone, String term, String functions,
			String supertag, String entityClass, List<Pair<String, String>> associates) {
		association.form = form;
		association.tone = tone;
		association.term = term;
		association.functions = functions;
		association.supertag = supertag;
		association.entityClass = entityClass;
		association.associates = associates;
	}

	// looks up the word equivalent to w, or if none, returns a new one
	// based on it
	/**
	 * Looks up whether there is an interned equivalent association and returns it if there is one.
	 * Otherwise, interns the association and returns the interned.
	 * 
	 * @return the interned association
	 */
	private Association internAssociation() {
		Association intern = (Association) Interner.getGlobalInterned(association);
		if (intern != null) {
			return intern;
		}
		if (association.isFormal() && association.associates == null) {
			if (association.tone != null) {
				intern = new FormToneAssociates(association.form, association.tone);
			} else {
				intern = new FormAssociate(association.form);
			}
		} else {
			intern = new AssociateCanon(association.form, association.tone, association.term,
					association.functions, association.supertag, association.entityClass,
					association.associates);
		}
		return (Association) Interner.globalIntern(intern);
	}

	@Override
	public final synchronized Association create(String form) {
		return create(form, null, null, null, null, null, null);
	}

	@Override
	public final synchronized Association create(String attributeName, String attributeValue) {
		String form = null;
		String tone = null;
		String term = null;
		String functions = null;
		String supertag = null;
		String entityClass = null;
		List<Pair<String, String>> associates = null;
		if (attributeName == Tokenizer.FORM_ASSOCIATE) {
			form = attributeValue;
		} else if (attributeName == Tokenizer.TONE_ASSOCIATE) {
			tone = attributeValue;
		} else if (attributeName == Tokenizer.TERM_ASSOCIATE) {
			term = attributeValue;
		} else if (attributeName == Tokenizer.FUNCTIONS_ASSOCIATE) {
			functions = attributeValue;
		} else if (attributeName == Tokenizer.SUPERTAG_ASSOCIATE) {
			supertag = attributeValue;
		} else if (attributeName == Tokenizer.ENTITY_CLASS_ASSOCIATE) {
			entityClass = attributeValue;
		} else {
			associates = new ArrayList<Pair<String, String>>(1);
			associates.add(new Pair<String, String>(attributeName, attributeValue));
		}
		return create(form, tone, term, functions, supertag, entityClass, associates);
	}

	@Override
	public final synchronized Association create(String form, String tone, String term, String functions,
			String supertag, String entityClass, List<Pair<String, String>> associates) {
		updateAssociates(form, tone, term, functions, supertag, entityClass, associates);
		return internAssociation();
	}
}
