/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2014-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.interpolantautomata.builders;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.AutomatonEpimorphism;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.InCaReAlphabet;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedRun;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWord;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.IsEmpty;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingCallTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingReturnTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.SummaryReturnTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.Transitionlet;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.ModifiableGlobalVariableManager;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.IAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.ICallAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.IInternalAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.IReturnAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.hoaretriple.IHoareTripleChecker;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.hoaretriple.IHoareTripleChecker.Validity;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplicationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.BasicCegarLoop;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.PredicateFactoryForInterpolantAutomata;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer.AssertCodeBlockOrder;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer.HoareTripleChecks;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer.InterpolationTechnique;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer.UnsatCores;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.IInterpolantGenerator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.InterpolatingTraceChecker;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.InterpolatingTraceCheckerCraig;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.PredicateUnifier;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.TraceCheckerSpWp;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.TraceCheckerUtils.InterpolantsPreconditionPostcondition;
import de.uni_freiburg.informatik.ultimate.util.statistics.IStatisticsDataProvider;
import de.uni_freiburg.informatik.ultimate.util.statistics.IStatisticsType;
import de.uni_freiburg.informatik.ultimate.util.statistics.StatisticsData;

public class TotalInterpolationAutomatonBuilder implements IInterpolantAutomatonBuilder<CodeBlock, IPredicate> {

	private final List<IPredicate> mStateSequence;
	// private final IPredicate[] mInterpolants;
	private final NestedWordAutomaton<CodeBlock, IPredicate> mIA;
	private final PredicateUnifier mPredicateUnifier;
	// private final TraceChecker mTraceChecker;
	private final INestedWordAutomaton<CodeBlock, IPredicate> mAbstraction;

	private final SmtManager mSmtManager;

	private final ArrayDeque<IPredicate> mWorklist = new ArrayDeque<IPredicate>();
	private final Set<IPredicate> mAnnotated = new HashSet<IPredicate>();

	// private final IPredicate mTruePredicate;
	// private final IPredicate mFalsePredicate;
	private final AutomatonEpimorphism<IPredicate> mEpimorphism;
	private final IHoareTripleChecker mHtc;
	private final ModifiableGlobalVariableManager mModifiedGlobals;
	private final InterpolationTechnique mInterpolation;

	private final TotalInterpolationBenchmarkGenerator mBenchmarkGenerator = new TotalInterpolationBenchmarkGenerator();
	private final IUltimateServiceProvider mServices;
	private final SimplicationTechnique mSimplificationTechnique;
	private final XnfConversionTechnique mXnfConversionTechnique;

	public TotalInterpolationAutomatonBuilder(final INestedWordAutomaton<CodeBlock, IPredicate> abstraction,
			final ArrayList<IPredicate> stateSequence, final IInterpolantGenerator interpolantGenerator, final SmtManager smtManager,
			final PredicateFactoryForInterpolantAutomata predicateFactory, final ModifiableGlobalVariableManager modifiableGlobals,
			final InterpolationTechnique interpolation, final IUltimateServiceProvider services, final HoareTripleChecks hoareTripleChecks, 
			final SimplicationTechnique simplificationTechnique, final XnfConversionTechnique xnfConversionTechnique) throws AutomataOperationCanceledException {
		super();
		mServices = services;
		mSimplificationTechnique = simplificationTechnique;
		mXnfConversionTechnique = xnfConversionTechnique;
		mStateSequence = stateSequence;
		// mTraceChecker = traceChecker;
		mSmtManager = smtManager;
		// mInterpolants = traceChecker.getInterpolants();
		mPredicateUnifier = interpolantGenerator.getPredicateUnifier();
		mAbstraction = abstraction;
		final InCaReAlphabet<CodeBlock> alphabet = new InCaReAlphabet<CodeBlock>(abstraction);
		mIA = (new StraightLineInterpolantAutomatonBuilder(mServices, alphabet, interpolantGenerator, predicateFactory))
				.getResult();
		mModifiedGlobals = modifiableGlobals;
		mInterpolation = interpolation;
		mEpimorphism = new AutomatonEpimorphism<IPredicate>(new AutomataLibraryServices(mServices));
		{
			final IPredicate firstAutomatonState = mStateSequence.get(0);
			mEpimorphism.insert(firstAutomatonState, interpolantGenerator.getPrecondition());
			mAnnotated.add(firstAutomatonState);
			mWorklist.add(firstAutomatonState);
		}
		addInterpolants(mStateSequence, interpolantGenerator.getInterpolants());
		{
			final IPredicate lastAutomatonState = mStateSequence.get(mStateSequence.size() - 1);
			mEpimorphism.insert(lastAutomatonState, interpolantGenerator.getPostcondition());
			mAnnotated.add(lastAutomatonState);
			mWorklist.add(lastAutomatonState);
		}
		mHtc = BasicCegarLoop.getEfficientHoareTripleChecker(services, HoareTripleChecks.MONOLITHIC, mSmtManager,
				mModifiedGlobals, mPredicateUnifier);
		for (final IPredicate state : stateSequence) {
			mWorklist.add(state);
			mAnnotated.add(state);
		}
		while (!mWorklist.isEmpty()) {
			final IPredicate p = mWorklist.removeFirst();
			doThings(p);
		}
		mBenchmarkGenerator.addEdgeCheckerData(mHtc.getEdgeCheckerBenchmark());
	}

