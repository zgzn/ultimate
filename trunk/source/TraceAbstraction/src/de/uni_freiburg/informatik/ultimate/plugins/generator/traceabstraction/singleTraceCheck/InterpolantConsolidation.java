package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.Word;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonOldApi;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWord;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.Difference;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.IsEmpty;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.PowersetDeterminizer;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.RemoveUnreachable;
import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.ModifiableGlobalVariableManager;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.TermVarsProc;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.BasicCegarLoop;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.PredicateFactory;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.PredicateFactoryForInterpolantConsolidation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.InterpolantAutomataTransitionAppender.DeterministicInterpolantAutomaton;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.benchmark.IBenchmarkDataProvider;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.benchmark.IBenchmarkType;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.IHoareTripleChecker;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TAPreferences;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.InterpolatingTraceChecker.AllIntegers;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.TraceCheckerUtils.InterpolantsPreconditionPostcondition;
import de.uni_freiburg.informatik.ultimate.util.HashRelation;

/**
 * Interpolant Consolidation works as follows:
 * Requirements: 
 * 		(1) A path automaton for the given trace m_Trace.
 * 		(2) An interpolant automaton (finite automaton) for the given predicate annotation of the given trace m_Trace.
 * Procedure:
 * 		1. Compute the difference between the path automaton and the interpolant automaton.
 * 		2. If the difference is empty, then consolidate the interpolants as follows:
 * 		2.1 Compute a homomorphism for the states of the difference automaton.
 * 		2.2 Compute the annotation for a state p = {q_1, ..., q_k} where q_1 ... q_k are homomorphous to each other as follows:
 * 				Annot(p) = Annot(q_1) OR Annot(q_2) OR ... OR Annot(q_k)
 * 		3. If the difference is not empty, then... (TODO). This case is not yet implemented!
 * 
 * 
 * @author musab@informatik.uni-freiburg.de
 */
public class InterpolantConsolidation implements IInterpolantGenerator {
	
	private InterpolatingTraceChecker m_InterpolatingTraceChecker;
	private final IPredicate m_Precondition;
	private final IPredicate m_Postcondition;
	private final SortedMap<Integer, IPredicate> m_PendingContexts;
	private IPredicate[] m_ConsolidatedInterpolants;
	private TAPreferences m_TaPrefs;
	private final NestedWord<CodeBlock> m_Trace;
	private final IUltimateServiceProvider m_Services;
	private final SmtManager m_SmtManager;
	private final ModifiableGlobalVariableManager m_ModifiedGlobals;
	private final PredicateUnifier m_PredicateUnifier;
	private final Logger m_Logger;

	protected final InterpolantConsolidationBenchmarkGenerator m_InterpolantConsolidationBenchmarkGenerator;
	
	public InterpolantConsolidation(IPredicate precondition,
			IPredicate postcondition,
			SortedMap<Integer, IPredicate> pendingContexts,
			NestedWord<CodeBlock> trace, SmtManager smtManager,
			ModifiableGlobalVariableManager modifiedGlobals,
			IUltimateServiceProvider services,
			Logger logger, 
			PredicateUnifier predicateUnifier,
			InterpolatingTraceChecker tc,
			TAPreferences taPrefs) throws OperationCanceledException {
		m_Precondition = precondition;
		m_Postcondition = postcondition;
		m_PendingContexts = pendingContexts;
		m_Trace = trace;
		m_SmtManager = smtManager;
		m_ModifiedGlobals = modifiedGlobals;
		m_Services = services;
		m_Logger = logger;
		m_PredicateUnifier = predicateUnifier;
		m_InterpolatingTraceChecker = tc;
		m_ConsolidatedInterpolants = new IPredicate[m_Trace.length() - 1];
		m_TaPrefs = taPrefs;
		m_InterpolantConsolidationBenchmarkGenerator = new InterpolantConsolidationBenchmarkGenerator();

		if (m_InterpolatingTraceChecker.isCorrect() == LBool.UNSAT) {
			computeInterpolants(new AllIntegers());
		}
	}

