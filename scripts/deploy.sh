#!/bin/bash

SERVER_TYPE=$1
PROFILE=$2

# 인자 유효성 검사
if [ -z "$SERVER_TYPE" ] || [ -z "$PROFILE" ]; then
    echo "오류: 서버 타입 또는 프로파일이 누락되었습니다."
    echo "사용법: ./deploy.sh [server_type] [profile]"
    echo "예시: ./deploy.sh api prod"
    exit 1
fi

# ===============================
# 1. 공통 설정 및 서버별 동적 설정
# ===============================
TAR_NAME="beautiflow.tar.gz"

COMMON_JAR_NAME="beautiflow-0.0.1-SNAPSHOT.jar"

SERVICE_NAME="beautiflow-${SERVER_TYPE}" # 예: beautiflow-api, beautiflow-chat, beautiflow-gateway

DEPLOY_DIR="/opt/${SERVICE_NAME}"
LOG_DIR="/var/log/${SERVICE_NAME}"

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 배포 시작: ${SERVER_TYPE} 서버 (${PROFILE} 프로파일)"
echo "서비스 이름: ${SERVICE_NAME}"
echo "JAR 파일 (압축 내부): ${COMMON_JAR_NAME}"
echo "배포 디렉토리: ${DEPLOY_DIR}"
echo "로그 디렉토리: ${LOG_DIR}"

# =========================================================================
# 2. 배포 준비 단계
# =========================================================================

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 배포 디렉토리 (${DEPLOY_DIR}) 생성 및 권한 설정 중..."
sudo mkdir -p ${DEPLOY_DIR}
sudo chown -R ubuntu:ubuntu ${DEPLOY_DIR}
sudo chmod -R 755 ${DEPLOY_DIR}

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 로그 디렉토리 (${LOG_DIR}) 생성 및 권한 설정 중..."
sudo mkdir -p ${LOG_DIR}
sudo chown -R ubuntu:ubuntu ${LOG_DIR}
sudo chmod -R 755 ${LOG_DIR}

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 기존 배포 파일 정리 중..."
rm -rf ${DEPLOY_DIR}/*

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 압축 파일 해제 중: /home/ubuntu/${TAR_NAME} -> ${DEPLOY_DIR}/"
tar -xzf /home/ubuntu/${TAR_NAME} -C ${DEPLOY_DIR}/

# =========================================================================
# 3. 서비스 관리 단계
# =========================================================================

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 기존 서비스 중지 중: ${SERVICE_NAME}.service"
sudo systemctl stop ${SERVICE_NAME}.service || true
sleep 5

SERVICE_FILE_CONTENT="[Unit]
Description=${SERVICE_NAME} Spring Boot Application
After=syslog.target network.target

[Service]
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar ${DEPLOY_DIR}/${COMMON_JAR_NAME} --spring.profiles.active=${PROFILE},${SERVER_TYPE}
User=ubuntu
SuccessExitStatus=143
StandardOutput=file:${LOG_DIR}/stdout.log
StandardError=file:${LOG_DIR}/stderr.log
SyslogIdentifier=${SERVICE_NAME}
Restart=always
RestartSec=10s

[Install]
WantedBy=multi-user.target"

echo "[$(date +'%Y-%m-%d %H:%M:%S')] systemd 서비스 파일 생성/업데이트 중: /etc/systemd/system/${SERVICE_NAME}.service"
echo "${SERVICE_FILE_CONTENT}" | sudo tee /etc/systemd/system/${SERVICE_NAME}.service > /dev/null

sudo systemctl daemon-reload

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 새 서비스 시작 및 활성화 중: ${SERVICE_NAME}.service"
sudo systemctl start ${SERVICE_NAME}.service
sudo systemctl enable ${SERVICE_NAME}.service

# =========================================================================
# 4. 배포 후 검증 단계 (수정된 부분)
# =========================================================================

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 서비스 헬스 체크 대기 중..."

HEALTH_CHECK_URL="http://localhost:8080/health"

TIMEOUT=180 # 최대 180초 (3분) 대기 (애플리케이션 시작 시간에 따라 조정)
INTERVAL=5  # 5초마다 확인

for i in $(seq 1 $(($TIMEOUT / $INTERVAL))); do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_CHECK_URL)

    # HTTP 상태 코드가 200 (OK)인지 확인
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] 서비스가 성공적으로 시작되었습니다. HTTP 상태 코드: ${HTTP_CODE}"
        break # 성공 시 루프 종료
    else
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] 서비스 시작 대기 중... HTTP 상태 코드: ${HTTP_CODE} (현재 ${i}회 시도, 총 $((i * INTERVAL))/${TIMEOUT}초 경과)"
        sleep $INTERVAL # 다음 시도까지 대기
    fi

    # 타임아웃 검사
    if [ $i -eq $(($TIMEOUT / $INTERVAL)) ]; then
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] 오류: 서비스 시작 타임아웃! ${HEALTH_CHECK_URL} 응답 없음 또는 비정상."
        # 추가 디버깅 정보 출력
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] systemd 서비스 로그 확인:"
        journalctl -u ${SERVICE_NAME}.service --no-pager -n 50 # 최근 50줄 로그 출력
        exit 1 # 스크립트 실패
    fi
done

# 서비스가 active 상태인지 최종 확인
SERVICE_STATUS=$(sudo systemctl is-active ${SERVICE_NAME}.service)

if [ "$SERVICE_STATUS" = "active" ]; then
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 최종 배포 성공! ${SERVICE_NAME}.service가 활성화되었습니다."
    exit 0 # 성공 시 스크립트 종료
else
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 최종 배포 실패! ${SERVICE_NAME}.service가 활성화되지 않았습니다."
    echo "자세한 내용은 로그 파일 (${LOG_DIR}/stdout.log, ${LOG_DIR}/stderr.log) 및 'journalctl -u ${SERVICE_NAME}.service --no-pager' 를 확인하세요."
    exit 1 # 실패 시 스크립트 종료
fi