@echo off
set db.bin=D:\
set dbloc=net://trelenggtk.ottawa.ibm.com
set bb.plugins=.\..\..\..
set args=%*

java -cp .\..\..\..\startup.jar org.eclipse.core.launcher.Main -application org.eclipse.ant.core.antRunner -f genresults.xml "-Dargs=%args%" "-Ddb.classpath=%CLASSPATH%" "-Ddbloc=%dbloc%" "-Ddb.bin=%db.bin"