	private void doThings(final IPredicate p) throws AutomataOperationCanceledException {
		for (final OutgoingInternalTransition<CodeBlock, IPredicate> transition : mAbstraction.internalSuccessors(p)) {
			continueCheckForOutgoingPath(p, transition, transition.getSucc());
		}
		for (final OutgoingCallTransition<CodeBlock, IPredicate> transition : mAbstraction.callSuccessors(p)) {
			continueCheckForOutgoingPath(p, transition, transition.getSucc());
		}
		for (final OutgoingReturnTransition<CodeBlock, IPredicate> transition : mAbstraction.returnSuccessors(p)) {
			if (mAnnotated.contains(transition.getHierPred())) {
				continueCheckForOutgoingPath(p, transition, transition.getSucc());
			}
		}

	}

	private void continueCheckForOutgoingPath(final IPredicate p, final Transitionlet<CodeBlock, IPredicate> transition,
			final IPredicate succ) throws AutomataOperationCanceledException {
		if (mAnnotated.contains(succ)) {
			final IPredicate predItp = mEpimorphism.getMapping(p);
			final IPredicate succItp = mEpimorphism.getMapping(succ);
			// this is a one-step path, no need to call TraceChecker
			if (interpolantAutomatonContainsTransition(predItp, transition, succItp)) {
				// do nothing, transition is already contained
			} else {
				mBenchmarkGenerator.incrementPathLenght1();
				checkRunOfLenthOne(predItp, transition, succItp);
			}
		} else {
			mBenchmarkGenerator.incrementRunSearches();
			final NestedRun<CodeBlock, IPredicate> runStartingInSucc = findRun(succ, mAnnotated);
			if (runStartingInSucc != null) {
				final NestedRun<CodeBlock, IPredicate> firstStep = constructRunOfLengthOne(p, transition);
				final NestedRun<CodeBlock, IPredicate> completeRun = firstStep.concatenate(runStartingInSucc);
				checkRun(completeRun);
			}
		}

	}

