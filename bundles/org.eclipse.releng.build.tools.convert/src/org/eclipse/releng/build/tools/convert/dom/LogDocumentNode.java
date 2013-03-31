/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.releng.build.tools.convert.dom;

import java.util.ArrayList;

public class LogDocumentNode {
	private static final ProblemsNode[] NO_PROBLEM_NODES = new ProblemsNode[0];
	private ArrayList problems;
	private ProblemSummaryNode summaryNode;
	private ProblemsNode[] problemsNodes;

	public void addProblemsNode(ProblemsNode node) {
		if (this.problems == null) this.problems = new ArrayList();
		this.problems.add(node);
	}

	public void setProblemSummary(ProblemSummaryNode node) {
		this.summaryNode = node;
	}

	public ProblemsNode[] getProblems() {
		if (this.problemsNodes != null) return this.problemsNodes;
		if (this.problems == null) {
			return this.problemsNodes = NO_PROBLEM_NODES;
		}
		this.problemsNodes = new ProblemsNode[this.problems.size()];
		this.problems.toArray(this.problemsNodes);
		return this.problemsNodes;
	}

	public ProblemSummaryNode getSummaryNode() {
		return this.summaryNode;
	}
}
