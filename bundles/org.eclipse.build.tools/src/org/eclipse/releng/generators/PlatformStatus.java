/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.releng.generators;

import java.util.*;
import org.w3c.dom.*;

public class PlatformStatus {

	private String id;
	private String name;
	private String fileName;
	private String format;
	private List images;
	private boolean hasErrors = false;

	PlatformStatus(Element anElement) {
		super();
		NamedNodeMap attributes = anElement.getAttributes();
		this.id = attributes.getNamedItem("id").getNodeValue();
		Node node = attributes.getNamedItem("name");
		this.name = node == null ? "" : node.getNodeValue();
		this.fileName = attributes.getNamedItem("fileName").getNodeValue();
		node = attributes.getNamedItem("format");
		if (node != null)
			this.format = node.getNodeValue();
		node = attributes.getNamedItem("images");
		if (node != null)
			this.images = listFromString(node.getNodeValue());
	}

	private static List listFromString(String value) {
		List result = new ArrayList();
		for (StringTokenizer tokenizer = new StringTokenizer(value, ","); tokenizer.hasMoreTokens(); result.add(tokenizer.nextToken()))
			;
		return result;
	}

	public List getImages() {
		return this.images;
	}

	public String getFormat() {
		return this.format;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		return fileName;
	}

	public void registerError() {
		this.hasErrors = true;
	}

	public boolean hasErrors() {
		return this.hasErrors;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("PlatformStatus(");
		buffer.append("id=" + id);
		buffer.append(", name=" + name);
		buffer.append(", filename=" + fileName);
		buffer.append(")");
		return buffer.toString();
	}
}
