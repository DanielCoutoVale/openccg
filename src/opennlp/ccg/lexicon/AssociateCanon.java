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

import java.util.*;

/**
 * A FullWord object is a word with all possible fields. The factory methods
 * return interned objects.
 *
 * @author Michael White
 * @version $Revision: 1.6 $, $Date: 2009/07/17 04:23:30 $
 */
public class AssociateCanon extends Word {

	private static final long serialVersionUID = -3115687437782457735L;

	/** List of attribute-value pairs, which must be strings. */
	protected List<Pair<String, String>> associates;

	/** The semantic class (optional). */
	protected String entityClass;

	protected String form;

	/** The part of speech. */
	protected String functions;

	/** The supertag. */
	protected String supertag;

	/** The stem. */
	protected String term;

	protected String tone;

	/**
	 * Constructor
	 * 
	 * @param form the form of the word
	 * @param tone the tone of the word
	 * @param term the term
	 * @param functions the functions
	 * @param supertag the supertag
	 * @param entityClass the rhetoric entity class
	 * @param associates the non-canonical associates
	 */
	protected AssociateCanon(String form, String tone, String term, String functions,
			String supertag, String entityClass, List<Pair<String, String>> associates) {
		this.form = form;
		this.tone = tone;
		this.term = term;
		this.functions = functions;
		this.supertag = supertag;
		this.entityClass = entityClass;
		this.associates = associates;
	}

	@Override
	public final List<Pair<String, String>> getAssociates() {
		return associates;
	}

	@Override
	public final String getAssociateValue(String associateKey) {
		associateKey = associateKey.intern();
		if (associateKey == Tokenizer.WORD_ASSOCIATE)
			return getForm();
		if (associateKey == Tokenizer.TONE_ASSOCIATE)
			return getTone();
		if (associateKey == Tokenizer.TERM_ASSOCIATE)
			return getTerm();
		if (associateKey == Tokenizer.FUNCTIONS_ASSOCIATE)
			return getFunctions();
		if (associateKey == Tokenizer.SUPERTAG_ASSOCIATE)
			return getSupertag();
		if (associateKey == Tokenizer.ENTITY_CLASS_ASSOCIATE)
			return getEntityClass();
		List<Pair<String, String>> pairs = getAssociates();
		if (pairs == null)
			return null;
		for (int i = 0; i < pairs.size(); i++) {
			Pair<String, String> p = pairs.get(i);
			if (p.a == associateKey)
				return p.b;
		}
		return null;
	}

	@Override
	public final String getEntityClass() {
		return entityClass;
	}

	@Override
	public final String getForm() {
		return form;
	}

	@Override
	public final String getFunctions() {
		return functions;
	}

	@Override
	public final String getSupertag() {
		return supertag;
	}

	@Override
	public final String getTerm() {
		return term;
	}

	@Override
	public final String getTone() {
		return tone;
	}

}
