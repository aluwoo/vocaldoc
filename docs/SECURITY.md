# 安全配置说明

## 需要的Spring Security依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## 安全配置要点

### 1. 认证与授权
- JWT Token认证
- 基于角色的访问控制 (RBAC)
- 密码加密存储 (BCrypt)

### 2. 接口安全
- 公开接口: /api/v1/auth/**, /api/v1/sms/**, /api/v1/analysis/**
- 需要认证: 其他接口
- 管理员接口: /api/v1/admin/** 需ADMIN角色

### 3. 数据安全
- 用户敏感信息脱敏
- 音频文件访问权限控制
- SQL注入防护 (MyBatis参数化查询)
- XSS防护

### 4. 传输安全
- HTTPS强制使用
- CORS配置
- CSRF防护

### 5. 审计日志
- 记录关键操作
- 登录日志
- 数据访问日志
