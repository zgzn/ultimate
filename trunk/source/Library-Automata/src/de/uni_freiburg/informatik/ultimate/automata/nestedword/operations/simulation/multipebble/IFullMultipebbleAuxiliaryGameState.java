/*
 * Copyright (C) 2017 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2017 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.multipebble;

/**
 * @author Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 */
public interface IFullMultipebbleAuxiliaryGameState {

	public enum AuxiliaryGameStateType {
		SPOILER_WINNING_SINK,
		EMPTY_STACK,
		DEFAULT_SINK_FOR_AUTOMATA_OPERATIONS,
	}

	public AuxiliaryGameStateType getAuxiliaryGameStateType();

	public static <STATE, GS extends FullMultipebbleGameState<STATE>> boolean isSpoilerWinningSink(final GS gs) {
		if (gs instanceof IFullMultipebbleAuxiliaryGameState) {
			if (((IFullMultipebbleAuxiliaryGameState) gs)
					.getAuxiliaryGameStateType() == AuxiliaryGameStateType.SPOILER_WINNING_SINK) {
				return true;
			}
		}
		return false;
	}

}
