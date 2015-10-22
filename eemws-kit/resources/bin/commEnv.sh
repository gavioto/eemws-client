#!/bin/sh
CLASSPATH=""
for f in "%INSTALL_PATH"/lib/*
do
  CLASSPATH="$CLASSPATH":$f
done
CLASSPATH=$CLASSPATH:"%INSTALL_PATH/config"
export CLASSPATH=$CLASSPATH

export CONSOLE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH/config/console-logging.properties"
export FILE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH/config/file-logging.properties"

# Default memory options. Increase Xmx value to support "huge" messages e.g. -Xmx2048m
export MEM_ARGS="-Xms32m -Xmx700m"

# Uncomment if you have to deal with systems that cannot handle SHA-2 algorithm
#export JAVA_OPTIONS=-DUSE_LEGACY_SHA1