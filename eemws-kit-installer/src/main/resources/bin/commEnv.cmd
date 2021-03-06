@echo off
setlocal EnableDelayedExpansion

SET CLASSPATH=
SET JAVA_OPTIONS=

for %%a in ("%INSTALL_PATH\lib\*.*") do set CLASSPATH=!CLASSPATH!;%%a
endlocal & set CLASSPATH=%CLASSPATH%
SET CLASSPATH=%CLASSPATH%;%INSTALL_PATH\config

SET CONSOLE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH\config\console-logging.properties"
SET FILE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH\config\file-logging.properties"

REM Default memory options. Increase Xmx value to support "huge" messages e.g. -Xmx2048m
SET MEM_ARGS=-Xms32m -Xmx700m

REM Add here any aditional java options you want to pass to the program.
SET JAVA_OPTIONS=

REM Uncomment if you have to deal with systems that cannot handle SHA-2 algorithm
REM SET JAVA_OPTIONS=%JAVA_OPTIONS% -DUSE_LEGACY_SHA1

REM Uncomment if you want to send as binary compressed XML payload which size is greater than the specified number of characters (here 1000)
REM SET JAVA_OPTIONS=%JAVA_OPTIONS% -DXML_TO_BINARY_THRESHOLD_CHARS=1000

REM Uncomment if you want to get the xml outputs pretty printed (note: this could impact in the performance if your system deals with "huge" messages)
REM SET JAVA_OPTIONS=%JAVA_OPTIONS% -DUSE_PRETTY_PRINT_OUTPUT

REM Uncomment if you face issues with server SNI miss configuration.
REM SET JAVA_OPTIONS=%JAVA_OPTIONS% -Djsse.enableSNIExtension=false
