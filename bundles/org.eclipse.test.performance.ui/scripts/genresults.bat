@echo off

REM set db.bin=D:\
REM set dbloc=net://trelenggtk.ottawa.ibm.com

set bb.plugins=.\..\..\..
set args=%*

java -jar .\..\..\..\startup.jar -application org.eclipse.ant.core.antRunner -f genresults.xml "-Dargs=%args%" "-Ddbloc=%dbloc%" "-Ddb.bin=%db.bin"