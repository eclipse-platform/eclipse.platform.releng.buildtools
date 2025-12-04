/*******************************************************************************
 *  Copyright (c) 2006, 2025 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.build.tools.convert.dom;

import java.util.ArrayList;

public class LogDocumentNode {

    public record ProblemSummaryNode(int numberOfProblems, int numberOfErrors, int numberOfWarnings,
            int numberOfInfos) {
        @Override
        public String toString() {
            return "problems : " + numberOfProblems //
                    + " errors : " + numberOfErrors //
                    + " warnings : " + numberOfWarnings //
                    + " infos : " + numberOfInfos;
        }
    }

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
