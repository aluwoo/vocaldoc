# AGENTS.md - 声乐课堂嗓音档案管理系统开发规范

本文档为开发人员提供编码规范和构建命令指南。

---

## 1. 构建与运行命令

### 1.1 后端 (Java Spring Boot)

```bash
# 构建项目
mvn clean package

# 运行开发服务器 (端口8080)
mvn spring-boot:run

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=UserServiceTest

# 运行单个测试方法
mvn test -Dtest=UserServiceTest#testLogin

# 代码格式化 (spotless)
mvn spotless:apply

# 代码检查
mvn checkstyle:check

# 跳过测试构建
mvn clean package -DskipTests
```

### 1.2 前端 (Vue 3 + Vite)

```bash
# 安装依赖
npm install

# 开发服务器 (端口5173)
npm run dev

# 构建生产版本
npm run build

# 代码检查
npm run lint

# 代码格式化并修复
npm run lint -- --fix

# 运行单元测试
npm run test:unit

# 运行单个测试文件
npm run test:unit -- UserService.test.js

# 运行测试并监听
npm run test:unit -- --watch

# 预览生产构建
npm run preview
```

### 1.3 数据库

```bash
# 运行MySQL容器
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root123 -e MYSQL_DATABASE=vocaldoc mysql:8

# 运行Redis容器
docker run -d -p 6379:6379 redis:7
```

---

## 2. 代码风格指南

### 2.1 Java 后端规范

#### 2.1.1 命名约定

| 类型 | 规则 | 示例 |
|------|------|------|
| 类/接口 | PascalCase | `UserService`, `VoiceRecord` |
| 方法/变量 | camelCase | `getUserById`, `recordAudio` |
| 常量 | UPPER_SNAKE_CASE | `MAX_FILE_SIZE`, `DEFAULT_PAGE_SIZE` |
| 包名 | lowercase | `com.vocaldoc.user` |
| 枚举 | PascalCase | `UserRole.TEACHER` |

#### 2.1.2 Import 排序 (IDEA/Eclipse标准)

```java
// 1. Java/JDK internal
import java.util.*;
import java.io.*;

// 2. javax
import javax.servlet.*;

// 3. Third-party (alphabetical)
import com.alibaba.fastjson.*;
import org.apache.commons.*;

// 4. Project internal
import com.vocaldoc.model.*;
import com.vocaldoc.service.*;
```

#### 2.1.3 类结构顺序

```java
public class UserService {
    // 1. 常量 (static final)
    // 2. 成员变量
    // 3. 构造方法
    // 4. 静态方法
    // 5. 公共方法
    // 6. 私有方法
    // 7. getter/setter (如果有必要)
}
```

#### 2.1.4 方法规范

```java
// ✅ 推荐：单一职责，方法名清晰，参数适量
public User getUserById(Long id) {
    if (id == null) {
        throw new IllegalArgumentException("ID不能为空");
    }
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("用户不存在: " + id));
}

// ❌ 避免：方法过长，职责过多
public User complexMethod(Long id, String name, Integer age, String email, boolean flag) {
    // 200行代码...
}
```

#### 2.1.5 错误处理

```java
// ✅ 推荐：统一异常处理，返回统一响应
@ExceptionHandler(BusinessException.class)
public ResponseEntity<?> handleBusinessException(BusinessException e) {
    return ResponseEntity
        .status(e.getCode())
        .body(Result.fail(e.getMessage()));
}

// 业务异常定义
public class BusinessException extends RuntimeException {
    private final int code;
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

// 全局统一响应
public class Result<T> {
    private int code;
    private String message;
    private T data;
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "成功", data);
    }
    
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }
}
```

#### 2.1.6 日志规范

```java
// ✅ 推荐：使用占位符，避免字符串拼接
log.info("用户登录成功: userId={}, ip={}", userId, ip);

// ❌ 避免：字符串拼接
log.info("用户登录成功: " + userId + ", ip: " + ip);

// 不同级别使用
log.debug("调试信息: {}", detail);
log.info("业务流程: {}", action);
log.warn("警告: {}", warning);
log.error("异常: {}", e.getMessage(), e);
```

