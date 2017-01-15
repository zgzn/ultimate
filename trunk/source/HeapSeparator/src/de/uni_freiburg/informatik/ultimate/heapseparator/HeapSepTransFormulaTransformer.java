package de.uni_freiburg.informatik.ultimate.heapseparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.icfgtransformer.ITransformulaTransformer;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IcfgEdge;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.TransFormula;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.TransFormulaBuilder;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.UnmodifiableTransFormula;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.UnmodifiableTransFormula.Infeasibility;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramConst;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVarOrConst;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.Substitution;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayEquality;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayIndex;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayUpdate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalSelect;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalStore;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.VPDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.VPDomainHelpers;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.elements.EqNode;

public class HeapSepTransFormulaTransformer implements ITransformulaTransformer<TransFormula> {
	
	private final TransFormula mTransFormula;
	private final  ManagedScript mScript;
	private final VPDomain<IcfgEdge> mVpDomain;
	private final NewArrayIdProvider mNewArrayIdProvider;
	


	public HeapSepTransFormulaTransformer(TransFormula transFormula, 
			ManagedScript script, VPDomain<IcfgEdge> vpDomain, NewArrayIdProvider newArrayIdProvider) {
		super();
		this.mTransFormula = transFormula;
		this.mScript = script;
		this.mVpDomain = vpDomain;
		this.mNewArrayIdProvider = newArrayIdProvider;
	}

	@Override
	public TransFormula getTransformationResult() {
		final UnmodifiableTransFormula newTf = splitArraysInTransFormula(mTransFormula);
		return newTf;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		assert false : "TODO: insert an appropriate name";
		return null;
	}

	public static TermVariable getSplitTermVariable(final String arrayName, final int splitIndex, final Sort sort,
			final Script script) {
		return script.variable(String.format("{}_split_{}", arrayName, splitIndex), sort);
	}

	public static IProgramVar getBoogieVarFromTermVar(final TermVariable tv, final Map<IProgramVar, TermVariable> map1,
			final Map<IProgramVar, TermVariable> map2) {
		for (final Entry<IProgramVar, TermVariable> en : map1.entrySet()) {
			if (en.getValue().equals(tv)) {
				return en.getKey();
			}
		}
		for (final Entry<IProgramVar, TermVariable> en : map2.entrySet()) {
			if (en.getValue().equals(tv)) {
				return en.getKey();
			}
		}
		assert false : "did not find " + tv + " in the given maps";
		return null;
	}

	private UnmodifiableTransFormula splitArraysInTransFormula(final TransFormula tf) {

		final Map<IProgramVar, TermVariable> newInVars = new HashMap<>(tf.getInVars());
		final Map<IProgramVar, TermVariable> newOutVars = new HashMap<>(tf.getOutVars());
		
		Term intermediateFormula = tf.getFormula();

		intermediateFormula = substituteArrayUpdates(tf, newInVars, newOutVars, intermediateFormula);

		intermediateFormula = substituteArrayEqualites(tf, newInVars, newOutVars, intermediateFormula);

		intermediateFormula = substituteRemainingStoresAndSelects(tf, newInVars, newOutVars, intermediateFormula);
		
		boolean newEmptyNonTheoryConsts = false;
		Set<IProgramConst> newNonTheoryConsts = null;
		boolean newEmptyBranchEncoders = false;
		Collection<TermVariable> newBranchEncoders = null; // TODO: deal with these for working LBE, right?..
		boolean newEmptyAuxVars = false;
		TransFormulaBuilder tfBuilder = new TransFormulaBuilder(
				newInVars, 
				newOutVars, 
				newEmptyNonTheoryConsts, 
				newNonTheoryConsts, 
				newEmptyBranchEncoders, 
				newBranchEncoders, 
				newEmptyAuxVars);
		
		tfBuilder.setFormula(intermediateFormula);
		
		tfBuilder.setInfeasibility(Infeasibility.NOT_DETERMINED);
		
		return tfBuilder.finishConstruction(mScript);
	}



	private Term substituteRemainingStoresAndSelects(final TransFormula tf,
			final Map<IProgramVar, TermVariable> newInVars, final Map<IProgramVar, TermVariable> newOutVars,
			Term intermediateFormula) {
		final Map<Term, Term> substitutionMapPvoc = new HashMap<>();
		
		List<MultiDimensionalSelect> mdSelects = 
				MultiDimensionalSelect.extractSelectShallow(intermediateFormula, true);//TODO allowArrayValues??
		List<MultiDimensionalSelect> mdSelectsInOriginalTf = 
				MultiDimensionalSelect.extractSelectShallow(tf.getFormula(), true);//TODO allowArrayValues??
		for (MultiDimensionalSelect mds : mdSelects) {
			if (!mdSelectsInOriginalTf.contains(mds)) {
				// the current mds comes from a replacement we made earlier (during ArrayUpdate or ArrayEquality-handling)
				continue;
			}
			if (!mVpDomain.getPreAnalysis().isArrayTracked(
					VPDomainHelpers.getArrayTerm(mds.getArray()),
					VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf))) {
//					VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars))) {
				continue;
			}

			//TODO: we can't work on the normalized TermVariables like this, I think..
			IProgramVarOrConst oldArray = 
					mVpDomain.getPreAnalysis().getIProgramVarOrConstOrLiteral(
							VPDomainHelpers.getArrayTerm(mds.getArray()),
							VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf));
//							VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars));
			assert oldArray != null;

