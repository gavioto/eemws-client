@echo off
call commEnv.cmd
@java %MEM_ARGS% %JAVA_ARGS% es.ree.eemws.kit.cmd.get.Main %*
