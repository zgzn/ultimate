package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.buchiNwa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonSimple;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.IDeterminizedState;

/**
 * Represents a state (S,O,g) in the complement automaton.
 * <ul>
 *   <li> The level ranking g is modeled by m_LevelRanking
 *   <li> The set O is modeled by m_O (set O contains all states of S that
 *   have not visited an odd state since the last time O was emptied)
 *   <li> The set S contains all DoubleDecker for which m_LevelRanking is
 *   defined 
 * </ul> 
 * TODO Encode O in m_LevelRanking. E.g. map DoubleDecker in O instead of
 * its rank to rank-1000.
 */
public class LevelRankingState<LETTER, STATE> implements IDeterminizedState<LETTER, STATE> {
	protected final Map<STATE,HashMap<STATE,Integer>> m_LevelRanking;
	protected final Map<STATE,Set<STATE>> m_O;
	
	protected final INestedWordAutomatonSimple<LETTER, STATE> m_Operand;
	
	/**
	 * Highest rank in this LevelRankingState. Only used to get statistics.
	 */
	int m_HighestRank;
	
	LevelRankingState(INestedWordAutomatonSimple<LETTER, STATE> operand) {
		m_LevelRanking = new HashMap<STATE,HashMap<STATE,Integer>>();
		m_O = new HashMap<STATE,Set<STATE>>();
		m_Operand = operand;
		m_HighestRank = -1;
	}
	
	LevelRankingState(LevelRankingState<LETTER, STATE> lrs) {
		m_LevelRanking = copyLevelRanking(lrs.m_LevelRanking);
		m_O = copyO(lrs.m_O);
		m_HighestRank = lrs.m_HighestRank;
		m_Operand = lrs.getOperand();
	}
	
	Map<STATE,HashMap<STATE,Integer>> copyLevelRanking(Map<STATE,HashMap<STATE,Integer>> lr) {
		Map<STATE,HashMap<STATE,Integer>> result = new HashMap<STATE,HashMap<STATE,Integer>>();
		for (Entry<STATE, HashMap<STATE, Integer>> entry  : lr.entrySet()) {
			result.put(entry.getKey(), new HashMap<STATE, Integer>(entry.getValue()));
		}
		return result;
	}
	
	Map<STATE,Set<STATE>> copyO(Map<STATE,Set<STATE>> lr) {
		Map<STATE,Set<STATE>> result = new HashMap<STATE,Set<STATE>>();
		for (Entry<STATE, Set<STATE>> entry  : lr.entrySet()) {
			result.put(entry.getKey(), new HashSet<STATE>(entry.getValue()));
		}
		return result;
	}
	
	public INestedWordAutomatonSimple<LETTER, STATE> getOperand() {
		return m_Operand;
	}
	
	
	public Set<STATE> getDownStates() {
		return m_LevelRanking.keySet();
	}
	
	public Set<STATE> getUpStates(STATE downState) {
		return m_LevelRanking.get(downState).keySet();
	}
	
	protected void addRank(STATE down, STATE up, Integer rank, boolean addToO) {
		assert rank != null;
		assert BuchiComplementFKVNwa.isEven(rank) || !m_Operand.isFinal(up) : "final states must have even ranks";
		HashMap<STATE, Integer> up2rank = m_LevelRanking.get(down);
		if (up2rank == null) {
			up2rank = new HashMap<STATE,Integer>();
			m_LevelRanking.put(down, up2rank);
		}
		assert !up2rank.containsKey(up);
		up2rank.put(up,rank);
		if (addToO) {
			assert BuchiComplementFKVNwa.isEven(getRank(down, up)) : "has to be even";
			addToO(down,up);
		}
		if (m_HighestRank < rank) {
			m_HighestRank = rank;
		}
	}
	
	protected void addToO(STATE down, STATE up) {
		Set<STATE> upStates = m_O.get(down);
		if (upStates == null) {
			upStates = new HashSet<STATE>();
			m_O.put(down, upStates);
		}
		upStates.add(up);
	}
	
	public Integer getRank(STATE down, STATE up) {
		HashMap<STATE, Integer> up2rank = m_LevelRanking.get(down);
		if (up2rank == null) {
			return null;
		}
		else {
			return up2rank.get(up);
		}
	}
	
	public boolean inO(STATE down, STATE up) {
		Set<STATE> upStates = m_O.get(down);
		if (upStates == null) {
			return false;
		}
		return upStates.contains(up);
	}
	
	boolean isOempty() {
		return m_O.isEmpty();
	}
	
	@Override
	public String toString() {
		return m_LevelRanking.toString() +" O"+m_O;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((m_LevelRanking == null) ? 0 : m_LevelRanking.hashCode());
		result = prime * result + ((m_O == null) ? 0 : m_O.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LevelRankingState<LETTER, STATE> other = (LevelRankingState<LETTER, STATE>) obj;
		if (m_LevelRanking == null) {
			if (other.m_LevelRanking != null)
				return false;
		} else if (!m_LevelRanking.equals(other.m_LevelRanking))
			return false;
		if (m_O == null) {
			if (other.m_O != null)
				return false;
		} else if (!m_O.equals(other.m_O))
			return false;
		return true;
	}
	
	boolean isTight() {
		assert m_HighestRank >= 0;
		assert m_HighestRank < Integer.MAX_VALUE : "not applicable";
		if (BuchiComplementFKVNwa.isEven(m_HighestRank)) {
			return false;
		} else {
			int[] ranks = new int[m_HighestRank+1];
			for (STATE down  : getDownStates()) {
				for (STATE up : getUpStates(down)) {
					ranks[getRank(down, up)]++;
				}
			}
			for (int i=1; i<=m_HighestRank; i+=2) {
				if (ranks[i] == 0) {
					return false;
				}
			}
			return true;
		}
	}
}