			List<EqNode> pointers = convertArrayIndexToEqNodeList(newInVars, newOutVars, mds.getIndex());

			IProgramVarOrConst newArray = mNewArrayIdProvider.getNewArrayId(oldArray, pointers);

			updateMappingsForSubstitution(oldArray, newArray, newInVars, newOutVars, substitutionMapPvoc);
		}

		List<MultiDimensionalStore> mdStores = MultiDimensionalStore.extractArrayStoresShallow(intermediateFormula);
		List<MultiDimensionalStore> mdStoresInOriginalTf = MultiDimensionalStore.extractArrayStoresShallow(tf.getFormula());
		for (MultiDimensionalStore mds : mdStores) {
			if (!mdStoresInOriginalTf.contains(mds)) {
				// the current mds comes from a replacement we made earlier (during ArrayUpdate or ArrayEquality-handling)
				continue;
			}
			if (!mVpDomain.getPreAnalysis().isArrayTracked(
					VPDomainHelpers.getArrayTerm(mds.getArray()),
					VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf))) {
//					VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars))) {
				continue;
			}

			IProgramVarOrConst oldArray = 
					mVpDomain.getPreAnalysis().getIProgramVarOrConstOrLiteral(
							VPDomainHelpers.getArrayTerm(mds.getArray()),
							VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf));

			List<EqNode> pointers = convertArrayIndexToEqNodeList(newInVars, newOutVars, mds.getIndex());
					
			IProgramVarOrConst newArray = mNewArrayIdProvider.getNewArrayId(oldArray, pointers);

			updateMappingsForSubstitution(oldArray, newArray, newInVars, newOutVars, substitutionMapPvoc);
		}
		intermediateFormula = new Substitution(mScript, substitutionMapPvoc).transform(intermediateFormula);	
		return intermediateFormula;
	}



	private Term substituteArrayUpdates(final TransFormula tf,
			final Map<IProgramVar, TermVariable> newInVars, final Map<IProgramVar, TermVariable> newOutVars,
			Term formula) {

		final Map<Term, Term> substitutionMapPvoc = new HashMap<>();

		List<ArrayUpdate> arrayUpdates = ArrayUpdate.extractArrayUpdates(formula);
		for (ArrayUpdate au : arrayUpdates) {
			

			List<EqNode> pointers = convertArrayIndexToEqNodeList(newInVars, newOutVars, au.getMultiDimensionalStore().getIndex());

			if (mVpDomain.getPreAnalysis().isArrayTracked(au.getNewArray(), 
					VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf))) {
//					VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars))) {
				IProgramVarOrConst lhs = 
						mVpDomain.getPreAnalysis().getIProgramVarOrConstOrLiteral(
								au.getNewArray(), 
								VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf));
//								VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars));
				assert lhs != null;
				IProgramVarOrConst newArrayLhs = mNewArrayIdProvider.getNewArrayId(lhs, pointers);
				updateMappingsForSubstitution(lhs, newArrayLhs, newInVars, newOutVars, substitutionMapPvoc);
			}
			
			if (mVpDomain.getPreAnalysis().isArrayTracked(au.getOldArray(), 
					VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf))) {
//					VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars))) {
				IProgramVarOrConst rhsArray = 
						mVpDomain.getPreAnalysis().getIProgramVarOrConstOrLiteral(
								au.getOldArray(), 
								VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf));
//								VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars));
				assert rhsArray != null;
				IProgramVarOrConst newArrayRhs = mNewArrayIdProvider.getNewArrayId(rhsArray, pointers);
				updateMappingsForSubstitution(rhsArray, newArrayRhs, newInVars, newOutVars, substitutionMapPvoc);
			}
		}
		
		Term newTerm = new Substitution(mScript, substitutionMapPvoc).transform(formula);
		return newTerm;
	}



	private List<EqNode> convertArrayIndexToEqNodeList(final Map<IProgramVar, TermVariable> newInVars,
			final Map<IProgramVar, TermVariable> newOutVars, ArrayIndex index) {
		List<EqNode> pointers = index.stream()
				.map(indexTerm -> mVpDomain.getPreAnalysis().getEqNode(
						indexTerm, 
						VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars)))
				.collect(Collectors.toList());
		return pointers;
	}



	private Term substituteArrayEqualites(final TransFormula tf,
			final Map<IProgramVar, TermVariable> newInVars, 
			final Map<IProgramVar, TermVariable> newOutVars, 
			final Term intermediateFormula) {
		List<ArrayEquality> arrayEqualities = ArrayEquality.extractArrayEqualities(intermediateFormula);
		Map<Term, Term> equalitySubstitution = new HashMap<>();
		mScript.lock(this);
		for (ArrayEquality ae : arrayEqualities) {
			/*
			 * plan:
			 *  (- check compatibility --> should be guaranteed by NewArrayIdProvider)
			 *  - make an assignment between all the partitions
			 */
			if (!mVpDomain.getPreAnalysis().isArrayTracked(ae.getLhs(), 
//					VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars))
					VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf))
					|| !mVpDomain.getPreAnalysis().isArrayTracked(ae.getRhs(), 
					VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf))) {
//							VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars))) {
				continue;
			}
			
			
			List<Term> newEqualities = new ArrayList<>();
			
			IProgramVarOrConst oldLhs = mVpDomain.getPreAnalysis().getIProgramVarOrConstOrLiteral(
							ae.getLhs(), 
					VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf));
