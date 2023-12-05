/*******************************************************************************
 * Copyright (c) 2013, 2016 Tomasz Zarna and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tomasz Zarna <tzarna@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.releng.tests.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.releng.tools.git.GitCopyrightAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GitCopyrightAdapterTest extends LocalGitRepositoryTestData {

	private static final IProgressMonitor NULL_MONITOR = new NullProgressMonitor();

	private static final String PROJECT_NAME = "Project";

	private static final String FILE1_NAME = "Foo.java";

	private static final String FILE2_NAME = "Bar.java";

	private Repository db;

	private File trash;

	private File gitDir;

	private IProject project;

	private IFile file1;

	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		db = createWorkRepository();
		trash = db.getWorkTree();
		gitDir = new File(trash, Constants.DOT_GIT);
		project = createProject(PROJECT_NAME);
		file1 = project.getFile(FILE1_NAME);
		connect();
	}

	@Override
	@AfterEach
	public void tearDown() throws Exception {
		if (project.exists())
			project.delete(true, true, NULL_MONITOR);
		if (gitDir.exists())
			FileUtils.delete(gitDir, FileUtils.RECURSIVE | FileUtils.RETRY | FileUtils.IGNORE_ERRORS);
		super.tearDown();
	}

	@Test
	void testLastModifiedYear() throws Exception {

		try (final Git git = new Git(db)) {
			git.add().addFilepattern(PROJECT_NAME + "/" + FILE1_NAME).call();
			final PersonIdent committer2011 = new PersonIdent(committer, getDateForYear(2011));
			git.commit().setMessage("old commit").setCommitter(committer2011).call();
			git.add().addFilepattern(PROJECT_NAME + "/" + FILE2_NAME).call();
			git.commit().setMessage("new commit").call();
		}

		final GitCopyrightAdapter adapter = new GitCopyrightAdapter(
				new IResource[] { project });
		adapter.initialize(NULL_MONITOR);
		final int lastModifiedYear = adapter.getLastModifiedYear(file1,
				NULL_MONITOR);

		assertEquals(2011, lastModifiedYear);
	}

	@Test
	void testCopyrightUpdateComment() throws Exception {

		try (final Git git = new Git(db)) {
			git.add().addFilepattern(PROJECT_NAME + "/" + FILE1_NAME).call();
			git.commit().setMessage("copyright update").call();
		}
		final GitCopyrightAdapter adapter = new GitCopyrightAdapter(
				new IResource[] { project });
		adapter.initialize(NULL_MONITOR);
		final int lastModifiedYear = adapter.getLastModifiedYear(file1,
				NULL_MONITOR);

		assertEquals(0, lastModifiedYear);
	}

	private IProject createProject(String name) throws Exception {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(name);
		if (project.exists())
			project.delete(true, null);
		final IProjectDescription desc = ResourcesPlugin.getWorkspace()
				.newProjectDescription(name);
		desc.setLocation(IPath.fromOSString(new File(db.getWorkTree(), name).getPath()));
		project.create(desc, null);
		project.open(null);

		final IFile file1 = project.getFile(FILE1_NAME);
		file1.create(
				new ByteArrayInputStream("Hello, world".getBytes(project
						.getDefaultCharset())), false, null);

		final IFile file2 = project.getFile(FILE2_NAME);
		file2.create(
				new ByteArrayInputStream("Hi there".getBytes(project
						.getDefaultCharset())), false, null);
		return project;
	}

	@SuppressWarnings("restriction")
	private void connect() throws CoreException {
		new org.eclipse.egit.core.op.ConnectProviderOperation(project, gitDir).execute(null);
	}

	private Date getDateForYear(int year) throws ParseException {
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		return formatter.parse(Integer.toString(year) + "/6/30");
	}

}