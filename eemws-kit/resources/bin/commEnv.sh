#!/bin/sh
CLASSPATH=""
for f in "%INSTALL_PATH"/lib/*
do
  CLASSPATH="$CLASSPATH":$f
done
CLASSPATH=$CLASSPATH:"%INSTALL_PATH/lib/config"
export CLASSPATH=$CLASSPATH
export MEM_ARGS="-Xms32m -Xmx700m"
export CONSOLE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH/config/console-logging.properties"
export FILE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH\config\file-logging.properties"
