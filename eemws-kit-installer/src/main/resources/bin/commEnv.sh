#!/bin/sh
CLASSPATH=""
for f in "%INSTALL_PATH"/lib/*
do
  CLASSPATH="${CLASSPATH}":$f
done
CLASSPATH=${CLASSPATH}:"%INSTALL_PATH/config"
export CLASSPATH=${CLASSPATH}

export CONSOLE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH/config/console-logging.properties"
export FILE_LOG=-Djava.util.logging.config.file="%INSTALL_PATH/config/file-logging.properties"

# Default memory options. Increase Xmx value to support "huge" messages e.g. -Xmx2048m
export MEM_ARGS="-Xms32m -Xmx700m"

# Add here any aditional java options you want to pass to the program.
export JAVA_OPTIONS=

# Uncomment if you have to deal with systems that cannot handle SHA-2 algorithm
#export JAVA_OPTIONS="${JAVA_OPTIONS} -DUSE_LEGACY_SHA1"

# Uncomment if you want to send as binary compressed XML payload which size is greater than the specified number of characters (here 1000)
#export JAVA_OPTIONS="${JAVA_OPTIONS} -DXML_TO_BINARY_THRESHOLD_CHARS=1000"

# Uncomment if you want to get the xml outputs pretty printed (note: this could impact in the performance if your system deals with "huge" messages)
#export JAVA_OPTIONS="${JAVA_OPTIONS} -DDUSE_PRETTY_PRINT_OUTPUT"

# Uncomment if you face issues with server SNI miss configuration.
#export JAVA_OPTIONS="${JAVA_OPTIONS} -Djsse.enableSNIExtension=false"
