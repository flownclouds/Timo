#!/bin/sh

#set HOME
CURR_DIR=`pwd`
cd `dirname "$0"`/..
TIMO_HOME=`pwd`
cd $CURR_DIR
if [ -z "$TIMO_HOME" ] ; then
    echo
    echo "Error: TIMO_HOME environment variable is not defined correctly."
    echo
    exit 1
fi
#==============================================================================

#==============================================================================
#set JAVA_OPTS
JAVA_OPTS="-server -Xms2G -Xmx2G -XX:PermSize=64M -XX:MaxDirectMemorySize=4G"
#performance Options
#JAVA_OPTS="$JAVA_OPTS -XX:+AggressiveOpts"
#JAVA_OPTS="$JAVA_OPTS -XX:MaxTenuringThreshold=8"
#GC Options
#JAVA_OPTS="$JAVA_OPTS -XX:+DisableExplicitGC"
#JAVA_OPTS="$JAVA_OPTS -XX:ParallelGCThreads=32"
#JAVA_OPTS="$JAVA_OPTS -XX:+ParallelRefProcEnabled"

#JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC"
#JAVA_OPTS="$JAVA_OPTS -XX:CMSMarkStackSize=2M"
#JAVA_OPTS="$JAVA_OPTS -XX:CMSMarkStackSizeMax=8M"
#JAVA_OPTS="$JAVA_OPTS -XX:+CMSClassUnloadingEnabled"
#JAVA_OPTS="$JAVA_OPTS -XX:+CMSPermGenSweepingEnabled "

#JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"

#GC Log Options
#JAVA_OPTS="$JAVA_OPTS -Xverbosegc:file=/$TIMO_HOME/logs/hotdb.vgc"
#JAVA_OPTS="$JAVA_OPTS -Xloggc:/$TIMO_HOME/logs/gc.log"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCApplicationStoppedTime"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCTimeStamps"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
#debug Options
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9005 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8065,server=y,suspend=n"
#==============================================================================

#set CLASSPATH
TIMO_CLASSPATH="$TIMO_HOME/conf:$TIMO_HOME/lib/classes"
for i in "$TIMO_HOME"/lib/*.jar
do
    TIMO_CLASSPATH="$TIMO_CLASSPATH:$i"
done
#==============================================================================

#startup Server
RUN_CMD="java "
RUN_CMD="$RUN_CMD -DTIMO_HOME=\"$TIMO_HOME\""
RUN_CMD="$RUN_CMD -classpath \"$TIMO_CLASSPATH\""
RUN_CMD="$RUN_CMD $JAVA_OPTS"
RUN_CMD="$RUN_CMD re.ovo.timo.TimoStartup $@"
RUN_CMD="$RUN_CMD >> \"$TIMO_HOME/logs/console.log\" 2>&1 &"
echo $RUN_CMD
eval $RUN_CMD
#==============================================================================
