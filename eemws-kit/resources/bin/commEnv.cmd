@echo off
setlocal EnableDelayedExpansion
SET CLASSPATH=
for %%a in ("%INSTALL_PATH\lib\*.*") do set CLASSPATH=!CLASSPATH!;%%a
endlocal & set CLASSPATH=%CLASSPATH%
SET CLASSPATH=%CLASSPATH%;%INSTALL_PATH\config

SET CONSOLE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH\config\console-logging.properties"
SET FILE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH\config\file-logging.properties"

REM Default memory options. Increase Xmx value to support "huge" messages e.g. -Xmx2048m
SET MEM_ARGS=-Xms32m -Xmx700m

REM Uncomment if you have to deal with systems that cannot handle SHA-2 algorithm
REM SET JAVA_OPTIONS=-DUSE_LEGACY_SHA1