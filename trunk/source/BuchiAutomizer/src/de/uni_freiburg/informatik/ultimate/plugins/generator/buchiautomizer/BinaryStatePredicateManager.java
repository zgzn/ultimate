package de.uni_freiburg.informatik.ultimate.plugins.generator.buchiautomizer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWord;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogieType;
import de.uni_freiburg.informatik.ultimate.boogie.type.PrimitiveType;
import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Util;
import de.uni_freiburg.informatik.ultimate.logic.simplification.SimplifyDDA;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SMT;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rankingfunctions.SupportingInvariant;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rankingfunctions.functions.LinearRankingFunction;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rankingfunctions.functions.RankingFunction;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ModifiableGlobalVariableManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootAnnot;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager.TermVarsProc;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.TraceChecker;

public class BinaryStatePredicateManager {
	
	private static Logger s_Logger = 
			UltimateServices.getInstance().getLogger(Activator.s_PLUGIN_ID);
	
	public final static String s_UnseededIdentifier = "unseeded";
	public final static String s_OldRankIdentifier = "oldRank";
	public final static int s_MaxLexComponents = 10;
	

	
	private final Script m_Script;
	private final SmtManager m_SmtManager;
	private final BoogieVar m_OldRankVariable;
	private final BoogieVar m_UnseededVariable;
	private final BoogieVar[] m_OldRankVariables;
	
	/**
	 * True if predicates have been computed.
	 * False if predicates have been cleared or predicates have never been
	 * computed so far.
	 */
	private boolean m_ProvidesPredicates;
	
	private IPredicate m_StemPrecondition;
	private IPredicate m_StemPostcondition;
	private IPredicate m_Honda;
	private IPredicate m_RankEqualityAndSi;

	private IPredicate m_RankDecrease;

	
	private Collection<SupportingInvariant> m_SiList;
	private LinearRankingFunction m_LinRf;
	
	
	private Term[] m_LexTerms;
	private IPredicate[] m_LexEquality;
	private IPredicate[] m_LexDecrease;

	/**
	 * Is the loop also terminating without the stem?
	 */
	private Boolean m_LoopTermination;
	
	
	public BinaryStatePredicateManager(SmtManager smtManager) {
		m_Script = smtManager.getScript();
		m_SmtManager = smtManager;
		Boogie2SMT boogie2Smt = smtManager.getBoogie2Smt();
		m_OldRankVariable = constructGlobalBoogieVar(s_OldRankIdentifier, boogie2Smt, BoogieType.intType);
		m_UnseededVariable = constructGlobalBoogieVar(s_UnseededIdentifier, boogie2Smt, BoogieType.boolType);
		
		m_OldRankVariables = new BoogieVar[s_MaxLexComponents];
		for (int i=0; i<s_MaxLexComponents; i++) {
			String name = s_OldRankIdentifier + i;
			m_OldRankVariables[i] = constructGlobalBoogieVar(name, boogie2Smt, BoogieType.intType);
		}
	}


	/**
	 * Construct a global BoogieVar and the corresponding oldVar. Return the
	 * global var.
	 * @param type 
	 */
	private BoogieVar constructGlobalBoogieVar(String name,
			Boogie2SMT boogie2Smt, PrimitiveType type) {
		BoogieVar globalBv;
		globalBv = boogie2Smt.constructBoogieVar(
				name, null, type, false, null);
		boogie2Smt.constructBoogieVar(
				name, null, type, true, null);
		return globalBv;
	}
	
	
	public boolean providesPredicates() {
		return m_ProvidesPredicates;
	}

	public boolean isLoopWithoutStemTerminating() {
		assert m_ProvidesPredicates;
		return m_LoopTermination;
	}
	
	public LinearRankingFunction getLinRf() {
		assert m_ProvidesPredicates;
		return m_LinRf;
	}

	public Collection<SupportingInvariant> getSiList() {
		assert m_ProvidesPredicates;
		return m_SiList;
	}

	public IPredicate getStemPrecondition() {
		assert m_ProvidesPredicates;
		return m_StemPrecondition;
	}

	public IPredicate getStemPostcondition() {
		assert m_ProvidesPredicates;
		return m_StemPostcondition;
	}

	public IPredicate getHondaPredicate() {
		assert m_ProvidesPredicates;
		return m_Honda;
	}

