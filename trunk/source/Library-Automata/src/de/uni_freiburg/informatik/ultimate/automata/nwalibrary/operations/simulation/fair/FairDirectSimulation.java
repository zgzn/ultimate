/*
 * Copyright (C) 2015-2016 Daniel Tischner
 * Copyright (C) 2009-2016 University of Freiburg
 * 
 * This file is part of the ULTIMATE Automata Library.
 * 
 * The ULTIMATE Automata Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Automata Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automata Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.fair;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonOldApi;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.ESimulationType;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.direct.DirectSimulation;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.performance.ECountingMeasure;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.performance.EMultipleDataOption;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.performance.ETimeMeasure;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.performance.SimulationPerformance;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.util.Vertex;
import de.uni_freiburg.informatik.ultimate.core.services.model.IProgressAwareTimer;
import de.uni_freiburg.informatik.ultimate.util.scc.StronglyConnectedComponent;

/**
 * Simulation that realizes <b>fair and direct simulation</b> for reduction of a
 * given buechi automaton. It primarily uses fair simulation and uses direct
 * simulation as an optimization.<br/>
 * Once created it starts the simulation, results can then be get by using
 * {@link #getResult()}.<br/>
 * <br/>
 * 
 * For more information on the type of simulation see
 * {@link FairDirectGameGraph}.<br/>
 * <br/>
 * 
 * The algorithm runs in <b>O(n^4 * k^2)</b> time and <b>O(n * k)</b> space
 * where n is the amount of states and k the amount of transitions from the
 * inputed automaton.<br/>
 * The algorithm is based on the paper: <i>Fair simulation minimization<i> by
 * <i>Gurumurthy, Bloem and Somenzi</i>.
 * 
 * @author Daniel Tischner
 * 
 * @param <LETTER>
 *            Letter class of buechi automaton
 * @param <STATE>
 *            State class of buechi automaton
 */
public final class FairDirectSimulation<LETTER, STATE> extends FairSimulation<LETTER, STATE> {

	/**
	 * True if the simulation currently mimics the behavior of a
	 * DirectSimulation, false if it mimics a FairSimulation.
	 */
	private boolean m_IsCurrentlyDirectSimulation;

	/**
	 * Holds information about the performance of the simulation after usage.
	 */
	private SimulationPerformance m_Performance;
	/**
	 * True if the algorithm has already finished, false otherwise.
	 */
	private boolean m_HasFinished;

