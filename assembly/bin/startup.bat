set "JAVA_CMD=java"
:mainEntry
REM set HOME_DIR
set "CURR_DIR=%cd%"
cd ..
set "HOTDB_HOME=%cd%"
cd %CURR_DIR%
"%JAVA_CMD%" -server -Xms2G -Xmx2G -XX:PermSize=64M  -XX:+AggressiveOpts -XX:MaxDirectMemorySize=1G -DTIMO_HOME=%TIMO_HOME% -cp "..\conf;..\lib\*" fm.liu.timo.TimoStartup