	private boolean interpolantAutomatonContainsTransition(final IPredicate predItp,
			final Transitionlet<CodeBlock, IPredicate> transition, final IPredicate succItp) {
		if (transition instanceof OutgoingInternalTransition) {
			final OutgoingInternalTransition<CodeBlock, IPredicate> internalTrans =
					(OutgoingInternalTransition<CodeBlock, IPredicate>) transition;
			return mIA.succInternal(predItp, internalTrans.getLetter()).contains(succItp);
		} else if (transition instanceof OutgoingCallTransition) {
			final OutgoingCallTransition<CodeBlock, IPredicate> callTrans =
					(OutgoingCallTransition<CodeBlock, IPredicate>) transition;
			return mIA.succCall(predItp, callTrans.getLetter()).contains(succItp);
		} else if (transition instanceof OutgoingReturnTransition) {
			final OutgoingReturnTransition<CodeBlock, IPredicate> returnTrans =
					(OutgoingReturnTransition<CodeBlock, IPredicate>) transition;
			final IPredicate hierPredItp = mEpimorphism.getMapping(returnTrans.getHierPred());
			return mIA.succReturn(predItp, hierPredItp, returnTrans.getLetter()).contains(succItp);
		} else if (transition instanceof SummaryReturnTransition) {
			final SummaryReturnTransition<CodeBlock, IPredicate> summaryTrans =
					(SummaryReturnTransition<CodeBlock, IPredicate>) transition;
			final IPredicate linPredItp = mEpimorphism.getMapping(summaryTrans.getLinPred());
			return mIA.succReturn(linPredItp, predItp, summaryTrans.getLetter()).contains(succItp);
		} else {
			throw new AssertionError("unsupported" + transition.getClass());
		}
	}

	private NestedRun<CodeBlock, IPredicate> constructRunOfLengthOne(final IPredicate p,
			final Transitionlet<CodeBlock, IPredicate> transition) {
		if (transition instanceof OutgoingInternalTransition) {
			final OutgoingInternalTransition<CodeBlock, IPredicate> internalTrans =
					(OutgoingInternalTransition<CodeBlock, IPredicate>) transition;
			return new NestedRun<>(p, internalTrans.getLetter(), NestedWord.INTERNAL_POSITION, internalTrans.getSucc());
		} else if (transition instanceof OutgoingCallTransition) {
			final OutgoingCallTransition<CodeBlock, IPredicate> callTrans =
					(OutgoingCallTransition<CodeBlock, IPredicate>) transition;
			return new NestedRun<>(p, callTrans.getLetter(), NestedWord.PLUS_INFINITY, callTrans.getSucc());
		} else if (transition instanceof OutgoingReturnTransition) {
			final OutgoingReturnTransition<CodeBlock, IPredicate> returnTrans =
					(OutgoingReturnTransition<CodeBlock, IPredicate>) transition;
			return new NestedRun<>(p, returnTrans.getLetter(), NestedWord.MINUS_INFINITY, returnTrans.getSucc());
		} else if (transition instanceof SummaryReturnTransition) {
			final SummaryReturnTransition<CodeBlock, IPredicate> summaryTrans =
					(SummaryReturnTransition<CodeBlock, IPredicate>) transition;
			return new NestedRun<>(summaryTrans.getLinPred(), summaryTrans.getLetter(), NestedWord.MINUS_INFINITY,
					summaryTrans.getSucc());
		} else {
			throw new AssertionError("unsupported" + transition.getClass());
		}

	}

