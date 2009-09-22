@echo off

:ENVSET
rem *** Set up project environment
rem ****************************************
echo Setting up environment ...

set PATH=%JAVA_HOME%\bin;%PATH%;%CD%\bin;%ANT_HOME%\bin
set ANT_OPTS=-Xmx512m
set JAVA_HOME=
set ANT_HOME=
set IDEA_HOME

rem *** Display configuration
rem *************************
which java.exe javac.exe
java -version
call ant.bat

:EXIT
