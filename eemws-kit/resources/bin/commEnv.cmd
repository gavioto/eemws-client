@echo off
setlocal EnableDelayedExpansion
SET CLASSPATH=
for %%a in ("%INSTALL_PATH\lib\*.*") do set CLASSPATH=!CLASSPATH!;%%a
endlocal & set CLASSPATH=%CLASSPATH%
SET CLASSPATH=%CLASSPATH%;%INSTALL_PATH\config
SET MEM_ARGS=-Xms32m -Xmx700m
SET CONSOLE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH\config\console-logging.properties"
SET FILE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH\config\file-logging.properties"
