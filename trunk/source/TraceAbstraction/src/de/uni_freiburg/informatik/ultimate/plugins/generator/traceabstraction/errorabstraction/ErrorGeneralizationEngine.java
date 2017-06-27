/*
 * Copyright (C) 2017 Christian Schilling (schillic@informatik.uni-freiburg.de)
 * Copyright (C) 2017 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.errorabstraction;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.IRun;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INwaOutgoingLetterAndTransitionProvider;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWord;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.lib.exceptions.RunningTaskInfo;
import de.uni_freiburg.informatik.ultimate.core.lib.exceptions.ToolchainCanceledException;
import de.uni_freiburg.informatik.ultimate.core.lib.results.StatisticsResult;
import de.uni_freiburg.informatik.ultimate.core.model.results.IResult;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.IIcfgSymbolTable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplificationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicateUnifier;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.AbstractCegarLoop;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.AbstractCegarLoop.Result;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.PredicateFactoryForInterpolantAutomata;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.PredicateFactoryResultChecking;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.errorabstraction.ErrorTraceContainer.ErrorTrace;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.PredicateFactory;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TAPreferences.InterpolantAutomatonEnhancement;
import de.uni_freiburg.informatik.ultimate.util.statistics.StatisticsData;

/**
 * Constructs an error automaton for a given error trace.
 * 
 * @author Christian Schilling (schillic@informatik.uni-freiburg.de)
 * @param <LETTER>
 *            letter type in the trace
 */
public class ErrorGeneralizationEngine<LETTER extends IIcfgTransition<?>> implements IErrorAutomatonBuilder<LETTER> {
	private static final ErrorAutomatonType TYPE = ErrorAutomatonType.DANGER_AUTOMATON;

	protected final IUltimateServiceProvider mServices;
	protected final ILogger mLogger;

	private final ErrorTraceContainer<LETTER> mErrorTraces;
	private final ErrorAutomatonStatisticsGenerator mErrorAutomatonStatisticsGenerator;
	private IErrorAutomatonBuilder<LETTER> mErrorAutomatonBuilder;
	private int mLastIteration = -1;

	/**
	 * @param services
	 *            Ultimate services.
	 */
	public ErrorGeneralizationEngine(final IUltimateServiceProvider services) {
		mServices = services;
		mLogger = services.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mErrorAutomatonStatisticsGenerator = new ErrorAutomatonStatisticsGenerator();
		mErrorTraces = new ErrorTraceContainer<>();
	}

	@Override
	public NestedWordAutomaton<LETTER, IPredicate> getResultBeforeEnhancement() {
		return mErrorAutomatonBuilder.getResultBeforeEnhancement();
	}

	@Override
	public INwaOutgoingLetterAndTransitionProvider<LETTER, IPredicate> getResultAfterEnhancement() {
		return mErrorAutomatonBuilder.getResultAfterEnhancement();
	}

	@Override
	public ErrorAutomatonType getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPredicate getErrorPrecondition() {
		return mErrorAutomatonBuilder.getErrorPrecondition();
	}

	@Override
	public InterpolantAutomatonEnhancement getEnhancementMode() {
		return mErrorAutomatonBuilder.getEnhancementMode();
	}

	/**
	 * @param iteration
	 *            Iteration of CEGAR loop.
	 * @return {@code true} iff iteration of last error automaton construction coincides with passed iteration
	 */
	public boolean hasAutomatonInIteration(final int iteration) {
		return mLastIteration == iteration;
	}

