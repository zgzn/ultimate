/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE TraceAbstraction plug-in.
 * 
 * The ULTIMATE TraceAbstraction plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE TraceAbstraction plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE TraceAbstraction plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE TraceAbstraction plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE TraceAbstraction plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.interpolantautomata.transitionappender;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.ModifiableGlobalVariableManager;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.hoaretriple.IHoareTripleChecker;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.hoaretriple.IHoareTripleChecker.Validity;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.PredicateUnifier;

/**
 * Nondeterministic interpolant automaton with on-demand construction.
 * The set of successor states S for a given state ψ and a CodeBlock cb are 
 * constructed as follows.
 * First, we check if state state ψ is the "false" state. If this is the case
 * S is the singleton set {false} and the construction is finished. Otherwise,
 * we add to S all states φ such that (ψ, cb, φ) is a transition in 
 * the given interpolant automaton {@code #mInputInterpolantAutomaton} (which
 * is typically the "canonical interpolant automaton" that was constructed for
 * a given trace).
 * In case S contains the state "false", we set S to the singleton set {false}
 * and return. Otherwise, we try to add more states to S.
 * How may states we try depends on the construction
 * mode.
 * <ul>
 * <li> If we are in the conservative construction mode 
 * ({@code #mConservativeConstructionMode} is true) we check if the Hoare 
 * triple (ψ, cb, ψ) is valid. If this is the case we add ψ to S.
 * <li> If we are in the non-conservative construction mode 
 * ({@code #mConservativeConstructionMode} is false) we check for each 
 * nontrivial predicate φ (i.e., each predicate but "true" and "false") if the
 * Hoare triple (ψ, cb, φ) is valid. Whenever the Hoare triple is valid, we
 * add φ to the set S.
 * </ul>
 * Finally, we check if S is empty. If this is the case and mSecondChance is 
 * set we add "true" to S. Hence if mSecondChance is set this automaton is 
 * total because S is never empty.
 * 
 * @author Matthias Heizmann
 */
public class NondeterministicInterpolantAutomaton extends BasicAbstractInterpolantAutomaton {
	
	protected final Set<IPredicate> mNonTrivialPredicates;
	protected final boolean mConservativeSuccessorCandidateSelection;
	/**
	 * If true, than states that do not have a successor, get mIaTrueState
	 * as successor (they get a second chance to reach false).
	 * If false, mIaTrueState will have a selfloop labeled with all states
	 * hence there this flag does not change the language it only determines
	 * the amount of nondeterminism.
	 */
	protected final boolean mSecondChance;
	

	public NondeterministicInterpolantAutomaton(IUltimateServiceProvider services, 
			SmtManager smtManager, ModifiableGlobalVariableManager modglobvarman, IHoareTripleChecker hoareTripleChecker,
			INestedWordAutomaton<CodeBlock, IPredicate> abstraction, 
			NestedWordAutomaton<CodeBlock, IPredicate> interpolantAutomaton, 
			PredicateUnifier predicateUnifier, ILogger  logger, 
			boolean conservativeSuccessorCandidateSelection, boolean secondChance) {
		super(services, smtManager, hoareTripleChecker, true, abstraction, 
				predicateUnifier, 
				interpolantAutomaton, logger);
		mConservativeSuccessorCandidateSelection = conservativeSuccessorCandidateSelection;
		mSecondChance = secondChance;
		final Collection<IPredicate> allPredicates = interpolantAutomaton.getStates(); 
		
		assert SmtUtils.isTrue(mIaTrueState.getFormula());
		assert allPredicates.contains(mIaTrueState);
		mAlreadyConstrucedAutomaton.addState(true, false, mIaTrueState);
		assert SmtUtils.isFalse(mIaFalseState.getFormula());
		assert allPredicates.contains(mIaFalseState);
		mAlreadyConstrucedAutomaton.addState(false, true, mIaFalseState);

		mNonTrivialPredicates = new HashSet<IPredicate>();
		for (final IPredicate state : allPredicates) {
			if (state != mIaTrueState && state != mIaFalseState) {
				mNonTrivialPredicates.add(state);
				// the following two lines are important if not (only) 
				// true/false are initial/final states of the automaton.
				final boolean isInitial = interpolantAutomaton.isInitial(state);
				final boolean isFinal = interpolantAutomaton.isFinal(state);
				mAlreadyConstrucedAutomaton.addState(isInitial, isFinal, state);
			}
		}

		mLogger.info(startMessage());
		
	}
	
