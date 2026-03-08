# VocalDoc 运维文档

## 1. 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                      Nginx (Reverse Proxy)              │
│                   Port: 80 / 443                       │
└──────────────────────────┬──────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│  Backend API  │  │ Analysis API  │  │   Frontend   │
│   Port:8080   │  │   Port:8000   │  │   Port:80    │
└───────┬───────┘  └───────┬───────┘  └───────────────┘
        │                  │
        └────────┬─────────┘
                 │
    ┌────────────┼────────────┐
    │            │            │
    ▼            ▼            ▼
┌────────┐  ┌────────┐  ┌────────┐
│ MySQL  │  │ Redis  │  │  OSS   │
│ :3306  │  │ :6379  │  │ Cloud  │
└────────┘  └────────┘  └────────┘
```

## 2. 服务部署

### 2.1 环境要求
- Ubuntu 20.04+ / CentOS 8+
- Docker 20.10+
- Docker Compose 2.0+
- 4核CPU / 8GB内存 / 100GB硬盘

### 2.2 部署步骤
```bash
# 1. 克隆项目
git clone https://github.com/your-org/vocaldoc.git
cd vocaldoc

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件

# 3. 启动服务
docker-compose -f docker-compose.prod.yml up -d

# 4. 查看日志
docker-compose -f docker-compose.prod.yml logs -f
```

### 2.3 环境变量
```env
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_PASSWORD=your_db_password
REDIS_PASSWORD=your_redis_password
ALIYUN_ACCESS_KEY=your_key
ALIYUN_ACCESS_SECRET=your_secret
ALIYUN_OSS_BUCKET=your_bucket
```

## 3. 服务管理

### 3.1 常用命令
```bash
# 启动所有服务
docker-compose -f docker-compose.prod.yml up -d

# 停止所有服务
docker-compose -f docker-compose.prod.yml down

# 重启单个服务
docker-compose -f docker-compose.prod.yml restart backend

# 查看服务状态
docker-compose -f docker-compose.prod.yml ps

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f [service]

# 进入容器
docker exec -it vocaldoc-backend /bin/bash
```

### 3.2 健康检查
```bash
# Backend API
curl http://localhost:8080/health

# Analysis API
curl http://localhost:8000/health

# Frontend
curl http://localhost/
```

## 4. 备份与恢复

### 4.1 数据库备份
```bash
# 备份MySQL
docker exec vocaldoc-mysql mysqldump -u root -p vocaldoc > backup_$(date +%Y%m%d).sql

# 备份Redis
docker exec vocaldoc-redis redis-cli SAVE
```

### 4.2 数据恢复
```bash
# 恢复MySQL
docker exec -i vocaldoc-mysql mysql -u root -p vocaldoc < backup_20240308.sql
```

## 5. 监控

### 5.1 日志
- 应用日志: `/var/log/vocaldoc/`
- Nginx日志: 容器内 `/var/log/nginx/`

### 5.2 监控指标
- CPU使用率
- 内存使用率
- 磁盘使用率
- 网络流量
- 请求响应时间

## 6. 安全配置

### 6.1 防火墙
```bash
# 开放必要端口
ufw allow 22    # SSH
ufw allow 80    # HTTP
ufw allow 443   # HTTPS
```

### 6.2 SSL证书
使用Let's Encrypt免费证书:
```bash
certbot --nginx -d yourdomain.com
```

## 7. 故障排查

### 7.1 服务无法启动
```bash
# 检查Docker状态
docker status

# 检查端口占用
netstat -tulpn | grep PORT

# 查看详细日志
docker-compose -f docker-compose.prod.yml logs service_name
```

### 7.2 数据库连接失败
```bash
# 检查MySQL容器
docker exec vocaldoc-mysql mysqladmin ping

# 检查网络连接
docker exec vocaldoc-backend ping mysql
```

### 7.3 性能问题
```bash
# 查看资源使用
docker stats

# 查看进程
docker top vocaldoc-backend
```

## 8. 更新升级

```bash
# 拉取最新代码
git pull origin main

# 重新构建镜像
docker-compose -f docker-compose.prod.yml build

# 重启服务
docker-compose -f docker-compose.prod.yml up -d
```