	private void checkRunOfLenthOne(final IPredicate predItp, final Transitionlet<CodeBlock, IPredicate> transition,
			final IPredicate succItp) {
		if (transition instanceof OutgoingInternalTransition) {
			final OutgoingInternalTransition<CodeBlock, IPredicate> internalTrans =
					(OutgoingInternalTransition<CodeBlock, IPredicate>) transition;
			final Validity validity = mHtc.checkInternal(predItp, (IInternalAction) transition.getLetter(), succItp);
			if (validity == Validity.VALID) {
				mIA.addInternalTransition(predItp, internalTrans.getLetter(), succItp);
			}
		} else if (transition instanceof OutgoingCallTransition) {
			final OutgoingCallTransition<CodeBlock, IPredicate> callTrans =
					(OutgoingCallTransition<CodeBlock, IPredicate>) transition;
			final Validity validity = mHtc.checkCall(predItp, (ICallAction) callTrans.getLetter(), succItp);
			if (validity == Validity.VALID) {
				mIA.addCallTransition(predItp, callTrans.getLetter(), succItp);
			}
		} else if (transition instanceof OutgoingReturnTransition) {
			final OutgoingReturnTransition<CodeBlock, IPredicate> returnTrans =
					(OutgoingReturnTransition<CodeBlock, IPredicate>) transition;
			final IPredicate hierPredItp = mEpimorphism.getMapping(returnTrans.getHierPred());
			final Validity validity =
					mHtc.checkReturn(predItp, hierPredItp, (IReturnAction) returnTrans.getLetter(), succItp);
			if (validity == Validity.VALID) {
				mIA.addReturnTransition(predItp, hierPredItp, returnTrans.getLetter(), succItp);
			}
		} else if (transition instanceof SummaryReturnTransition) {
			final SummaryReturnTransition<CodeBlock, IPredicate> summaryTrans =
					(SummaryReturnTransition<CodeBlock, IPredicate>) transition;
			final IPredicate linPredItp = mEpimorphism.getMapping(summaryTrans.getLinPred());
			final Validity validity =
					mHtc.checkReturn(linPredItp, predItp, (IReturnAction) summaryTrans.getLetter(), succItp);
			if (validity == Validity.VALID) {
				mIA.addReturnTransition(linPredItp, predItp, summaryTrans.getLetter(), succItp);
			}
		} else {
			throw new AssertionError("unsupported" + transition.getClass());
		}
	}

	private void caseDistinction(final IPredicate p, final Transitionlet<CodeBlock, IPredicate> transition, final IPredicate succ) {
		if (transition instanceof OutgoingInternalTransition) {
			final OutgoingInternalTransition<CodeBlock, IPredicate> internalTrans =
					(OutgoingInternalTransition<CodeBlock, IPredicate>) transition;
		} else if (transition instanceof OutgoingCallTransition) {
			final OutgoingCallTransition<CodeBlock, IPredicate> callTrans =
					(OutgoingCallTransition<CodeBlock, IPredicate>) transition;
		} else if (transition instanceof OutgoingReturnTransition) {
			final OutgoingReturnTransition<CodeBlock, IPredicate> returnTrans =
					(OutgoingReturnTransition<CodeBlock, IPredicate>) transition;
		} else if (transition instanceof SummaryReturnTransition) {
			final SummaryReturnTransition<CodeBlock, IPredicate> summaryTrans =
					(SummaryReturnTransition<CodeBlock, IPredicate>) transition;
		} else {
			throw new AssertionError("unsupported" + transition.getClass());
		}

	}

	private void checkRun(final NestedRun<CodeBlock, IPredicate> run) {
		final IPredicate first = run.getStateAtPosition(0);
		final IPredicate last = run.getStateAtPosition(run.getLength() - 1);
		final IPredicate precondition = mEpimorphism.getMapping(first);
		final IPredicate postcondition = mEpimorphism.getMapping(last);
		final SortedMap<Integer, IPredicate> pendingContexts = computePendingContexts(run);
		// SortedMap<Integer, IPredicate> pendingContexts = new TreeMap<>();

		InterpolatingTraceChecker tc;
		switch (mInterpolation) {
		case Craig_NestedInterpolation:
		case Craig_TreeInterpolation:
			tc = new InterpolatingTraceCheckerCraig(precondition, postcondition,
					pendingContexts, run.getWord(),
					mSmtManager, mModifiedGlobals, AssertCodeBlockOrder.NOT_INCREMENTALLY,
					mServices, true, mPredicateUnifier, mInterpolation, true, mXnfConversionTechnique, mSimplificationTechnique);
			break;
		case ForwardPredicates:
		case BackwardPredicates:
		case FPandBP:
			tc = new TraceCheckerSpWp(precondition, postcondition, pendingContexts, run.getWord(), mSmtManager,
					mModifiedGlobals, AssertCodeBlockOrder.NOT_INCREMENTALLY, UnsatCores.CONJUNCT_LEVEL, true,
					mServices, true, mPredicateUnifier, mInterpolation, mSmtManager, mXnfConversionTechnique, mSimplificationTechnique);

			break;
		case PathInvariants:
		default:
			throw new UnsupportedOperationException("unsupported interpolation");
		}
		mBenchmarkGenerator.addTraceCheckerData(tc.getTraceCheckerBenchmark());
		if (tc.getToolchainCancelledExpection() != null) {
			throw tc.getToolchainCancelledExpection();
		}
		if (tc.isCorrect() == LBool.UNSAT) {
			mBenchmarkGenerator.incrementUsefullRunGeq2();
			final int additionalInterpolants = addInterpolants(run.getStateSequence(), tc.getInterpolants());
			mBenchmarkGenerator.reportAdditionalInterpolants(additionalInterpolants);
			addTransitions(run.getStateSequence(), tc);
		} else {
			mBenchmarkGenerator.incrementUselessRunGeq2();
		}
	}

