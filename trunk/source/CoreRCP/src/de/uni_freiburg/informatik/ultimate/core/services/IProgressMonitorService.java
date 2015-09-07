/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.core.services;

public interface IProgressMonitorService {

	/**
	 * Return false iff cancellation of Toolchain is requested or deadline is
	 * exceeded.
	 */
	public boolean continueProcessing();

	public void setSubtask(String task);

	/**
	 * Set a time limit after which the toolchain should be stopped.
	 * 
	 * A convenient way of setting this deadline is using
	 * System.currentTimeMillis() + timelimit (in ms) as value right before
	 * calling start(...).
	 * 
	 * @param date
	 *            A date in the future (aka, the difference, measured in
	 *            milliseconds, between the current time and midnight, January
	 *            1, 1970 UTC) after which a running toolchain should be
	 *            stopped.
	 */
	public void setDeadline(long date);

	public void cancelToolchain();

}
