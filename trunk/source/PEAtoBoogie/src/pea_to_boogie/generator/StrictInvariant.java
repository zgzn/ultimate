/*
 * Copyright (C) 2013-2015 Jochen Hoenicke (hoenicke@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE PEAtoBoogie plug-in.
 * 
 * The ULTIMATE PEAtoBoogie plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE PEAtoBoogie plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE PEAtoBoogie plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE PEAtoBoogie plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE PEAtoBoogie plug-in grant you additional permission 
 * to convey the resulting work.
 */
package pea_to_boogie.generator;

import java.util.List;

import pea.CDD;
import pea.Decision;
import pea.RangeDecision;
public class StrictInvariant {

	 public CDD genStrictInv(CDD cdd, List<String> resetList) {
	            
		    if (cdd == CDD.TRUE) {	         	     	
	            return CDD.TRUE;
	        }
	        if (cdd == CDD.FALSE) {
	            return CDD.FALSE;
	        }
	        
	    	final CDD[] childs = cdd.getChilds();
	    	final Decision decision = cdd.getDecision();
            
          	CDD decisionCDD;
          	if (!resetList.contains(decision.getVar())) {
          	   decisionCDD = 
          			toStrictRange(decision.getVar(), ((RangeDecision) decision).getLimits()); 
          	   final CDD[] newChilds = new CDD[childs.length];
               for (int i = 0; i < childs.length; i++) {
	        	newChilds[i] = genStrictInv(childs[i],resetList);	            	
	           } 
               return  decisionCDD.getDecision().simplify(newChilds);	
          	} else {
      			assert childs.length == 2;
      			decisionCDD = genStrictInv(childs[0], resetList).or(genStrictInv(childs[1], resetList));

          	}
          	return decisionCDD;       
	    }
	    public CDD toStrictRange(String var, int[] limits) {

            return  RangeDecision.create(var, -2, (limits[0] / 2));  

	    }

}
