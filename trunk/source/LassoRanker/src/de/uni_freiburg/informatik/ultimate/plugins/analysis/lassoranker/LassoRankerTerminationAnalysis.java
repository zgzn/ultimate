package de.uni_freiburg.informatik.ultimate.plugins.analysis.lassoranker;

import java.util.*;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;
import de.uni_freiburg.informatik.ultimate.logic.SMTLIBException;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SMT;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.lassoranker.exceptions.TermException;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.lassoranker.preferences.Preferences;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.lassoranker.preferences.Preferences.DivisionImplementation;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.lassoranker.preprocessors.*;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.lassoranker.templates.RankingFunctionTemplate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.TransFormula;


/**
 * This is the class that controls LassoRanker's (non-)termination argument
 * synthesis.
 * 
 * Tools that use LassoRanker as a library probably want to use this class
 * as an interface for invoking LassoRanker. This class can also be derived
 * for a more fine-grained control over the synthesis process.
 * 
 * @author Jan Leike
 */
public class LassoRankerTerminationAnalysis {
	private static Logger s_Logger =
			UltimateServices.getInstance().getLogger(Activator.s_PLUGIN_ID);
	
	/**
	 * Stem formula of the linear lasso program
	 */
	private TransFormula m_stem_transition;
	
	/**
	 * Loop formula of the linear lasso program
	 */
	private TransFormula m_loop_transition;
		
	/**
	 * Stem formula of the linear lasso program as linear inequalities in DNF
	 */
	private LinearTransition m_stem;
	
	/**
	 * Loop formula of the linear lasso program as linear inequalities in DNF
	 */
	private LinearTransition m_loop;
	
	/**
	 * SMT script that created the transition formulae
	 */
	protected Script m_old_script;
	
	/**
	 * New SMT script created by this class, used by the templates
	 */
	protected Script m_script;
	
	/**
	 * Auxiliary variables generated by the preprocessors
	 */
	protected Collection<TermVariable> m_auxVars;
	
	/**
	 * The current preferences
	 */
	protected Preferences m_preferences;
	
	/**
	 * The boogie2smt object that created the TransFormulas
	 */
	private Boogie2SMT m_boogie2smt;
	
	/**
	 * Constructor for the LassoRanker interface. Calling this invokes the
	 * preprocessor on the stem and loop formula.
	 * 
	 * If the stem is null, the stem has to be added separately by calling
	 * addStem().
	 * 
	 * @param script the SMT script used to construct the transition formulae
	 * @param boogie2smt the boogie2smt object that created the TransFormulas
	 * @param stem a transition formula corresponding to the lasso's stem
	 * @param loop a transition formula corresponding to the lasso's loop
	 * @param preferences configuration options for this plugin
	 * @throws TermException if preprocessing fails
	 */
	public LassoRankerTerminationAnalysis(Script script, Boogie2SMT boogie2smt,
			TransFormula stem, TransFormula loop, Preferences preferences)
					throws TermException {
		m_preferences = preferences;
		checkPreferences(preferences);
		m_boogie2smt = boogie2smt;
		
		m_old_script = script;
		m_script = SMTSolver.newScript(preferences.smt_solver_command,
				preferences.annotate_terms);
		
		m_auxVars = new HashSet<TermVariable>();
		
		m_stem_transition = stem;
		if (stem != null) {
			m_stem_transition = AuxiliaryMethods.matchInVars(boogie2smt, m_stem_transition);
			s_Logger.debug("Stem transition:\n" + m_stem_transition);
			m_stem = preprocess(m_stem_transition);
			s_Logger.debug("Preprocessed stem:\n" + m_stem);
		} else {
			m_stem = null;
		}
		
		assert(loop != null);
		m_loop_transition = AuxiliaryMethods.matchInVars(boogie2smt, loop);
		s_Logger.debug("Loop transition:\n" + m_loop_transition);
		m_loop = preprocess(m_loop_transition);
		s_Logger.debug("Preprocessed loop:\n" + m_loop);
	}
	
	/**
	 * Constructor for the LassoRanker interface. Calling this invokes the
	 * preprocessor on the stem and loop formula.
	 *  
	 * This constructor may only be supplied a loop transition, a stem has to
	 * be added later by calling addStem().
	 * 
	 * @param script the SMT script used to construct the transition formulae
	 * @param boogie2smt the boogie2smt object that created the TransFormulas
	 * @param loop a transition formula corresponding to the lasso's loop
	 * @param preferences configuration options for this plugin
	 * @throws TermException if preprocessing fails
	 */
	public LassoRankerTerminationAnalysis(Script script, Boogie2SMT boogie2smt,
			TransFormula loop, Preferences preferences) throws TermException {
		this(script, boogie2smt, null, loop, preferences);
	}
	
