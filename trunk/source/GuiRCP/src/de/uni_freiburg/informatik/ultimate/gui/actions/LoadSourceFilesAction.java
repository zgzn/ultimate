package de.uni_freiburg.informatik.ultimate.gui.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.toolchain.BasicToolchainJob;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.toolchain.DefaultToolchainJob;
import de.uni_freiburg.informatik.ultimate.core.services.PreludeProvider;
import de.uni_freiburg.informatik.ultimate.ep.ExtensionPoints;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.IController;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.ICore;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.ISource;
import de.uni_freiburg.informatik.ultimate.gui.GuiController;
import de.uni_freiburg.informatik.ultimate.gui.GuiToolchainJob;
import de.uni_freiburg.informatik.ultimate.gui.contrib.PreludeContribution;
import de.uni_freiburg.informatik.ultimate.gui.interfaces.IImageKeys;
import de.uni_freiburg.informatik.ultimate.gui.interfaces.IPreferencesKeys;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * The action that telles the core what files to open
 * 
 * may be do modification to listen for a response..
 * 
 * 
 * @author Christian Ortolf
 * @version 0.1.0 $LastChangedDate: 2013-10-28 15:45:22 +0100 (Mo, 28 Okt 2013)
 *          $ $LastChangedBy$LastChangedRevision: 173 $
 */
public class LoadSourceFilesAction extends Action implements IWorkbenchAction {

	private IWorkbenchWindow mWorkbenchWindow;

	private ICore mCore;

	private GuiController mController;

	public static final String s_ID = "de.uni_freiburg.informatik.ultimate.gui.LoadSourceFiles";

	public static final String s_DIALOG_NAME = "Open Source ... ";

	private final Logger mLogger;

	/**
	 * 
	 * @param window
	 *            the contexwindow
	 * @param ist
	 *            the steerablecore that will take the command
	 */
	public LoadSourceFilesAction(final IWorkbenchWindow window, final ICore icore, final GuiController controller,
			Logger logger) {
		mWorkbenchWindow = window;
		mCore = icore;
		mController = controller;
		mLogger = logger;
		setId(s_ID);
		setText(s_DIALOG_NAME);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(GuiController.sPLUGINID,
				IImageKeys.LOADSOURCEFILES));

	}

	/**
	 * the action opens a file dialog then passes the files for opening to the
	 * core
	 */
	public final void run() {
		// TODO: Check this code, its old and uses API in wrong ways

		ArrayList<ISource> sourceplugins = new ArrayList<ISource>();
		IExtensionRegistry reg = Platform.getExtensionRegistry();

		IConfigurationElement[] configElements_source = reg.getConfigurationElementsFor(ExtensionPoints.EP_SOURCE);
		// iterate through every config element
		for (IConfigurationElement element : configElements_source) {
			try {
				// create class from plugin
				ISource source = (ISource) element.createExecutableExtension("class");
				// and add to plugin ArrayList
				sourceplugins.add(source);
			} catch (CoreException e) {
				mLogger.error("Cannot create extension " + element, e);
			}
		}

		FileDialog fd = new FileDialog(mWorkbenchWindow.getShell(), SWT.OPEN | SWT.MULTI);
		fd.setText(s_DIALOG_NAME);

		ArrayList<String> extensions = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();

		extensions.add("*.*");
		names.add("All");

		for (ISource source : sourceplugins) {
			for (String s : source.getFileTypes()) {
				extensions.add("*." + s);
				names.add(source.getPluginName() + " (*." + s + ")");
			}
		}

		ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, GuiController.sPLUGINID);
		IEclipsePreferences prefscope = InstanceScope.INSTANCE.getNode(GuiController.sPLUGINID);
		String filterpath = prefscope.get(IPreferencesKeys.LASTPATH, null);
		fd.setFilterExtensions(extensions.toArray(new String[extensions.size()]));
		fd.setFilterNames(names.toArray(new String[names.size()]));
		fd.setFileName(filterpath);
		String fp = fd.open();
		if (fp != null) {
			prefscope.put(IPreferencesKeys.LASTPATH, fp);
			try {
				store.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (fp != null) {
			File prelude = PreludeContribution.getPreludeFile();
			BasicToolchainJob tcj = new GuiToolchainJob("Processing Toolchain", mCore, mController, new File(fp),
					prelude == null ? null : new PreludeProvider(prelude.getAbsolutePath(), mLogger), mLogger);
			tcj.schedule();
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
}
