@echo off
call commEnv.cmd
@java %MEM_ARGS% %CONSOLE_LOG% es.ree.eemws.kit.cmd.query.Main %*