	/**
	 * @param counterexample
	 *            Counterexample.
	 * @param predicateFactory
	 *            predicate factory
	 * @param predicateUnifier
	 *            predicate unifier
	 * @param csToolkit
	 *            SMT toolkit
	 * @param simplificationTechnique
	 *            simplification technique
	 * @param xnfConversionTechnique
	 *            XNF conversion technique
	 * @param symbolTable
	 *            symbol table
	 * @param stateFactoryForAutomaton
	 *            state factory for automaton (will be refactored eventually)
	 * @param abstraction
	 *            abstraction
	 * @param iteration
	 *            current CEGAR loop iteration
	 */
	public void constructErrorAutomaton(final IRun<LETTER, IPredicate, ?> counterexample,
			final PredicateFactory predicateFactory, final IPredicateUnifier predicateUnifier,
			final CfgSmtToolkit csToolkit, final SimplificationTechnique simplificationTechnique,
			final XnfConversionTechnique xnfConversionTechnique, final IIcfgSymbolTable symbolTable,
			final PredicateFactoryForInterpolantAutomata stateFactoryForAutomaton,
			final INestedWordAutomaton<LETTER, IPredicate> abstraction, final int iteration) {
		mErrorTraces.addTrace(counterexample);
		mLastIteration = iteration;

		final NestedWord<LETTER> trace = (NestedWord<LETTER>) counterexample.getWord();
		if (mLogger.isInfoEnabled()) {
			mLogger.info("Constructing " + (TYPE == ErrorAutomatonType.ERROR_AUTOMATON ? "error" : "danger")
					+ " automaton for trace of length " + trace.length());
		}

		mErrorAutomatonStatisticsGenerator.reportTraceLength(trace.length());
		mErrorAutomatonStatisticsGenerator.startErrorAutomatonConstructionTime();

		try {
			switch (TYPE) {
				case ERROR_AUTOMATON:
					mErrorAutomatonBuilder = new ErrorAutomatonBuilder<>(mServices, predicateFactory, predicateUnifier,
							csToolkit, simplificationTechnique, xnfConversionTechnique, symbolTable,
							stateFactoryForAutomaton, abstraction, trace);
					break;
				case DANGER_AUTOMATON:
					mErrorAutomatonBuilder = new DangerAutomatonBuilder<>(mServices, predicateFactory, predicateUnifier,
							csToolkit, simplificationTechnique, xnfConversionTechnique, symbolTable,
							stateFactoryForAutomaton, abstraction, trace);
					break;
				default:
					throw new IllegalArgumentException("Unknown automaton type: " + TYPE);
			}
		} catch (final ToolchainCanceledException tce) {
			mErrorAutomatonStatisticsGenerator.stopErrorAutomatonConstructionTime();
			mErrorAutomatonStatisticsGenerator.finishAutomatonInstance();
			final RunningTaskInfo rti = new RunningTaskInfo(getClass(),
					"constructing error automaton for trace of length " + trace.length() + " (spent "
							+ mErrorAutomatonStatisticsGenerator.getLastConstructionTime() + " nanoseconds)");
			throw new ToolchainCanceledException(tce, rti);
		}
		mErrorAutomatonStatisticsGenerator.stopErrorAutomatonConstructionTime();
		mErrorTraces.addPrecondition(mErrorAutomatonBuilder.getErrorPrecondition());
	}

	/**
	 * Starts difference time measurement.
	 */
	public void startDifference() {
		mErrorAutomatonStatisticsGenerator.startErrorAutomatonDifferenceTime();
	}

	/**
	 * Stops difference time measurement. Also evaluates the automaton.
	 * 
	 * @param abstraction
	 *            abstraction
	 * @param predicateFactoryInterpolantAutomata
	 *            state factory for automaton
	 * @param predicateFactoryResultChecking
	 *            state factory for result checking
	 * @throws AutomataLibraryException
	 *             thrown by automaton evaluation
	 */
	public void stopDifference(final INestedWordAutomaton<LETTER, IPredicate> abstraction,
			final PredicateFactoryForInterpolantAutomata predicateFactoryInterpolantAutomata,
			final PredicateFactoryResultChecking predicateFactoryResultChecking,
			final IRun<LETTER, IPredicate, ?> errorTrace) throws AutomataLibraryException {
		mErrorAutomatonStatisticsGenerator.stopErrorAutomatonDifferenceTime();
		mErrorAutomatonStatisticsGenerator.evaluateFinalErrorAutomaton(mServices, mLogger, mErrorAutomatonBuilder,
				(INwaOutgoingLetterAndTransitionProvider<LETTER, IPredicate>) abstraction,
				predicateFactoryInterpolantAutomata, predicateFactoryResultChecking, errorTrace);
		mErrorAutomatonStatisticsGenerator.finishAutomatonInstance();
	}

	/**
	 * Reports final error statistics.
	 */
	public void reportErrorGeneralizationBenchmarks() {
		final StatisticsData stat = new StatisticsData();
		stat.aggregateBenchmarkData(mErrorAutomatonStatisticsGenerator);
		final IResult benchmarkResult = new StatisticsResult<>(Activator.PLUGIN_NAME, "ErrorAutomatonStatistics", stat);
		mServices.getResultService().reportResult(Activator.PLUGIN_ID, benchmarkResult);
	}

	/**
	 * In case error traces are not reported immediately, the analysis may terminate with an empty abstraction or may
	 * run into termination issues, but it has already found out that the program contains errors. This method can be
	 * used to ask for such results whenever the analysis terminates.
	 * 
	 * @param abstractResult
	 *            result that would be reported by {@link AbstractCegarLoop}
	 * @return {@code true} if at least one feasible counterexample was detected
	 */
	public boolean isResultUnsafe(final Result abstractResult) {
		if (mErrorTraces.isEmpty()) {
			return false;
		}
		if (mLogger.isInfoEnabled()) {
			mLogger.info("Found " + mErrorTraces.size()
					+ (mErrorTraces.size() == 1 ? " error trace:" : " different error traces in total:"));
			int ctr = 0;
			for (final ErrorTrace<LETTER> errorTrace : mErrorTraces) {
				final IPredicate precondition = errorTrace.getPrecondition();
				// TODO 2017-06-14 Christian: Do not print error precondition on info level after testing phase.
				mLogger.info(++ctr + ": Error trace of length " + errorTrace.getTrace().getWord().length()
						+ (precondition == null
								? " (precondition not computed)."
								: " has precondition " + precondition.getFormula() + '.'));
			}
		}
		// TODO 2017-06-18 Christian: Currently we want to run the CEGAR loop until the abstraction is empty.
		return abstractResult == Result.SAFE;
	}
}
