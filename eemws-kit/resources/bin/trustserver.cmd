@echo off
call commEnv.cmd
@java %MEM_ARGS% %JAVA_OPTIONS% %CONSOLE_LOG% es.ree.eemws.kit.cmd.trustserver.Main %*