	public IPredicate getRankEqAndSi() {
		assert m_ProvidesPredicates;
		return m_RankEqualityAndSi;
	}
	
	public BoogieVar getUnseededVariable() {
		assert m_ProvidesPredicates;
		return m_UnseededVariable;
	}
	
	public BoogieVar getOldRankVariable() {
		assert m_ProvidesPredicates;
		return m_OldRankVariable;
	}
	
	public void clearPredicates() {
		if (!m_ProvidesPredicates) {
			throw new AssertionError("no predicates provided cannot clear");
		}
		m_LoopTermination = null;
		m_SiList = null;
		m_LinRf = null;
		m_StemPrecondition = null;
		m_StemPostcondition = null;
		m_Honda = null;
		m_RankEqualityAndSi = null;
		m_RankDecrease = null;
		m_ProvidesPredicates = false;
	}

	public void computePredicates(boolean loopTermination,
			LinearRankingFunction linRf, 
			Collection<SupportingInvariant> siList) {
		assert m_LoopTermination == null;
		assert m_SiList == null;
		assert m_LinRf == null;
		assert m_StemPrecondition == null;
		assert m_StemPostcondition == null;
		assert m_Honda == null;
		assert m_RankEqualityAndSi == null;
		assert m_RankDecrease == null;
		m_LoopTermination = loopTermination;
		m_SiList = siList;
		m_LinRf = linRf;
		IPredicate unseededPredicate = unseededPredicate();
		m_StemPrecondition = unseededPredicate;
		IPredicate siConjunction = computeSiConjunction(m_SiList);
		boolean siConjunctionIsTrue = isTrue(siConjunction);
		if (siConjunctionIsTrue) {
			m_StemPostcondition = unseededPredicate;
		} else {
			TermVarsProc tvp = m_SmtManager.and(unseededPredicate, siConjunction);
			m_StemPostcondition = m_SmtManager.newPredicate(tvp.getFormula(), 
					tvp.getProcedures(), tvp.getVars(), tvp.getClosedFormula()); 
		}
		Term rfTerm = m_LinRf.asFormula(m_Script, m_SmtManager.getSmt2Boogie());
		IPredicate rankEquality = getRankEquality(rfTerm);
		if (siConjunctionIsTrue) {
			m_RankEqualityAndSi = rankEquality;
		} else {
			TermVarsProc tvp = m_SmtManager.and(rankEquality, siConjunction);
			m_RankEqualityAndSi = m_SmtManager.newPredicate(tvp.getFormula(), 
					tvp.getProcedures(), tvp.getVars(), tvp.getClosedFormula()); 
		}
		m_RankDecrease = getRankDecrease(rfTerm);
		IPredicate unseededOrRankDecrease; 
		{
			TermVarsProc tvp = m_SmtManager.or(unseededPredicate, m_RankDecrease);
			unseededOrRankDecrease = m_SmtManager.newPredicate(tvp.getFormula(), 
					tvp.getProcedures(), tvp.getVars(), tvp.getClosedFormula());
		}
		if (siConjunctionIsTrue) {
			m_Honda = unseededOrRankDecrease;
		} else {
			TermVarsProc tvp = m_SmtManager.and(siConjunction, unseededOrRankDecrease);
			m_Honda = m_SmtManager.newPredicate(tvp.getFormula(), 
					tvp.getProcedures(), tvp.getVars(), tvp.getClosedFormula());
		}
		m_ProvidesPredicates = true;
	}


//	private BoogieVar constructOldRankVariable() {
//		Sort sort = m_Script.sort("Int");
//		String name = "oldRank";
//		TermVariable termVariable = m_Script.variable(name, sort);
//		
//		ApplicationTerm defaultConstant;
//		{
//			String defaultConstantName = "c_" + name;
//			m_Script.declareFun(defaultConstantName, new Sort[0], sort);
//			defaultConstant = (ApplicationTerm) m_Script.term(defaultConstantName);
//		}
//		ApplicationTerm primedConstant;
//		{
//			String primedConstantName = "c_" + name + "_primed";
//			m_Script.declareFun(primedConstantName, new Sort[0], sort);
//			primedConstant = (ApplicationTerm) m_Script.term(primedConstantName);
//		}
//		BoogieVar oldRank = new BoogieVar(name,
//				null, BoogieType.intType, false, 
//				termVariable, defaultConstant, primedConstant);
//		return oldRank;
//	}
//	
//	private BoogieVar constructUnseededVariable() {
//		Sort sort = m_Script.sort("Bool");
//		String name = "unseeded";
//		TermVariable termVariable = m_Script.variable(name, sort);
//		
//		ApplicationTerm defaultConstant;
//		{
//			String defaultConstantName = "c_" + name;
//			m_Script.declareFun(defaultConstantName, new Sort[0], sort);
//			defaultConstant = (ApplicationTerm) m_Script.term(defaultConstantName);
//		}
//		ApplicationTerm primedConstant;
//		{
//			String primedConstantName = "c_" + name + "_primed";
//			m_Script.declareFun(primedConstantName, new Sort[0], sort);
//			primedConstant = (ApplicationTerm) m_Script.term(primedConstantName);
//		}
//		BoogieVar oldRank = new BoogieVar(name,
//				null, BoogieType.boolType, false, 
//				termVariable, defaultConstant, primedConstant);
//		return oldRank;
//	}

	
	private IPredicate unseededPredicate() {
		Set<BoogieVar> vars = new HashSet<BoogieVar>(1);
		vars.add(m_UnseededVariable);
		Term formula = m_UnseededVariable.getTermVariable();
		IPredicate result = m_SmtManager.newPredicate(formula, 
				new String[0], vars,m_UnseededVariable.getDefaultConstant());
		return result;
	}
	
