echo off
call commEnv.cmd
if NOT EXIST ../log (
	CD ..
	MKDIR log
	CD bin
) 
@REM do not include argument "-Dinteractive" in no-interctative environments  (ie daemons, background process, etc.)
start javaw %MEM_ARGS% %JAVA_OPTIONS% %FILE_LOG% -Dinteractive es.ree.eemws.kit.folders.FolderManager