	/**
	 * Creates a new fair simulation that tries to reduce the given buechi
	 * automaton using <b>fair and direct simulation</b>.<br/>
	 * After construction the simulation starts and results can be get by using
	 * {@link #getResult()}.<br/>
	 * <br/>
	 * 
	 * For correctness its important that the inputed automaton has <b>no dead
	 * ends</b> nor <b>duplicate transitions</b>.
	 * 
	 * @param progressTimer
	 *            Timer used for responding to timeouts and operation
	 *            cancellation.
	 * @param logger
	 *            Logger of the Ultimate framework.
	 * @param buechi
	 *            The buechi automaton to reduce with no dead ends nor with
	 *            duplicate transitions
	 * @param useSCCs
	 *            If the simulation calculation should be optimized using SCC,
	 *            Strongly Connected Components.
	 * @param stateFactory
	 *            The state factory used for creating states.
	 * @param game
	 *            The fair direct game graph to use for simulation.
	 * @throws OperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	public FairDirectSimulation(final IProgressAwareTimer progressTimer, final Logger logger,
			final INestedWordAutomatonOldApi<LETTER, STATE> buechi, final boolean useSCCs,
			final StateFactory<STATE> stateFactory, final FairDirectGameGraph<LETTER, STATE> game)
					throws OperationCanceledException {
		this(progressTimer, logger, buechi, useSCCs, stateFactory, Collections.emptyList(), game);
	}

	/**
	 * Creates a new fair simulation that tries to reduce the given buechi
	 * automaton using <b>fair and direct simulation</b>. Uses a given
	 * collection of equivalence classes to optimize the simulation.<br/>
	 * After construction the simulation starts and results can be get by using
	 * {@link #getResult()}.<br/>
	 * <br/>
	 * 
	 * For correctness its important that the inputed automaton has <b>no dead
	 * ends</b> nor <b>duplicate transitions</b>.
	 * 
	 * @param progressTimer
	 *            Timer used for responding to timeouts and operation
	 *            cancellation.
	 * @param logger
	 *            Logger of the Ultimate framework.
	 * @param buechi
	 *            The buechi automaton to reduce with no dead ends nor with
	 *            duplicate transitions
	 * @param useSCCs
	 *            If the simulation calculation should be optimized using SCC,
	 *            Strongly Connected Components.
	 * @param stateFactory
	 *            The state factory used for creating states.
	 * @param possibleEquivalentClasses
	 *            A collection of sets which contains states of the buechi
	 *            automaton that may be merge-able. States which are not in the
	 *            same set are definitely not merge-able which is used as an
	 *            optimization for the simulation
	 * @param game
	 *            The fair direct game graph to use for simulation.
	 * @throws OperationCanceledException
	 *             If the operation was canceled, for example from the Ultimate
	 *             framework.
	 */
	public FairDirectSimulation(final IProgressAwareTimer progressTimer, final Logger logger,
			final INestedWordAutomatonOldApi<LETTER, STATE> buechi, final boolean useSCCs,
			final StateFactory<STATE> stateFactory, final Collection<Set<STATE>> possibleEquivalentClasses,
			final FairDirectGameGraph<LETTER, STATE> game) throws OperationCanceledException {
		super(progressTimer, logger, buechi, useSCCs, stateFactory, possibleEquivalentClasses, game);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * buchiReduction.ASimulation#getSimulationPerformance()
	 */
	@Override
	public SimulationPerformance getSimulationPerformance() {
		if (m_HasFinished) {
			return m_Performance;
		} else {
			return super.getSimulationPerformance();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * buchiReduction.fair.FairSimulation#attemptMerge(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	protected FairGameGraphChanges<LETTER, STATE> attemptMerge(final STATE firstState, final STATE secondState)
			throws OperationCanceledException {
		// Use previous calculated direct simulation results as optimization
		if (getGameGraph() instanceof FairDirectGameGraph) {
			FairDirectGameGraph<LETTER, STATE> game = (FairDirectGameGraph<LETTER, STATE>) getGameGraph();
			// If states direct simulate each other (in both directions) we can
			// safely merge without validating the change.
			if (game.isDirectSimulating(game.getSpoilerVertex(firstState, secondState, false))
					&& game.isDirectSimulating(game.getSpoilerVertex(secondState, firstState, false))) {
				if (getLogger().isDebugEnabled()) {
					getLogger().debug("\tAttempted merge for " + firstState + " and " + secondState
							+ " clearly is possible since they direct simulate each other.");
				}
				return null;
			}
		}

		// If there is no direct simulation attempt the merge using fair
		// simulation
		return super.attemptMerge(firstState, secondState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * buchiReduction.fair.FairSimulation#attemptTransitionRemoval(java.lang.
	 * Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	protected FairGameGraphChanges<LETTER, STATE> attemptTransitionRemoval(final STATE src, final LETTER a,
			final STATE dest, final STATE invoker) throws OperationCanceledException {
		// Use previous calculated direct simulation results as optimization
		if (getGameGraph() instanceof FairDirectGameGraph) {
			FairDirectGameGraph<LETTER, STATE> game = (FairDirectGameGraph<LETTER, STATE>) getGameGraph();
			// If invoker direct simulates the destination we can safely remove
			// the transition without validating the change.
			if (game.isDirectSimulating(game.getSpoilerVertex(dest, invoker, false))) {
				if (getLogger().isDebugEnabled()) {
					getLogger().debug("\tAttempted transition removal for " + src + " -" + a + "-> " + dest
							+ " clearly is possible since invoker " + invoker + " direct simulates " + dest + ".");
				}
				return null;
			}
		}

		// If there is no direct simulation attempt the removal using fair
		// simulation
		return super.attemptTransitionRemoval(src, a, dest, invoker);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * buchiReduction.ASimulation#calculateInfinityOfSCC(de.uni_freiburg.
	 * informatik.ultimate.util.scc.StronglyConnectedComponent)
	 */
	@Override
	protected int calculateInfinityOfSCC(final StronglyConnectedComponent<Vertex<LETTER, STATE>> scc) {
		if (m_IsCurrentlyDirectSimulation) {
			// In a direct simulation every SCC will have a local infinity of 1
			return 1;
		} else {
			// Use the original fair infinity
			return super.calculateInfinityOfSCC(scc);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.
	 * buchiReduction.ASimulation#doSimulation()
	 */
	@Override
	public void doSimulation() throws OperationCanceledException {
		m_Performance = new SimulationPerformance(ESimulationType.FAIRDIRECT, isUsingSCCs());
		m_Performance.startTimeMeasure(ETimeMeasure.OVERALL_TIME);
		m_Performance.startTimeMeasure(ETimeMeasure.SIMULATION_ONLY_TIME);

		int directSimSimulationSteps = 0;
		long directSimSCCBuildTime = 0;

		// First calculate direct simulation
		if (getGameGraph() instanceof FairDirectGameGraph) {
			FairDirectGameGraph<LETTER, STATE> game = (FairDirectGameGraph<LETTER, STATE>) getGameGraph();

			// Do direct simulation
			getLogger().debug("Starting direct simulation...");
			m_IsCurrentlyDirectSimulation = true;
			game.transformToDirectGameGraph();
			DirectSimulation<LETTER, STATE> directSim = new DirectSimulation<LETTER, STATE>(getProgressTimer(),
					getLogger(), isUsingSCCs(), getStateFactory(), game);
			directSim.doSimulation();

			// Remember results before transforming back and clear changes made
			// in the direct simulation
			game.rememberAndClearDirectSimulationResults();

			// Remember performance data
			directSimSimulationSteps = directSim.getSimulationPerformance()
					.getCountingMeasureResult(ECountingMeasure.SIMULATION_STEPS);
			directSimSCCBuildTime = directSim.getSimulationPerformance().getTimeMeasureResult(ETimeMeasure.BUILD_SCC,
					EMultipleDataOption.ADDITIVE);

			// Transform back to fair simulation
			game.transformToFairGameGraph();
			// Reset performance data linkage to fair simulation
			getGameGraph().setSimulationPerformance(super.getSimulationPerformance());
			m_IsCurrentlyDirectSimulation = false;
			getLogger().debug("Starting fair simulation...");
		}

		m_Performance.stopTimeMeasure(ETimeMeasure.SIMULATION_ONLY_TIME);

		// After that do the normal fair simulation process that will use the
		// overridden methods which profit from the direct simulation.
		super.doSimulation();

		SimulationPerformance fairPerformance = super.getSimulationPerformance();
		long durationFairSimOnly = fairPerformance.getTimeMeasureResult(ETimeMeasure.SIMULATION_ONLY_TIME,
				EMultipleDataOption.ADDITIVE);
		if (durationFairSimOnly != SimulationPerformance.NO_TIME_RESULT) {
			m_Performance.addTimeMeasureValue(ETimeMeasure.SIMULATION_ONLY_TIME, durationFairSimOnly);
		}

		long duration = m_Performance.stopTimeMeasure(ETimeMeasure.OVERALL_TIME);
		// Add time building of the graph took to the overall time since this
		// happens outside of simulation
		long durationGraph = fairPerformance.getTimeMeasureResult(ETimeMeasure.BUILD_GRAPH_TIME,
				EMultipleDataOption.ADDITIVE);
		if (durationGraph != SimulationPerformance.NO_TIME_RESULT) {
			duration += durationGraph;
			m_Performance.addTimeMeasureValue(ETimeMeasure.OVERALL_TIME, duration);
		}

		getLogger().info((isUsingSCCs() ? "SCC version" : "nonSCC version") + " of fairdirect simulation took "
				+ duration + " milliseconds");

		// Merge performance data
		// TODO A clone method would be far better
		if (directSimSimulationSteps == SimulationPerformance.NO_COUNTING_RESULT) {
			directSimSimulationSteps = 0;
		}
		if (directSimSCCBuildTime == SimulationPerformance.NO_TIME_RESULT) {
			directSimSCCBuildTime = 0;
		}
		m_Performance.addTimeMeasureValue(ETimeMeasure.BUILD_SCC,
				fairPerformance.getTimeMeasureResult(ETimeMeasure.BUILD_SCC, EMultipleDataOption.ADDITIVE)
						+ directSimSCCBuildTime);
		m_Performance.setCountingMeasure(ECountingMeasure.SIMULATION_STEPS,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.SIMULATION_STEPS) + directSimSimulationSteps);
		m_Performance.addTimeMeasureValue(ETimeMeasure.BUILD_GRAPH_TIME,
				fairPerformance.getTimeMeasureResult(ETimeMeasure.BUILD_GRAPH_TIME, EMultipleDataOption.ADDITIVE));
		m_Performance.addTimeMeasureValue(ETimeMeasure.BUILD_RESULT_TIME,
				fairPerformance.getTimeMeasureResult(ETimeMeasure.BUILD_RESULT_TIME, EMultipleDataOption.ADDITIVE));
		m_Performance.setCountingMeasure(ECountingMeasure.REMOVED_STATES,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.REMOVED_STATES));
		m_Performance.setCountingMeasure(ECountingMeasure.REMOVED_TRANSITIONS,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.REMOVED_TRANSITIONS));
		m_Performance.setCountingMeasure(ECountingMeasure.FAILED_MERGE_ATTEMPTS,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.FAILED_MERGE_ATTEMPTS));
		m_Performance.setCountingMeasure(ECountingMeasure.FAILED_TRANSREMOVE_ATTEMPTS,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.FAILED_TRANSREMOVE_ATTEMPTS));
		m_Performance.setCountingMeasure(ECountingMeasure.BUCHI_STATES,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.BUCHI_STATES));
		m_Performance.setCountingMeasure(ECountingMeasure.BUCHI_TRANSITIONS,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.BUCHI_TRANSITIONS));
		m_Performance.setCountingMeasure(ECountingMeasure.GAMEGRAPH_VERTICES,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.GAMEGRAPH_VERTICES));
		m_Performance.setCountingMeasure(ECountingMeasure.GAMEGRAPH_EDGES,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.GAMEGRAPH_EDGES));
		m_Performance.setCountingMeasure(ECountingMeasure.SCCS,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.SCCS));
		m_Performance.setCountingMeasure(ECountingMeasure.GLOBAL_INFINITY,
				fairPerformance.getCountingMeasureResult(ECountingMeasure.GLOBAL_INFINITY));

		m_HasFinished = true;
	}

	/**
	 * Returns if the simulation currently mimics the behavior of a
	 * DirectSimulation or a FairSimulation.
	 * 
	 * @return True if the simulation currently mimics the behavior of a
	 *         DirectSimulation, false if it mimics a FairSimulation.
	 */
	protected boolean isCurrentlyDirectGameGraph() {
		return m_IsCurrentlyDirectSimulation;
	}
}
