/*******************************************************************************
 * Copyright (c) 2014 Leo Ufimtsev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Leo Ufimtsev - initial API and implementation
 *******************************************************************************/
package org.eclipse.releng.tests.tools;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.releng.tools.AdvancedCopyrightComment;
import org.eclipse.releng.tools.BlockComment;
import org.eclipse.releng.tools.CopyrightComment;
import org.junit.jupiter.api.Test;


/**
 * <h1> Parse Tests </h1>
 *
 * <p> Test that year is updated correctly by the comment parser. <br>
 * This can be ran as a standard Junit4 test or as a Plugin Test </p>
 */
public class AdvancedCopyrightCommentTests {

	/**
	 * verify that standard comment will have the new year appended to it
	 */
	@Test
	public void singleYearComment() {
		String original = "<!--\n" +
				"    Copyright (c) 2000 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		//Last year updated to 2015
		String expectedOut = "<!--\n" +
				"    Copyright (c) 2000, 2015 IBM Corporation and others.  \n" +  //<<<< Note appended 2015
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		proccessAndCompare(original, 2015, expectedOut);
	}


	/**
	 * verify standard two year comments work correctly 2000, 2014
	 */
	@Test
	public void twoYearCommentCommaSeperated() {
		String original = "<!--\n" +
				"    Copyright (c) 2000, 2014 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		//Last year updated to 2015
		String expectedOut = "<!--\n" +
				"    Copyright (c) 2000, 2015 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * verify standard two year comments work correctly 2000 - 2014 <br>
	 * It should also handle dashes as well as commas.
	 */
	@Test
	public void twoYearCommentDashSeperated() {
		String original = "<!--\n" +
				"    Copyright (c) 2000 - 2014 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		//Last year updated to 2015
		String expectedOut = "<!--\n" +
				"    Copyright (c) 2000 - 2015 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		proccessAndCompare(original, 2015, expectedOut);
	}



	/**
	 * Verify that comments with multiple years are handled correctly.
	 */
	@Test
	public void multiYearComment() {
		String original = "<!--\n" +
				"    Copyright (c) 2000, 2011-2012, 2014 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		//Last year updated to 2015
		String expectedOut = "<!--\n" +
				"    Copyright (c) 2000, 2011-2012, 2015 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		proccessAndCompare(original, 2015, expectedOut);
	}


	/**
	 * Verify that newline at the end is copied across to the new comment correctly.
	 */
	@Test
	public void multiYearCommentNewlinePost() {
		String original = "<!--\n" +
				"    Copyright (c) 2000, 2011-2012, 2014 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->\n"; // NOTE new line char here.

		//
		String expectedOut = "<!--\n" +
				"    Copyright (c) 2000, 2011-2012, 2015 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->\n";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * Verify that newline at the beginning is copied across to the new comment correctly.
	 */
	@Test
	public void multiYearCommentNewlinePre() {
		String original = "\n<!--\n" + // NOTE new line char here.
				"    Copyright (c) 2000, 2011-2012, 2014 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		//
		String expectedOut = "\n<!--\n" +
				"    Copyright (c) 2000, 2011-2012, 2015 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		proccessAndCompare(original, 2015, expectedOut);
	}


	/**
	 * Check with Unix delimiters.
	 */
	@Test
	public void unixDelimiters() {
		String original = "<!--\n" +
				"    Copyright (c) 2000, 2014 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		//Last year updated to 2015
		String expectedOut = "<!--\n" +
				"    Copyright (c) 2000, 2015 IBM Corporation and others.  \n" +
				"    All rights reserved. This program and the accompanying materials\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\n" +
				"    which accompanies this distribution, and is available at\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\n" +
				"   \n" +
				"    Contributors:\n" +
				"        IBM Corporation - initial API and implementation\n" +
				" -->";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * Check with windows delimiters. {@code \r\n}
	 */
	@Test
	public void windowsDelimiters() {
		String original = "<!--\r\n" + // NOTE new line char here.
				"    Copyright (c) 2000, 2014 IBM Corporation and others.  \r\n" +
				"    All rights reserved. This program and the accompanying materials \r\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\r\n" +
				"    which accompanies this distribution, and is available at\r\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\r\n" +
				"   \r\n" +
				"    Contributors:\r\n" +
				"        IBM Corporation - initial API and implementation\r\n" +
				" -->";

		String expectedOut = "<!--\r\n" + // NOTE new line char here.
				"    Copyright (c) 2000, 2015 IBM Corporation and others.  \r\n" +
				"    All rights reserved. This program and the accompanying materials \r\n" +
				"    are made available under the terms of the Eclipse Public License v1.0\r\n" +
				"    which accompanies this distribution, and is available at\r\n" +
				"    http://www.eclipse.org/legal/epl-v10.html\r\n" +
				"   \r\n" +
				"    Contributors:\r\n" +
				"        IBM Corporation - initial API and implementation\r\n" +
				" -->";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * the tool should work with the official header.
	 * https://www.eclipse.org/legal/copyrightandlicensenotice.php
	 */
	@Test
	public void eclipseCopyrightComment() {
		String original =
				"    /*******************************************************************************\n" +
				"     * Copyright (c) 2000 {INITIAL COPYRIGHT OWNER} {OTHER COPYRIGHT OWNERS}.\n" +
				"     * All rights reserved. This program and the accompanying materials\n" +
				"     * are made available under the terms of the Eclipse Public License v1.0\n" +
				"     * which accompanies this distribution, and is available at\n" +
				"     * http://www.eclipse.org/legal/epl-v10.html\n" +
				"     *\n" +
				"     * Contributors:\n" +
				"     *    {INITIAL AUTHOR} - initial API and implementation and/or initial documentation\n" +
				"     *******************************************************************************/";

		String expectedOut =
				"    /*******************************************************************************\n" +
				"     * Copyright (c) 2000, 2015 {INITIAL COPYRIGHT OWNER} {OTHER COPYRIGHT OWNERS}.\n" +
				"     * All rights reserved. This program and the accompanying materials\n" +
				"     * are made available under the terms of the Eclipse Public License v1.0\n" +
				"     * which accompanies this distribution, and is available at\n" +
				"     * http://www.eclipse.org/legal/epl-v10.html\n" +
				"     *\n" +
				"     * Contributors:\n" +
				"     *    {INITIAL AUTHOR} - initial API and implementation and/or initial documentation\n" +
				"     *******************************************************************************/";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * the tool should work with IBM headers.
	 * https://www.eclipse.org/legal/copyrightandlicensenotice.php
	 */
	@Test
	public void ibmCopyrightComment() {
		String original =
				"Copyright (c) 2000, 2010 IBM Corporation. \n" +
				"All rights reserved. This program and the accompanying materials \n" +
				"are made available under the terms of the Eclipse Public License v1.0 \n" +
				"which accompanies this distribution, and is available at \n" +
				"http://www.eclipse.org/legal/epl-v10.html  \n" +
				"\n" +
				"Contributors: \n" +
				"   IBM Corporation - initial API and implementation";

		String expectedOut =
				"Copyright (c) 2000, 2015 IBM Corporation. \n" +
				"All rights reserved. This program and the accompanying materials \n" +
				"are made available under the terms of the Eclipse Public License v1.0 \n" +
				"which accompanies this distribution, and is available at \n" +
				"http://www.eclipse.org/legal/epl-v10.html  \n" +
				"\n" +
				"Contributors: \n" +
				"   IBM Corporation - initial API and implementation";

		proccessAndCompare(original, 2015, expectedOut);
	}



	/**
	 * the tool should work with non-IBM copy right comments as well. <br>.
	 * for the purpose, a random realistic comment was extracted.
	 */
	@Test
	public void redHatCopyrightComment() {
		String original =
				"    /*******************************************************************************\n" +
				"     * Copyright (c) 2004, 2008, 2009, 2012 Red Hat, Inc. and others\n" +
				"     * All rights reserved. This program and the accompanying materials\n" +
				"     * are made available under the terms of the Eclipse Public License v1.0\n" +
				"     * which accompanies this distribution, and is available at\n" +
				"     * http://www.eclipse.org/legal/epl-v10.html\n" +
				"     *\n" +
				"     * Contributors:\n" +
				"     *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation\n" +
				"     *    Keith Seitz <keiths@redhat.com> - setup code in launch the method, initially\n" +
				"     *        written in the now-defunct OprofileSession class\n" +
				"     *    QNX Software Systems and others - the section of code marked in the launch\n" +
				"     *        method, and the exec method\n" +
				"     *    Lev Ufimtsev <lufimtse@redhat.com> --Added automatical enablement of options\n" +
				"     *                                         if thery are not set.\n" +
				"     *    Red Hat Inc. - modification of OProfileLaunchConfigurationDelegate to here\n" +
				"     *******************************************************************************/";

		String expectedOut =
				"    /*******************************************************************************\n" +
				"     * Copyright (c) 2004, 2008, 2009, 2015 Red Hat, Inc. and others\n" +
				"     * All rights reserved. This program and the accompanying materials\n" +
				"     * are made available under the terms of the Eclipse Public License v1.0\n" +
				"     * which accompanies this distribution, and is available at\n" +
				"     * http://www.eclipse.org/legal/epl-v10.html\n" +
				"     *\n" +
				"     * Contributors:\n" +
				"     *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation\n" +
				"     *    Keith Seitz <keiths@redhat.com> - setup code in launch the method, initially\n" +
				"     *        written in the now-defunct OprofileSession class\n" +
				"     *    QNX Software Systems and others - the section of code marked in the launch\n" +
				"     *        method, and the exec method\n" +
				"     *    Lev Ufimtsev <lufimtse@redhat.com> --Added automatical enablement of options\n" +
				"     *                                         if thery are not set.\n" +
				"     *    Red Hat Inc. - modification of OProfileLaunchConfigurationDelegate to here\n" +
				"     *******************************************************************************/";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * We test the AdvancedCopyrightComment parse(..) function.
	 *
	 * @param original  original comment
	 * @param reviseTo  year to which it should be updated to
	 * @param expected  expected updated comment.
	 * @return          true if modified original matches expected.
	 */
	private void proccessAndCompare(String original, int reviseTo, String expected) {

		//For our purposes, start/end line & start/end comment don't matter.
		BlockComment commentBlock = new BlockComment(0, 0, original, null, null);

		//Proccess input string.
		AdvancedCopyrightComment advComment = AdvancedCopyrightComment.parse(commentBlock,CopyrightComment.XML_COMMENT);

		advComment.setRevisionYear(reviseTo);

		//get updated comment.
		String actual = advComment.getCopyrightComment();

		assertEquals(expected, actual, original);
	}
}
