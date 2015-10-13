#!/bin/sh
. ./commEnv.sh
javaw $MEM_ARGS $JAVA_OPTIONS $CONSOLE_LOG es.ree.eemws.kit.cmd.list.Main $*
