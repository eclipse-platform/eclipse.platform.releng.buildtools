@echo off

REM set dbbin=D:\Cloudscape_10.0\lib
REM set dbloc=net://trelenggtk.ottawa.ibm.com

set bbplugins=.\..\..\..
set args=%*

java -jar .\..\..\..\startup.jar -application org.eclipse.ant.core.antRunner -f genresults.xml "-Dargs=%args%" "-Ddbloc=%dbloc%" "-Ddbbin=%db.bin"