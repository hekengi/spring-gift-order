#!/bin/bash

JAR_PATH=$(ls /home/ec2-user/*[^plain].jar)
JAR_NAME=$(basename $JAR_PATH)

echo ">>> 실행할 파일: $JAR_NAME"

CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]; then
    echo ">>> 현재 실행 중인 애플리케이션이 없습니다."
else
    echo ">>> 기존 애플리케이션을 종료합니다. (PID: $CURRENT_PID)"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo ">>> 새 애플리케이션을 배포합니다: $JAR_PATH"
nohup java -jar $JAR_PATH > ~/deploy.log 2>&1 &