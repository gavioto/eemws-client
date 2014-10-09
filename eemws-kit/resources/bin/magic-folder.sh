#!/bin/sh
. ./commEnv.sh
if [ ! -d ../log ] ; then
        cd ..
        mkdir log
        cd bin
fi
javaw $MEM_ARGS $FILE_LOG es.ree.eemws.kit.folders.FolderManager
