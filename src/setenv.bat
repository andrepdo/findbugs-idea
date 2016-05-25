@echo off

:ENVSET
rem *** Set up project environment
rem ****************************************
echo Setting up environment ...

set ANT_OPTS=-Xmx512m
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_92
set ANT_HOME=C:\tools\apache-ant-1.9.1
set PATH=%JAVA_HOME%\bin;%PATH%;%CD%\bin;%ANT_HOME%\bin

rem *** Display configuration
rem *************************
echo Check java.exe ...
where java.exe || GOTO EXIT

echo Check javac.exe ...
where javac.exe || GOTO EXIT

echo Check ANT ...
where ant || GOTO EXIT

echo Java version:
java -version || GOTO EXIT

echo Javac version:
javac -version || GOTO EXIT

echo ANT version:
call ant -version || GOTO EXIT

echo Environment OK

:EXIT
