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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.simulation.performance;

/**
 * Types of time measures.
 * 
 * @author Daniel Tischner
 *
 */
public enum ETimeMeasure {
	/**
	 * The time building the game graph took.
	 */
	BUILD_GRAPH,
	/**
	 * The time building the result automaton took.
	 */
	BUILD_RESULT,
	/**
	 * The time building the SCC took.
	 */
	BUILD_SCC,
	/**
	 * The time computing which vertex down states are safe, took in nwa game
	 * graph generation.
	 */
	COMPUTE_SAFE_VERTEX_DOWN_STATES,
	/**
	 * The time computing priorities for summarize edges took in nwa game graph
	 * generation.
	 */
	COMPUTE_SUMMARIZE_EDGE_PRIORITIES,
	/**
	 * The time generating summarize edges took in nwa game graph generation.
	 */
	GENERATE_SUMMARIZE_EDGES,
	/**
	 * The overall time an operation took.
	 */
	OVERALL,
	/**
	 * The time the simulation only took, this is the overall time minus the
	 * time to build the graph and the result.
	 */
	SIMULATION_ONLY
}
