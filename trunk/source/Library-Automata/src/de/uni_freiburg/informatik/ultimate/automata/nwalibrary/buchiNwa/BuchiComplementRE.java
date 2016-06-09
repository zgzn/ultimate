/*
 * Copyright (C) 2012-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2009-2015 University of Freiburg
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
 * along with the ULTIMATE Automata Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.buchiNwa;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.LibraryIdentifiers;
import de.uni_freiburg.informatik.ultimate.automata.ResultChecker;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonOldApi;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.PowersetDeterminizer;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.DeterminizeUnderappox;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.ReachableStatesCopy;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;

public class BuchiComplementRE<LETTER,STATE> implements IOperation<LETTER,STATE> {

	private final AutomataLibraryServices mServices;
	private final ILogger mLogger;

	private final INestedWordAutomatonOldApi<LETTER,STATE> mOperand;
	private INestedWordAutomatonOldApi<LETTER,STATE> mResult;
	
	private boolean mbuchiComplementREApplicable;
	
	@Override
	public String operationName() {
		return "buchiComplementRE";
	}

	@Override
	public String startMessage() {
		return "Start " + operationName() + " Operand " + 
			mOperand.sizeInformation();
	}

	@Override
	public String exitMessage() {
		if (mbuchiComplementREApplicable) {
			return "Finished " + operationName() + " Result " + 
				mResult.sizeInformation();
		} else {
			return "Unable to perform " + operationName() + "on this input";
		}
	}

	@Override
	public INestedWordAutomatonOldApi<LETTER,STATE> getResult() throws AutomataLibraryException {
		if (mbuchiComplementREApplicable) {
			return mResult;
		} else {
			assert mResult == null;
			throw new UnsupportedOperationException("Operation was not applicable");
		}
	}
	
	
	public BuchiComplementRE(AutomataLibraryServices services,
			StateFactory<STATE> stateFactory,
			INestedWordAutomatonOldApi<LETTER,STATE> operand) throws AutomataLibraryException {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(LibraryIdentifiers.PLUGIN_ID);
		mOperand = operand;
		mLogger.info(startMessage());
		final INestedWordAutomatonOldApi<LETTER,STATE> operandWithoutNonLiveStates = 
				(new ReachableStatesCopy<LETTER,STATE>(mServices, operand, false, false, false, true)).getResult();
		if (operandWithoutNonLiveStates.isDeterministic()) {
			mLogger.info("Rüdigers determinization knack not necessary, already deterministic");
			mResult = (new BuchiComplementDeterministic<LETTER,STATE>(mServices, operandWithoutNonLiveStates)).getResult();
		}
		else {
			final PowersetDeterminizer<LETTER,STATE> pd = 
					new PowersetDeterminizer<LETTER,STATE>(operandWithoutNonLiveStates, true, stateFactory);
			final INestedWordAutomatonOldApi<LETTER,STATE> determinized = 
					(new DeterminizeUnderappox<LETTER,STATE>(mServices, operandWithoutNonLiveStates,pd)).getResult();
			final INestedWordAutomatonOldApi<LETTER,STATE> determinizedComplement =
					(new BuchiComplementDeterministic<LETTER,STATE>(mServices, determinized)).getResult();
			final INestedWordAutomatonOldApi<LETTER,STATE> intersectionWithOperand =
					(new BuchiIntersectDD<LETTER,STATE>(mServices, operandWithoutNonLiveStates, determinizedComplement, true)).getResult();
			final NestedLassoRun<LETTER,STATE> run = (new BuchiIsEmpty<LETTER,STATE>(mServices, intersectionWithOperand)).getAcceptingNestedLassoRun();
			if (run == null) {
				mLogger.info("Rüdigers determinization knack applicable");
				mbuchiComplementREApplicable = true;
				mResult = determinizedComplement;
			}
			else {
				mLogger.info("Rüdigers determinization knack not applicable");
				mbuchiComplementREApplicable = false;
				mResult = null;
			}
		}


		
		mLogger.info(exitMessage());
	}
	
	
	/**
	 * Return true if buchiComplementRE was applicable on the input.
	 */
	public boolean applicable() {
		return mbuchiComplementREApplicable;
	}

	@Override
	public boolean checkResult(StateFactory<STATE> stateFactory)
			throws AutomataLibraryException {
		return ResultChecker.buchiComplement(mServices, mOperand, mResult);
	}

}
