/*******************************************************************************
 * Copyright (c) 2014, 2023 Leo Ufimtsev and others.
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
class AdvancedCopyrightCommentTests {

	/**
	 * verify that standard comment will have the new year appended to it
	 */
	@Test
	void singleYearComment() {
		String original = """
				<!--
				    Copyright (c) 2000 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		//Last year updated to 2015
		String expectedOut = """
				<!--
				    Copyright (c) 2000, 2015 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		proccessAndCompare(original, 2015, expectedOut);
	}


	/**
	 * verify standard two year comments work correctly 2000, 2014
	 */
	@Test
	void twoYearCommentCommaSeperated() {
		String original = """
				<!--
				    Copyright (c) 2000, 2014 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		//Last year updated to 2015
		String expectedOut = """
				<!--
				    Copyright (c) 2000, 2015 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * verify standard two year comments work correctly 2000 - 2014 <br>
	 * It should also handle dashes as well as commas.
	 */
	@Test
	void twoYearCommentDashSeperated() {
		String original = """
				<!--
				    Copyright (c) 2000 - 2014 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		//Last year updated to 2015
		String expectedOut = """
				<!--
				    Copyright (c) 2000 - 2015 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		proccessAndCompare(original, 2015, expectedOut);
	}



	/**
	 * Verify that comments with multiple years are handled correctly.
	 */
	@Test
	void multiYearComment() {
		String original = """
				<!--
				    Copyright (c) 2000, 2011-2012, 2014 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		//Last year updated to 2015
		String expectedOut = """
				<!--
				    Copyright (c) 2000, 2011-2012, 2015 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		proccessAndCompare(original, 2015, expectedOut);
	}


	/**
	 * Verify that newline at the end is copied across to the new comment correctly.
	 */
	@Test
	void multiYearCommentNewlinePost() {
		String original = """
				<!--
				    Copyright (c) 2000, 2011-2012, 2014 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->
				"""; // NOTE new line char here.

		//
		String expectedOut = """
				<!--
				    Copyright (c) 2000, 2011-2012, 2015 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->
				""";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * Verify that newline at the beginning is copied across to the new comment correctly.
	 */
	@Test
	void multiYearCommentNewlinePre() {
		String original = """

				<!--
				    Copyright (c) 2000, 2011-2012, 2014 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		//
		String expectedOut = """

				<!--
				    Copyright (c) 2000, 2011-2012, 2015 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		proccessAndCompare(original, 2015, expectedOut);
	}


	/**
	 * Check with Unix delimiters.
	 */
	@Test
	void unixDelimiters() {
		String original = """
				<!--
				    Copyright (c) 2000, 2014 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		//Last year updated to 2015
		String expectedOut = """
				<!--
				    Copyright (c) 2000, 2015 IBM Corporation and others.\s\s
				    All rights reserved. This program and the accompanying materials
				    are made available under the terms of the Eclipse Public License v1.0
				    which accompanies this distribution, and is available at
				    http://www.eclipse.org/legal/epl-v10.html
				 \s\s
				    Contributors:
				        IBM Corporation - initial API and implementation
				 -->""";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * Check with windows delimiters. {@code \r\n}
	 */
	@Test
	void windowsDelimiters() {
		String original = """
				<!--\r
				    Copyright (c) 2000, 2014 IBM Corporation and others.  \r
				    All rights reserved. This program and the accompanying materials \r
				    are made available under the terms of the Eclipse Public License v1.0\r
				    which accompanies this distribution, and is available at\r
				    http://www.eclipse.org/legal/epl-v10.html\r
				   \r
				    Contributors:\r
				        IBM Corporation - initial API and implementation\r
				 -->""";

		String expectedOut = """
				<!--\r
				    Copyright (c) 2000, 2015 IBM Corporation and others.  \r
				    All rights reserved. This program and the accompanying materials \r
				    are made available under the terms of the Eclipse Public License v1.0\r
				    which accompanies this distribution, and is available at\r
				    http://www.eclipse.org/legal/epl-v10.html\r
				   \r
				    Contributors:\r
				        IBM Corporation - initial API and implementation\r
				 -->""";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * the tool should work with the official header.
	 * https://www.eclipse.org/legal/copyrightandlicensenotice.php
	 */
	@Test
	void eclipseCopyrightComment() {
		String original =
				"""
						/*******************************************************************************
						 * Copyright (c) 2000 {INITIAL COPYRIGHT OWNER} {OTHER COPYRIGHT OWNERS}.
						 * All rights reserved. This program and the accompanying materials
						 * are made available under the terms of the Eclipse Public License v1.0
						 * which accompanies this distribution, and is available at
						 * http://www.eclipse.org/legal/epl-v10.html
						 *
						 * Contributors:
						 *    {INITIAL AUTHOR} - initial API and implementation and/or initial documentation
						 *******************************************************************************/""";

		String expectedOut =
				"""
						/*******************************************************************************
						 * Copyright (c) 2000, 2015 {INITIAL COPYRIGHT OWNER} {OTHER COPYRIGHT OWNERS}.
						 * All rights reserved. This program and the accompanying materials
						 * are made available under the terms of the Eclipse Public License v1.0
						 * which accompanies this distribution, and is available at
						 * http://www.eclipse.org/legal/epl-v10.html
						 *
						 * Contributors:
						 *    {INITIAL AUTHOR} - initial API and implementation and/or initial documentation
						 *******************************************************************************/""";

		proccessAndCompare(original, 2015, expectedOut);
	}

	/**
	 * the tool should work with IBM headers.
	 * https://www.eclipse.org/legal/copyrightandlicensenotice.php
	 */
	@Test
	void ibmCopyrightComment() {
		String original =
				"""
						Copyright (c) 2000, 2010 IBM Corporation.\s
						All rights reserved. This program and the accompanying materials\s
						are made available under the terms of the Eclipse Public License v1.0\s
						which accompanies this distribution, and is available at\s
						http://www.eclipse.org/legal/epl-v10.html\s\s

						Contributors:\s
						   IBM Corporation - initial API and implementation""";

		String expectedOut =
				"""
						Copyright (c) 2000, 2015 IBM Corporation.\s
						All rights reserved. This program and the accompanying materials\s
						are made available under the terms of the Eclipse Public License v1.0\s
						which accompanies this distribution, and is available at\s
						http://www.eclipse.org/legal/epl-v10.html\s\s

						Contributors:\s
						   IBM Corporation - initial API and implementation""";

		proccessAndCompare(original, 2015, expectedOut);
	}



	/**
	 * the tool should work with non-IBM copy right comments as well. <br>.
	 * for the purpose, a random realistic comment was extracted.
	 */
	@Test
	void redHatCopyrightComment() {
		String original =
				"""
						/*******************************************************************************
						 * Copyright (c) 2004, 2008, 2009, 2012 Red Hat, Inc. and others
						 * All rights reserved. This program and the accompanying materials
						 * are made available under the terms of the Eclipse Public License v1.0
						 * which accompanies this distribution, and is available at
						 * http://www.eclipse.org/legal/epl-v10.html
						 *
						 * Contributors:
						 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
						 *    Keith Seitz <keiths@redhat.com> - setup code in launch the method, initially
						 *        written in the now-defunct OprofileSession class
						 *    QNX Software Systems and others - the section of code marked in the launch
						 *        method, and the exec method
						 *    Lev Ufimtsev <lufimtse@redhat.com> --Added automatical enablement of options
						 *                                         if thery are not set.
						 *    Red Hat Inc. - modification of OProfileLaunchConfigurationDelegate to here
						 *******************************************************************************/""";

		String expectedOut =
				"""
						/*******************************************************************************
						 * Copyright (c) 2004, 2008, 2009, 2015 Red Hat, Inc. and others
						 * All rights reserved. This program and the accompanying materials
						 * are made available under the terms of the Eclipse Public License v1.0
						 * which accompanies this distribution, and is available at
						 * http://www.eclipse.org/legal/epl-v10.html
						 *
						 * Contributors:
						 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
						 *    Keith Seitz <keiths@redhat.com> - setup code in launch the method, initially
						 *        written in the now-defunct OprofileSession class
						 *    QNX Software Systems and others - the section of code marked in the launch
						 *        method, and the exec method
						 *    Lev Ufimtsev <lufimtse@redhat.com> --Added automatical enablement of options
						 *                                         if thery are not set.
						 *    Red Hat Inc. - modification of OProfileLaunchConfigurationDelegate to here
						 *******************************************************************************/""";

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
		BlockComment commentBlock = new BlockComment(0, 0, original);

		//Proccess input string.
		AdvancedCopyrightComment advComment = AdvancedCopyrightComment.parse(commentBlock,CopyrightComment.XML_COMMENT);

		advComment.setRevisionYear(reviseTo);

		//get updated comment.
		String actual = advComment.getCopyrightComment();

		assertEquals(expected, actual, original);
	}
}
