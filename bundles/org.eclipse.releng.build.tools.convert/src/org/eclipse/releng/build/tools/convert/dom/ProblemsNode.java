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

public class ProblemsNode {

    private static final ProblemNode[] EMPTY_NODES = new ProblemNode[0];

    public String                      sourceFileName;
    public int                         numberOfProblems;
    public int                         numberOfErrors;
    public int                         numberOfWarnings;

    private ArrayList<ProblemNode>     errorNodes;
    private ArrayList<ProblemNode>                  otherWarningNodes;
    private ArrayList<ProblemNode>     discouragedWarningsNodes;
    private ArrayList<ProblemNode>     forbiddenWarningsNodes;
    private ProblemNode[]              errors;
    private ProblemNode[]              otherWarnings;
    private ProblemNode[]              discouragedWarnings;
    private ProblemNode[]              forbiddenWarnings;

    public void addDiscouragedWarning(final ProblemNode node) {
        if (discouragedWarningsNodes == null) {
            discouragedWarningsNodes = new ArrayList<>();
        }
        discouragedWarningsNodes.add(node);
    }

    public void addError(final ProblemNode node) {
        if (errorNodes == null) {
            errorNodes = new ArrayList<>();
        }
        errorNodes.add(node);
    }

    public void addForbiddenWarning(final ProblemNode node) {
        if (forbiddenWarningsNodes == null) {
            forbiddenWarningsNodes = new ArrayList<>();
        }
        forbiddenWarningsNodes.add(node);
    }

    public void addOtherWarning(final ProblemNode node) {
        if (otherWarningNodes == null) {
            otherWarningNodes = new ArrayList<>();
        }
        otherWarningNodes.add(node);
    }

    public ProblemNode[] getDiscouragedWarnings() {
        if (discouragedWarnings != null) {
            return discouragedWarnings;
        }
        if (discouragedWarningsNodes == null) {
            return discouragedWarnings = EMPTY_NODES;
        }
        discouragedWarnings = new ProblemNode[discouragedWarningsNodes.size()];
        discouragedWarningsNodes.toArray(discouragedWarnings);
        return discouragedWarnings;
    }

    public ProblemNode[] getErrors() {
        if (errors != null) {
            return errors;
        }
        if (errorNodes == null) {
            return errors = EMPTY_NODES;
        }
        errors = new ProblemNode[errorNodes.size()];
        errorNodes.toArray(errors);
        return errors;
    }

    public ProblemNode[] getForbiddenWarnings() {
        if (forbiddenWarnings != null) {
            return forbiddenWarnings;
        }
        if (forbiddenWarningsNodes == null) {
            return forbiddenWarnings = EMPTY_NODES;
        }
        forbiddenWarnings = new ProblemNode[forbiddenWarningsNodes.size()];
        forbiddenWarningsNodes.toArray(forbiddenWarnings);
        return forbiddenWarnings;
    }

    public ProblemNode[] getOtherWarnings() {
        if (otherWarnings != null) {
            return otherWarnings;
        }
        if (otherWarningNodes == null) {
            return otherWarnings = EMPTY_NODES;
        }
        otherWarnings = new ProblemNode[otherWarningNodes.size()];
        otherWarningNodes.toArray(otherWarnings);
        return otherWarnings;
    }
}
