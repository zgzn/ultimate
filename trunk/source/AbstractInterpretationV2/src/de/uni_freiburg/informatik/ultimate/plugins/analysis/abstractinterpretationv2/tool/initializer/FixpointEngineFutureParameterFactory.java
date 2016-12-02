package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.tool.initializer;

import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.symboltable.BoogieSymbolTable;
import de.uni_freiburg.informatik.ultimate.boogie.type.PreprocessorAnnotation;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceProvider;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IProgressAwareTimer;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.FixpointEngineParameters;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.IAbstractStateStorage;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.IDebugHelper;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.ILoopDetector;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.ITransitionProvider;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.IVariableProvider;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.rcfg.FutureRcfgVariableProvider;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.rcfg.RcfgAbstractStateStorageProvider;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.rcfg.RcfgDebugHelper;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.empty.EmptyDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.dataflow.DataflowDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.relational.octagon.OctagonDomain.LiteralCollectorFactory;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.RCFGArrayIndexCollector;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.VPDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.preferences.AbsIntPrefInitializer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.BoogieIcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.IIcfg;

public class FixpointEngineFutureParameterFactory {

	private final BoogieSymbolTable mSymbolTable;
	private final IIcfg<?> mRoot;
	private final IUltimateServiceProvider mServices;
	private final LiteralCollectorFactory mLiteralCollector;

	public FixpointEngineFutureParameterFactory(final IIcfg<?> root, final LiteralCollectorFactory literalCollector,
			final IUltimateServiceProvider services) {
		mRoot = root;
		mServices = services;

		final PreprocessorAnnotation pa = PreprocessorAnnotation.getAnnotation(root);
		if (pa == null || pa.getSymbolTable() == null) {
			throw new IllegalArgumentException("Could not get BoogieSymbolTable");
		}
		mSymbolTable = pa.getSymbolTable();
		mLiteralCollector = literalCollector;
	}

	@SuppressWarnings("unchecked")
	public <STATE extends IAbstractState<STATE, CodeBlock, IProgramVar>>
			FixpointEngineParameters<STATE, CodeBlock, IProgramVar, BoogieIcfgLocation, Expression>
			createParamsFuture(final IProgressAwareTimer timer,
					final ITransitionProvider<CodeBlock, BoogieIcfgLocation> transitionProvider,
					final ILoopDetector<CodeBlock> loopDetector) {
		final IAbstractDomain<STATE, CodeBlock, IProgramVar> domain =
				(IAbstractDomain<STATE, CodeBlock, IProgramVar>) selectDomainFutureCfg();
		final IAbstractStateStorage<STATE, CodeBlock, IProgramVar, BoogieIcfgLocation> storageProvider =
				new RcfgAbstractStateStorageProvider<>(domain.getMergeOperator(), mServices, transitionProvider);
		final IVariableProvider<STATE, CodeBlock, IProgramVar> variableProvider =
				new FutureRcfgVariableProvider<>(mSymbolTable, mRoot.getSymboltable(), mServices);
		final IDebugHelper<STATE, CodeBlock, IProgramVar, BoogieIcfgLocation> debugHelper =
				new RcfgDebugHelper<>(mRoot.getCfgSmtToolkit(), mServices, mRoot.getSymboltable());

		return new FixpointEngineParameters<STATE, CodeBlock, IProgramVar, BoogieIcfgLocation, Expression>(mServices)
				.setDomain(domain).setLoopDetector(loopDetector).setStorage(storageProvider)
				.setTransitionProvider(transitionProvider).setVariableProvider(variableProvider)
				.setDebugHelper(debugHelper).setTimer(timer);
	}

	/**
	 * Creates parameters for when the equality domain is invoked from another plugin (e.g. HeapSeparator) and should
	 * not take (all) its parameters from the settings.
	 *
	 * @param timer
	 * @param transitionProvider
	 * @param loopDetector
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <STATE extends IAbstractState<STATE, CodeBlock, IProgramVar>>
			FixpointEngineParameters<STATE, CodeBlock, IProgramVar, BoogieIcfgLocation, Expression>
			createParamsFutureEqualityDomain(final IProgressAwareTimer timer,
					final ITransitionProvider<CodeBlock, BoogieIcfgLocation> transitionProvider,
					final ILoopDetector<CodeBlock> loopDetector) {
		final ILogger logger = mServices.getLoggingService().getLogger(Activator.PLUGIN_ID);
		final IAbstractDomain<STATE, CodeBlock, IProgramVar> domain =
				(IAbstractDomain<STATE, CodeBlock, IProgramVar>) createEqualityDomain(logger);
		final IAbstractStateStorage<STATE, CodeBlock, IProgramVar, BoogieIcfgLocation> storageProvider =
				new RcfgAbstractStateStorageProvider<>(domain.getMergeOperator(), mServices, transitionProvider);
		final IVariableProvider<STATE, CodeBlock, IProgramVar> variableProvider =
				new FutureRcfgVariableProvider<>(mSymbolTable, mRoot.getSymboltable(), mServices);
		final IDebugHelper<STATE, CodeBlock, IProgramVar, BoogieIcfgLocation> debugHelper =
				new RcfgDebugHelper<>(mRoot.getCfgSmtToolkit(), mServices, mRoot.getSymboltable());
		return new FixpointEngineParameters<STATE, CodeBlock, IProgramVar, BoogieIcfgLocation, Expression>(mServices)
				.setDomain(domain).setLoopDetector(loopDetector).setStorage(storageProvider)
				.setTransitionProvider(transitionProvider).setVariableProvider(variableProvider)
				.setDebugHelper(debugHelper).setTimer(timer);
	}

	private IAbstractDomain<?, CodeBlock, IProgramVar> selectDomainFutureCfg() {
		final IPreferenceProvider prefs = mServices.getPreferenceProvider(Activator.PLUGIN_ID);
		final String selectedDomain = prefs.getString(AbsIntPrefInitializer.LABEL_ABSTRACT_DOMAIN);
		final ILogger logger = mServices.getLoggingService().getLogger(Activator.PLUGIN_ID);

		if (EmptyDomain.class.getSimpleName().equals(selectedDomain)) {
			return new EmptyDomain<>();
		} else if (DataflowDomain.class.getSimpleName().equals(selectedDomain)) {
			return new DataflowDomain(logger);
		} else if (VPDomain.class.getSimpleName().equals(selectedDomain)) {
			return createEqualityDomain(logger);
		}
		throw new UnsupportedOperationException(getFailureString(selectedDomain));
	}

	private IAbstractDomain<?, CodeBlock, IProgramVar> createEqualityDomain(final ILogger logger) {
		final RCFGArrayIndexCollector preAnalysis = new RCFGArrayIndexCollector(mRoot);
		preAnalysis.postProcess();
		return new VPDomain(logger, mRoot.getCfgSmtToolkit().getManagedScript(), mServices, mRoot.getSymboltable(),
				preAnalysis);
	}

	private static String getFailureString(final String selectedDomain) {
		return "The value \"" + selectedDomain + "\" of preference \"" + AbsIntPrefInitializer.LABEL_ABSTRACT_DOMAIN
				+ "\" was not considered before! ";
	}
}