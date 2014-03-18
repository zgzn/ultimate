/*
 * Copyright (C) 2009-2014 University of Freiburg
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
 * along with the ULTIMATE Automata Library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations;

import java.util.Map;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.automata.Activator;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.ResultChecker;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonOldApi;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonSimple;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordAutomatonFilteredStates;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.DifferenceDD;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.IOpWithDelayedDeadEndRemoval;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.reachableStatesAutomaton.NestedWordAutomatonReachableStates;
import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;


public class Difference<LETTER,STATE> implements IOperation<LETTER,STATE>, IOpWithDelayedDeadEndRemoval<LETTER, STATE> {

	protected static Logger s_Logger = 
		UltimateServices.getInstance().getLogger(Activator.PLUGIN_ID);
	
	private final INestedWordAutomatonSimple<LETTER,STATE> m_FstOperand;
	private final INestedWordAutomatonSimple<LETTER,STATE> m_SndOperand;
	private DeterminizeNwa<LETTER,STATE> m_SndDeterminized;
	private final IStateDeterminizer<LETTER, STATE> m_StateDeterminizer;
	private ComplementDeterministicNwa<LETTER,STATE> m_SndComplemented;
	private IntersectNwa<LETTER, STATE> m_Intersect;
	private NestedWordAutomatonReachableStates<LETTER,STATE> m_Result;
	private NestedWordAutomatonFilteredStates<LETTER, STATE> m_ResultWOdeadEnds;
	private final StateFactory<STATE> m_StateFactory;
	
	
	@Override
	public String operationName() {
		return "difference";
	}
	
	
	@Override
	public String startMessage() {
		return "Start " + operationName() + ". First operand " + 
				m_FstOperand.sizeInformation() + ". Second operand " + 
				m_SndOperand.sizeInformation();	
	}
	
	
	@Override
	public String exitMessage() {
		return "Finished " + operationName() + " Result " + 
				m_Result.sizeInformation();
	}
	
	
	
	
	public Difference(StateFactory<STATE> stateFactory, 
			INestedWordAutomatonOldApi<LETTER,STATE> fstOperand,
			INestedWordAutomatonSimple<LETTER,STATE> sndOperand
			) throws AutomataLibraryException {
		m_FstOperand = fstOperand;
		m_SndOperand = sndOperand;
		m_StateFactory = m_FstOperand.getStateFactory();
		m_StateDeterminizer = new PowersetDeterminizer<LETTER,STATE>(sndOperand, true, stateFactory);
		s_Logger.info(startMessage());
		computateDifference(false);
		s_Logger.info(exitMessage());
	}
	
	
	public Difference(INestedWordAutomatonOldApi<LETTER,STATE> fstOperand,
			INestedWordAutomatonSimple<LETTER,STATE> sndOperand,
			IStateDeterminizer<LETTER, STATE> stateDeterminizer,
			StateFactory<STATE> sf,
			boolean finalIsTrap) throws AutomataLibraryException {
		m_FstOperand = fstOperand;
		m_SndOperand = sndOperand;
		m_StateFactory = sf;
		m_StateDeterminizer = stateDeterminizer;
		s_Logger.info(startMessage());
		computateDifference(finalIsTrap);
		s_Logger.info(exitMessage());
	}
	
	private void computateDifference(boolean finalIsTrap) throws AutomataLibraryException {
		if (m_StateDeterminizer instanceof PowersetDeterminizer) {
			TotalizeNwa<LETTER, STATE> sndTotalized = new TotalizeNwa<LETTER, STATE>(m_SndOperand, m_StateFactory);
			ComplementDeterministicNwa<LETTER,STATE> sndComplemented = new ComplementDeterministicNwa<LETTER, STATE>(sndTotalized);
			IntersectNwa<LETTER, STATE> intersect = new IntersectNwa<LETTER, STATE>(m_FstOperand, sndComplemented, m_StateFactory, finalIsTrap);
			NestedWordAutomatonReachableStates<LETTER, STATE> result = new NestedWordAutomatonReachableStates<LETTER, STATE>(intersect);
			if (!sndTotalized.nonDeterminismInInputDetected()) {
				m_SndComplemented = sndComplemented;
				m_Intersect = intersect;
				m_Result = result;
				s_Logger.info("Subtrahend was deterministic. Have not used determinization.");
				return;
			} else {
			s_Logger.info("Subtrahend was not deterministic. Recomputing result with determinization.");
			}
		}
		m_SndDeterminized = new DeterminizeNwa<LETTER,STATE>(m_SndOperand,m_StateDeterminizer,m_StateFactory);
		m_SndComplemented = new ComplementDeterministicNwa<LETTER, STATE>(m_SndDeterminized);
		m_Intersect = new IntersectNwa<LETTER, STATE>(m_FstOperand, m_SndComplemented, m_StateFactory, finalIsTrap);
		m_Result = new NestedWordAutomatonReachableStates<LETTER, STATE>(m_Intersect);
	}
	






	@Override
	public INestedWordAutomatonOldApi<LETTER, STATE> getResult()
			throws OperationCanceledException {
		if (m_ResultWOdeadEnds == null) {
			return m_Result;
		} else {
			return m_ResultWOdeadEnds;
		}
	}


	
	public boolean checkResult(StateFactory<STATE> sf) throws AutomataLibraryException {
		s_Logger.info("Start testing correctness of " + operationName());
		INestedWordAutomatonOldApi<LETTER, STATE> fstOperandOldApi = ResultChecker.getOldApiNwa(m_FstOperand);
		INestedWordAutomatonOldApi<LETTER, STATE> sndOperandOldApi = ResultChecker.getOldApiNwa(m_SndOperand);
		INestedWordAutomatonOldApi<LETTER, STATE> resultDD = 
				(new DifferenceDD<LETTER, STATE>(fstOperandOldApi,sndOperandOldApi, 
						new PowersetDeterminizer<LETTER, STATE>(sndOperandOldApi,true, sf),sf,false,false)).getResult();
		boolean correct = true;
//		correct &= (resultDD.size() == m_Result.size());
//		assert correct;
		correct &= (ResultChecker.nwaLanguageInclusion(resultDD, m_Result, sf) == null);
		assert correct;
		correct &= (ResultChecker.nwaLanguageInclusion(m_Result, resultDD, sf) == null);
		assert correct;
		if (!correct) {
			ResultChecker.writeToFileIfPreferred(operationName() + "Failed", "", m_FstOperand,m_SndOperand);
		}
		s_Logger.info("Finished testing correctness of " + operationName());
		return correct;
	}





	@Override
	public boolean removeDeadEnds() {
		m_Result.computeDeadEnds();
		m_ResultWOdeadEnds = new NestedWordAutomatonFilteredStates<LETTER, STATE>(m_Result, m_Result.getWithOutDeadEnds());
		s_Logger.info("With dead ends: " + m_Result.getStates().size());
		s_Logger.info("Without dead ends: " + m_ResultWOdeadEnds.getStates().size());
		return m_Result.getStates().size() != m_ResultWOdeadEnds.getStates().size();
	}


	@Override
	public long getDeadEndRemovalTime() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Iterable<UpDownEntry<STATE>> getRemovedUpDownEntry() {
		return m_Result.getWithOutDeadEnds().getRemovedUpDownEntry();
	}


	public Map<STATE, Map<STATE, IntersectNwa<LETTER, STATE>.ProductState>> getFst2snd2res() {
		return m_Intersect.getFst2snd2res();
	}
	
	
	
	
	
	
}

