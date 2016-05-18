@echo off
call commEnv.cmd
start javaw %MEM_ARGS% %JAVA_OPTIONS% es.ree.eemws.kit.gui.applications.sender.Sender
