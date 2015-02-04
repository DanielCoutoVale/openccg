package opennlp.ccg.lexicon;

import java.util.List;

import opennlp.ccg.util.Pair;

/**
 * A factory of associations.
 * 
 * @author Daniel Couto-Vale
 */
public interface AssociationFactory {

	/**
	 * Creates an association with a single associate: namely, the form.
	 * 
	 * @param form the form of the association
	 * @return the association
	 */
	public Association create(String form);

	/**
	 * Creates an association with a single associate. The canonical associate
	 * keys are Tokenizer.FORM_ASSOCIATE... Tokenizer.ENTITY_CLASS_ASSOCIATE and
	 * they can be used to create an association respectively with a form... or
	 * an entity class.
	 * 
	 * @param associateKey the associate key
	 * @param associateValue the associate value
	 * @return the association
	 */
	public Association create(String associateKey, String associateValue);

	/**
	 * Creates an association.
	 * 
	 * @param form the form
	 * @param tone the tone
	 * @param term the term
	 * @param functions the functions
	 * @param supertag the supertag
	 * @param entityClass the entity class
	 * @param associates the associates
	 * @return the association
	 */
	public Association create(String form, String tone, String term, String functions, String supertag,
			String entityClass, List<Pair<String, String>> associates);

}