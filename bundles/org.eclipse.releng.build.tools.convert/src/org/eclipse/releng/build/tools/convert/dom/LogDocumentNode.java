/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.build.tools.convert.dom;

import java.util.ArrayList;

public class LogDocumentNode {

    private static final ProblemsNode[] NO_PROBLEM_NODES = new ProblemsNode[0];
    private ArrayList<ProblemsNode>                   problems;
    private ProblemSummaryNode          summaryNode;
    private ProblemsNode[]              problemsNodes;

    public void addProblemsNode(final ProblemsNode node) {
        if (problems == null) {
            problems = new ArrayList<>();
        }
        problems.add(node);
    }

    public ProblemsNode[] getProblems() {
        if (problemsNodes != null) {
            return problemsNodes;
        }
        if (problems == null) {
            return problemsNodes = NO_PROBLEM_NODES;
        }
        problemsNodes = new ProblemsNode[problems.size()];
        problems.toArray(problemsNodes);
        return problemsNodes;
    }

    public ProblemSummaryNode getSummaryNode() {
        return summaryNode;
    }

    public void setProblemSummary(final ProblemSummaryNode node) {
        summaryNode = node;
    }
}
