/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/


package org.eclipse.releng.generators;

/**
 * @author SDimitrov
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EclipseTestResultsGeneratorNoMail extends TestResultsGenerator {

    public static void main(final String[] args) {
        final String publishingContent = "/home/davidw/gitdavidw2/eclipse.platform.releng.aggregator/eclipse.platform.releng.tychoeclipsebuilder/eclipse/publishingFiles";
        String localTestBuildId = "I20141120-1922";
        String localTestDropDir="/shared/eclipse/buildsdavidw/4I/siteDir/eclipse/downloads/drops4/" + localTestBuildId;

        final EclipseTestResultsGeneratorNoMail test = new EclipseTestResultsGeneratorNoMail();
        test.setBuildType("I");
        test.setIsBuildTested(true);
        test.setDropTokenList("%sdk%,%tests%,%example%,%rcpruntime%,%rcpsdk%,%deltapack%,%runtime%,%jdt%,%jdtsdk%,%jdtc%,%pde%,%pdesdk%,%cvs%,%cvssdk%,%swt%,%relengtools%");
        test.setXmlDirectoryName(localTestDropDir + "/testresults/xml");
        test.setDropDirectoryName(localTestDropDir);
        test.setTestResultsTemplateFileName(publishingContent + "/templateFiles/testResults.php.template");
        test.setDropTemplateFileName(publishingContent + "/templateFiles/index.php.template");
        test.setTestResultsHtmlFileName("testResults.php");
        test.setDropHtmlFileName("index.php");
        test.setHrefTestResultsTargetPath("testresults");
        test.setCompileLogsDirectoryName(localTestDropDir + "/compilelogs");
        test.setHrefCompileLogsTargetPath("compilelogs");
        test.setTestManifestFileName("/home/davidw/gitdavidw2/eclipse.platform.releng.aggregator/eclipse.platform.releng.tychoeclipsebuilder/eclipse/publishingFiles/testManifest.xml");
        test.execute();
    }

    private boolean sendMail = true;

    @Override
    public void execute() {
        super.execute();
        // if (sendMail)
        // mailResults();
    }

    public boolean isSendMail() {
        return sendMail;
    }

    @Override
    protected String processDropRow(final PlatformStatus aPlatform) {
        String result = "<tr>\n<td>" + aPlatform.getName() + "</td>\n";
        // generate file link, size and checksums in the php template
        result = result + "<?php genLinks(\"" + aPlatform.getFileName() + "\"); ?>\n";
        result = result + "</tr>\n";
        return result;
    }
    // I restored this 'mailResults' method from history. It was removed about 3.8 M3. It was commented out 
    // at that time. Not sure for how long. I am not sure where "Mailer" class was coming from. 
    // Needs more research or re-invention. (Compare with CBI aggregator method.)
//  private void mailResults() {
//  //send a different message for the following cases:
//  //build is not tested at all
//  //build is tested, tests have not run
//  //build is tested, tests have run with error and or failures
//  //build is tested, tests have run with no errors or failures
//  try {
//      mailer = new Mailer();
//  } catch (NoClassDefFoundError e) {
//      return;
//  }
//  String buildLabel = mailer.getBuildProperties().getBuildLabel();
//  String httpUrl = mailer.getBuildProperties().getHttpUrl()+"/"+buildLabel;
////    String ftpUrl = mailer.getBuildProperties().getftpUrl()+"/"+buildLabel;
//  
//  String subject = "Build is complete.  ";
//  
//  String downloadLinks="\n\nHTTP Download:\n\n\t"+httpUrl+" \n\n";
///*  downloadLinks=downloadLinks.concat("FTP Download:\n\n");
//  downloadLinks=downloadLinks.concat("\tuser: anonymous\n\tpassword: (e-mail address or leave blank)\n\tserver:  download.eclipse.org\n\tcd to directory:  "+buildLabel);
//  downloadLinks=downloadLinks.concat("\n\n\tor");
//  downloadLinks=downloadLinks.concat("\n\n\t"+ftpUrl);*/
//  
//  //provide http links
//  String message = "The build is complete."+downloadLinks;
//
//  if (testsRan()) {
//      subject = "Automated JUnit testing complete.  ";
//      message = "Automated JUnit testing is complete.  ";
//      subject =
//          subject.concat(
//              (getTestResultsWithProblems().endsWith("\n"))
//                  ? "All tests pass."
//                  : "Test failures/errors occurred.");
//      message =
//          message.concat(
//              (getTestResultsWithProblems().endsWith("\n"))
//                  ? "All tests pass."
//                  : "Test failures/errors occurred in the following:  "
//                      + getTestResultsWithProblems())+downloadLinks;
//  } else if (isBuildTested() && (!getTestedBuildType().equals("N"))) {
//      subject = subject.concat("Automated JUnit testing is starting.");
//      message = "The " + subject+downloadLinks;
//  }
//
//  if (subject.endsWith("Test failures/errors occurred."))
//      mailer.sendMessage(subject, message);
//  else if (!getTestedBuildType().equals("N"))
//      mailer.sendMessage(subject, message);
//
//}

    public void setSendMail(final boolean sendMail) {
        this.sendMail = sendMail;
    }

}
