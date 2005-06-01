@echo off

REM set dbloc=net://trelenggtk.ottawa.ibm.com

set args=%*

java -jar .\..\..\..\startup.jar -application org.eclipse.ant.core.antRunner -f genresults.xml "-Dargs=%args%" "-Ddbloc=%dbloc%"