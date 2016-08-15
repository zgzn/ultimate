/*
 * Copyright (C) 2014-2015 Jan Leike (leike@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE LassoRanker Library.
 * 
 * The ULTIMATE LassoRanker Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE LassoRanker Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE LassoRanker Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE LassoRanker Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE LassoRanker Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.lassoranker.variables;

import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVar;


/**
 * This is a wrapper for BoogieVar that extends RankVar.
 * 
 * @author Jan Leike
 */
public class BoogieVarWrapper extends RankVar {
	private static final long serialVersionUID = -452101904147428474L;
	
	private final IProgramVar mBoogieVar;
	
	public BoogieVarWrapper(IProgramVar boogieVar) {
		mBoogieVar = boogieVar;
	}
	
	@Override
	public Term getDefinition() {
		return mBoogieVar.getTermVariable();
	}
	
	@Override
	public String getIdentifier() {
		return mBoogieVar.getGloballyUniqueId();
	}
	
	@Override
	public String toString() {
		return mBoogieVar.toString();
	}
	
	@Override
	public int hashCode() {
		return mBoogieVar.hashCode();
	}
	
	public IProgramVar getBoogieVar() {
		return mBoogieVar;
	}
}
