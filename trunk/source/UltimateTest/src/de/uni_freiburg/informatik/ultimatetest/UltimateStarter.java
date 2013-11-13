package de.uni_freiburg.informatik.ultimatetest;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.UltimateCore;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.UltimateCore.Ultimate_Mode;

/**
 * 
 * This class wraps the Ultimate application and allows to start it without
 * setting an IController object.
 * 
 * Call runUltimate() to execute it and complete after processing the results
 * (to release resources).
 * 
 * @author dietsch
 * 
 */
public class UltimateStarter {

	private Logger mLogger;
	private FileAppender mAppender;

	private File mInputFile;
	private File mSettingsFile;
	private File mToolchainFile;
	private long mDeadline;

	private String mLogPattern;
	private File mLogFile;
	private UltimateCore mCurrentUltimateInstance;

	public UltimateStarter(File inputFile, File settingsFile,
			File toolchainFile, long deadline) {
		mLogger = Logger.getLogger(UltimateStarter.class);
		mInputFile = inputFile;
		mSettingsFile = settingsFile;
		mToolchainFile = toolchainFile;
		mDeadline = deadline;
		detachLogger();
	}

	public UltimateStarter(File inputFile, File settingsFile,
			File toolchainFile, long deadline, File logFile, String logPattern) {
		this(inputFile, settingsFile, toolchainFile, deadline);
		mLogFile = logFile;
		mLogPattern = logPattern;
	}

	public void runUltimate() throws Exception {
		if (mCurrentUltimateInstance != null) {
			throw new Exception(
					"You must call complete() before re-using this instance ");
		}
		mCurrentUltimateInstance = new UltimateCore(
				Ultimate_Mode.EXTERNAL_EXECUTION);
		mCurrentUltimateInstance.setM_InputFile(mInputFile);
		mCurrentUltimateInstance.setSettingsFile(mSettingsFile);
		mCurrentUltimateInstance.setToolchainXML(mToolchainFile);
		attachLogger();
		mCurrentUltimateInstance.setDeadline(System.currentTimeMillis()
				+ mDeadline);
		mCurrentUltimateInstance.start(null);
	}

	public void complete() {
		detachLogger();
	}

	private void attachLogger() {
		if (mLogFile == null) {
			return;
		}

		detachLogger();
		try {
			mAppender = new FileAppender(new PatternLayout(mLogPattern),
					mLogFile.getAbsolutePath());
			Logger.getRootLogger().addAppender(mAppender);
		} catch (IOException e1) {
			detachLogger();
			mLogger.fatal("Failed to create logfile " + mLogFile + ". Reason: "
					+ e1);
		}
	}

	private void detachLogger() {
		if (mAppender == null) {
			return;
		}
		Logger.getRootLogger().removeAppender(mAppender);
	}

}
