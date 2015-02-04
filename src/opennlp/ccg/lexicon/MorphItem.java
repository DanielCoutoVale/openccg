///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2003-5 Jason Baldridge, Gann Bierner and 
//                      University of Edinburgh (Michael White)
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

/**
 * A data structure for morphological entries.
 *
 * @author Jason Baldridge
 * @author Gann Bierner
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.11 $, $Date: 2009/10/17 20:46:20 $
 */
public class MorphItem {

	private static final String[] empty = new String[0];

	private final Association muster;
	private final Association container;
	private final Association coarticulee;
	private final String[] macros;
	private final String[] excluded;
	private final boolean modal;

	/** Constructor. */
	public MorphItem() {
		muster = null;
		container = null;
		coarticulee = null;
		macros = empty;
		excluded = empty;
		modal = false;
	};

	/** Constructor from XML element. */
	public MorphItem(Association muster, Association container, Association coarticulee,
			String[] macros, String[] excluded, boolean modal) {
		this.muster = muster;
		this.container = container;
		this.coarticulee = coarticulee;
		this.macros = macros;
		this.excluded = excluded;
		this.modal = modal;
	}

	/**
	 * Returns whether the name, qualified name or family name of the given
	 * entries item is in the excluded list.
	 */
	public boolean excluded(EntriesItem eItem) {
		if (excluded.length == 0)
			return false;
		for (int i = 0; i < excluded.length; i++) {
			if (eItem.getName().equals(excluded[i]))
				return true;
			if (eItem.getQualifiedName().equals(excluded[i]))
				return true;
			if (eItem.getFamilyName().equals(excluded[i]))
				return true;
		}
		return false;
	}

	/** Returns the full word. */
	public Association getContainer() {
		return container;
	}

	/** Returns the surface word (without the stem, POS and semantic class). */
	public Association getMuster() {
		return muster;
	}

	/** Returns the macro names. */
	public String[] getMacros() {
		return macros;
	}

	/** Returns the names of the excluded entries. */
	public String[] getExcluded() {
		return excluded;
	}

	/** Returns whether the morph item is a coarticulation, eg a pitch accent. */
	public boolean isModal() {
		return modal;
	}

	/**
	 * Returns the word for indexing this coarticulation (or null if not a
	 * coarticulation).
	 */
	public Association getCoarticulee() {
		return coarticulee;
	}

	/** Returns a string for this morph item. */
	// nb: excluded not handled
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int i = 0; i < macros.length; i++) {
			sb.append(macros[i]);
			if (i < macros.length - 1)
				sb.append(',');
		}
		sb.append(']');
		return "{" + container + " => " + sb + "}";
	}
}
