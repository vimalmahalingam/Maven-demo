#!/bin/bash
set -e

echo "=============================="
echo " SonarQube Installer Script"
echo "=============================="

# Variables
SONAR_VERSION="25.11.0.114957"
SONAR_ZIP="sonarqube-$SONAR_VERSION.zip"
SONAR_URL="https://binaries.sonarsource.com/Distribution/sonarqube/$SONAR_ZIP"
SONAR_DIR="/opt/sonarqube"

DB_NAME="sonarqube"
DB_USER="sonaruser"
DB_PASS="StrongPassword123!"   # <-- Change this

# Step 1: Update system
echo "[1/12] Updating system..."
apt update && apt upgrade -y

# Step 2: Install required packages
echo "[2/12] Installing required packages..."
apt install -y wget unzip curl git apt-transport-https

# Step 3: Install Java
echo "[3/12] Installing Java 17..."
apt install -y openjdk-17-jdk
java -version

# Step 4: Install PostgreSQL
echo "[4/12] Installing PostgreSQL..."
apt install -y postgresql postgresql-contrib

# Step 5: Configure PostgreSQL
echo "[5/12] Configuring PostgreSQL..."
sudo -u postgres psql -c "CREATE DATABASE $DB_NAME;"
sudo -u postgres psql -c "CREATE USER $DB_USER WITH ENCRYPTED PASSWORD '$DB_PASS';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;"

# Step 6: Download SonarQube
echo "[6/12] Downloading SonarQube..."
wget $SONAR_URL -P /tmp
unzip /tmp/$SONAR_ZIP -d /opt
mv /opt/sonarqube-$SONAR_VERSION $SONAR_DIR

# Step 7: Create SonarQube user
echo "[7/12] Creating SonarQube user..."
useradd -r -s /bin/false sonarqube
chown -R sonarqube:sonarqube $SONAR_DIR

# Step 8: Configure systemd service
echo "[8/12] Configuring SonarQube service..."
cat <<EOF > /etc/systemd/system/sonarqube.service
[Unit]
Description=SonarQube service
After=syslog.target network.target

[Service]
Type=forking
ExecStart=$SONAR_DIR/bin/linux-x86-64/sonar.sh start
ExecStop=$SONAR_DIR/bin/linux-x86-64/sonar.sh stop
User=sonarqube
Group=sonarqube
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# Step 9: Reload systemd
echo "[9/12] Reloading systemd..."
systemctl daemon-reload

# Step 10: Enable SonarQube
echo "[10/12] Enabling SonarQube..."
systemctl enable sonarqube

# Step 11: Start SonarQube
echo "[11/12] Starting SonarQube..."
systemctl start sonarqube

# Step 12: Verify status
echo "[12/12] Checking SonarQube status..."
systemctl status sonarqube