@echo off
setLocal EnableDelayedExpansion

set _G_ROOT_DIR=%~dp0

set _LAUNCHER_API_JAR=%_G_ROOT_DIR%..\lib\gosu-launcher-api-2.jar
set _LAUNCHER_IMPL_JAR=%_G_ROOT_DIR%..\lib\gosu-launcher-impl-2.1.jar
set _LAUNCHER_AETHER_JAR=%_G_ROOT_DIR%..\lib\gosu-launcher-aether-2.1.jar
set _G_CLASSPATH=%_LAUNCHER_API_JAR%;%_LAUNCHER_IMPL_JAR%;%_LAUNCHER_AETHER_JAR%
set _CMD_LINE_ARGS=

set _DEBUG=
if ""%1""==""debug"" set _DEBUG=-Xdebug -Xrunjdwp:transport=dt_shmem,address=gosu,server=y,suspend=y
if ""%1""==""debug"" shift

REM Slurp the command line arguments. This loop allows for an unlimited number
REM of arguments (up to the command line limit, anyway).

if ""%1""=="""" goto doneStart

:setupArgs
  if ""%1""=="""" goto doneStart
  set _CMD_LINE_ARGS=%_CMD_LINE_ARGS% %1
  shift
  goto setupArgs

REM This label provides a place for the argument list loop to break out
REM and for NT handling to skip to.
:doneStart

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:runGosu
call "%_JAVACMD%" %_DEBUG% %GOSU_OPTS% -classpath "%_G_CLASSPATH%" gw.lang.launch.impl.GosuLauncher -Dlauncher.properties.file="%_G_ROOT_DIR%gosulaunch.properties" %_CMD_LINE_ARGS%
