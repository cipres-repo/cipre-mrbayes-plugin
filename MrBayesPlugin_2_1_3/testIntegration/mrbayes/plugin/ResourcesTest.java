package mrbayes.plugin;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MrBayes's build.xml is "non-standard" as far as plugins go. I don't believe it was created using the createNewPlugin task in ant or whatever it is we usually use.
 *
 * One of the things that is different is that MrBayes' build.xml includes certain resources in its resources folder by name (including its executables), rather than just doing a wildcard
 * include of everything in the resources dir. To that end, this test
 *
 * @author alex
 *         Created on 22/05/14 12:39 PM
 */
public class ResourcesTest extends Assert {

    private static final int MINIMUM_COMMON_STARTING_CHARACTERS = 5;

    @Test
    public void testResourcesDirectory_IncludesInBuildXml_MatchTheExesInTheResourcesDir() throws URISyntaxException, IOException {
        File mrBayesDirectory = new File(new File(".").getAbsolutePath()+"/../plugins/marc/MrBayesPlugin");
        File buildXml = new File(mrBayesDirectory, "build.xml");
        File resourcesDirectory = new File(mrBayesDirectory, "resources");
        // probably not going to nullpointer unless something happens
        //noinspection ConstantConditions
        File[] resources = resourcesDirectory.listFiles();
        String commonSubstring = null;
        boolean foundCommonSubstring = false;
        for (File resource : resources == null ? new File[0] : resources) {
            String resourceName = resource.getName();
            if (resourceName.endsWith("exe")) {
                if (commonSubstring == null) {
                    commonSubstring = resource.getName();
                } else {
                    for (int substringLength = resourceName.length(); substringLength >= MINIMUM_COMMON_STARTING_CHARACTERS; substringLength--) {
                        String potentialSubstring = commonSubstring.substring(0, substringLength);
                        if (resourceName.startsWith(potentialSubstring)) {
                            commonSubstring = potentialSubstring;
                            foundCommonSubstring = true;
                            break;
                        }
                    }
                    if (foundCommonSubstring) break;
                }
            }
        }
        if (!foundCommonSubstring) {
            fail("Couldn't find a common pattern amongst the exes that we thought looks like a MrBayes executable.");
        }

        BufferedReader reader = new BufferedReader(new FileReader(buildXml));
        String line;
        String failureReason = "no include lines found matching our pattern. Something big has probably changed in the build xml.";
        Pattern includeLinePattern = Pattern.compile(".*<include name=\"\\$\\{plugin-name\\}/([^\"]*)\".*");
        List<String> includePatternsWeFound = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            Matcher includeLine = includeLinePattern.matcher(line);
            if (includeLine.matches()) {
                String includePattern = includeLine.group(1);
                includePatternsWeFound.add(includePattern);
                failureReason = "found some include lines, but none of them starts with our common substring of '"+commonSubstring+"' (this test's logic could be wrong though).\n" +
                        "Some includes we found were: "+includePatternsWeFound;
                // treat includedResource as a pattern. See if it would match our common substring of exes.
                if (includePattern.startsWith(commonSubstring)) {
                    failureReason = null;
                    break;
                }
            }
        }
        assertNull(failureReason, failureReason);
    }
}
