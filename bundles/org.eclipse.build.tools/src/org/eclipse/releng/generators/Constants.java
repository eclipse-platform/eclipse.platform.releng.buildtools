/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.generators;

/*
 * Copied from the org.eclipse.osgi.service.environment.Constants interface in OSGi.
 */
public interface Constants {
	public static final String OS_WIN32 = "win32";
	public static final String OS_LINUX = "linux";
	public static final String OS_AIX = "aix";
	public static final String OS_SOLARIS = "solaris";
	public static final String OS_HPUX = "hpux";
	public static final String OS_QNX = "qnx";
	public static final String OS_MACOSX = "macosx";
	public static final String OS_EPOC32 = "epoc32";
	public static final String OS_OS400 = "os/400"; 
	public static final String OS_OS390 = "os/390"; 
	public static final String OS_ZOS = "z/os"; 

	public static final String ARCH_X86 = "x86";
	public static final String ARCH_PA_RISC = "PA_RISC";
	public static final String ARCH_PPC = "ppc";
	public static final String ARCH_PPC64 = "ppc64";
	public static final String ARCH_SPARC = "sparc";
	public static final String ARCH_X86_64 = "x86_64";
	public static final String ARCH_IA64 = "ia64"; 
	public static final String ARCH_IA64_32 = "ia64_32";

	public static final String WS_WIN32 = "win32";
	public static final String WS_WPF = "wpf"; 
	public static final String WS_MOTIF = "motif";
	public static final String WS_GTK = "gtk";
	public static final String WS_PHOTON = "photon";
	public static final String WS_CARBON = "carbon";
	public static final String WS_COCOA = "cocoa";
	public static final String WS_S60 = "s60";
}
