#!/usr/bin/env bash

#exit if meet any error
set -e
bin=`dirname "$0"`
APP_HOME=`cd "${bin}"/..; pwd`
PRG_NAME="gateway-admin-2.0.0.jar"

# print out env properties
echo \$SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE}"
echo \$LOG_HOME="${LOG_HOME}"
echo \$JAVA_OPTS="${JAVA_OPTS} -Xms512m -Xmx512m"
echo "JAVA_OPTS:$JAVA_OPTS"

# export config center address
if [ -z "${SPRING_PROFILES_ACTIVE}" ];then
    echo "Error: SPRING_PROFILES_ACTIVE is not set, must be dev, test or prod"
    exit 1
fi

function print_usage(){
    echo " "
    echo "Usage:${PRG_NAME} COMMAND"
    echo "      where COMMAND is one of:"
    echo " start      start ${PRG_NAME}"
    echo " stop       stop ${PRG_NAME}"
    echo " status     show status of ${PRG_NAME}"
    echo ""
}

if [ $# = 0 ];then
    print_usage
    exit
fi

CLASSPATH="${APP_HOME}"/conf/

# auto find the executable jar with version
for jar in "${APP_HOME}"/*.jar
do
    PRG_NAME=${jar}
    echo \$PRG_HAME=${jar}
done

# some java parameters
if [ "${JAVA_HOME}" != "" ]; then
    #echo "run java in ${JAVA_HOME}"
    JAVA_HOME=${JAVA_HOME}
else
    JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")
fi

if [ "${JAVA_HOME}" = "" ]; then
    echo "Error: JAVA_HOME is not set."
    exit 1
fi

echo \$JAVA_HOME=${JAVA_HOME}

for jar in "${APP_HOME}"/*.jar
do
    CLASSPATH=${CLASSPATH}:${jar}
done
for jar in "${APP_HOME}"/lib/*.jar
do
    CLASSPATH=${CLASSPATH}:${jar}
done

#for jar in "${APP_HOME}"/target/*.jar
#do
#    CLASSPATH=${CLASSPATH}:${jar}
#done
#for jar in "${APP_HOME}"/target/lib/*.jar
#do
#    CLASSPATH=${CLASSPATH}:${jar}
#done

#
CONFIG_PARAMS=""
#CONFIG_PARAMS="--spring.config.location=$APP_HOME/config/ --logging.config=$APP_HOME/config/log4j2-$SPRING_PROFILES_ACTIVE.xml"
echo "config params: ${CONFIG_PARAMS}"


function start(){
    RUNNING=`ps -ef | grep $PRG_NAME | grep -v grep | awk '{print $2}'`
    if [ -n "$RUNNING" ] ; then
        echo "$PRG_HOME is running! $RUNNING"
    else
        echo "$JAVA_OPTS"
        echo "nohup $JAVA_HOME/bin/java ${JAVA_OPTS} -jar $PRG_NAME ${CONFIG_PARAMS}  > /dev/null 2>&1 &"
        exec nohup $JAVA_HOME/bin/java ${JAVA_OPTS} -jar $PRG_NAME ${CONFIG_PARAMS}  > /dev/null 2>&1 &
        if [ $? -eq 0 ] ; then
            echo "$PRG_NAME start success"
        else
            echo "$PRG_NAME start fail"
            exit 1
        fi
    fi
}

function status(){
    process_id=`pgrep -f "$PRG_NAME"`
    if [ $process_id ]; then
        echo "$PRG_NAME is running as process $process_id"
    else
        echo "$PRG_NAME is no running"
    fi
}

function stop(){
    echo " stopping $PRG_NAME..."
    pkill -f "$PRG_NAME"
    echo "$PRG_NAME is stopped!"
}


case $1 in
    --help|-help|-h)
        print_usage
        exit
        ;;

    start)
        start
        ;;
    stop)
        stop
        ;;
    status)
        status
        ;;
    restart)
        stop
        start
        ;;
    *)
esac
exit $?;


