/*
 * Copyright (C) 2013-2015 Betim Musa (musab@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AutomataScriptParser plug-in.
 * 
 * The ULTIMATE AutomataScriptParser plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AutomataScriptParser plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AutomataScriptParser plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AutomataScriptParser plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AutomataScriptParser plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.source.automatascriptparser.AST;


import de.uni_freiburg.informatik.ultimate.core.model.models.ILocation;
import de.uni_freiburg.informatik.ultimate.plugins.source.automatascriptparser.AtsASTNode;
/**
 * @author musab@informatik.uni-freiburg.de
 *
 */

public class AssignmentExpressionAST extends AtsASTNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7311672964327139443L;
	private AssignmentOperatorAST moperator;
	
	public AssignmentOperatorAST getOperator() {
		return moperator;
	}

	public void setOperator(AssignmentOperatorAST operator) {
		moperator = operator;
	}
	
	public AssignmentExpressionAST(ILocation loc, VariableExpressionAST var, AssignmentOperatorAST operator, AtsASTNode value) {
		super(loc);
		setOperator(operator);
		addOutgoingNode(var);
		addOutgoingNode(value);
		mreturnType = var.getReturnType();
		mexpectingType = mreturnType;
	}

	@Override
	public String toString() {
		return "AssignmentExpression [AssignmentOperator: " + operatorToString(moperator) + "]";
	}
	
	private String operatorToString(AssignmentOperatorAST o) {
		switch (o) {
		case ASSIGN: return " := ";
		case PLUSASSIGN: return " += ";
		case MINUSASSIGN: return " -= ";
		case MODASSIGN: return " %= ";
		case MULTASSIGN: return " *= ";
		case DIVASSIGN: return " /= ";
		default: return "";
		}
	}

	@Override
	public String getAsString() {
		AtsASTNode var = null;
		AtsASTNode value = null;
		for (final AtsASTNode n : mchildren) {
			if (n instanceof VariableExpressionAST) {
				var = n;
			} else {
				value = n;
			}
		}
		return var.getAsString() + operatorToString(moperator) + value.getAsString(); 
	}

}
