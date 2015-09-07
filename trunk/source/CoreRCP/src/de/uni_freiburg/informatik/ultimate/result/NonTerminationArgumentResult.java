/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Jan Leike
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * Copyright (C) 2015 leike
 * Copyright (C) 2013-2015 leikej
 * 
 * This file is part of the ULTIMATE Core.
 * 
 * The ULTIMATE Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Core. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Core, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Core grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_freiburg.informatik.ultimate.core.services.IBacktranslationService;
import de.uni_freiburg.informatik.ultimate.logic.Rational;
import de.uni_freiburg.informatik.ultimate.model.IElement;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;

/**
 * Nontermination argument in form of an infinite program execution.
 * 
 * It is composed of
 * <ul>
 * <li> an initial state at the begin of the lasso,
 * <li> a state at first visit of the honda,
 * <li> a list of rays of the loop's transition polyhedron, and
 * <li> a list of discount factors lambda and mu.
 * </ul>
 * 
 * The infinite execution described by this nontermination argument is
 * 
 * <pre>
 * state_init,
 * state_honda,
 * state_honda + ray_1 + ... + ray_n,
 * state_honda + (1 + lambda_1) ray_1 + (1 + lambda_2 + mu_1) ray_2 + ... + (1 + lambda_n + nu_(n-1)) ray_n,
 * ...
 * </pre>
 * 
 * The general form is x + Y*(sum_i J^i)*1 where
 * <ul>
 * <li> x is the initial state state_init
 * <li> Y is a matrix with the rays as columns
 * <li> J is a matrix with lamnbda_i on the diagonal and mu_i on the upper subdiagonal
 * <li> 1 is a column vector of ones
 * </ul>
 * 
 * This implementation nontermination argument is highly tailored to the
 * nontermination arguments that the LassoRanker plugin discovers and is
 * unlikely to be useful anywhere else.
 * 
 * @author Jan Leike
 * @param <P> program position class
 */
public class NonTerminationArgumentResult<P extends IElement> extends AbstractResultAtElement<P> implements IResult {
	/**
	 * How many steps the infinite execution should be schematically unwinded
	 */
	private final static int s_schematic_execution_length = 3;

	private final Map<Expression, Rational> m_StateInit;
	private final Map<Expression, Rational> m_StateHonda;
	private final List<Map<Expression, Rational>> m_Rays;
	private final List<Rational> m_Lambdas;
	private final List<Rational> m_Nus;

	private final boolean m_AlternativeLongDescription = !false;

	/**
	 * Construct a termination argument result
	 * 
	 * @param position
	 *            program position
	 * @param plugin
	 *            the generating plugin's name
	 * @param ranking_function
	 *            a ranking function as an implicitly lexicographically ordered
	 *            list of Boogie expressions
	 * @param rankingFunctionDescription
	 *            short name of the ranking function
	 * @param supporting_invariants
	 *            list of supporting invariants in form of Boogie expressions
	 * @param translatorSequence
	 *            the toolchain's translator sequence
	 * @param location
	 *            program location
	 */
	public NonTerminationArgumentResult(P position, String plugin,
			Map<Expression, Rational> state_init,
			Map<Expression, Rational> state_honda,
			List<Map<Expression, Rational>> rays,
			List<Rational> lambdas,
			List<Rational> nus,
			IBacktranslationService translatorSequence) {
		super(position, plugin, translatorSequence);
		this.m_StateInit = state_init;
		this.m_StateHonda = state_honda;
		this.m_Rays = rays;
		this.m_Lambdas = lambdas;
		this.m_Nus = nus;
		assert m_Rays.size() == m_Lambdas.size();
	}

	@Override
	public String getShortDescription() {
		return "Nontermination argument in form of an infinite " + "program execution.";
	}

