/*
 * Copyright (C) 2014-2015 Jan Leike (leike@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE LassoRanker Library.
 * 
 * The ULTIMATE LassoRanker Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE LassoRanker Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE LassoRanker Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE LassoRanker Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE LassoRanker Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.lassoranker.termination.rankingfunctions;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lassoranker.termination.AffineFunction;
import de.uni_freiburg.informatik.ultimate.lassoranker.variables.RankVar;
import de.uni_freiburg.informatik.ultimate.logic.Rational;
import de.uni_freiburg.informatik.ultimate.logic.SMTLIBException;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;


/**
 * A piecewise ranking function as generated by the piecewise template
 * 
 * @author Jan Leike
 */
public class PiecewiseRankingFunction extends RankingFunction {
	private static final long serialVersionUID = 1605612582853046558L;
	
	private final AffineFunction[] mranking;
	private final AffineFunction[] mpredicates;
	public final int pieces;
	
	public PiecewiseRankingFunction(AffineFunction[] ranking, AffineFunction[] predicates) {
		mranking = ranking;
		mpredicates = predicates;
		pieces = ranking.length;
		assert(pieces > 0);
		assert(pieces == predicates.length);
	}
	
	@Override
	public String getName() {
		return mranking.length + "-piece";
	}

	
	@Override
	public Set<RankVar> getVariables() {
		final Set<RankVar> vars = new LinkedHashSet<RankVar>();
		for (final AffineFunction af : mranking) {
			vars.addAll(af.getVariables());
		}
		return vars;
	}
	
	public AffineFunction[] getRankingComponents() {
		return mranking;
	}
	
	public AffineFunction[] getPredicates() {
		return mpredicates;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(mranking.length);
		sb.append("-piece ranking function:\n");
		sb.append("  f(");
		boolean first = true;
		for (final RankVar var : getVariables()) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(var.getIdentifier());
			first = false;
		}
		sb.append(") = {\n");
		for (int i = 0; i < pieces; ++i) {
			sb.append("    ");
			sb.append(mranking[i]);
			sb.append(",\tif ");
			sb.append(mpredicates[i]);
			sb.append(" >= 0");
			if (i < pieces - 1) {
				sb.append(",\n");
			} else {
				sb.append(".");
			}
		}
		return sb.toString();
	}
	
	@Override
	public Term[] asLexTerm(Script script) throws SMTLIBException {
		Term value = script.numeral(BigInteger.ZERO);
		for (int i = mranking.length - 1; i >= 0; --i) {
			final AffineFunction af = mranking[i];
			final AffineFunction gf = mpredicates[i];
			final Term pred = script.term(">=", gf.asTerm(script),
					script.numeral(BigInteger.ZERO));
			value = script.term("ite", pred, af.asTerm(script), value);
		}
		return new Term[] { value };
	}
	
	@Override
	public Ordinal evaluate(Map<RankVar, Rational> assignment) {
		Rational r = Rational.ZERO;
		for (int i = 0; i < pieces; ++i) {
			if (!mpredicates[i].evaluate(assignment).isNegative()) {
				final Rational rnew = mranking[i].evaluate(assignment);
				if (rnew.compareTo(r) > 0) {
					r = rnew;
				}
			}
		}
		return Ordinal.fromInteger(r.ceil().numerator());
	}

	@Override
	public Ordinal codomain() {
		return Ordinal.OMEGA;
	}
}
