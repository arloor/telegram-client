mvn clean install

host=someme.me
port=22

scp /c/Users/arloor/.m2/repository/com/arloor/telegram-client/1.0-SNAPSHOT/telegram-client-1.0-SNAPSHOT-jar-with-dependencies.jar root@${host}:/opt/tg/
ssh root@$host  -p$port "
mkdir /opt/tg
cat > /lib/systemd/system/tg.service <<EOF
[Unit]
Description=Telegram 机器人
After=network-online.target
Wants=network-online.target

[Service]
WorkingDirectory=/opt/tg
ExecStart=/usr/bin/java -Djava.library.path=/usr/local/bin -jar  /opt/tg/tdlib-use-1.0-SNAPSHOT-jar-with-dependencies.jar -c /opt/tg/telegram.properties
LimitNOFILE=100000
Restart=always
RestartSec=60

[Install]
WantedBy=multi-user.target
EOF

cat > /opt/tg/telegram.properties <<EOF
apiId=861784
apiHash=dbaf939227b6ff24f0a0521e329c91e6
author=878823128
# 测试群组
#mygroup=-1001248492840
# arloor博客
mygroup=-1001334979774,-1001446471776
EOF
systemctl daemon-reload
systemctl enable tg
systemctl stop tg
systemctl start tg
systemctl status tg
"