	private String printState(Map<Expression, String> state) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (Entry<Expression, String> entry : state.entrySet()) {
			String var = mTranslatorSequence.translateExpressionToString(entry.getKey(), Expression.class);
			if (var.contains("UnsupportedOperation")) {
				continue;
			}
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			// sb.append(BoogieStatementPrettyPrinter.print(entry.getKey()));
			sb.append(var);
			// sb.append(BackTranslationWorkaround.backtranslate(
			// m_TranslatorSequence, entry.getKey()));
			// TODO: apply backtranslation?
			sb.append("=");
			sb.append(entry.getValue());
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public String getLongDescription() {
		if (m_AlternativeLongDescription) {
			return alternativeLongDesciption();
		} else {
			return defaultLongDescription();
		}
	}

	public String defaultLongDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Nontermination argument in form of an infinite execution\n");
		sb.append(m_StateInit);
		assert (s_schematic_execution_length > 0);
		Rational[] geometric_sum =
				new Rational[m_Lambdas.size()]; // 1 + lambda_i + mu_(i-1) + ...
		for (int i = 0; i < m_Lambdas.size(); ++i) {
			geometric_sum[i] = Rational.ZERO;
		}
		for (int j = 0; j < s_schematic_execution_length; ++j) {
			Map<Expression, String> state = new HashMap<Expression, String>();
			for (Expression var : m_StateHonda.keySet()) {
				Rational x = m_StateHonda.get(var);
				for (int i = 0; i < m_Rays.size(); ++i) {
					Rational y = m_Rays.get(i).get(var);
					if (y != null) {
						x = x.add(y.mul(geometric_sum[i]));
					}
				}
				state.put(var, x.toString());
			}
			for (int i = m_Rays.size() - 1; i >= 0; --i) {
				geometric_sum[i] = geometric_sum[i].mul(m_Lambdas.get(i)).add(Rational.ONE);
				if (i > 0) {
					geometric_sum[i] = geometric_sum[i].add(geometric_sum[i-1].mul(m_Nus.get(i-1)));
				}
			}
			sb.append("\n");
			sb.append(printState(state));
		}
		return sb.toString();
	}

	private String alternativeLongDesciption() {
		StringBuilder sb = new StringBuilder();
		sb.append("Nontermination argument in form of an infinite execution\n");
		
		// State 1 (before the honda)
		sb.append("State at position 0 is\n");
		Map<Expression, String> statePos0 = new HashMap<Expression, String>();
		for (Entry<Expression, Rational> entry : m_StateHonda.entrySet()) {
			Expression var = entry.getKey();
			Rational x0 = m_StateInit.get(var);
			statePos0.put(var, x0.toString());
		}
		sb.append(printState(statePos0));
		
		// State 2 (at the honda)
		sb.append("\nState at position 1 is\n");
		Map<Expression, String> statePos1 = new HashMap<Expression, String>();
		for (Entry<Expression, Rational> entry : m_StateHonda.entrySet()) {
			Expression var = entry.getKey();
			Rational x = m_StateHonda.get(var);
			statePos1.put(var, x.toString());
		}
		sb.append(printState(statePos1));
		
		// Schematic execution afterwards
		sb.append("\nFor i>1, the state at position i is\n");
		Map<Expression, String> statePosI = new HashMap<Expression, String>();
		for (Expression var : m_StateHonda.keySet()) {
			StringBuilder sb2 = new StringBuilder();
			Rational x = m_StateHonda.get(var);
			for (int i = 0; i < m_Rays.size(); ++i) {
				Rational y = m_Rays.get(i).get(var);
				if (y != null && !y.equals(Rational.ZERO)) {
					if (sb2.length() > 0) {
						sb2.append(" + ");
					}
					for (int j = 0; j <= i && j < m_Rays.size(); ++j) {
						boolean ok = true;
						for (int s = i-j; s < i; ++s) {
							if (m_Nus.get(s).equals(Rational.ZERO)) {
								ok = false;
							}
						}
						if (!ok) {
							continue; // \prod_{s=i-j}^{i-1} mu_s == 0
							          // => skip summand
						}
						if (j > 0) {
							sb2.append(" + ");
						}
						Rational lambda = m_Lambdas.get(i - j);
						sb2.append(y);
						
						if (j == 1) {
							sb2.append("*k");
						} else if (j > 1) {
							sb2.append("*binomial(k, " + j + ")");
						}
						if (!lambda.equals(Rational.ONE)) {
							if (j == 0) {
								sb2.append("*" + lambda + "^k");
							} else {
								sb2.append("*" + lambda + "^(k-" + j + ")");
							}
						}
					}
				}
			}
			if (sb2.length() == 0) {
				sb2.append("0");
			}
			statePosI.put(var, x.toString() + " + sum_{k=0}^i " + sb2.toString());
		}
		sb.append(printState(statePosI));
		return sb.toString();
	}
}
