# Lightnode 服务器部署指南

## 1. 购买服务器

### 1.1 访问 Lightnode
访问 https://www.lightnode.com/ 注册账号

### 1.2 创建实例
1. 选择数据中心 (推荐: 香港/新加坡)
2. 选择配置 (最低建议):
   - CPU: 4核
   - RAM: 8GB
   - 硬盘: 100GB SSD
   - 带宽: 1Gbps
   - IP: 1个独立IP
3. 选择操作系统: Ubuntu 20.04 LTS 或 CentOS 8

### 1.3 获取登录信息
创建后获取:
- IP地址
- SSH端口 (默认22)
- root密码

## 2. 服务器初始化

### 2.1 SSH连接
```bash
ssh root@你的服务器IP
```

### 2.2 更新系统
```bash
# Ubuntu
apt update && apt upgrade -y

# CentOS
yum update -y
```

### 2.3 安装Docker
```bash
# Ubuntu
curl -fsSL https://get.docker.com | sh
systemctl enable docker
systemctl start docker

# 或使用一键安装
curl -sSL https://get.docker.com/ | CHANNEL=stable sh
```

### 2.4 安装Docker Compose
```bash
curl -L "https://github.com/docker/compose/releases/download/v2.23.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```

### 2.5 配置防火墙
```bash
# Ubuntu (UFW)
ufw allow 22    # SSH
ufw allow 80    # HTTP
ufw allow 443   # HTTPS
ufw enable

# CentOS (firewalld)
firewall-cmd --permanent --add-port=22/tcp
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --reload
```

## 3. 部署应用

### 3.1 克隆项目
```bash
cd /opt
git clone https://github.com/your-org/vocaldoc.git
cd vocaldoc
```

### 3.2 配置环境变量
```bash
cp .env.example .env
nano .env
```

配置内容:
```env
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_PASSWORD=your_db_password
REDIS_PASSWORD=your_redis_password
```

### 3.3 启动服务
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 3.4 检查状态
```bash
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f
```

## 4. 域名与SSL

### 4.1 域名解析
在域名服务商添加A记录:
- 记录类型: A
- 主机记录: @ 或 vocaldoc
- 记录值: 你的服务器IP

### 4.2 安装SSL证书
```bash
# 安装Certbot
apt install certbot python3-certbot-nginx -y

# 获取证书 (Ubuntu)
certbot --nginx -d yourdomain.com -d www.yourdomain.com

# 获取证书 (CentOS)
certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

### 4.3 自动续期
```bash
certbot renew --dry-run
```

## 5. 性能优化

### 5.1 Docker性能设置
```bash
# 编辑Docker配置
nano /etc/docker/daemon.json

{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}

systemctl restart docker
```

### 5.2 MySQL优化
```bash
# 进入MySQL容器
docker exec -it vocaldoc-mysql mysql -u root -p

# 查看状态
SHOW STATUS;
SHOW VARIABLES;
```

### 5.3 Redis优化
```bash
# 编辑Redis配置
docker exec vocaldoc-redis redis-cli CONFIG SET maxmemory 2gb
docker exec vocaldoc-redis redis-cli CONFIG SET maxmemory-policy allkeys-lru
```

## 6. 监控与告警

### 6.1 安装监控
```bash
# 安装Netdata (可选)
bash <(curl -Ss https://my-netdata.io/kickstart.sh)
```

### 6.2 日志管理
```bash
# 查看所有容器日志
docker-compose -f docker-compose.prod.yml logs -f --tail=100

# 查看特定服务
docker logs -f vocaldoc-backend
```

## 7. 备份策略

### 7.1 自动备份脚本
```bash
mkdir -p /opt/backup
nano /opt/backup.sh
```

```bash
#!/bin/bash
DATE=$(date +%Y%m%d)
BACKUP_DIR="/opt/backup"

# 备份数据库
docker exec vocaldoc-mysql mysqldump -u root -p$MYSQL_PASSWORD vocaldoc > $BACKUP_DIR/db_$DATE.sql

# 备份OSS (如有必要)

# 清理7天前的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
```

### 7.2 设置定时任务
```bash
crontab -e
# 每天凌晨3点执行备份
0 3 * * * /opt/backup.sh
```

## 8. 故障排查

### 8.1 端口检查
```bash
netstat -tulpn | grep LISTEN
```

### 8.2 资源使用
```bash
docker stats
```

### 8.3 网络诊断
```bash
# 测试外部连接
ping 8.8.8.8

# DNS测试
nslookup google.com

# 端口测试
telnet yourdomain.com 80
```

## 9. 常见问题

### Q: Docker启动失败?
A: 检查Docker版本 `docker --version`，确保使用最新版本

### Q: 端口被占用?
A: `lsof -i :80` 查找占用端口的进程

### Q: 数据库连接失败?
A: 检查MySQL容器日志 `docker logs vocaldoc-mysql`

### Q: 需要更多带宽?
A: Lightnode支持随时升级配置，在控制台操作即可