	protected void computeInterpolants(Set<Integer> interpolatedPositions) throws OperationCanceledException {
		int[] numOfPredicatesConsolidatedPerLocation = new int[m_Trace.length()];
		int differenceAutomatonEmptyCounter = 0;
		int disjunctionsGreaterOneCounter = 0;
		
		// 1. Build the path automaton for the given trace m_Trace
		PathProgramAutomatonConstructor ppc = new PathProgramAutomatonConstructor();
		INestedWordAutomaton<CodeBlock, IPredicate> pathprogramautomaton = ppc.constructAutomatonFromGivenPath(m_Trace, m_Services, m_SmtManager, m_TaPrefs);
		
		
		IHoareTripleChecker htc = BasicCegarLoop.getEfficientHoareTripleChecker(TraceAbstractionPreferenceInitializer.HoareTripleChecks.INCREMENTAL, 
				m_SmtManager, m_ModifiedGlobals, m_PredicateUnifier);
		
		// 2. Build the finite automaton (former interpolant path automaton) for the given Floyd-Hoare annotation
		NestedWordAutomaton<CodeBlock, IPredicate> interpolantAutomaton = constructInterpolantAutomaton(m_Trace, m_SmtManager, m_TaPrefs, m_Services, m_InterpolatingTraceChecker); // siehe BasicCegarLoop
		// 3. Determinize the finite automaton from step 2. 
		DeterministicInterpolantAutomaton interpolantAutomatonDeterminized = new DeterministicInterpolantAutomaton(
				m_Services, m_SmtManager, m_ModifiedGlobals, htc, pathprogramautomaton, interpolantAutomaton,
				m_PredicateUnifier, m_Logger, false); // PREDICATE_ABSTRACTION_CONSERVATIVE = false (default)
		
		 
		PredicateFactoryForInterpolantConsolidation pfconsol = new PredicateFactoryForInterpolantConsolidation(m_SmtManager, m_TaPrefs);
		
		PredicateFactory predicateFactoryInterpolantAutomata = new PredicateFactory(m_SmtManager, m_TaPrefs);
		
		PowersetDeterminizer<CodeBlock, IPredicate> psd2 = new PowersetDeterminizer<CodeBlock, IPredicate>(
				interpolantAutomatonDeterminized, true, predicateFactoryInterpolantAutomata);
		

		try {
			// 4. Compute the difference between the path automaton and the determinized
			//    finite automaton (from step 3)
			Difference<CodeBlock, IPredicate> diff = new Difference<CodeBlock, IPredicate>(m_Services,
					(INestedWordAutomatonOldApi<CodeBlock, IPredicate>) pathprogramautomaton,
					interpolantAutomatonDeterminized, psd2,
					pfconsol /* PredicateFactory for Refinement */, false /*explointSigmaStarConcatOfIA*/ );
			
//			INestedWordAutomatonOldApi<CodeBlock, IPredicate> testAutomaton = diff.getResult();
			htc.releaseLock();
			// 5. Check if difference is empty
			IsEmpty<CodeBlock, IPredicate> empty = new IsEmpty<CodeBlock, IPredicate>(m_Services, diff.getResult());
			if (!empty.getResult()) {
				// If the difference is not empty, we are not allowed to consolidate interpolants (at least by now) 
				m_ConsolidatedInterpolants = m_InterpolatingTraceChecker.getInterpolants();
				return;
			}
			
		} catch (AutomataLibraryException e) {
			if (e instanceof OperationCanceledException) {
				m_Logger.info("Timeout while computing interpolants");
			}
			throw ((OperationCanceledException)e);
		}

		// 6. Interpolant Consolidation step
		List<IPredicate> pathPositionsToLocations = ppc.getPositionsToStates();
		Map<IPredicate, Set<IPredicate>> locationsToSetOfPredicates = pfconsol.getLocationsToSetOfPredicates();
		m_ConsolidatedInterpolants = new IPredicate[m_Trace.length() - 1];
		for (int i = 0; i < m_ConsolidatedInterpolants.length; i++) {
			IPredicate loc = pathPositionsToLocations.get(i+1);
			// Compute the disjunction of the predicates for location i
			Set<IPredicate> predicatesForThisLocation = locationsToSetOfPredicates.get(loc);
			assert (predicatesForThisLocation != null) : "The set of predicates for the current location is null!";
			// Update benchmarks
			numOfPredicatesConsolidatedPerLocation[i] += predicatesForThisLocation.size();
			numOfPredicatesConsolidatedPerLocation[i] -= 1;
			if (predicatesForThisLocation.size() > 1) {
				disjunctionsGreaterOneCounter++;
			}
			
			IPredicate[] predicatesForThisLocationAsArray = predicatesForThisLocation.toArray(new IPredicate[predicatesForThisLocation.size()]);
			TermVarsProc predicatesForThisLocationConsolidated = m_SmtManager.or(predicatesForThisLocationAsArray);
			// Store the consolidated (the disjunction of the predicates for the current location)
			m_ConsolidatedInterpolants[i] = m_PredicateUnifier.getOrConstructPredicate(predicatesForThisLocationConsolidated); 
		}
		assert TraceCheckerUtils.checkInterpolantsInductivityForward(m_ConsolidatedInterpolants, 
				m_Trace, m_Precondition, m_Postcondition, m_PendingContexts, "CP", 
				m_SmtManager, m_ModifiedGlobals, m_Logger) : "invalid Hoare triple in consolidated interpolants";
		
		differenceAutomatonEmptyCounter = 1;
		// Set benchmark data
		m_InterpolantConsolidationBenchmarkGenerator.setInterpolantConsolidationData(numOfPredicatesConsolidatedPerLocation, differenceAutomatonEmptyCounter,
													 disjunctionsGreaterOneCounter);
	}
	
	
	/**
	 * Construct a finite automaton for the given Floyd-Hoare annotation.
	 * @param trace - the trace from which the automaton is constructed.
	 * @param traceChecker - contains the Floyd-Hoare annotation (the interpolants) for which the automaton is constructed.
	 * @return
	 */
	private NestedWordAutomaton<CodeBlock, IPredicate> constructInterpolantAutomaton(NestedWord<CodeBlock> trace, SmtManager smtManager, TAPreferences taPrefs, 
			IUltimateServiceProvider services, InterpolatingTraceChecker traceChecker) {
		// Set the alphabet
		Set<CodeBlock> internalAlphabet = new HashSet<CodeBlock>();
		Set<CodeBlock> callAlphabet = new HashSet<CodeBlock>();
		Set<CodeBlock> returnAlphabet = new HashSet<CodeBlock>();
		for (int i = 0; i < trace.length(); i++) {
			if (trace.isInternalPosition(i)) {
				internalAlphabet.add(trace.getSymbol(i));
			} else if (trace.isCallPosition(i)) {
				callAlphabet.add(trace.getSymbol(i));
			} else if (trace.isReturnPosition(i)) {
				returnAlphabet.add(trace.getSymbol(i));
			} else {
				throw new UnsupportedOperationException("Symbol at position " + i + " is neither internal, call, nor return symbol!");
			}
		}
		
		
		
		InterpolantsPreconditionPostcondition ipp = 
				new InterpolantsPreconditionPostcondition(traceChecker);
		StateFactory<IPredicate> predicateFactory = new PredicateFactory(smtManager, taPrefs);
		
		NestedWordAutomaton<CodeBlock, IPredicate> nwa  = new NestedWordAutomaton<CodeBlock, IPredicate>(   services, 
																											internalAlphabet,
																											callAlphabet,
																											returnAlphabet,
																											predicateFactory);
		// TODO: Implement the creation of states if both interpolant types (FP, BP) has been computed
		// Set the initial and the final state of the automaton
		nwa.addState(true, false, traceChecker.getPrecondition());
		nwa.addState(false, true, traceChecker.getPostcondition());
		// Add other states and corresponding transitions
		for (int i=0; i<trace.length(); i++) {
			IPredicate pred = ipp.getInterpolant(i);
			IPredicate succ = ipp.getInterpolant(i+1);
			assert nwa.getStates().contains(pred);
			if (!nwa.getStates().contains(succ)) {
				nwa.addState(false, false, succ);
			}
			if (trace.isCallPosition(i)) {
				nwa.addCallTransition(pred, trace.getSymbol(i), succ);
			} else if (trace.isReturnPosition(i)) {
				assert !trace.isPendingReturn(i);
				int callPos = trace.getCallPosition(i);
				IPredicate hierPred = ipp.getInterpolant(callPos);
				nwa.addReturnTransition(pred, hierPred, trace.getSymbol(i), succ);
			} else {
				assert trace.isInternalPosition(i);
				nwa.addInternalTransition(pred, trace.getSymbol(i), succ);
			}
		}
		
		return nwa;
	}