#### 2.1.7 VO/DTO/Entity 分离

```
model/
├── entity/          # 数据库实体
│   └── User.java
├── dto/             # 数据传输对象
│   ├── UserLoginDTO.java
│   └── UserRegisterDTO.java
├── vo/              # 视图对象（返回给前端）
│   ├── UserVO.java
│   └── VoiceRecordVO.java
└── converter/       # 转换器
    └── UserConverter.java
```

---

### 2.2 前端 (Vue 3) 规范

#### 2.2.1 文件命名

| 类型 | 规则 | 示例 |
|------|------|------|
| 组件 | PascalCase | `UserProfile.vue`, `VoiceRecorder.vue` |
| 工具函数 | camelCase | `formatDate.ts`, `audioUtils.ts` |
| API模块 | camelCase | `userApi.ts`, `voiceApi.ts` |
| 样式文件 | kebab-case | `button-styles.scss` |

#### 2.2.2 Import 排序 (ESLint rule: import/order)

```typescript
// 1. Vue/React internal
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 2. Third-party (alphabetical)
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 3. Project internal - absolute
import { useUserStore } from '@/store/user'
import { getUserInfo } from '@/api/user'

// 4. Project internal - relative
import VoiceChart from './components/VoiceChart.vue'
import './styles/main.scss'
```

#### 2.2.3 组件规范

```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup lang="ts">
// ✅ 推荐：使用 script setup
import { ref, computed, onMounted } from 'vue'
import type { User } from '@/types'

// 常量定义
const MAX_LENGTH = 100

// Ref 定义
const userName = ref<string>('')
const isLoading = ref(false)

// 计算属性
const displayName = computed(() => userName.value || '匿名用户')

// 方法
function handleSubmit() {
  isLoading.value = true
  // 业务逻辑
}

// 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* 样式内容 */
</style>
```

#### 2.2.4 API 调用规范

```typescript
// ✅ 推荐：统一封装，错误处理
import request from '@/utils/request'
import type { User, LoginParams } from '@/types'

export function login(data: LoginParams) {
  return request({
    url: '/api/auth/login',
    method: 'POST',
    data,
  })
}

export function getUserInfo() {
  return request<User>({
    url: '/api/user/info',
    method: 'GET',
  })
}
```

#### 2.2.5 状态管理 (Pinia)

```typescript
// ✅ 推荐：使用 Pinia，类型安全
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<User | null>(null)
  const token = ref('')
  
  function setUserInfo(info: User) {
    userInfo.value = info
  }
  
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  return { userInfo, token, setUserInfo, setToken }
})
```

#### 2.2.6 错误处理

```typescript
// ✅ 推荐：统一错误处理
import { ElMessage } from 'element-plus'

export function handleError(error: any) {
  const message = error?.response?.data?.message || error?.message || '网络错误'
  ElMessage.error(message)
}

// 使用
try {
  await fetchData()
} catch (error) {
  handleError(error)
}
```

---

## 3. Git 提交规范

### 3.1 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 3.2 Type 类型

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug修复 |
| docs | 文档变更 |
| style | 代码格式（不影响功能）|
| refactor | 重构 |
| test | 测试相关 |
| chore | 构建/工具变动 |

### 3.3 示例

```
feat(user): 添加用户注册短信验证码功能

- 新增短信验证码发送接口
- 验证码有效期5分钟
- 添加图形验证码防刷

Closes #123
```

---

## 4. 目录结构

### 4.1 后端结构

```
vocaldoc-backend/
├── src/main/java/com/vocaldoc/
│   ├── config/         # 配置类
│   ├── controller/    # 控制器
│   ├── service/       # 业务逻辑
│   ├── mapper/        # 数据访问
│   ├── entity/        # 实体类
│   ├── dto/           # 数据传输对象
│   ├── vo/            # 视图对象
│   ├── common/        # 公共组件
│   │   ├── exception/ # 异常定义
│   │   ├── result/    # 统一响应
│   │   └── constants/ # 常量
│   └── util/          # 工具类
├── src/main/resources/
│   ├── mapper/        # MyBatis XML
│   ├── application.yml
│   └── logback-spring.xml
└── src/test/java/     # 测试代码
```

