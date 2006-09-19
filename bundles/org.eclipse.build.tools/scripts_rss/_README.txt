These scripts are examples to get you started.

== Assembly ==

To build the feedTools.jar, use buildFeedToolsJar.sh (or .xml).
To create a zip of all the RSS-related code, use buildFeedToolsZip.sh (or .xml).

== Feed Manipulation ==

To do feed manipulation, like creating a feed, adding a entry to a feed, querying 
for attribute values in a feed, or changing attribute values in a feed, look at 
feedManipulation.sh (and .xml) and create a copy similar to suit your needs.

== Feed Publishing ==

To publish a feed, use a script similar to feedPublish.sh (and .xml). There 
are two Ant scripts provided - one using the custom task (feedPublishCustomTask.xml) 
and one using only Ant's core tasks (feedPublishAntOnly.xml). The second one is 
provided for reference only.

== Feed Watching (And Response) ==

To watch a feed for changes or the appearance of specific attribute values (like 
test results), use a script similar to feedWatch.sh (and .xml), along with properties 
like those in properties/watcher.*.properties

The script sendEmailAlert.sh is provided as an example of what to in response to 
a feed change. You can customize the response to suit your needs. 

Additional documentation can be found in the *.xml Ant scripts and *.properties files.