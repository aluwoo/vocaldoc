# API接口详细文档

## 接口规范

### 通用说明

| 项目 | 说明 |
|------|------|
| 基础URL | `https://api.vocaldoc.com/v1` |
| 认证方式 | Bearer Token |
| 请求格式 | JSON |
| 响应格式 | JSON |
| 编码 | UTF-8 |

### 通用响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1709875200
}
```

### 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 422 | 业务逻辑错误 |
| 500 | 服务器内部错误 |

---

## 一、认证模块

### 1.1 发送验证码

```
POST /auth/sms/send
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号 |
| type | string | 是 | 类型: LOGIN/REGISTER |

**请求示例:**
```json
{
  "phone": "13800138000",
  "type": "LOGIN"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": {
    "expiredIn": 300
  }
}
```

### 1.2 手机号验证码登录

```
POST /auth/login
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号 |
| code | string | 是 | 验证码 |

**响应示例:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "phone": "13800138000",
      "name": "张三",
      "avatar": "https://...",
      "role": "TEACHER"
    }
  }
}
```

### 1.3 刷新Token

```
POST /auth/refresh
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | string | 是 | 刷新Token |

### 1.4 登出

```
POST /auth/logout
```

---

## 二、用户模块

### 2.1 获取当前用户信息

```
GET /user/me
```

**响应示例:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "phone": "13800138000",
    "name": "张三",
    "avatar": "https://...",
    "role": "TEACHER",
    "profile": {
      "title": "声乐教授",
      "specialty": "美声唱法",
      "studentCount": 25,
      "inviteCode": "TEA123456"
    }
  }
}
```

### 2.2 更新用户信息

```
PUT /user/me
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 否 | 姓名 |
| avatar | string | 否 | 头像URL |

---

## 三、教师模块

### 3.1 获取教师信息

```
GET /teachers/{id}
```

### 3.2 获取我的学生列表

```
GET /teachers/{id}/students
```

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认20 |
| keyword | string | 否 | 搜索姓名/手机号 |
| status | int | 否 | 状态筛选 |

**响应示例:**
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "name": "李四",
        "phone": "13900139000",
        "avatar": "https://...",
        "voiceType": "SOPRANO",
        "level": "中级",
        "recordCount": 15,
        "joinedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 25,
    "page": 1,
    "pageSize": 20
  }
}
```

### 3.3 邀请学生加入

```
POST /teachers/{id}/students/invite
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| studentPhone | string | 是 | 学生手机号 |
| studentName | string | 是 | 学生姓名 |

### 3.4 学生自主通过邀请码加入

```
POST /students/join
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| inviteCode | string | 是 | 教师邀请码 |

---

## 四、学生模块

### 4.1 获取学生档案

```
GET /students/{id}
```

**响应示例:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "李四",
    "phone": "13900139000",
    "avatar": "https://...",
    "age": 20,
    "gender": "FEMALE",
    "voiceType": "SOPRANO",
    "level": "中级",
    "teacher": {
      "id": 1,
      "name": "张老师"
    },
    "recordCount": 15,
    "avgScore": 85.5,
    "trend": "improving"
  }
}
```

### 4.2 获取学生嗓音档案列表

```
GET /students/{id}/records
```

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期 |
| songName | string | 否 | 曲目名称 |

---

## 五、嗓音档案模块

### 5.1 创建嗓音档案(上传音频)

```
POST /records
```

**请求参数 (multipart/form-data):**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 音频文件 |
| studentId | int | 是 | 学生ID |
| recordCategory | string | 是 | **VOWEL(元音录音) / DURATION(声时长测试) / SONG(完整曲目)** |
| vowelType | string | 否 | 元音类型: A/I/O/U/E (VOWEL时必填) |
| title | string | 是 | 档案标题 |
| songName | string | 否 | 曲目名称 (SONG时必填) |
| recordType | string | 否 | LESSON/PRACTICE/EXAM |
| recordDevice | string | 否 | PHONE/PROFESSIONAL/UPLOAD |
| recordLocation | string | 否 | 录音地点 |
| recordAt | string | 否 | 录音时间 |

**响应示例 (元音录音):**
```json
{
  "code": 200,
  "message": "元音录音创建成功，已开始分析",
  "data": {
    "id": 1,
    "recordNo": "VD202403080001",
    "recordCategory": "VOWEL",
    "vowelType": "A",
    "status": "ANALYZING"
  }
}
```

**响应示例 (完整曲目):**
```json
{
  "code": 200,
  "message": "曲目录音创建成功",
  "data": {
    "id": 2,
    "recordNo": "VD202403080002",
    "recordCategory": "SONG",
    "status": "PENDING_REVIEW"
  }
}
```

### 5.2 获取嗓音档案详情

```
GET /records/{id}
```

**响应示例:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "recordNo": "VD202403080001",
    "student": {
      "id": 1,
      "name": "李四"
    },
    "teacher": {
      "id": 1,
      "name": "张老师"
    },
    "title": "《我的太阳》演唱",
    "songName": "我的太阳",
    "audioUrl": "https://...",
    "audioDuration": 180,
    "recordAt": "2024-03-08T10:00:00Z",
    "status": "COMPLETED",
    "teacherComment": "音准很好，气息需要加强",
    "teacherRating": 4,
    "teacherTags": ["音准好", "气息需加强"],
    "analysis": {
      "id": 1,
      "meanF0": 280.5,
      "minF0": 220.0,
      "maxF0": 520.0,
      "f1Mean": 800.0,
      "f2Mean": 1200.0,
      "jitterLocal": 0.8,
      "shimmerLocal": 2.5,
      "hnr": 15.2,
      "overallScore": 85.0,
      "aiSuggestion": "建议加强气息训练..."
    }
  }
}
```