	@Override
	public IPredicate[] getInterpolants() {
		return m_ConsolidatedInterpolants;
	}

	@Override
	public Word<CodeBlock> getTrace() {
		return m_Trace;
	}

	@Override
	public IPredicate getPrecondition() {
		return m_Precondition;
	}

	@Override
	public IPredicate getPostcondition() {
		return m_Postcondition;
	}

	@Override
	public Map<Integer, IPredicate> getPendingContexts() {
		return m_PendingContexts;
	}
	
	public InterpolatingTraceChecker getInterpolatingTraceChecker() {
		return m_InterpolatingTraceChecker;
	}
	
	@Override
	public PredicateUnifier getPredicateUnifier() {
		return m_PredicateUnifier;
	}

	public InterpolantConsolidationBenchmarkGenerator getInterpolantConsolidationBenchmarks() {
		return m_InterpolantConsolidationBenchmarkGenerator;
	}
	
	// Benchmarks Section
	public static class InterpolantConsolidationBenchmarkType implements IBenchmarkType {
		private static InterpolantConsolidationBenchmarkType s_Instance = new InterpolantConsolidationBenchmarkType();
		
		/* Keys */
		// Counts how often we were allowed to consolidate interpolants
		protected final static String s_DifferenceAutomatonEmptyCounter = "DifferenceAutomatonEmptyCounter";
		// Counts the num of interpolants consolidated per location
		protected final static String s_SumOfPredicatesConsolidated = "SumOfPredicatesConsolidated";
		
