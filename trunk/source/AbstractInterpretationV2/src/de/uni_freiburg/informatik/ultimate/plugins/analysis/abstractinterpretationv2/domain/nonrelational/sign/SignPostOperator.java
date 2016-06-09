/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AbstractInterpretationV2 plug-in.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AbstractInterpretationV2 plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AbstractInterpretationV2 plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AbstractInterpretationV2 plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.sign;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.output.BoogiePrettyPrinter;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.rcfg.RcfgStatementExtractor;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractPostOperator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;

/**
 * Applies a post operation to an abstract state of the {@link SignDomain}.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 */
public class SignPostOperator implements IAbstractPostOperator<SignDomainState, CodeBlock, IBoogieVar> {

	private final RcfgStatementExtractor mStatementExtractor;
	private final SignDomainStatementProcessor mStatementProcessor;
	private final ILogger mLogger;

	/**
	 * Default constructor.
	 */
	protected SignPostOperator(IUltimateServiceProvider services) {
		mStatementExtractor = new RcfgStatementExtractor();
		mStatementProcessor = new SignDomainStatementProcessor(services);
		mLogger = services.getLoggingService().getLogger(Activator.PLUGIN_ID);
	}

	/**
	 * Applies the post operator to a given {@link IAbstractState}, according to some Boogie {@link CodeBlock}.
	 * 
	 * @param oldstate
	 *            The current abstract state, the post operator is applied on.
	 * @param transition
	 *            The Boogie code block that is used to apply the post operator.
	 * @return A new abstract state which is the result of applying the post operator to a given abstract state.
	 */
	@Override
	public List<SignDomainState> apply(final SignDomainState oldstate, final CodeBlock transition) {
		assert oldstate != null;
		assert !oldstate.isBottom();
		assert transition != null;

		List<SignDomainState> currentStates = new ArrayList<>();
		currentStates.add(oldstate);
		final List<Statement> statements = mStatementExtractor.process(transition);

		for (final Statement stmt : statements) {
			final List<SignDomainState> afterProcessStates = new ArrayList<>();
			for (final SignDomainState currentState : currentStates) {
				final List<SignDomainState> processed = mStatementProcessor.process(currentState, stmt);
				assert processed.size() > 0;
				afterProcessStates.addAll(processed);
			}

			currentStates = afterProcessStates;
		}

		return currentStates;
	}

	@Override
	public List<SignDomainState> apply(final SignDomainState stateBeforeLeaving, final SignDomainState stateAfterLeaving,
	        final CodeBlock transition) {
		assert transition instanceof Call || transition instanceof Return;

		final List<SignDomainState> returnList = new ArrayList<>();
		
		if (transition instanceof Call) {
			// nothing changes during this switch
			returnList.add(stateAfterLeaving);
		} else if (transition instanceof Return) {
			// TODO: Handle assign on return! This is just the old behavior
			final Return ret = (Return) transition;
			final CallStatement correspondingCall = ret.getCallStatement();
			mLogger.error("SignDomain does not handle returns correctly: " + ret + " for "
			        + BoogiePrettyPrinter.print(correspondingCall));
			returnList.add(stateAfterLeaving);
		} else {
			throw new UnsupportedOperationException(
			        "SignDomain does not support context switches other than Call and Return (yet)");
		}
		
		return returnList;
	}

}