### 4.2 前端结构

```
vocaldoc-web/
├── src/
│   ├── api/           # API 接口
│   ├── assets/       # 静态资源
│   ├── components/   # 公共组件
│   ├── composables/  # 组合式函数
│   ├── layouts/      # 布局组件
│   ├── router/       # 路由配置
│   ├── store/        # Pinia 状态
│   ├── styles/       # 全局样式
│   ├── types/        # TypeScript 类型
│   ├── utils/        # 工具函数
│   └── views/        # 页面组件
├── public/
├── index.html
└── vite.config.ts
```

### 4.3 语音分析服务结构

```
vocaldoc-analysis/
├── requirements.txt
├── config.py
├── service/
│   ├── voice_service.py
│   ├── pitch.py
│   ├── formants.py
│   └── intensity.py
├── api/routes.py
├── tests/
└── main.py
```

---

## 5. Python 语音分析服务规范 (基于Parselmouth)

### 5.1 项目结构

```
vocaldoc-analysis/
├── requirements.txt      # 依赖
├── config.py             # 配置
├── service/
│   ├── __init__.py
│   ├── voice_service.py  # 核心分析服务
│   ├── pitch.py          # 音高分析
│   ├── formants.py       # 共振峰分析
│   ├── intensity.py      # 音量分析
│   └── visualization.py  # 可视化
├── api/
│   └── routes.py         # FastAPI路由
├── tests/
│   └── test_voice.py    # 测试
└── main.py               # 入口
```

### 5.2 依赖安装

```bash
pip install praat-parselmouth
pip install numpy scipy
pip install fastapi uvicorn
pip install matplotlib seaborn  # 可选
```

### 5.3 录音样本规格 (必须遵守)

```python
# config.py
class AudioConfig:
    SAMPLING_RATE = 44100     # 采样率：44100Hz
    PITCH_FLOOR = 75          # 最低音高 (女低音)
    PITCH_CEILING = 800       # 最高音高 (男高音)
    TIME_STEP = 0.01          # 时间步长 (10ms)
    MAX_DURATION = 120        # 最大时长 (秒)
    
    FORMANT_CEILING = {
        'child': 8000,
        'female': 5000,
        'male': 5500
    }
    
    # 微扰分析参数
    JITTER_WINDOW = 0.0001    # 起点
    JITTER_LENGTH = 0.02      # 窗口长度
    JITTER_FILTER = 1.3       # 滤波器
    
    SUPPORTED_FORMATS = ['wav', 'flac', 'mp3', 'm4a']
```

### 5.4 完整分析代码 (含Jitter/Shimmer)

