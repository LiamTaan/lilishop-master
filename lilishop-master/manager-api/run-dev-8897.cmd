@echo off
setlocal
cd /d %~dp0

set "JAVA_HOME=D:\utils\jdk21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist runtime-logs (
  mkdir runtime-logs
)

"%JAVA_HOME%\bin\java.exe" -cp "target\classes;..\framework\target\classes;target\dependency\*" cn.lili.ManagerApiApplication --spring.profiles.active=dev --server.port=8897 1>runtime-logs\manager-8897.out.log 2>runtime-logs\manager-8897.err.log
