#!/bin/sh
. ./commEnv.sh
if [ ! -d ../log ] ; then
        cd ..
        mkdir log
        cd bin
fi
javaw $MEM_ARGS $JAVA_OPTIONS $FILE_LOG es.ree.eemws.kit.folders.FolderManager
