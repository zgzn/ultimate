/*
 * Copyright (C) 2017 Yong Li (liyong@ios.ac.cn)
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
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

package de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.optncsb.util;

import java.util.Iterator;
import java.util.TreeSet;


public class IntSetTreeSet implements IntSet {
	
	private final TreeSet<Integer> set;
	
	public IntSetTreeSet() {
		set = new TreeSet<>();
	}

	@Override
	public IntIterator iterator() {
		return new TreeSetIterator(this);
	}

	@Override
	public IntSet clone() {
		IntSetTreeSet copy = new IntSetTreeSet();
		copy.set.addAll(set);
		return copy;
	}

	@Override
	public void andNot(IntSet set) {
		if(! (set instanceof IntSetTreeSet)) {
			System.err.println("OPERAND should be TreeSet");
			System.exit(-1);
		}
		IntSetTreeSet temp = (IntSetTreeSet)set;
		this.set.removeAll(temp.set);
	}

	@Override
	public void and(IntSet set) {
		if(! (set instanceof IntSetTreeSet)) {
			System.err.println("OPERAND should be TreeSet");
			System.exit(-1);
		}
		IntSetTreeSet temp = (IntSetTreeSet)set;
		this.set.retainAll(temp.set);
	}

	@Override
	public void or(IntSet set) {
		if(! (set instanceof IntSetTreeSet)) {
			System.err.println("OPERAND should be TreeSet");
			System.exit(-1);
		}
		IntSetTreeSet temp = (IntSetTreeSet)set;
		this.set.addAll(temp.set);
	}

	@Override
	public boolean get(int value) {
		return set.contains(value);
	}

	@Override
	public void clear(int value) {
		set.remove(value);
	}
	
	@Override
	public String toString() {
		return set.toString();
	}
	
	@Override
	public void clear() {
		set.clear();
	}
	
	@Override
	public void set(int value) {
		set.add(value);
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public int cardinality() {
		return set.size();
	}

	@Override
	public boolean subsetOf(IntSet set) {
		if(! (set instanceof IntSetTreeSet)) {
			System.err.println("OPERAND should be TreeSet");
			System.exit(-1);
		}
		IntSetTreeSet temp = (IntSetTreeSet)set;
		return temp.set.containsAll(this.set);
	}

	@Override
	public boolean contentEq(IntSet set) {
		if(! (set instanceof IntSetTreeSet)) {
			System.err.println("OPERAND should be TreeSet");
			System.exit(-1);
		}
		IntSetTreeSet temp = (IntSetTreeSet)set;
		return this.set.equals(temp.set);
	}

	@Override
	public Object get() {
		return set;
	}
	
	public boolean equals(Object obj) {
		if(! (obj instanceof IntSetTreeSet)) {
			System.err.println("OPERAND should be TreeSet");
			System.exit(-1);
		}
		IntSetTreeSet temp = (IntSetTreeSet)obj;
		return this.contentEq(temp);
	}
	
	public static class TreeSetIterator implements IntIterator {

		private Iterator<Integer> iterator;
		
		public TreeSetIterator(IntSetTreeSet set) {
			this.iterator = set.set.iterator();
		}
		
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		public int next() {
			return iterator.next();
		}
		
	}
	

}