```python
# service/voice_service.py
from parselmouth import Sound
import numpy as np
from dataclasses import dataclass
from typing import List, Optional

@dataclass
class VoiceResult:
    """完整嗓音分析结果"""
    # 基础参数
    mean_f0: float           # 平均基频 (Hz)
    min_f0: float           # 最低基频
    max_f0: float           # 最高基频
    f0_range: float         # 音域范围
    
    # 共振峰
    f1_mean: float          # F1均值
    f2_mean: float          # F2均值
    f3_mean: float          # F3均值
    f4_mean: float          # F4均值
    
    # 音量
    mean_dB: float          # 平均响度
    max_dB: float          # 最大响度
    
    # 频率微扰 (Jitter)
    jitter_local: float     # Jitter (local) %
    jitter_rap: float      # Jitter (rap) %
    jitter_ppq5: float     # Jitter (ppq5) %
    jitter_ddp: float      # Jitter (ddp) %
    
    # 振幅微扰 (Shimmer)
    shimmer_local: float    # Shimmer (local) %
    shimmer_local_dB: float # Shimmer (local, dB)
    shimmer_apq3: float     # Shimmer (apq3) %
    shimmer_apq5: float     # Shimmer (apq5) %
    
    # 谐噪比
    hnr: float              # Harmonics-to-Noise Ratio (dB)
    
    # 原始数据 (可视化用)
    pitch_contour: List[float]
    intensity_contour: List[float]


class VoiceService:
    """基于Parselmouth的完整嗓音分析服务"""
    
    def __init__(self, voice_type: str = 'female'):
        self.voice_type = voice_type
        self.ceiling = {
            'child': 8000,
            'female': 5000,
            'male': 5500
        }.get(voice_type, 5000)
    
    def analyze(self, audio_path: str) -> VoiceResult:
        """完整分析"""
        sound = Sound(audio_path)
        
        # 1. 音高分析
        pitch_result = self._analyze_pitch(sound)
        
        # 2. 共振峰分析
        formant_result = self._analyze_formants(sound)
        
        # 3. 音量分析
        intensity_result = self._analyze_intensity(sound)
        
        # 4. 微扰分析 (Jitter/Shimmer)
        perturbation_result = self._analyze_perturbation(sound)
        
        # 5. 谐噪比
        hnr = self._analyze_hnr(sound)
        
        return VoiceResult(
            mean_f0=pitch_result['mean'],
            min_f0=pitch_result['min'],
            max_f0=pitch_result['max'],
            f0_range=pitch_result['range'],
            f1_mean=formant_result['f1'],
            f2_mean=formant_result['f2'],
            f3_mean=formant_result['f3'],
            f4_mean=formant_result['f4'],
            mean_dB=intensity_result['mean'],
            max_dB=intensity_result['max'],
            jitter_local=perturbation_result['jitter_local'],
            jitter_rap=perturbation_result['jitter_rap'],
            jitter_ppq5=perturbation_result['jitter_ppq5'],
            jitter_ddp=perturbation_result['jitter_ddp'],
            shimmer_local=perturbation_result['shimmer_local'],
            shimmer_local_dB=perturbation_result['shimmer_dB'],
            shimmer_apq3=perturbation_result['shimmer_apq3'],
            shimmer_apq5=perturbation_result['shimmer_apq5'],
            hnr=hnr,
            pitch_contour=pitch_result['contour'],
            intensity_contour=intensity_result['contour']
        )
    
    def _analyze_pitch(self, sound: Sound) -> dict:
        """音高分析"""
        pitch = sound.to_pitch(
            time_step=0.01,
            pitch_floor=75,
            pitch_ceiling=800
        )
        
        f0_values = pitch.selected_array['frequency']
        f0_values = f0_values[f0_values > 0]
        
        if len(f0_values) == 0:
            return {'mean': 0, 'min': 0, 'max': 0, 'range': 0, 'contour': []}
        
        return {
            'mean': float(np.mean(f0_values)),
            'min': float(np.min(f0_values)),
            'max': float(np.max(f0_values)),
            'range': float(np.max(f0_values) - np.min(f0_values)),
            'contour': f0_values.tolist()
        }
    
    def _analyze_formants(self, sound: Sound) -> dict:
        """共振峰分析"""
        formants = sound.to_formant_burg(
            time_step=0.01,
            maximum_formant=self.ceiling
        )
        
        t = formants.xs()
        results = {}
        
        for i in range(1, 5):
            f_values = [
                formants.get_value_at_time(i, time)
                for time in t
                if formants.get_value_at_time(i, time) > 0
            ]
            results[f'f{i}'] = float(np.mean(f_values)) if f_values else 0.0
        
        return results
    
    def _analyze_intensity(self, sound: Sound) -> dict:
        """音量分析"""
        intensity = sound.to_intensity(time_step=0.01)
        
        values = intensity.values.T[0]
        values = values[np.isfinite(values)]
        
        return {
            'mean': float(np.mean(values)),
            'max': float(np.max(values)),
            'contour': values.tolist()
        }
    
    def _analyze_perturbation(self, sound: Sound) -> dict:
        """微扰分析 - Jitter & Shimmer"""
        # 创建点过程
        point_process = sound.to_point_process(
            "periodic",
            "cc",
            75,  # min pitch
            500  # max pitch
        )
        
        # 使用Praat函数计算Jitter
        jitter_local = sound.get_jitter(
            0.0001, 0.02, 0.02, 1.3
        )
        jitter_rap = sound.get_jitter_rap(0.0001, 0.02, 0.02, 1.3)
        jitter_ppq5 = sound.get_jitter_ppq5(0.0001, 0.02, 0.02, 1.3)
        jitter_ddp = sound.get_jitter_ddp(0.0001, 0.02, 0.02, 1.3)
        
        # Shimmer
        shimmer_local = sound.get_shimmer(
            0.0001, 0.02, 0.02, 1.3
        )
        shimmer_dB = sound.get_shimmer_dB(
            0.0001, 0.02, 0.02, 1.3
        )
        shimmer_apq3 = sound.get_shimmer_apq3(
            0.0001, 0.02, 0.02, 1.3
        )
        shimmer_apq5 = sound.get_shimmer_apq5(
            0.0001, 0.02, 0.02, 1.3
        )
        
        return {
            'jitter_local': float(jitter_local),
            'jitter_rap': float(jitter_rap),
            'jitter_ppq5': float(jitter_ppq5),
            'jitter_ddp': float(jitter_ddp),
            'shimmer_local': float(shimmer_local),
            'shimmer_dB': float(shimmer_dB),
            'shimmer_apq3': float(shimmer_apq3),
            'shimmer_apq5': float(shimmer_apq5)
        }
    
    def _analyze_hnr(self, sound: Sound) -> float:
        """谐噪比分析"""
        try:
            harmonicity = sound.to_harmonicity_cc(
                time_step=0.01,
                minimum_pitch=75,
                silence_threshold=0.1,
                periods_per_window=1.0
            )
            hnr = harmonicity.values.T[0]
            hnr = hnr[np.isfinite(hnr)]
            return float(np.mean(hnr)) if len(hnr) > 0 else 0.0
        except:
            return 0.0
```

