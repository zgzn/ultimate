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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.IncomingCallTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.IncomingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.IncomingReturnTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingCallTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingReturnTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.SummaryReturnTransition;

public class INWA2INestedWordAutomaton<LETTER, STATE> implements
		INestedWordAutomatonOldApi<LETTER, STATE> {
	
	private final INestedWordAutomaton<LETTER, STATE> m_Nwa;
	
	public INWA2INestedWordAutomaton(INestedWordAutomaton<LETTER, STATE> inwa) {
		m_Nwa = inwa;
	}

	public Set<LETTER> getInternalAlphabet() {
		return m_Nwa.getInternalAlphabet();
	}

	public Set<LETTER> getCallAlphabet() {
		return m_Nwa.getCallAlphabet();
	}

	public Set<LETTER> getReturnAlphabet() {
		return m_Nwa.getReturnAlphabet();
	}

	public Set<STATE> getStates() {
		return m_Nwa.getStates();
	}

	public STATE getEmptyStackState() {
		return m_Nwa.getEmptyStackState();
	}

	public StateFactory<STATE> getStateFactory() {
		return m_Nwa.getStateFactory();
	}

	public int size() {
		return m_Nwa.size();
	}

	public Set<LETTER> getAlphabet() {
		return m_Nwa.getAlphabet();
	}

	public Set<STATE> getInitialStates() {
		return m_Nwa.getInitialStates();
	}

	public boolean isInitial(STATE state) {
		return m_Nwa.isInitial(state);
	}

	public boolean isFinal(STATE state) {
		return m_Nwa.isFinal(state);
	}

	public Set<LETTER> lettersInternal(STATE state) {
		return m_Nwa.lettersInternal(state);
	}

	public Set<LETTER> lettersInternalIncoming(STATE state) {
		return m_Nwa.lettersInternalIncoming(state);
	}

	public Set<LETTER> lettersCall(STATE state) {
		return m_Nwa.lettersCall(state);
	}

	public Set<LETTER> lettersCallIncoming(STATE state) {
		return m_Nwa.lettersCallIncoming(state);
	}

	public Set<LETTER> lettersReturn(STATE state) {
		return m_Nwa.lettersReturn(state);
	}

	public Set<LETTER> lettersReturnIncoming(STATE state) {
		return m_Nwa.lettersReturnIncoming(state);
	}

	public Set<LETTER> lettersReturnSummary(STATE state) {
		return m_Nwa.lettersReturnSummary(state);
	}

	public Iterable<SummaryReturnTransition<LETTER, STATE>> returnSummarySuccessor(
			LETTER letter, STATE hier) {
		return m_Nwa.returnSummarySuccessor(letter, hier);
	}
	
	public Iterable<SummaryReturnTransition<LETTER, STATE>> returnSummarySuccessor(
			STATE hier) {
		return m_Nwa.returnSummarySuccessor(hier);
	}

	public Iterable<IncomingInternalTransition<LETTER, STATE>> internalPredecessors(
			LETTER letter, STATE succ) {
		return m_Nwa.internalPredecessors(letter, succ);
	}

	public Iterable<IncomingInternalTransition<LETTER, STATE>> internalPredecessors(
			STATE succ) {
		return m_Nwa.internalPredecessors(succ);
	}

	public Iterable<IncomingCallTransition<LETTER, STATE>> callPredecessors(
			LETTER letter, STATE succ) {
		return m_Nwa.callPredecessors(letter, succ);
	}

	public Iterable<IncomingCallTransition<LETTER, STATE>> callPredecessors(
			STATE succ) {
		return m_Nwa.callPredecessors(succ);
	}

	public Iterable<OutgoingInternalTransition<LETTER, STATE>> internalSuccessors(
			STATE state, LETTER letter) {
		return m_Nwa.internalSuccessors(state, letter);
	}

	public Iterable<OutgoingInternalTransition<LETTER, STATE>> internalSuccessors(
			STATE state) {
		return m_Nwa.internalSuccessors(state);
	}

	public Iterable<OutgoingCallTransition<LETTER, STATE>> callSuccessors(
			STATE state, LETTER letter) {
		return m_Nwa.callSuccessors(state, letter);
	}

	public Iterable<OutgoingCallTransition<LETTER, STATE>> callSuccessors(
			STATE state) {
		return m_Nwa.callSuccessors(state);
	}

	public Iterable<IncomingReturnTransition<LETTER, STATE>> returnPredecessors(
			STATE hier, LETTER letter, STATE succ) {
		return m_Nwa.returnPredecessors(hier, letter, succ);
	}

	public Iterable<IncomingReturnTransition<LETTER, STATE>> returnPredecessors(
			LETTER letter, STATE succ) {
		return m_Nwa.returnPredecessors(letter, succ);
	}

	public Iterable<IncomingReturnTransition<LETTER, STATE>> returnPredecessors(
			STATE succ) {
		return m_Nwa.returnPredecessors(succ);
	}

	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSucccessors(
			STATE state, STATE hier, LETTER letter) {
		return m_Nwa.returnSucccessors(state, hier, letter);
	}

	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSuccessors(
			STATE state, LETTER letter) {
		return m_Nwa.returnSuccessors(state, letter);
	}

	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSuccessors(
			STATE state) {
		return m_Nwa.returnSuccessors(state);
	}

	public String sizeInformation() {
		return m_Nwa.sizeInformation();
	}

	public Iterable<OutgoingReturnTransition<LETTER, STATE>> returnSuccessorsGivenHier(
			STATE state, STATE hier) {
		return m_Nwa.returnSuccessorsGivenHier(state, hier);
	}

	@Override
	public Collection<STATE> getFinalStates() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<STATE> succInternal(final STATE state, final LETTER letter) {
		return new Iterable<STATE>() {
			Iterable<OutgoingInternalTransition<LETTER,STATE>> m_Iterable;
			{
				m_Iterable = m_Nwa.internalSuccessors(state,letter);
			}
			@Override
			public Iterator<STATE> iterator() {
				Iterator<STATE> iterator = new Iterator<STATE>() {
				Iterator<OutgoingInternalTransition<LETTER,STATE>> m_BackingIterator;
				{
					m_BackingIterator = m_Iterable.iterator();
				}
					@Override
					public boolean hasNext() {
						return m_BackingIterator.hasNext();
					}

					@Override
					public STATE next() {
							return m_BackingIterator.next().getSucc();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
				return iterator;
			}
		};
	}

	@Override
	public Iterable<STATE> succCall(final STATE state, final LETTER letter) {
		return new Iterable<STATE>() {
			Iterable<OutgoingCallTransition<LETTER,STATE>> m_Iterable;
			{
				m_Iterable = m_Nwa.callSuccessors(state,letter);
			}
			@Override
			public Iterator<STATE> iterator() {
				Iterator<STATE> iterator = new Iterator<STATE>() {
				Iterator<OutgoingCallTransition<LETTER,STATE>> m_BackingIterator;
				{
					m_BackingIterator = m_Iterable.iterator();
				}
					@Override
					public boolean hasNext() {
						return m_BackingIterator.hasNext();
					}

					@Override
					public STATE next() {
							return m_BackingIterator.next().getSucc();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
				return iterator;
			}
		};
	}

	@Override
	public Iterable<STATE> hierPred(STATE state, LETTER letter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<STATE> succReturn(final STATE state, STATE hier, final LETTER letter) {
		return new Iterable<STATE>() {
			Iterable<OutgoingReturnTransition<LETTER,STATE>> m_Iterable;
			{
				m_Iterable = m_Nwa.returnSuccessors(state,letter);
			}
			@Override
			public Iterator<STATE> iterator() {
				Iterator<STATE> iterator = new Iterator<STATE>() {
				Iterator<OutgoingReturnTransition<LETTER,STATE>> m_BackingIterator;
				{
					m_BackingIterator = m_Iterable.iterator();
				}
					@Override
					public boolean hasNext() {
						return m_BackingIterator.hasNext();
					}

					@Override
					public STATE next() {
							return m_BackingIterator.next().getSucc();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
				return iterator;
			}
		};
	}

	@Override
	public Iterable<STATE> predInternal(STATE state, LETTER letter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<STATE> predCall(STATE state, LETTER letter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean finalIsTrap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDeterministic() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTotal() {
		throw new UnsupportedOperationException();
	}

}