	private SortedMap<Integer, IPredicate> computePendingContexts(final NestedRun<CodeBlock, IPredicate> run) {
		final SortedMap<Integer, IPredicate> result = new TreeMap<>();
		for (final int pendingReturnPos : run.getWord().getPendingReturns().keySet()) {
			final IPredicate linPred = run.getStateAtPosition(pendingReturnPos);
			final Iterable<IPredicate> hierPreds = mAbstraction.hierPred(linPred, run.getSymbol(pendingReturnPos));
			final IPredicate hierPred = getSomeAnnotatedState(hierPreds);
			if (hierPred == null) {
				throw new AssertionError("found nothing");
			} else {
				result.put(pendingReturnPos, mEpimorphism.getMapping(hierPred));
			}
		}
		return result;
	}

	private IPredicate getSomeAnnotatedState(final Iterable<IPredicate> states) {
		for (final IPredicate state : states) {
			if (mAnnotated.contains(state)) {
				return state;
			}
		}
		return null;
	}

	private void addTransitions(final ArrayList<IPredicate> stateSequence, final InterpolatingTraceChecker tc) {
		final InterpolantsPreconditionPostcondition ipp = new InterpolantsPreconditionPostcondition(tc);
		final NestedWord<? extends IAction> nw = NestedWord.nestedWord(tc.getTrace());
		for (int i = 0; i < nw.length(); i++) {
			if (nw.isInternalPosition(i)) {
				mIA.addInternalTransition(ipp.getInterpolant(i), (CodeBlock) nw.getSymbol(i),
						ipp.getInterpolant(i + 1));
			} else if (nw.isCallPosition(i)) {
				mIA.addCallTransition(ipp.getInterpolant(i), (CodeBlock) nw.getSymbol(i), ipp.getInterpolant(i + 1));
			} else if (nw.isReturnPosition(i)) {
				IPredicate hierPred;
				if (nw.isPendingReturn(i)) {
					hierPred = tc.getPendingContexts().get(i);
				} else {
					final int callPredPos = nw.getCallPosition(i);
					hierPred = ipp.getInterpolant(callPredPos);
				}
				mIA.addReturnTransition(ipp.getInterpolant(i), hierPred, (CodeBlock) nw.getSymbol(i),
						ipp.getInterpolant(i + 1));
			} else {
				throw new AssertionError();
			}
		}
	}

	/**
	 * Add a sequence of interpolants itp_1,...,itp_{n-1} for a sequence of states s_0,...,s_n. For each i add itp_i to
	 * the interpolant automaton if not already contained add s_i to the worklist add s_i to the annotated states add
	 * (s_i, itp_i) to the epimorphism Return the number of (different) interpolants that have been in the automaton
	 * before.
	 */
	private int addInterpolants(final List<IPredicate> stateSequence, final IPredicate[] interpolants) {
		int numberOfNewPredicates = 0;
		for (int i = 0; i < interpolants.length; i++) {
			final IPredicate state = stateSequence.get(i + 1);
			final IPredicate interpolant = interpolants[i];
			if (!mIA.getStates().contains(interpolant)) {
				mIA.addState(false, false, interpolant);
				numberOfNewPredicates++;
			}
			mAnnotated.add(state);
			mEpimorphism.insert(state, interpolant);
			mWorklist.add(state);
		}
		return numberOfNewPredicates;
	}