	private IPredicate computeSiConjunction(Iterable<SupportingInvariant> siList) {
		List<IPredicate> siPreds = new ArrayList<IPredicate>();
		for (SupportingInvariant si : siList) {
			IPredicate siPred = supportingInvariant2Predicate(si);
			siPreds.add(siPred);
		}
		TermVarsProc tvp = m_SmtManager.and(siPreds.toArray(new IPredicate[0]));
		IPredicate siConjunction = m_SmtManager.newPredicate(tvp.getFormula(), 
				tvp.getProcedures(), tvp.getVars(), tvp.getClosedFormula()); 
		return siConjunction;
	}
	
	
	private IPredicate supportingInvariant2Predicate(SupportingInvariant si) {
		Set<BoogieVar> coefficients = si.getCoefficients().keySet();
		Term formula = si.asTerm(m_SmtManager.getScript(), m_SmtManager.getSmt2Boogie());
		formula = m_SmtManager.simplify(formula);
		TermVarsProc termVarsProc = m_SmtManager.computeTermVarsProc(formula);
		assert termVarsProc.getVars().equals(coefficients);
		
		IPredicate result = m_SmtManager.newPredicate(termVarsProc.getFormula(),
				termVarsProc.getProcedures(), termVarsProc.getVars(), termVarsProc.getClosedFormula());
		return result;
	}
	
	private IPredicate getRankEquality(Term rfTerm) {
		return getRankInEquality(rfTerm, "=", m_OldRankVariable, false);
	}
	
	private IPredicate getRankDecrease(Term rfTerm) {
		return getRankInEquality(rfTerm, ">", m_OldRankVariable, true);
	}
	
	private void decodeLex(LinearRankingFunction rf) {
		Term term = m_LinRf.asFormula(m_Script, m_SmtManager.getSmt2Boogie());
		m_LexTerms = new Term[] { term };
		m_LexEquality = new IPredicate[m_LexTerms.length];
		for (int i=0; i<m_LexTerms.length; i++) {
			m_LexEquality[i] = getRankInEquality(
					m_LexTerms[i], "=", m_OldRankVariable, false);
		}
		m_LexDecrease = new IPredicate[m_LexTerms.length];
		for (int i=0; i<m_LexTerms.length; i++) {
			m_LexDecrease[i] = getRankInEquality(
					m_LexTerms[i], "=", m_OldRankVariable, false);
		}
	}
	
	
	private IPredicate getRankEquality(Term[] lexTerms) {
		TermVarsProc tvp = m_SmtManager.and(m_LexEquality);
		IPredicate result = m_SmtManager.newPredicate(tvp.getFormula(), 
				tvp.getProcedures(), tvp.getVars(), tvp.getClosedFormula());
		return result;
	}
	
	
	private IPredicate getRankDecrease(Term[] lexTerms) {
		IPredicate[] disjuncts = new IPredicate[lexTerms.length];
		for (int i=lexTerms.length-1; i>=0; i--) {
			IPredicate[] conjuncts = new IPredicate[lexTerms.length-i];
			for (int j=lexTerms.length-1; j>=i+1; j--) {
				conjuncts[lexTerms.length-j] = m_LexEquality[j];
			}
			conjuncts[0] = m_LexDecrease[i];
			TermVarsProc tvp = m_SmtManager.and(conjuncts);
			disjuncts[i] = m_SmtManager.newPredicate(tvp);
		}
		
		TermVarsProc tvp = m_SmtManager.or(disjuncts);
		IPredicate result = m_SmtManager.newPredicate(tvp);
		return result;
	}

	
	
