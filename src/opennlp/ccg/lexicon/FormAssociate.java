///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2004 University of Edinburgh (Michael White)
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
 * A form associate is an association that has a form as associate.
 *
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.4 $, $Date: 2009/07/17 04:23:30 $
 */
public class FormAssociate extends Association {

	/**
	 * Generated serial version
	 */
	private static final long serialVersionUID = 181491057498517717L;

	/**
	 * The form
	 */
	protected String form;

	/**
	 * Constructor
	 * 
	 * @param form the form
	 */
	protected FormAssociate(String form) {
		this.form = form;
	}

	@Override
	protected List<Pair<String, String>> getAssociates() {
		return null;
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
	public String getEntityClass() {
		return null;
	}

	@Override
	public final String getForm() {
		return form;
	}

	@Override
	public String getFunctions() {
		return null;
	}

	@Override
	public String getSupertag() {
		return null;
	}

	@Override
	public String getTerm() {
		return null;
	}

	@Override
	public String getTone() {
		return null;
	}
}
