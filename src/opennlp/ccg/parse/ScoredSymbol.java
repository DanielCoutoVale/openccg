///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 Michael White
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

package opennlp.ccg.parse;

import opennlp.ccg.synsem.*;
import java.util.*;
import java.io.Serializable;
import java.text.*;

/**
 * <p>
 * A scored symbol is a symbol annotated with a score and, optionally, with a
 * list of alternative scored symbols.
 * 
 * A representative scored symbol is a scored symbol that stands in for other
 * scored symbols. It has the same category but different rhetorico-semantic
 * entities (LFs). The represented scored symbols are stored in the list of
 * alternative scored symbols during chart construction. The representative
 * scored symbol is considered disjunctive when there is more than one
 * alternatives to it.
 * 
 * WARNING: A representative scored symbol will initially be present in its list
 * of alternatives, but this may not be the case all the way through chart
 * construction. This is the case because the representative scored symbol as
 * any other alternative may be removed from the list of alternatives through
 * pruning.
 * </p>
 *
 * @author Michael White
 * @author Daniel Couto-Vale
 * @version $Revision: 1.4 $, $Date: 2009/12/22 22:19:00 $
 */
public class ScoredSymbol implements Serializable {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -4998260616580204169L;

	/**
	 * A holder of a scored symbol.
	 */
	public static class ScoredSymbolHolder implements Serializable {

		/**
		 * Generated serial version UID
		 */
		private static final long serialVersionUID = -5600090138895781778L;

		/**
		 * The scored symbol held
		 */
		public final ScoredSymbol scoredSymbol;

		/**
		 * Constructor
		 * 
		 * @param scoredSymbol the scored symbol to hold
		 */
		public ScoredSymbolHolder(ScoredSymbol scoredSymbol) {
			this.scoredSymbol = scoredSymbol;
		}

	}

	/**
	 * Recovers a scored symbol for a symbol
	 * 
	 * @param symbol the symbol to recover a scored symbol for
	 * @return the scored symbol if any
	 */
	public final static ScoredSymbol recoverScoredSymbol(Symbol symbol) {
		ScoredSymbolHolder holder = (ScoredSymbolHolder) symbol.getData(ScoredSymbolHolder.class);
		return (holder != null) ? holder.scoredSymbol : null;
	}

	/**
	 * The symbol.
	 */
	protected Symbol symbol;

	/** The edge score. */
	protected double score;

	/**
	 * The x1 position
	 */
	protected int x1 = -1;

	/** The alternative scored symbols (none initially). */
	protected List<ScoredSymbol> alternatives = null;

	/** Saved list of alternative edges, for restoring chart after unpacking. */
	protected transient List<ScoredSymbol> savedAlternatives = null;

	/** Constructor (score defaults to 0.0). */
	public ScoredSymbol(Symbol symbol) {
		this(symbol, 0.0);
	}

	/** Constructor with score. */
	public ScoredSymbol(Symbol symbol, double score) {
		this.symbol = symbol;
		this.score = score;
		symbol.addData(new ScoredSymbolHolder(this));
	}

	/**
	 * Gets the symbol.
	 * 
	 * @return the symbol
	 */
	public final Symbol getSymbol() {
		return symbol;
	}

	/**
	 * Gets the score.
	 * 
	 * @return the score
	 */
	public final double getScore() {
		return score;
	}

	/**
	 * Sets the score.
	 * 
	 * @param score the score
	 */
	public final void setScore(double score) {
		this.score = score;
	}

	/**
	 * Returns whether this symbol is a representative.
	 */
	public final boolean isRepresentative() {
		return alternatives != null;
	}

	/**
	 * Returns whether this symbol is disjunctive.
	 */
	public final boolean isDisjunctive() {
		return alternatives != null && alternatives.size() > 1;
	}

	/**
	 * Gets the list of alternative symbols, or the empty list if none.
	 */
	public final List<ScoredSymbol> getAlternatives() {
		if (alternatives == null) {
			return Collections.emptyList();
		} else {
			return alternatives;
		}
	}

	/**
	 * Initializes the alt symbols list with a default capacity, adding this symbol.
	 */
	public final void initAlternatives() {
		initAlternatives(3);
	}

	/**
	 * Initializes the alt symbols list with the given capacity, adding this symbol.
	 */
	private final void initAlternatives(int capacity) {
		// check uninitialized
		if (alternatives != null)
			throw new RuntimeException("Alt edges already initialized!");
		alternatives = new ArrayList<ScoredSymbol>(capacity);
		alternatives.add(this);
	}

	/**
	 * Replaces the alt symbols, saving the current ones for later restoration.
	 */
	public final void replaceAltSymbols(List<ScoredSymbol> newAlternatives) {
		savedAlternatives = alternatives;
		alternatives = newAlternatives;
	}

	/** Recursively restores saved alt edges, if any. */
	public final void restoreAlternatives() {
		if (savedAlternatives != null) {
			// restore
			alternatives = savedAlternatives;
			savedAlternatives = null;
			// recurse
			for (ScoredSymbol alt : alternatives) {
				Symbol[] inputs = alt.symbol.getDerivationHistory().getInputs();
				if (inputs != null) {
					for (Symbol s : inputs)
						recoverScoredSymbol(s).restoreAlternatives();
				}
			}
		}
	}

	/**
	 * Returns a hash code for this edge, based on its sign. (Alternatives and
	 * the score are not considered.)
	 */
	public final int hashCode() {
		return symbol.hashCode() * 23;
	}

	/**
	 * Returns a hash code for this edge based on the sign's surface words.
	 * (Alternatives and the score are not considered.)
	 */
	public final int surfaceWordHashCode() {
		return symbol.surfaceWordHashCode() * 23;
	}

	/**
	 * Returns whether this edge equals the given object. (Alternatives and the
	 * score are not considered.)
	 */
	public final boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ScoredSymbol))
			return false;
		ScoredSymbol edge = (ScoredSymbol) obj;
		return symbol.equals(edge.symbol);
	}

	/**
	 * Returns whether this edge equals the given object based on the sign's
	 * surface words. (Alternatives and the score are not considered.)
	 */
	public final boolean surfaceWordEquals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof ScoredSymbol)) {
			return false;
		}
		ScoredSymbol symbol = (ScoredSymbol) object;
		return symbol.surfaceWordEquals(symbol.symbol);
	}

	/**
	 * Returns a string for the edge in the format [score] orthography :-
	 * category.
	 */
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		if (score >= 0.001 || score == 0.0) {
			sbuf.append("[" + nf3.format(score) + "] ");
		} else {
			sbuf.append("[" + nfE.format(score) + "] ");
		}
		sbuf.append(symbol.toString());
		return sbuf.toString();
	}

	// formats to three decimal places
	private static final NumberFormat nf3 = initNF3();

	private static NumberFormat initNF3() {
		NumberFormat f = NumberFormat.getInstance();
		f.setMinimumIntegerDigits(1);
		f.setMinimumFractionDigits(3);
		f.setMaximumFractionDigits(3);
		return f;
	}

	// formats to "0.##E0"
	private static final NumberFormat nfE = new DecimalFormat("0.##E0");
}
