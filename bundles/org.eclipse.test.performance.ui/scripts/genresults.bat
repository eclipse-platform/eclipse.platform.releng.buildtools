@echo off

REM set dbloc=net://trelenggtk.ottawa.ibm.com

java -Declipse.perf.dbloc=%dbloc% -jar .\..\..\..\startup.jar -os win32 -ws win32 -arch x86 -application org.eclipse.test.performance.ui.resultGenerator %*

