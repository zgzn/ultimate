/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE IRSDependencies plug-in.
 * 
 * The ULTIMATE IRSDependencies plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE IRSDependencies plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE IRSDependencies plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE IRSDependencies plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE IRSDependencies plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.analysis.irsdependencies.loopdetector;

import java.lang.reflect.Field;

import de.uni_freiburg.informatik.ultimate.core.lib.models.annotation.AbstractAnnotations;

/**
 * 
 * @author dietsch@informatik.uni-freiburg.de
 *
 * @param <E>
 */
class AstarAnnotation<E> extends AbstractAnnotations implements Comparable<AstarAnnotation<E>> {

	private static final long serialVersionUID = 1L;
	private static int sId;
	private final int mId;

	private E mEdge;
	private AstarAnnotation<E> mPreAnnotation;
	private int mCostSoFar; // g-value, how much have we already payed?
	private int mLowestExpectedCost; // h-value
	private int mExpectedCostToTarget; // f-value, i.e. how expensive from start
										// to target if we use this node, i.e.
										// g+h

	AstarAnnotation() {
		setExpectedCostToTarget(Integer.MAX_VALUE);
		setLowestExpectedCost(Integer.MAX_VALUE);
		mId = sId++;
	}

	void setExpectedCostToTarget(int value) {
		mExpectedCostToTarget = value;
		if (value < getLowestExpectedCost()) {
			setLowestExpectedCost(value);
		}
	}

	boolean isLowest() {
		return getLowestExpectedCost() == getExpectedCostToTarget();
	}

	@Override
	public int compareTo(AstarAnnotation<E> o) {
		return Integer.compare(mExpectedCostToTarget, o.mExpectedCostToTarget);
	}

	@Override
	protected String[] getFieldNames() {
		return new String[] { "mBackPointer", "mCostSoFar", "mExpectedCostToTarget" };
	}

	@Override
	protected Object getFieldValue(String field) {
		try {
			final Field f = getClass().getDeclaredField(field);
			f.setAccessible(true);
			return f.get(this);
		} catch (final Exception ex) {
			return ex;
		}
	}

	E getEdge() {
		return mEdge;
	}
	
	 AstarAnnotation<E> getBackpointer() {
		return mPreAnnotation;
	}

	void setBackPointers(E currentEdge, AstarAnnotation<E> pre) {
		mEdge = currentEdge;
		mPreAnnotation = pre;
	}

	int getCostSoFar() {
		return mCostSoFar;
	}

	void setCostSoFar(int costSoFar) {
		mCostSoFar = costSoFar;
	}

	int getExpectedCostToTarget() {
		return mExpectedCostToTarget;
	}

	private int getLowestExpectedCost() {
		return mLowestExpectedCost;
	}

	private void setLowestExpectedCost(int lowestExpectedCost) {
		mLowestExpectedCost = lowestExpectedCost;
	}

	@Override
	public int hashCode() {
		return mId;
	}
}
