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

# 현재 실행 중인 JAR 파일 백업
if [ -f "${DEPLOY_DIR}/${JAR_NAME}" ]; then
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 기존 JAR 파일 백업 중..."
    cp ${DEPLOY_DIR}/${JAR_NAME} ${DEPLOY_DIR}/${JAR_NAME}.backup
fi

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 기존 배포 파일 정리 및 압축 해제..."
rm -rf ${DEPLOY_DIR}/*
tar -xzf /home/ubuntu/${TAR_NAME} -C ${DEPLOY_DIR}/

# 백업 파일 복원
if [ -f "${DEPLOY_DIR}/${JAR_NAME}.backup" ]; then
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 백업 파일 보존 중..."
    cp ${DEPLOY_DIR}/${JAR_NAME}.backup ${DEPLOY_DIR}/${JAR_NAME}.backup.tmp
fi

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
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar ${DEPLOY_DIR}/${JAR_NAME} --spring.profiles.active=${PROFILE},api
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

        # 백업 파일 정리 (성공 시)
        if [ -f "${DEPLOY_DIR}/${JAR_NAME}.backup.tmp" ]; then
            echo "[$(date +'%Y-%m-%d %H:%M:%S')] 백업 파일 정리 중..."
            rm ${DEPLOY_DIR}/${JAR_NAME}.backup.tmp
        fi

        echo "[$(date +'%Y-%m-%d %H:%M:%S')] 최종 배포 성공!"
        exit 0
    fi
    echo "서비스 시작 대기 중... (현재 ${i}회 시도)"
    sleep 5
done

# 롤백 프로세스 시작
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 오류: 서비스 시작 타임아웃! 롤백을 시작합니다..."

# 백업 파일이 있는 경우 복원
if [ -f "${DEPLOY_DIR}/${JAR_NAME}.backup.tmp" ]; then
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 이전 버전으로 롤백 중..."
    mv ${DEPLOY_DIR}/${JAR_NAME}.backup.tmp ${DEPLOY_DIR}/${JAR_NAME}

    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 롤백된 서비스 재시작 중..."
    sudo systemctl restart ${SERVICE_NAME}.service

    # 롤백 후 헬스 체크
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 롤백 후 서비스 헬스 체크 중..."
    sleep 10
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_CHECK_URL)
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] 롤백 성공! 이전 버전으로 서비스가 복원되었습니다."
    else
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] 경고: 롤백 후에도 서비스가 정상 동작하지 않습니다."
    fi
else
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 경고: 롤백할 백업 파일이 없습니다."
fi

echo "로그를 확인해주세요: journalctl -u ${SERVICE_NAME}.service -n 50"
exit 1