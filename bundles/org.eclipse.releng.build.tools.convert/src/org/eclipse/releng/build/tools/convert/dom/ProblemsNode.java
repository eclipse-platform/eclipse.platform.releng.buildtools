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

public class ProblemsNode {
	private static final ProblemNode[] EMPTY_NODES = new ProblemNode[0];

	public String sourceFileName;
	public int numberOfProblems;
	public int numberOfErrors;
	public int numberOfWarnings;

	private ArrayList errorNodes;
	private ArrayList otherWarningNodes;
	private ArrayList discouragedWarningsNodes;
	private ArrayList forbiddenWarningsNodes;
	private ProblemNode[] errors;
	private ProblemNode[] otherWarnings;
	private ProblemNode[] discouragedWarnings;
	private ProblemNode[] forbiddenWarnings;

	public void addError(ProblemNode node) {
		if (this.errorNodes == null) this.errorNodes = new ArrayList();
		this.errorNodes.add(node);
	}

	public void addForbiddenWarning(ProblemNode node) {
		if (this.forbiddenWarningsNodes == null) this.forbiddenWarningsNodes = new ArrayList();
		this.forbiddenWarningsNodes.add(node);
	}

	public void addDiscouragedWarning(ProblemNode node) {
		if (this.discouragedWarningsNodes == null) this.discouragedWarningsNodes = new ArrayList();
		this.discouragedWarningsNodes.add(node);
	}

	public void addOtherWarning(ProblemNode node) {
		if (this.otherWarningNodes == null) this.otherWarningNodes= new ArrayList();
		this.otherWarningNodes.add(node);
	}

	public ProblemNode[] getErrors() {
		if (this.errors != null) return this.errors;
		if (this.errorNodes == null) return this.errors = EMPTY_NODES;
		this.errors = new ProblemNode[this.errorNodes.size()];
		this.errorNodes.toArray(this.errors);
		return this.errors;
	}

	public ProblemNode[] getOtherWarnings() {
		if (this.otherWarnings != null) return this.otherWarnings;
		if (this.otherWarningNodes == null) return this.otherWarnings = EMPTY_NODES;
		this.otherWarnings = new ProblemNode[this.otherWarningNodes.size()];
		this.otherWarningNodes.toArray(this.otherWarnings);
		return this.otherWarnings;
	}

	public ProblemNode[] getDiscouragedWarnings() {
		if (this.discouragedWarnings != null) return this.discouragedWarnings;
		if (this.discouragedWarningsNodes == null) return this.discouragedWarnings = EMPTY_NODES;
		this.discouragedWarnings = new ProblemNode[this.discouragedWarningsNodes.size()];
		this.discouragedWarningsNodes.toArray(this.discouragedWarnings);
		return this.discouragedWarnings;
	}

	public ProblemNode[] getForbiddenWarnings() {
		if (this.forbiddenWarnings != null) return this.forbiddenWarnings;
		if (this.forbiddenWarningsNodes == null) return this.forbiddenWarnings = EMPTY_NODES;
		this.forbiddenWarnings = new ProblemNode[this.forbiddenWarningsNodes.size()];
		this.forbiddenWarningsNodes.toArray(this.forbiddenWarnings);
		return this.forbiddenWarnings;
	}
}