	private NestedRun<CodeBlock, IPredicate> findRun(final IPredicate p, final Set<IPredicate> annotated)
			throws AutomataOperationCanceledException {
		return (new IsEmpty<CodeBlock, IPredicate>(new AutomataLibraryServices(mServices), mAbstraction,
				Collections.singleton(p), Collections.emptySet(), mAnnotated)).getNestedRun();
	}

	@Override
	public NestedWordAutomaton<CodeBlock, IPredicate> getResult() {
		return mIA;
	}

	// private void startDfs(IPredicate state,
	// OutgoingInternalTransition<CodeBlock, IPredicate> transition) {
	// new GraphDfs(null, state, transition);
	// }
	//
	//
	// private class GraphDfs {
	// private final Set<IPredicate> mGoal;
	// private final Set<IPredicate> mVisited = new HashSet<IPredicate>();
	// private final Stack<Iterator<?>> mIteratorStack = new
	// Stack<Iterator<?>>();
	// private final Stack<Transitionlet<CodeBlock, IPredicate>>
	// mTransitionStack = new Stack<Transitionlet<CodeBlock, IPredicate>>();
	// private final Stack<IPredicate> mStateStack = new Stack<IPredicate>();
	// private final Stack<IPredicate> mCallPredecessors = new
	// Stack<IPredicate>();
	//
	// IPredicate mCurrentPred;
	// IPredicate mCurrentSucc;
	// Iterator<Transitionlet<CodeBlock, IPredicate>> mCurrentIterator;
	// Transitionlet<CodeBlock, IPredicate> mCurrentTransition;
	//
	//
	//
	// public GraphDfs(Set<IPredicate> goal, IPredicate currentPred,
	// Transitionlet<CodeBlock, IPredicate> initialTransition) {
	// super();
	// mGoal = goal;
	// mCurrentPred = currentPred;
	// mCurrentTransition = initialTransition;
	// mCurrentIterator = null;
	// mCurrentSucc = getSuccessor(initialTransition);
	// }
	//
	// private IPredicate getSuccessor(Transitionlet<CodeBlock, IPredicate>
	// transition) {
	// IPredicate result;
	// if (transition instanceof OutgoingInternalTransition) {
	// result = ((OutgoingInternalTransition<CodeBlock, IPredicate>)
	// transition).getSucc();
	// } else if (transition instanceof OutgoingCallTransition) {
	// result = ((OutgoingCallTransition<CodeBlock, IPredicate>)
	// transition).getSucc();
	// } else if (transition instanceof OutgoingReturnTransition) {
	// result = ((OutgoingReturnTransition<CodeBlock, IPredicate>)
	// transition).getSucc();
	// } else {
	// throw new AssertionError("unsupported" + transition.getClass());
	// }
	// return result;
	// }
	//
	// public void searchGoal() {
	// while (!mGoal.contains(mCurrentSucc)) {
	// mVisited.add(mCurrentSucc);
	// push();
	// getNextTransition();
	// while(mCurrentTransition == null) {
	// if (getStackHeight() == 1) {
	// // we never iterate over the initial Iterator.
	// return;
	// }
	// pop();
	// getNextTransition();
	// }
	// mCurrentSucc = getSuccessor(mCurrentTransition);
	// }
	// }
	//
	// private int getStackHeight() {
	// assert allStacksHaveSameHeight();
	// return mStateStack.size();
	// }
	//
	// private boolean allStacksHaveSameHeight() {
	// boolean result = (mStateStack.size() == mIteratorStack.size());
	// result &= (mStateStack.size() == mTransitionStack.size());
	// return result;
	// }
	//
	// private void push() {
	// assert allStacksHaveSameHeight();
	// mTransitionStack.push(mCurrentTransition);
	// mIteratorStack.push(mCurrentIterator);
	// mStateStack.push(mCurrentPred);
	// if (mCurrentTransition instanceof OutgoingCallTransition) {
	// mCallPredecessors.add(mCurrentPred);
	// }
	// mCurrentPred = mCurrentSucc;
	// mCurrentTransition = null;
	// mCurrentIterator = null;
	// mCurrentSucc = null;
	// }
	//
	// private void pop() {
	// assert allStacksHaveSameHeight();
	// mCurrentSucc = mCurrentPred;
	// mCurrentPred = mStateStack.pop();
	// if (mCurrentTransition instanceof OutgoingCallTransition) {
	// IPredicate callPred = mCallPredecessors.pop();
	// assert callPred == mCurrentPred;
	// }
	// mCurrentIterator = (Iterator<Transitionlet<CodeBlock, IPredicate>>)
	// mIteratorStack.pop();
	// mCurrentTransition = mTransitionStack.pop();
	// }
	//
	// public void getNextTransition() {
	// if (mCurrentIterator.hasNext()) {
	// mCurrentTransition = mCurrentIterator.next();
	// } else {
	// if (mCurrentTransition instanceof OutgoingInternalTransition) {
	// switchIteratorInternalToCall();
	// //TODO: implement
	// }
	// }
	// if (mCurrentTransition instanceof OutgoingInternalTransition) {
	// mCurrentTransition = getNextInternalTransition();
	// if (mCurrentTransition == null) {
	//
	// }
	// }
	//
	// }
	//
	// public void switchIteratorInternalToCall() {
	// assert !mIteratorStack.peek().hasNext();
	// mIteratorStack.pop();
	// IPredicate top = mStateStack.peek();
	// Iterator<OutgoingCallTransition<CodeBlock, IPredicate>> it =
	// mAbstraction.callSuccessors(top).iterator();
	// mIteratorStack.push(it);
	// }
	//
	// public void switchIteratorCallToReturn() {
	// assert !mIteratorStack.peek().hasNext();
	// mIteratorStack.pop();
	// IPredicate top = mStateStack.peek();
	// Iterator<OutgoingReturnTransition<CodeBlock, IPredicate>> it =
	// mAbstraction.returnSuccessors(top).iterator();
	// mIteratorStack.push(it);
	// }
	//
	// public OutgoingInternalTransition<CodeBlock, IPredicate>
	// getNextInternalTransition() {
	// if (mIteratorStack.peek().hasNext()) {
	// return (OutgoingInternalTransition<CodeBlock, IPredicate>)
	// mIteratorStack.peek().next();
	// } else {
	// return null;
	// }
	// }
	// }
	//