	@Override
	protected String startMessage() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Constructing nondeterministic interpolant automaton ");
		sb.append(" with ");
		sb.append(mNonTrivialPredicates.size() + 2);
		sb.append(" interpolants.");
		return sb.toString();
	}
	
	@Override
	protected String switchToReadonlyMessage() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Switched to read-only mode: nondeterministic interpolant automaton has ");
		sb.append(mAlreadyConstrucedAutomaton.size()).append(" states. ");
		return sb.toString();
	}
	
	@Override
	protected String switchToOnTheFlyConstructionMessage() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Switched to On-DemandConstruction mode: nondeterministic interpolant automaton has ");
		sb.append(mAlreadyConstrucedAutomaton.size()).append(" states. ");
		return sb.toString();
	}


	
	@Override
	protected void addOtherSuccessors(IPredicate resPred, IPredicate resHier,
			CodeBlock letter, SuccessorComputationHelper sch,
			final Set<IPredicate> inputSuccs) {
		Set<IPredicate> successorCandidates;
		if (mConservativeSuccessorCandidateSelection) {
			if (resHier == null) {
				successorCandidates = Collections.singleton(resPred);
			} else {
				// we are computing successors for a return transition
				// let's use the linear predecessor and the hierarchical
				// predecessor.
				successorCandidates = new HashSet<IPredicate>(2);
				successorCandidates.add(resPred);
				successorCandidates.add(resHier);
			}
		} else {
			successorCandidates = mNonTrivialPredicates;
		}
		for (final IPredicate succCand : mNonTrivialPredicates) {
			if (!inputSuccs.contains(succCand)) {
				final Validity sat = sch.computeSuccWithSolver(resPred, resHier, letter, succCand);
				if (sat == Validity.VALID) {
					inputSuccs.add(succCand);
				}
			}
		}
		
		if (mSecondChance) {
			if (inputSuccs.isEmpty()) {
				inputSuccs.add(mIaTrueState);
			}
		} else {
			if (inputSuccs.isEmpty() && (letter instanceof Call)) {
				// special case, call may have mIaTrueState as successor
				inputSuccs.add(mIaTrueState);
			}
			if (resPred == mIaTrueState) {
				// mIaTrueState will get a selfloop labeled with all statements
				inputSuccs.add(mIaTrueState);
			}
		}
	}


	/**
	 * Add all successors of input automaton. As an optimization, we omit
	 * the "true" state if it is a successor. Additionally, we also add
	 * all successors of the "true" state.
	 */
	@Override
	protected void addInputAutomatonSuccs(
			IPredicate resPred, IPredicate resHier, CodeBlock letter,
			SuccessorComputationHelper sch, Set<IPredicate> inputSuccs) {
			final Collection<IPredicate> succs = 
					sch.getSuccsInterpolantAutomaton(resPred, resHier, letter);
			copyAllButTrue(inputSuccs, succs);
			final Collection<IPredicate> succsOfTrue = 
					sch.getSuccsInterpolantAutomaton(mIaTrueState, resHier, letter);
			copyAllButTrue(inputSuccs, succsOfTrue);
			if (resHier != null) {
				final Collection<IPredicate> succsForResPredTrue = 
						sch.getSuccsInterpolantAutomaton(resPred, mIaTrueState, letter);
				copyAllButTrue(inputSuccs, succsForResPredTrue);
				final Collection<IPredicate> succsForTrueTrue = 
						sch.getSuccsInterpolantAutomaton(mIaTrueState, mIaTrueState, letter);
				copyAllButTrue(inputSuccs, succsForTrueTrue);
			}
	}
	
	private void copyAllButTrue(Set<IPredicate> target,	Collection<IPredicate> source) {
		for (final IPredicate pred : source) {
			if (pred == mIaTrueState) {
				// do nothing, transition to the "true" state are useless
			} else {
				target.add(pred);
			}
		}
	}

	@Override
	protected void constructSuccessorsAndTransitions(IPredicate resPred,
			IPredicate resHier, CodeBlock letter, 
			SuccessorComputationHelper sch, Set<IPredicate> inputSuccs) {
		for (final IPredicate succ : inputSuccs) {
			sch.addTransition(resPred, resHier, letter, succ);
		}
		sch.reportSuccsComputed(resPred, resHier, letter);
	}
	
	
}