//							VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars));
			List<IProgramVarOrConst> newLhss = mNewArrayIdProvider.getAllNewArrayIds(oldLhs);

			IProgramVarOrConst oldRhs = mVpDomain.getPreAnalysis().getIProgramVarOrConstOrLiteral(
							ae.getRhs(), 
					VPDomainHelpers.computeProgramVarMappingFromTransFormula(tf));
//							VPDomainHelpers.computeProgramVarMappingFromInVarOutVarMappings(newInVars, newOutVars));
			List<IProgramVarOrConst> newRhss = mNewArrayIdProvider.getAllNewArrayIds(oldRhs);
			
			
			assert newLhss.size() == newRhss.size();
			for (int i = 0; i < newLhss.size(); i++) {
				IProgramVarOrConst newLhs = newLhss.get(i);
				IProgramVarOrConst newRhs = newRhss.get(i);
				Term newEquality = mScript.term(this, "=", 
						newLhs.getTerm(), 
						newRhs.getTerm());
				newEqualities.add(newEquality);
				
				if (tf.getInVars().containsKey(oldLhs)) {
					newInVars.remove(oldLhs);
					newInVars.put((IProgramVar) newLhs, (TermVariable) newLhs.getTerm());
				}
				if (tf.getInVars().containsKey(oldRhs)) {
					newInVars.remove(oldRhs);
					newInVars.put((IProgramVar) newRhs, (TermVariable) newRhs.getTerm());
				}
				if (tf.getOutVars().containsKey(oldLhs)) {
					newOutVars.remove(oldLhs);
					newOutVars.put((IProgramVar) newLhs, (TermVariable) newLhs.getTerm());
				}
				if (tf.getOutVars().containsKey(oldRhs)) {
					newOutVars.remove(oldRhs);
					newOutVars.put((IProgramVar) newRhs, (TermVariable) newRhs.getTerm());
				}

			}
			assert newEqualities.size() > 0;
			Term newConjunctionOfEquations = newEqualities.size() == 1 ?
					newEqualities.get(0) :
					mScript.term(this, "and", newEqualities.toArray(new Term[newEqualities.size()]));
			equalitySubstitution.put(ae.getOriginalTerm(), newConjunctionOfEquations);
		}
		mScript.unlock(this);
		Term newTerm = new Substitution(mScript, equalitySubstitution).transform(intermediateFormula);
		return newTerm;
	}



	/**
	 * 
	 * - updates the maps newInVars and newOutVars
	 * - updates the map substitutionMap
	 * 
	 * This method is for the simple cases, where we just need to replace the arrayIdentifer "one-by-one".
	 * (not like the ArrayEquality, where we replace one-by-many)
	 * 
	 * @param oldArray
	 * @param newArray
	 * @param tf
	 * @param newInVars
	 * @param newOutVars
	 * @param substitutionMap
	 */
	private void updateMappingsForSubstitution(IProgramVarOrConst oldArray, IProgramVarOrConst newArray,
			final Map<IProgramVar, TermVariable> newInVars,
			final Map<IProgramVar, TermVariable> newOutVars,
			final Map<Term, Term> substitutionMap) {
		if (oldArray instanceof IProgramVar) {
			assert newArray instanceof IProgramVar : "right?..";
		
			TermVariable inv = newInVars.get(oldArray);
			TermVariable outv = newOutVars.get(oldArray);

			TermVariable invNewTv = null;
			if (inv != null) {
				invNewTv = mScript.constructFreshCopy((TermVariable) newArray.getTerm());
				newInVars.remove(oldArray);
				newInVars.put((IProgramVar) newArray, invNewTv);
				substitutionMap.put(inv, invNewTv);
			}
		
			if (outv != null) {
				TermVariable newTv;
				if (inv == outv) {
					newTv = invNewTv;
				} else {
					newTv = mScript.constructFreshCopy((TermVariable) newArray.getTerm());
				}
				newOutVars.remove(oldArray);
				newOutVars.put((IProgramVar) newArray, newTv);
				substitutionMap.put(outv, newTv);
			}
			
		} else {
			/*
			 * the array id is a constant (or literal)
			 *  --> there are no changes to the invar/outvar mappings, only to the substitution
			 */
			substitutionMap.put(oldArray.getTerm(), newArray.getTerm());
		}
	}

}