	private IPredicate getRankInEquality(Term rfTerm, String symbol, 
			BoogieVar oldRankVariable,boolean addGeq0) {
		assert symbol.equals("=") || symbol.equals(">");
		TermVarsProc termVarsProc = m_SmtManager.computeTermVarsProc(rfTerm);

		Term equality = m_Script.term(symbol, oldRankVariable.getTermVariable(), rfTerm);
		if (addGeq0) {
			equality = Util.and(m_Script, equality, getRankGeq0(oldRankVariable));
		}
		
		Set<BoogieVar> vars = new HashSet<BoogieVar>();
		vars.add(oldRankVariable);
		vars.addAll(termVarsProc.getVars());
		
		Term closedFormula = SmtManager.computeClosedFormula(equality, vars, m_Script);
		
		IPredicate result = m_SmtManager.newPredicate(equality,
				termVarsProc.getProcedures(), vars, closedFormula);
		return result;
	}
	
	
	private Term getRankGeq0(BoogieVar oldRankVariable) {
		Term geq = m_Script.term(">=", oldRankVariable.getTermVariable(), m_Script.numeral(BigInteger.ZERO));
		return geq;
	}
	
	
	
	public boolean checkSupportingInvariant(SupportingInvariant si, 
			NestedWord<CodeBlock> stem, NestedWord<CodeBlock> loop, 
			ModifiableGlobalVariableManager modGlobVarManager) {
		boolean result = true;
		TraceChecker traceChecker;
		IPredicate truePredicate = m_SmtManager.newTruePredicate();
		IPredicate siPred = supportingInvariant2Predicate(si);
		if (isTrue(siPred)) {
			siPred = truePredicate;
		}
		traceChecker = new TraceChecker(truePredicate, siPred, null, stem, m_SmtManager,
				modGlobVarManager);
		LBool stemCheck = traceChecker.isCorrect();
		if (stemCheck == LBool.UNSAT) {
			traceChecker.finishTraceCheckWithoutInterpolantsOrProgramExecution();
//			IPredicate[] interpolants = m_TraceChecker.getInterpolants(new TraceChecker.AllIntegers());
//			interpolants.toString();
		} else {
			result = false;			
		}
		traceChecker = new TraceChecker(siPred, siPred, null, stem, m_SmtManager,
				modGlobVarManager);
		LBool loopCheck = traceChecker.isCorrect();
		if (loopCheck == LBool.UNSAT) {
			traceChecker.finishTraceCheckWithoutInterpolantsOrProgramExecution();
//			IPredicate[] interpolants = m_TraceChecker.getInterpolants(new TraceChecker.AllIntegers());
//			interpolants.toString();
		} else {
			result = false;
		}
		return result;
	}
	
	public boolean checkRankDecrease(NestedWord<CodeBlock> loop, 
			ModifiableGlobalVariableManager modGlobVarManager) {
		TraceChecker traceChecker = new TraceChecker(m_RankEqualityAndSi, 
				m_RankDecrease, null, loop, m_SmtManager, modGlobVarManager);
		LBool loopCheck = traceChecker.isCorrect();
		traceChecker.finishTraceCheckWithoutInterpolantsOrProgramExecution();
		if (loopCheck == LBool.UNSAT) {
			return true;
		} else {
			return false;
		}
	}
	

	
	private static boolean isTrue(IPredicate pred) {
		Term term = pred.getFormula();
		if (term instanceof ApplicationTerm) {
			ApplicationTerm appTerm = (ApplicationTerm) term;
			if (appTerm.getFunction().getName().equals("true")) {
				return true;
			}
		}
		return false;
	}
	
	


}
