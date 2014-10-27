/*
 * Created on Apr 8, 2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

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

        final EclipseTestResultsGeneratorNoMail test = new EclipseTestResultsGeneratorNoMail();
        test.buildType = "I";
        test.setIsBuildTested(true);
        test.setDropTokenList("%sdk%,%tests%,%example%,%rcpruntime%,%rcpsdk%,%deltapack%,%runtime%,%jdt%,%jdtsdk%,%jdtc%,%pde%,%pdesdk%,%cvs%,%cvssdk%,%swt%,%relengtools%");
        test.getDropTokensFromList(test.getDropTokenList());
        test.setXmlDirectoryName("/data/shared/eclipse/builds/4I/siteDir/eclipse/downloads/drops4/I20140923-0105/testresults/xml");
        test.setHtmlDirectoryName("/data/shared/eclipse/builds/4I/siteDir/eclipse/downloads/drops4/I20140923-0105/testresults/html");
        test.setDropDirectoryName("/data/shared/eclipse/builds/4I/siteDir/eclipse/downloads/drops4/I20140923-0105");
        test.setTestResultsTemplateFileName(publishingContent + "/templateFiles/testResults.php.template");
        test.setDropTemplateFileName(publishingContent + "/templateFiles/index.php.template");
        test.setTestResultsHtmlFileName("testResults.php");
        test.setDropHtmlFileName("index.php");
        // test.setDropHtmlFileName("index.html");
        test.setPlatformIdentifierToken("%platform%");
        test.setHrefTestResultsTargetPath("/shared/eclipse/builds/4I/siteDir/eclipse/downloads/drops4/I20140923-0105/testresults");
        test.setCompileLogsDirectoryName("/shared/eclipse/builds/4I/siteDir/eclipse/downloads/drops4/I20140923-0105/compilelogs");
        test.setHrefCompileLogsTargetPath("compilelogs");
        test.setTestManifestFileName("/home/davidw/gitdavidw2/eclipse.platform.releng.aggregator/eclipse.platform.releng.tychoeclipsebuilder/eclipse/publishingFiles/testManifest.xml");
        test.execute();
    }

    // buildType used to determine if mail should be sent on
    // successful build completion
    private String  buildType;
    private boolean sendMail = true;

    @Override
    public void execute() {
        super.execute();
        // if (sendMail)
        // mailResults();
    }

    /**
     * @return
     */
    @Override
    public String getBuildType() {
        return buildType;
    }

    public boolean isSendMail() {
        return sendMail;
    }

    @Override
    protected String processDropRow(final PlatformStatus aPlatform) {

        String result = "<tr>";

        result = result + "<td>" + aPlatform.getName() + "</td>";

        // generate http, md5 and sha1 links by calling php functions in the
        // template
        result = result + "<td><?php genLinks($_SERVER[\"SERVER_NAME\"],\"@buildlabel@\",\"" + aPlatform.getFileName()
                + "\"); ?></td>\n";
        result = result + "</tr>\n";

        return result;
    }

    // private void mailResults() {
    // //send a different message for the following cases:
    // //build is not tested at all
    // //build is tested, tests have not run
    // //build is tested, tests have run with error and or failures
    // //build is tested, tests have run with no errors or failures
    // try {
    // mailer = new Mailer();
    // } catch (NoClassDefFoundError e) {
    // return;
    // }
    // String buildLabel = mailer.getBuildProperties().getBuildLabel();
    // String httpUrl = mailer.getBuildProperties().getHttpUrl()+"/"+buildLabel;
    // // String ftpUrl =
    // mailer.getBuildProperties().getftpUrl()+"/"+buildLabel;
    //
    // String subject = "Build is complete.  ";
    //
    // String downloadLinks="\n\nHTTP Download:\n\n\t"+httpUrl+" \n\n";
    // /* downloadLinks=downloadLinks.concat("FTP Download:\n\n");
    // downloadLinks=downloadLinks.concat("\tuser: anonymous\n\tpassword: (e-mail address or leave blank)\n\tserver:  download.eclipse.org\n\tcd to directory:  "+buildLabel);
    // downloadLinks=downloadLinks.concat("\n\n\tor");
    // downloadLinks=downloadLinks.concat("\n\n\t"+ftpUrl);*/
    //
    // //provide http links
    // String message = "The build is complete."+downloadLinks;
    //
    // if (testsRan()) {
    // subject = "Automated JUnit testing complete.  ";
    // message = "Automated JUnit testing is complete.  ";
    // subject =
    // subject.concat(
    // (getTestResultsWithProblems().endsWith("\n"))
    // ? "All tests pass."
    // : "Test failures/errors occurred.");
    // message =
    // message.concat(
    // (getTestResultsWithProblems().endsWith("\n"))
    // ? "All tests pass."
    // : "Test failures/errors occurred in the following:  "
    // + getTestResultsWithProblems())+downloadLinks;
    // } else if (isBuildTested() && (!buildType.equals("N"))) {
    // subject = subject.concat("Automated JUnit testing is starting.");
    // message = "The " + subject+downloadLinks;
    // }
    //
    // if (subject.endsWith("Test failures/errors occurred."))
    // mailer.sendMessage(subject, message);
    // else if (!buildType.equals("N"))
    // mailer.sendMessage(subject, message);
    //
    // }

    /**
     * @param buildType
     */
    @Override
    public void setBuildType(final String buildType) {
        this.buildType = buildType;
    }

    public void setSendMail(final boolean sendMail) {
        this.sendMail = sendMail;
    }

}
