#!/bin/bash

# ===============================
# 1. 설정
# ===============================
PROFILE=$1
SERVICE_NAME="beautiflow-api" # 서비스 이름을 'beautiflow-api'로 고정
JAR_NAME="beautiflow-0.0.1-SNAPSHOT.jar"
TAR_NAME="beautiflow.tar.gz"
DEPLOY_DIR="/opt/${SERVICE_NAME}"
LOG_DIR="/var/log/${SERVICE_NAME}"

# 인자 유효성 검사
if [ -z "$PROFILE" ]; then
    echo "오류: 프로파일이 누락되었습니다."
    echo "사용법: ./deploy.sh [profile]"
    echo "예시: ./deploy.sh prod"
    exit 1
fi

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 배포 시작: ${SERVICE_NAME} (${PROFILE} 프로파일)"

# =========================================================================
# 2. 배포 준비
# =========================================================================
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 디렉토리 생성 및 권한 설정..."
sudo mkdir -p ${DEPLOY_DIR} ${LOG_DIR}
sudo chown -R ubuntu:ubuntu ${DEPLOY_DIR} ${LOG_DIR}

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 기존 배포 파일 정리 및 압축 해제..."
rm -rf ${DEPLOY_DIR}/*
tar -xzf /home/ubuntu/${TAR_NAME} -C ${DEPLOY_DIR}/

# =========================================================================
# 3. 서비스 관리
# =========================================================================
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 기존 서비스 중지: ${SERVICE_NAME}.service"
sudo systemctl stop ${SERVICE_NAME}.service || true
sleep 5

SERVICE_FILE_CONTENT="[Unit]
Description=${SERVICE_NAME} Spring Boot Application
After=network.target

[Service]
User=ubuntu
# 환경 변수 파일 경로를 지정합니다.
EnvironmentFile=/etc/beautiflow/beautiflow-api.conf
# Spring 프로파일은 'prod'와 'api'를 활성화합니다.
ExecStart=/usr/bin/java -jar ${DEPLOY_DIR}/${JAR_NAME} --spring.profiles.active=${PROFILE},api
SuccessExitStatus=143
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target"

echo "[$(date +'%Y-%m-%d %H:%M:%S')] systemd 서비스 파일 생성/업데이트..."
echo "${SERVICE_FILE_CONTENT}" | sudo tee /etc/systemd/system/${SERVICE_NAME}.service > /dev/null

sudo systemctl daemon-reload

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 새 서비스 시작 및 활성화..."
sudo systemctl start ${SERVICE_NAME}.service
sudo systemctl enable ${SERVICE_NAME}.service

# =========================================================================
# 4. 배포 후 검증
# =========================================================================
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 서비스 헬스 체크 대기 중 (최대 3분)..."
HEALTH_CHECK_URL="http://localhost:8080/health"
for i in {1..36}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_CHECK_URL)
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] 서비스가 성공적으로 시작되었습니다. (HTTP 200 OK)"
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] 최종 배포 성공!"
        exit 0
    fi
    echo "서비스 시작 대기 중... (현재 ${i}회 시도)"
    sleep 5
done

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 오류: 서비스 시작 타임아웃!"
echo "로그를 확인해주세요: journalctl -u ${SERVICE_NAME}.service -n 50"
exit 1
