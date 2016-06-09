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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import de.uni_freiburg.informatik.ultimate.core.model.models.IElement;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.irsdependencies.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RCFGEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RCFGNode;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootAnnot;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootNode;

/**
 * 
 * {@link RCFGLoopDetector} computes for each Loophead of an RCFG the edge which is used to enter the loop body and the
 * corresponding edge that leads back to the loop head.
 * 
 * If your graph looks like this and L1 and L2 are loop heads,
 * 
 * <pre>
 * 		 e1              e4 
 * 		+--+            +--+
 * 		\  v     e2     \  v
 * 		 L1  --------->  L2 
 * 		     <---------     
 * 		         e3
 * </pre>
 * 
 * you will get the following map:<br>
 * L1 -> (e2 -> e3, e1 -> e1)<br>
 * L2 -> (e4 -> e4) <br>
 * <br>
 * 
 * You should call {@link #process(IElement)} on the root element of your RCFG and then get the resulting map via
 * {@link #getResult()}.
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * 
 */
public class RCFGLoopDetector {

	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final Map<ProgramPoint, Map<RCFGEdge, RCFGEdge>> mLoopEntryExit;

	public RCFGLoopDetector(final IUltimateServiceProvider services) {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mLoopEntryExit = new HashMap<>();
	}

	public Map<ProgramPoint, Map<RCFGEdge, RCFGEdge>> getResult() {
		return mLoopEntryExit;
	}

	public void process(RootNode rootNode) throws Throwable {
		final RootAnnot annot = rootNode.getRootAnnot();

		// get a hashset of all loop heads
		final Set<ProgramPoint> loopHeadsSet = new HashSet<>(annot.getLoopLocations().keySet());

		// order the loopheads after their occurance in the program
		final List<ProgramPoint> loopHeads = orderLoopHeads(loopHeadsSet, rootNode);

		// compute the edges that constitute the loop for one loophead while
		// ignoring loops that result from nesting
		List<RCFGEdge> forbiddenEdges = new ArrayList<>();
		for (final ProgramPoint p : loopHeads) {
			final Map<RCFGEdge, RCFGEdge> map = new HashMap<>();
			mLoopEntryExit.put(p, map);
			process(p, map, forbiddenEdges);
			forbiddenEdges = new ArrayList<>();
			forbiddenEdges.addAll(map.values());
		}
		// print result if debug is enabled
		printResult(mLoopEntryExit);
	}

	private List<ProgramPoint> orderLoopHeads(Set<ProgramPoint> loopHeads, RootNode programStart) {
		final List<ProgramPoint> rtr = new ArrayList<>();

		final Stack<RCFGNode> open = new Stack<>();
		final HashSet<RCFGNode> closed = new HashSet<>();

		open.push(programStart);
		while (!open.isEmpty()) {
			final RCFGNode current = open.pop();
			if (closed.contains(current)) {
				continue;
			}
			closed.add(current);
			for (final RCFGEdge edge : current.getOutgoingEdges()) {
				open.push(edge.getTarget());
			}
			if (loopHeads.contains(current)) {
				rtr.add((ProgramPoint) current);
			}
		}

		return rtr;
	}

	private void process(ProgramPoint loopHead, Map<RCFGEdge, RCFGEdge> map, List<RCFGEdge> forbiddenEdges) {
		AStar<RCFGNode, RCFGEdge> walker = new AStar<>(mLogger, loopHead, loopHead, new ZeroHeuristic(),
				new RcfgWrapper(), forbiddenEdges, mServices.getProgressMonitorService());

		List<RCFGEdge> path = walker.findPath();
		if (forbiddenEdges.isEmpty() && (path == null || path.isEmpty())) {
			mLogger.warn(
					"RCFGNode " + loopHead + " is not a valid loop head, because there is no cycle leading back to it");
		}

		// got first path, add it to the results and get the edge starting this
		// path to find different entry/exits for this loop
		while (path != null) {
			final RCFGEdge forbiddenEdge = addToResult(path, map);
			forbiddenEdges.add(forbiddenEdge);

			walker = new AStar<RCFGNode, RCFGEdge>(mLogger, loopHead, loopHead, new ZeroHeuristic(), new RcfgWrapper(),
					createDenier(forbiddenEdges), mServices.getProgressMonitorService());
			path = walker.findPath();
		}
	}

	private IEdgeDenier<RCFGEdge> createDenier(List<RCFGEdge> forbiddenEdges) {
		final List<IEdgeDenier<RCFGEdge>> rtr = new ArrayList<>();
		rtr.add(new CollectionEdgeDenier<RCFGEdge>(forbiddenEdges));
		rtr.add(new RcfgCallReturnDenier());
		return new CompositEdgeDenier<>(rtr);
	}

	private RCFGEdge addToResult(List<RCFGEdge> path, Map<RCFGEdge, RCFGEdge> map) {
		final RCFGEdge first = path.get(0);
		final RCFGEdge last = path.get(path.size() - 1);
		assert first.getSource().equals(last.getTarget());
		map.put(first, last);
		return first;
	}

	private void printResult(final Map<ProgramPoint, Map<RCFGEdge, RCFGEdge>> result) {
		if (!mLogger.isDebugEnabled()) {
			return;
		}
		mLogger.debug("---------------");
		for (final ProgramPoint p : result.keySet()) {
			mLogger.debug("Loophead " + p);
			final Map<RCFGEdge, RCFGEdge> map = result.get(p);
			for (final Entry<RCFGEdge, RCFGEdge> entry : map.entrySet()) {
				mLogger.debug("* " + entry.getKey().hashCode() + " >< " + entry.getValue().hashCode());
			}
		}
		mLogger.debug("---------------");
	}

	private static final class RcfgCallReturnDenier implements IEdgeDenier<RCFGEdge> {
		@Override
		public boolean isForbidden(final RCFGEdge edge, final Iterator<RCFGEdge> backpointers) {
			if (edge instanceof Return) {
				// check if the first call on the path spanned by the
				// backpointers is the call matching this return
				final Call call = ((Return) edge).getCorrespondingCall();
				while (backpointers.hasNext()) {
					if (call.equals(backpointers.next())) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}
}
