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
public class AssociateCanon extends FormToneAssociates {

	private static final long serialVersionUID = -3115687437782457735L;

	/** List of attribute-value pairs, which must be strings. */
	protected List<Pair<String, String>> associates;

	/** The semantic class (optional). */
	protected String entityClass;

	/** The part of speech. */
	protected String functions;

	/** The supertag. */
	protected String supertag;

	/** The stem. */
	protected String term;


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
		super(form, tone);
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
	public final String getEntityClass() {
		return entityClass;
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

}
