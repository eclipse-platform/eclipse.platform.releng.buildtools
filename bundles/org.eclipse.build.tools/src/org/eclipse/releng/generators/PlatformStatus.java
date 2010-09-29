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

import org.w3c.dom.*;

public class PlatformStatus implements Constants {

	private String id;
	private String name;
	private String fileName;
	private String os;
	private String ws;
	private String arch;
	private String format;
	private boolean hasErrors = false;

	PlatformStatus(Element anElement) {
		super();
		NamedNodeMap attributes = anElement.getAttributes();
		this.id = attributes.getNamedItem("id").getNodeValue();
		this.name = attributes.getNamedItem("name").getNodeValue();
		this.fileName = attributes.getNamedItem("fileName").getNodeValue();
		Node node = attributes.getNamedItem("format");
		if (node != null)
			this.format = node.getNodeValue();
		setOS(attributes.getNamedItem("os"));
		setWS(attributes.getNamedItem("ws"));
		setArch(attributes.getNamedItem("arch"));
	}

	public String getFormat() {
		return this.format;
	}
	
	private void setWS(Node node) {
		if (node == null)
			return;
		String value = node.getNodeValue();
		if (WS_WIN32.equalsIgnoreCase(value))
			ws = WS_WIN32;
		else if (WS_WPF.equalsIgnoreCase(value))
			ws = WS_WPF;
		else if (WS_MOTIF.equalsIgnoreCase(value))
			ws = WS_MOTIF;
		else if (WS_GTK.equalsIgnoreCase(value))
			ws = WS_GTK;
		else if (WS_PHOTON.equalsIgnoreCase(value))
			ws = WS_PHOTON;
		else if (WS_CARBON.equalsIgnoreCase(value))
			ws = WS_CARBON;
		else if (WS_COCOA.equalsIgnoreCase(value))
			ws = WS_COCOA;
		else if (WS_S60.equalsIgnoreCase(value))
			ws = WS_S60;
	}

	private void setOS(Node node) {
		if (node == null)
			return;
		String value = node.getNodeValue();
		if (OS_WIN32.equalsIgnoreCase(value))
			os = OS_WIN32;
		else if (OS_LINUX.equalsIgnoreCase(value))
			os = OS_LINUX;
		else if (OS_AIX.equalsIgnoreCase(value))
			os = OS_AIX;
		else if (OS_SOLARIS.equalsIgnoreCase(value))
			os = OS_SOLARIS;
		else if (OS_HPUX.equalsIgnoreCase(value))
			os = OS_HPUX;
		else if (OS_QNX.equalsIgnoreCase(value))
			os = OS_QNX;
		else if (OS_MACOSX.equalsIgnoreCase(value))
			os = OS_MACOSX;
		else if (OS_EPOC32.equalsIgnoreCase(value))
			os = OS_EPOC32;
		else if (OS_OS400.equalsIgnoreCase(value))
			os = OS_OS400;
		else if (OS_OS390.equalsIgnoreCase(value))
			os = OS_OS390;
		else if (OS_ZOS.equalsIgnoreCase(value))
			os = OS_ZOS;
	}

	private void setArch(Node node) {
		if (node == null)
			return;
		String value = node.getNodeValue();
		if (ARCH_X86.equalsIgnoreCase(value))
			arch = ARCH_X86;
		else if (ARCH_PA_RISC.equalsIgnoreCase(value))
			arch = ARCH_PA_RISC;
		else if (ARCH_PPC.equalsIgnoreCase(value))
			arch = ARCH_PPC;
		else if (ARCH_PPC64.equalsIgnoreCase(value))
			arch = ARCH_PPC64;
		else if (ARCH_SPARC.equalsIgnoreCase(value))
			arch = ARCH_SPARC;
		else if (ARCH_X86_64.equalsIgnoreCase(value))
			arch = ARCH_X86_64;
		else if (ARCH_IA64.equalsIgnoreCase(value))
			arch = ARCH_IA64;
		else if (ARCH_IA64_32.equalsIgnoreCase(value))
			arch = ARCH_IA64_32;
	}

	public String getOS() {
		return this.os;
	}

	public String getWS() {
		return this.ws;
	}

	public String getArch() {
		return this.arch;
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
		if (os != null)
			buffer.append(", os=" + os);
		if (ws != null)
			buffer.append(", ws=" + ws);
		if (arch != null)
			buffer.append(", arch=" + arch);
		buffer.append(")");
		return buffer.toString();
	}
}
