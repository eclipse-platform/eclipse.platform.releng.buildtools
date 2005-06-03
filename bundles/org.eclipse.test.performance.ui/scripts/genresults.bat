@echo off

REM set dbloc=net://trelenggtk.ottawa.ibm.com

java -Declipse.perf.dbloc=%dbloc% -jar .\..\..\..\startup.jar -application org.eclipse.test.performance.ui.resultGenerator %*