	public TotalInterpolationBenchmarkGenerator getTotalInterpolationBenchmark() {
		return mBenchmarkGenerator;
	}

	public static class TotalInterpolationBenchmarkType implements IStatisticsType {

		private static TotalInterpolationBenchmarkType s_Instance = new TotalInterpolationBenchmarkType();
		public final static String s_AdditionalInterpolants = "AdditionalInterpolants";
		public final static String s_PathLenght1 = "RunLenght1";
		public final static String s_RunSearches = "RunSearches";
		public final static String s_UsefullRunGeq2 = "UsefullRunGeq2";
		public final static String s_UselessRunGeq2 = "UselessRunGeq2";
		public final static String s_TraceCheckerBenchmarks = "TraceCheckerBenchmarks";
		public final static String s_EdgeCheckerBenchmarks = "EdgeCheckerBenchmarks";

		public static TotalInterpolationBenchmarkType getInstance() {
			return s_Instance;
		}

		@Override
		public Collection<String> getKeys() {
			return Arrays.asList(new String[] { s_AdditionalInterpolants, s_PathLenght1, s_RunSearches,
					s_UsefullRunGeq2, s_UselessRunGeq2, s_TraceCheckerBenchmarks, s_EdgeCheckerBenchmarks });
		}

		@Override
		public Object aggregate(final String key, final Object value1, final Object value2) {
			switch (key) {
			case s_AdditionalInterpolants:
			case s_PathLenght1:
			case s_RunSearches:
			case s_UsefullRunGeq2:
			case s_UselessRunGeq2:
				return (int) value1 + (int) value2;
			case s_TraceCheckerBenchmarks:
			case s_EdgeCheckerBenchmarks:
				final StatisticsData bmData1 = (StatisticsData) value1;
				final StatisticsData bmData2 = (StatisticsData) value2;
				bmData1.aggregateBenchmarkData(bmData2);
				return bmData1;
			default:
				throw new AssertionError("unknown key");
			}
		}

