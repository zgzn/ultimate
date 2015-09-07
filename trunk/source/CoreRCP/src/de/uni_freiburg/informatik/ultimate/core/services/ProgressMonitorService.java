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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

public class ProgressMonitorService implements IStorable, IToolchainCancel, IProgressMonitorService {

	private static final String sKey = "CancelNotificationService";

	private IProgressMonitor mMonitor;
	private long mDeadline;
	private Logger mLogger;
	private IToolchainCancel mToolchainCancel;
	private boolean mCancelRequest;

	public ProgressMonitorService(IProgressMonitor monitor, long deadline, Logger logger, IToolchainCancel cancel) {
		assert monitor != null;
		mMonitor = monitor;
		mDeadline = deadline;
		mLogger = logger;
		mToolchainCancel = cancel;
		mCancelRequest = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.core.services.IProgressMonitorService
	 * #continueProcessing()
	 */
	@Override
	public boolean continueProcessing() {
		boolean cancel = mMonitor.isCanceled() || mCancelRequest || System.currentTimeMillis() > mDeadline;
		if (cancel) {
			mLogger.debug("Do not continue processing!");
		}
		return !cancel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.core.services.IProgressMonitorService
	 * #setSubtask(java.lang.String)
	 */
	@Override
	public void setSubtask(String task) {
		mMonitor.subTask(task);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.core.services.IProgressMonitorService
	 * #setDeadline(long)
	 */
	@Override
	public void setDeadline(long date) {
		if (System.currentTimeMillis() >= date) {
			mLogger.warn(String
					.format("Deadline was set to a date in the past, " + "effectively stopping the toolchain. "
							+ "Is this what you intended? Value of date was %,d", date));

		}
		mDeadline = date;
	}

	static ProgressMonitorService getService(IToolchainStorage storage) {
		assert storage != null;
		return (ProgressMonitorService) storage.getStorable(sKey);
	}

	public static String getServiceKey() {
		return sKey;
	}

	@Override
	public void destroy() {
		mMonitor.done();
		mMonitor = null;
		mToolchainCancel = null;
		mLogger = null;
	}

	@Override
	public void cancelToolchain() {
		mToolchainCancel.cancelToolchain();
		mCancelRequest = true;
	}

}
