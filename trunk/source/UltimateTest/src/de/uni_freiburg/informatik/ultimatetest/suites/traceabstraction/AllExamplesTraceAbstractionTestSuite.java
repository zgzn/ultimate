/*
 * Copyright (C) 2015 Betim Musa (musab@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Test Library.
 * 
 * The ULTIMATE Test Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Test Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Test Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Test Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Test Library grant you additional permission 
 * to convey the resulting work.
 */
/**
 * 
 */
package de.uni_freiburg.informatik.ultimatetest.suites.traceabstraction;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimatetest.UltimateTestCase;

/**
 * @author musab@informatik.uni-freiburg.de
 *
 */
public class AllExamplesTraceAbstractionTestSuite extends
		AbstractTraceAbstractionTestSuite {
	private static final String[] m_Directories = { "examples/programs/" };
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getTimeout() {
		return 20 * 1000;
	}

	private static final boolean m_TraceAbstractionWithForwardPredicates = true;
	private static final boolean m_TraceAbstractionWithBackwardPredicates = true;
	private static final boolean m_TraceAbstractionCWithForwardPredicates = true;
	private static final boolean m_TraceAbstractionCWithBackwardPredicates = true;
	
	@Override
	public Collection<UltimateTestCase> createTestCases() {
		if (m_TraceAbstractionWithForwardPredicates) {
			addTestCase(
					"AutomizerBpl.xml",
					"automizer/ForwardPredicates.epf",
				    m_Directories,
				    new String[] {".bpl"});
		} 
		if (m_TraceAbstractionWithBackwardPredicates) {
			addTestCase(
					"AutomizerBpl.xml",
					"automizer/BackwardPredicates.epf",
				    m_Directories,
				    new String[] {".bpl"});
		}
		if (m_TraceAbstractionCWithForwardPredicates) {
			addTestCase(
					"AutomizerC.xml",
					"automizer/ForwardPredicates.epf",
				    m_Directories,
				    new String[] {".c", ".i"});
		}
		if (m_TraceAbstractionCWithBackwardPredicates) {
			addTestCase(
					"AutomizerC.xml",
					"automizer/BackwardPredicates.epf",
				    m_Directories,
				    new String[] {".c", ".i"});
		}
		return super.createTestCases();
	}
}