		protected final static String s_DisjunctionsGreaterOneCounter = "DisjunctionsGreaterOneCounter";
		
		public static InterpolantConsolidationBenchmarkType getInstance() {
			return s_Instance;
		}
		
		@Override
		public Collection<String> getKeys() {
			ArrayList<String> result = new ArrayList<String>();
			result.add(s_DifferenceAutomatonEmptyCounter);
			result.add(s_SumOfPredicatesConsolidated);
			result.add(s_DisjunctionsGreaterOneCounter);
			return result;
		}

		@Override
		public Object aggregate(String key, Object value1, Object value2) {
			switch(key) {
			case s_DifferenceAutomatonEmptyCounter: {
				int result = ((int) value1) + ((int) value2);
				return result;
			}
			case s_DisjunctionsGreaterOneCounter: {
				int result = ((int) value1) + ((int) value2);
				return result;
			}
			case s_SumOfPredicatesConsolidated: {
				long result = ((long) value1) + ((long) value2);
				return result;
			}
			default:
				throw new AssertionError("unknown key");
			}
		}

		@Override
		public String prettyprintBenchmarkData(
				IBenchmarkDataProvider benchmarkData) {
			StringBuilder sb = new StringBuilder();
			sb.append(s_DifferenceAutomatonEmptyCounter).append(": ");
			sb.append((int) benchmarkData.getValue(s_DifferenceAutomatonEmptyCounter));
			sb.append("\t");
			sb.append(s_DisjunctionsGreaterOneCounter).append(": ");
			sb.append((int) benchmarkData.getValue(s_DisjunctionsGreaterOneCounter));
			
			sb.append("\t").append(s_SumOfPredicatesConsolidated).append(": ");
			sb.append((long) benchmarkData.getValue(s_SumOfPredicatesConsolidated));
			return sb.toString();
		}
		
	}
	
	public class InterpolantConsolidationBenchmarkGenerator implements 	IBenchmarkDataProvider {
		private int m_DifferenceAutomatonEmptyCounter = 0;
		private long m_SumOfPredicatesConsolidated = 0;
		private int m_DisjunctionsGreaterOneCounter = 0;

		
		
		public void incrementInterpolantConsolidationCounter() {
			m_DifferenceAutomatonEmptyCounter++;
		}
		
		public void setInterpolantConsolidationData(int[] numOfPredicatesConsolidatedPerLocation, int differenceAutomatonEmptyCounter,
				int disjunctionsGreaterOneCounter) {
			assert numOfPredicatesConsolidatedPerLocation != null;
			m_DifferenceAutomatonEmptyCounter = differenceAutomatonEmptyCounter;
			m_DisjunctionsGreaterOneCounter  = disjunctionsGreaterOneCounter;
			m_SumOfPredicatesConsolidated = getSumOfIntArray(numOfPredicatesConsolidatedPerLocation);
		}
		
		@Override
		public Collection<String> getKeys() {
			return InterpolantConsolidationBenchmarkType.getInstance().getKeys();
		}

		@Override
		public Object getValue(String key) {
			switch (key) {
			case InterpolantConsolidationBenchmarkType.s_DifferenceAutomatonEmptyCounter:
				return m_DifferenceAutomatonEmptyCounter;
			case InterpolantConsolidationBenchmarkType.s_SumOfPredicatesConsolidated:
				return m_SumOfPredicatesConsolidated;
			case InterpolantConsolidationBenchmarkType.s_DisjunctionsGreaterOneCounter:
				return m_DisjunctionsGreaterOneCounter;
			default:
				throw new AssertionError("unknown data");
			}
		}

		@Override
		public IBenchmarkType getBenchmarkType() {
			return InterpolantConsolidationBenchmarkType.getInstance();
		}
		
		private long getSumOfIntArray(int[] arr) {
			long sum = 0;
			for (int i = 0; i < arr.length; i++) {
				sum += arr[i];
			}
			return sum;
		}
	}
}
