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
        //test.setPlatformIdentifierToken("%platform%");
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
        result = result + "<td><?php genLinks($_SERVER[\"SERVER_NAME\"],\"${buildId}\",\"" + aPlatform.getFileName()
                + "\"); ?></td>\n";
        result = result + "</tr>\n";

        return result;
    }

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