		@Override
		public String prettyprintBenchmarkData(final IStatisticsDataProvider benchmarkData) {
			final StringBuilder sb = new StringBuilder();

			for (final String id : new String[] { s_AdditionalInterpolants, s_PathLenght1, s_RunSearches,
					s_UsefullRunGeq2, s_UselessRunGeq2 }) {
				final int value = (int) benchmarkData.getValue(id);
				sb.append(id);
				sb.append(": ");
				sb.append(value);
				sb.append("  ");
			}

			sb.append(s_TraceCheckerBenchmarks);
			sb.append(": ");
			final StatisticsData ecData = (StatisticsData) benchmarkData.getValue(s_TraceCheckerBenchmarks);
			sb.append(ecData);
			sb.append("  ");

			sb.append(s_EdgeCheckerBenchmarks);
			sb.append(": ");
			final StatisticsData tcData = (StatisticsData) benchmarkData.getValue(s_EdgeCheckerBenchmarks);
			sb.append(tcData);
			return sb.toString();
		}

	}

	public static class TotalInterpolationBenchmarkGenerator implements IStatisticsDataProvider {

		private int mAdditionalInterpolants = 0;
		private int mPathLenght1 = 0;
		private int mRunSearches = 0;
		private int mUsefullRunGeq2 = 0;
		private int mUselessRunGeq2 = 0;
		private final StatisticsData mEcData = new StatisticsData();
		private final StatisticsData mTcData = new StatisticsData();

		public TotalInterpolationBenchmarkGenerator() {
		}

		@Override
		public Collection<String> getKeys() {
			return TotalInterpolationBenchmarkType.getInstance().getKeys();
		}

		public void reportAdditionalInterpolants(final int additionalInterpolants) {
			mAdditionalInterpolants += additionalInterpolants;
		}

		public void incrementPathLenght1() {
			mPathLenght1++;
		}

		public void incrementRunSearches() {
			mRunSearches++;
		}

		public void incrementUsefullRunGeq2() {
			mUsefullRunGeq2++;
		}

		public void incrementUselessRunGeq2() {
			mUselessRunGeq2++;
		}

		public void addEdgeCheckerData(final IStatisticsDataProvider ecbd) {
			mEcData.aggregateBenchmarkData(ecbd);
		}

		public void addTraceCheckerData(final IStatisticsDataProvider tcbd) {
			mTcData.aggregateBenchmarkData(tcbd);
		}

		@Override
		public Object getValue(final String key) {
			switch (key) {
			case TotalInterpolationBenchmarkType.s_AdditionalInterpolants:
				return mAdditionalInterpolants;
			case TotalInterpolationBenchmarkType.s_PathLenght1:
				return mPathLenght1;
			case TotalInterpolationBenchmarkType.s_RunSearches:
				return mRunSearches;
			case TotalInterpolationBenchmarkType.s_UsefullRunGeq2:
				return mUsefullRunGeq2;
			case TotalInterpolationBenchmarkType.s_UselessRunGeq2:
				return mUselessRunGeq2;
			case TotalInterpolationBenchmarkType.s_TraceCheckerBenchmarks:
				return mTcData;
			case TotalInterpolationBenchmarkType.s_EdgeCheckerBenchmarks:
				return mEcData;
			default:
				throw new AssertionError("unknown key");
			}
		}

		@Override
		public IStatisticsType getBenchmarkType() {
			return TotalInterpolationBenchmarkType.getInstance();
		}

	}

}