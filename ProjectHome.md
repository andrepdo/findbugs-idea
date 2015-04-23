# FindBugs for IntelliJ IDEA  #


[FindBugs-IDEA](Dashboard.md) provides static byte code analysis to look for bugs in Java code from within IntelliJ IDEA. FindBugs is a defect detection tool for Java that uses static analysis to look for more than 200 bug patterns, such as null pointer dereferences, infinite recursive loops, bad uses of the Java libraries and deadlocks. FindBugs can identify hundreds of serious defects in large applications (typically about 1 defect per 1000-2000 lines of non-commenting source statements). The name FindBugs™ and the FindBugs logo are trademarked by The University of Maryland. As of July, 2008, FindBugs has been downloaded more than 700,000 times. FindBugs requires JRE (or JDK) 1.5.0 or later to run. However, it can analyze programs compiled for any version of Java and is used by many major companies and financial institutions. FindBugs-IDEA uses FindBugs™ under the hood. for more information see http://findbugs.sourceforge.net/.

..."We use IDEA inspections and FindBugs complementary. FindBugs is running in our continuous integration process and IDEA inspections are used during coding." What about using both during coding from within IntelliJ IDEA?

  * for screenshots see http://plugins.intellij.net/plugin?idea&id=3847
  * @see article at dzone: http://jetbrains.dzone.com/tips/intellij-idea-finds-bugs



---


## Downloads ##

Please download the latest version from http://plugins.jetbrains.com/plugin/3847

We can not provide new downloads here any more, see http://google-opensource.blogspot.de/2013/05/a-change-to-google-code-download-service.html


---


## Supporters ##
YourKit is kindly supporting FindBugs-IDEA open source project with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
<a href='http://www.yourkit.com/java/profiler/index.jsp'>YourKit Java Profiler</a> and
<a href='http://www.yourkit.com/.net/profiler/index.jsp'>YourKit .NET Profiler</a>.

[![](http://www.yourkit.com/images/yklogo.png)](http://www.yourkit.com/java/profiler/index.jsp)

---



### FindBugs-IDEA plugin features: ###

  * findbugs-2 support
  * Quickfix: annotate, suppress findbugs patterns (GutterIcon and document markup (alt+enter)) idea 9 until 13 compatibility
  * configure findbugs on idea project and module level
  * jump to source from results shown in toolwindow including anonymous classes
  * bug descriptions, solutions
  * run analysis always in background
  * run findbugs analysis on all affected files after compile
  * min. priority to report
  * background scanning
  * configurable effort run level
  * configurable detectors
  * bug categories to report
  * file filter (include, exclude, exclude baseline bugs)
  * load additional detector plugins like fb-contrib.jar
  * group results by: bug category, classname, package, priority, bug rank
  * intellij local history support
  * quick search within the result tree
  * run Findbugs analysis as IntelliJ inspection (experimental)
  * Analyze actions (all actions are also available in the intellij project tree, toolbar  menu, context menu):
  * analyze all modified files
  * analyze all files on the active changelist (available from FindBugs-IDEA main ToolWindow and the ChangelistToolWindow)
  * analyze class under cursor
  * analyze current editor file
  * analyze all files of an intellij project
  * analyze all files of an intellij module
  * analyze all files of the selected package
  * analyze a bunch of selected files (project tree)
  * export a bug collection to html and/or xml with limited
  * configurable dir based archive support
  * import a bug collection from xml
  * configurable : open an exported bug collection html page in the intellij configured  browser
  * configurable : preview found bugs in an editor preview panel

## Screenshots ##
<a href='#screenshots'></a>
![http://plugins.jetbrains.com/oldimg/screenshots/FindBugs-IDEA_2543.png](http://plugins.jetbrains.com/oldimg/screenshots/FindBugs-IDEA_2543.png)
![http://plugins.jetbrains.com/oldimg/screenshots/FindBugs-IDEA_5750.png](http://plugins.jetbrains.com/oldimg/screenshots/FindBugs-IDEA_5750.png)

## Supporters ##
<a href='#supporters'></a>

### YourKit ###
<a href='#yourkit'></a>
YourKit is kindly supporting FindBugs-IDEA open source project with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
<a href='http://www.yourkit.com/java/profiler/index.jsp'>YourKit Java Profiler</a> and
<a href='http://www.yourkit.com/.net/profiler/index.jsp'>YourKit .NET Profiler</a>.<br>
<a href='http://www.yourkit.com/java/profiler/index.jsp'><img src='http://www.yourkit.com/images/yklogo.png' /></a>