### 5.3 获取嗓音档案列表

```
GET /records
```

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| studentId | int | 否 | 学生ID |
| teacherId | int | 否 | 教师ID |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期 |
| status | string | 否 | 状态筛选 |

### 5.4 更新教师评价

```
PUT /records/{id}/comment
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| comment | string | 是 | 评语 |
| rating | int | 是 | 星级(1-5) |
| tags | array | 否 | 标签数组 |

### 5.5 删除嗓音档案

```
DELETE /records/{id}
```

---

## 六、语音分析模块

### 6.1 获取分析结果

```
GET /analysis/{recordId}
```

### 6.2 手动触发重新分析

```
POST /analysis/{recordId}/reanalyze
```

### 6.3 获取分析图表数据

```
GET /analysis/{recordId}/charts
```

**响应示例:**
```json
{
  "code": 200,
  "data": {
    "pitchContour": {
      "type": "line",
      "data": [
        [0, 220], [0.1, 250], [0.2, 280], ...
      ]
    },
    "intensityContour": {
      "type": "line", 
      "data": [
        [0, 40], [0.1, 55], [0.2, 60], ...
      ]
    },
    "spectrogram": {
      "type": "heatmap",
      "data": "base64..."
    }
  }
}
```

---

## 七、历史对比模块

### 7.1 两次档案对比

```
POST /comparison/two
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| recordId1 | int | 是 | 档案1 ID |
| recordId2 | int | 是 | 档案2 ID |

**响应示例:**
```json
{
  "code": 200,
  "data": {
    "record1": {
      "id": 1,
      "recordAt": "2024-01-01",
      "meanF0": 280.0,
      "jitterLocal": 0.8,
      "overallScore": 80.0
    },
    "record2": {
      "id": 2,
      "recordAt": "2024-03-01",
      "meanF0": 290.0,
      "jitterLocal": 0.6,
      "overallScore": 85.0
    },
    "comparison": {
      "meanF0": "+10 Hz (+3.6%)",
      "jitterLocal": "-0.2% (改善)",
      "overallScore": "+5 (改善)",
      "trend": "IMPROVING"
    }
  }
}
```

### 7.2 趋势分析

```
GET /trend/{studentId}
```

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| metric | string | 否 | 指标: F0/JITTER/SHIMMER/SCORE |
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期 |

**响应示例:**
```json
{
  "code": 200,
  "data": {
    "metric": "SCORE",
    "points": [
      {"date": "2024-01", "value": 75},
      {"date": "2024-02", "value": 78},
      {"date": "2024-03", "value": 85}
    ],
    "trend": "IMPROVING",
    "changeRate": "+13.3%",
    "prediction": "预计下月达到90分"
  }
}
```

---

## 八、管理后台模块

### 8.1 管理员-教师管理

```
GET /admin/teachers
```

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| keyword | string | 否 | 搜索 |

### 8.2 管理员-创建教师

```
POST /admin/teachers
```

**请求参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号 |
| name | string | 是 | 姓名 |
| title | string | 否 | 职称 |

### 8.3 数据统计

```
GET /admin/statistics
```

**响应示例:**
```json
{
  "code": 200,
  "data": {
    "totalUsers": 1000,
    "totalTeachers": 50,
    "totalStudents": 950,
    "totalRecords": 5000,
    "totalDuration": 900000,
    "dailyActive": 200,
    "weeklyTrend": [180, 190, 200, 210, 195, 185, 200]
  }
}
```

---

**文档版本**: 1.0  
**创建日期**: 2026-03-08