### 5.5 正常参考值

| 参数 | 正常范围 | 异常参考 | 说明 |
|------|----------|----------|------|
| Jitter (local) | < 1.04% | > 1.04% | 频率微扰 |
| Jitter (rap) | < 0.8% | > 0.8% | 相对平均微扰 |
| Jitter (ppq5) | < 1.0% | > 1.0% | 周期微扰商 |
| Shimmer (local) | < 3.81% | > 3.81% | 振幅微扰 |
| Shimmer (dB) | < 0.5 dB | > 0.5 dB | 分贝振幅微扰 |
| Shimmer (apq3) | < 3% | > 3% | 振幅微扰商 |
| HNR | > 10 dB | < 10 dB | 谐噪比 |

> 注意：Jitter/Shimmer > 5% 时结果不可靠（算法无法正确识别声带振动周期）

### 5.6 运行命令

```bash
# 开发模式
uvicorn main:app --reload --port 8000

# 测试
pytest tests/ -v

# 单个测试
pytest tests/test_voice.py::test_pitch -v
```

---

## 6. 数据库设计原则

### 6.1 命名规范

- 表名：`snake_case`，复数形式 `users`, `voice_records`
- 字段名：`snake_case` `user_name`, `created_at`
- 主键：`id` (BIGINT, 自增)
- 外键：`xxx_id` `user_id`, `teacher_id`

### 6.2 通用字段

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    name VARCHAR(50) COMMENT '姓名',
    role VARCHAR(20) NOT NULL COMMENT '角色: TEACHER/STUDENT/ADMIN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    INDEX idx_phone (phone),
    INDEX idx_role (role)
) COMMENT '用户表';
```

---

**文档版本**: 1.0  
**最后更新**: 2026-03-07
