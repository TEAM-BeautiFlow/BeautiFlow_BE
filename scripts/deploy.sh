#!/bin/bash

SERVER_TYPE=$1 # 첫 번째 인자: api, chat, gateway 중 하나
PROFILE=$2     # 두 번째 인자: prod (또는 dev, staging 등)

# 인자 유효성 검사
if [ -z "$SERVER_TYPE" ] || [ -z "$PROFILE" ]; then
    echo "오류: 서버 타입 또는 프로파일이 누락되었습니다."
    echo "사용법: ./deploy.sh [server_type] [profile]"
    echo "예시: ./deploy.sh api prod"
    exit 1
fi

TAR_NAME="beautiflow.tar.gz"

COMMON_JAR_NAME="beautiflow-0.0.1-SNAPSHOT.jar"

SERVICE_NAME="beautiflow-${SERVER_TYPE}" # 예: beautiflow-api, beautiflow-chat, beautiflow-gateway

# 배포 디렉토리 (EC2 인스턴스 내에서 앱이 실행될 경로)
DEPLOY_DIR="/opt/${SERVICE_NAME}"
LOG_DIR="/var/log/${SERVICE_NAME}" # 서비스별 로그 파일 저장 경로

echo "[$(date +'%Y-%m-%d %H:%M:%S')] 배포 시작: ${SERVER_TYPE} 서버 (${PROFILE} 프로파일)"
echo "서비스 이름: ${SERVICE_NAME}"
echo "JAR 파일 (압축 내부): ${COMMON_JAR_NAME}"
echo "배포 디렉토리: ${DEPLOY_DIR}"
echo "로그 디렉토리: ${LOG_DIR}"

# 배포 디렉토리 생성 및 권한 설정
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 배포 디렉토리 (${DEPLOY_DIR}) 생성 및 권한 설정 중..."
sudo mkdir -p ${DEPLOY_DIR}
sudo chown -R ubuntu:ubuntu ${DEPLOY_DIR} # ubuntu 사용자가 파일 소유권 가짐
sudo chmod -R 755 ${DEPLOY_DIR} # 읽기/쓰기/실행 권한 설정

# 로그 디렉토리 생성 및 권한 설정
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 로그 디렉토리 (${LOG_DIR}) 생성 및 권한 설정 중..."
sudo mkdir -p ${LOG_DIR}
sudo chown -R ubuntu:ubuntu ${LOG_DIR}
sudo chmod -R 755 ${LOG_DIR}

# 기존 JAR 파일 및 압축 해제된 파일 정리
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 기존 배포 파일 정리 중..."
rm -rf ${DEPLOY_DIR}/* # 배포 디렉토리 안의 모든 파일 삭제

# /home/ubuntu/ 에 복사된 .tar.gz 파일 압축 해제
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 압축 파일 해제 중: /home/ubuntu/${TAR_NAME} -> ${DEPLOY_DIR}/"
# 압축 해제 시, .tar.gz 파일 안에 있는 JAR 파일이 ${DEPLOY_DIR} 바로 아래에 놓이도록 합니다.
# 만약 tarball 내부에 불필요한 상위 폴더 (예: 'dist/')가 있다면 '--strip-components=1' 옵션 사용을 고려하세요.
tar -xzf /home/ubuntu/${TAR_NAME} -C ${DEPLOY_DIR}/
# tar -xzf /home/ubuntu/${TAR_NAME} --strip-components=1 -C ${DEPLOY_DIR}/ # <-- 불필요한 상위 폴더 제거용 예시

# =========================================================================
# 3. 서비스 관리 단계
# =========================================================================

# 기존 서비스 중지
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 기존 서비스 중지 중: ${SERVICE_NAME}.service"
sudo systemctl stop ${SERVICE_NAME}.service || true # 서비스가 없거나 실행 중이 아니어도 오류 없이 진행
sleep 5 # 서비스가 완전히 종료될 시간을 줌

# systemd 서비스 파일 생성 또는 업데이트
# 이 서비스 파일은 EC2 인스턴스에 Java가 설치되어 있고,
# 애플리케이션이 실행될 때 필요한 환경 변수들이 EC2 환경에 설정되어 있음을 가정합니다.
# (예: /etc/environment 파일, 또는 Secrets Manager/Parameter Store를 통한 런타임 로드)
SERVICE_FILE_CONTENT="[Unit]
Description=${SERVICE_NAME} Spring Boot Application
After=syslog.target network.target

[Service]
ExecStart=/usr/bin/java -jar ${DEPLOY_DIR}/${COMMON_JAR_NAME} --spring.profiles.active=${PROFILE},${SERVER_TYPE}
User=ubuntu
SuccessExitStatus=143 # SIGTERM (143) 종료 코드를 성공으로 간주 (Spring Boot graceful shutdown)
StandardOutput=file:${LOG_DIR}/stdout.log # 표준 출력을 서비스별 로그 파일로 리디렉션
StandardError=file:${LOG_DIR}/stderr.log  # 표준 에러를 서비스별 로그 파일로 리디렉션
SyslogIdentifier=${SERVICE_NAME}
Restart=always
RestartSec=10s # 서비스 실패 시 10초 후 재시작

[Install]
WantedBy=multi-user.target"

echo "[$(date +'%Y-%m-%d %H:%M:%S')] systemd 서비스 파일 생성/업데이트 중: /etc/systemd/system/${SERVICE_NAME}.service"
echo "${SERVICE_FILE_CONTENT}" | sudo tee /etc/systemd/system/${SERVICE_NAME}.service > /dev/null

# systemd 데몬 리로드 (서비스 파일 변경 시 필수)
sudo systemctl daemon-reload

# 새로운 서비스 시작 및 활성화
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 새 서비스 시작 및 활성화 중: ${SERVICE_NAME}.service"
sudo systemctl start ${SERVICE_NAME}.service
sudo systemctl enable ${SERVICE_NAME}.service # 부팅 시 자동 시작 활성화

# =========================================================================
# 4. 배포 후 검증 단계
# =========================================================================

# 서비스 시작 후 약간의 시간을 기다린 후 상태 확인
echo "[$(date +'%Y-%m-%d %H:%M:%S')] 서비스 상태 확인 중..."
sleep 15 # 서비스가 완전히 시작될 충분한 시간을 줍니다. (필요시 조정)
SERVICE_STATUS=$(sudo systemctl is-active ${SERVICE_NAME}.service)

if [ "$SERVICE_STATUS" = "active" ]; then
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 배포 성공! ${SERVICE_NAME}.service가 활성화되었습니다."
    exit 0 # 성공 시 스크립트 종료
else
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] 배포 실패! ${SERVICE_NAME}.service가 활성화되지 않았습니다."
    echo "자세한 내용은 로그 파일 (${LOG_DIR}/stdout.log, ${LOG_DIR}/stderr.log) 및 'journalctl -u ${SERVICE_NAME}.service --no-pager' 를 확인하세요."
    exit 1 # 실패 시 스크립트 종료 (GitHub Actions Job도 실패로 처리됨)
fi