	/**
	 * Verify that the preferences are set self-consistent and sensible
	 * Issues a bunch of logger infos and warnings.
	 */
	protected void checkPreferences(Preferences preferences) {
		assert(preferences.num_strict_invariants >= 0);
		assert(preferences.num_non_strict_invariants >= 0);
		if (preferences.num_strict_invariants == 0 &&
				preferences.num_non_strict_invariants == 0) {
			s_Logger.warn("Generation of supporting invariants is disabled.");
		}
		if (preferences.division_implementation == DivisionImplementation.C_STYLE
				&& !preferences.enable_disjunction) {
			s_Logger.warn("Using C-style integer division, but support for " +
				"disjunctions is disabled.");
		}
	}
	
	/**
	 * @return an array of all preprocessors that should be called before
	 *         termination analysis
	 */
	protected PreProcessor[] getPreProcessors() {
		return new PreProcessor[] {
				new RewriteDivision(),
				new RewriteBooleans(),
				new RewriteTrueFalse(),
				new RewriteEquality(),
				new DNF(),
				new RemoveNegation()
		};
	}
	
	/**
	 * Add a stem transition to the lasso program.
	 * Calling this invokes the preprocessor on the stem transition.
	 * 
	 * @param stem a transition formula corresponding to the lasso's stem
	 * @throws TermException 
	 */
	public void addStem(TransFormula stem_transition) throws TermException {
		if (m_stem != null) {
			s_Logger.warn("Adding a stem to a lasso that already had one.");
		}
		m_stem_transition = AuxiliaryMethods.matchInVars(m_boogie2smt,
				stem_transition);
		s_Logger.debug("Adding stem transition:\n" + stem_transition);
		m_stem = preprocess(stem_transition);
		s_Logger.debug("Preprocessed stem:\n" + m_stem);
	}
	
	/**
	 * Preprocess the stem or loop transition. This applies the preprocessor
	 * classes and transforms the formula into a list of inequalities in DNF.
	 * 
	 * The list of preprocessors is given by this.getPreProcessors().
	 * 
	 * @see PreProcessor
	 * @throws TermException
	 */
	protected LinearTransition preprocess(TransFormula transition)
			throws TermException {
		s_Logger.info("Starting preprocessing step...");
		
		Term trans_term = transition.getFormula();
		
		// Apply preprocessors
		for (PreProcessor preprocessor : this.getPreProcessors()) {
			trans_term = preprocessor.process(m_old_script, trans_term);
			m_auxVars.addAll(preprocessor.getAuxVars());
		}
		
		s_Logger.debug(SMTPrettyPrinter.print(trans_term));
		
		LinearTransition linear_trans = LinearTransition.fromTerm(trans_term);
		if (!m_preferences.enable_disjunction
				&& !linear_trans.isConjunctive()) {
			throw new UnsupportedOperationException(
					"Support for non-conjunctive lasso programs " +
					"is disabled.");
		}
		
		return linear_trans;
	}
	
	/**
	 * Try to find a non-termination argument for the lasso program.
	 * 
	 * @return the non-termination argument or null of none is found
	 */
	public NonTerminationArgument checkNonTermination() {
		s_Logger.info("Checking for non-termination...");
		
		NonTerminationArgumentSynthesizer synthesizer =
				new NonTerminationArgumentSynthesizer(
						!m_preferences.nontermination_check_nonlinear,
						m_script,
						m_stem,
						m_loop,
						m_stem_transition,
						m_loop_transition
				);
		boolean nonterminating = synthesizer.checkForNonTermination();
		if (nonterminating) {
			s_Logger.info("Proved non-termination.");
			s_Logger.info(synthesizer.getArgument());
		}
		SMTSolver.resetScript(m_script, m_preferences.annotate_terms);
		return nonterminating ? synthesizer.getArgument() : null;
	}
	
	/**
	 * Try to find a termination argument for the lasso program specified by
	 * the given ranking function template.
	 * 
	 * @param template the ranking function template
	 * @return the non-termination argument or null of none is found
	 */
	public TerminationArgument tryTemplate(RankingFunctionTemplate template)
			throws SMTLIBException, TermException {
		// ignore stem
		s_Logger.info("Using template '" + template.getClass().getSimpleName()
				+ "'.");
		s_Logger.info("Template has degree " + template.getDegree() + ".");
		s_Logger.debug(template);
		
		TerminationArgumentSynthesizer synthesizer =
				new TerminationArgumentSynthesizer(m_script, m_stem_transition,
				m_loop_transition, m_stem, m_loop, m_preferences);
		boolean terminating = synthesizer.synthesize(template);
		if (terminating) {
			s_Logger.info("Proved termination.");
			s_Logger.info(synthesizer.getArgument());
		}
		SMTSolver.resetScript(m_script, m_preferences.annotate_terms);
		return terminating ? synthesizer.getArgument() : null;
	}
	
	/**
	 * Perform cleanup actions
	 */
	public void cleanUp() {
		m_script.exit();
		m_script = null;
	}
}
