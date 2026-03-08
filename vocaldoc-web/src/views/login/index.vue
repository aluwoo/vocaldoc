<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { sendCode, login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginForm = ref({
  phone: '',
  code: ''
})
const loading = ref(false)
const countdown = ref(0)
const countdownTimer = ref<number | null>(null)

const canSendCode = computed(() => {
  return loginForm.value.phone.length === 11 && countdown.value === 0
})

const buttonText = computed(() => {
  if (countdown.value > 0) {
    return `${countdown.value}s`
  }
  return '发送验证码'
})

async function handleSendCode() {
  if (!canSendCode.value) {
    if (loginForm.value.phone.length !== 11) {
      ElMessage.warning('请输入正确的手机号')
    }
    return
  }

  try {
    await sendCode({ phone: loginForm.value.phone })
    ElMessage.success('验证码已发送')
    
    countdown.value = 60
    countdownTimer.value = window.setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && countdownTimer.value) {
        clearInterval(countdownTimer.value)
        countdownTimer.value = null
      }
    }, 1000)
  } catch (error) {
    // error handled by interceptor
  }
}

async function handleLogin() {
  if (!loginForm.value.phone || !loginForm.value.code) {
    ElMessage.warning('请输入手机号和验证码')
    return
  }

  if (loginForm.value.phone.length !== 11) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  loading.value = true
  try {
    const res = await login({
      phone: loginForm.value.phone,
      code: loginForm.value.code
    })
    userStore.setToken(res.token)
    userStore.setUserInfo(res.user)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="40" color="#409eff"><Microphone /></el-icon>
        <h1>声乐课堂嗓音档案管理系统</h1>
      </div>
      <el-form :model="loginForm" class="login-form">
        <el-form-item>
          <el-input
            v-model="loginForm.phone"
            placeholder="请输入手机号"
            prefix-icon="User"
            size="large"
            maxlength="11"
            :disabled="loading"
          />
        </el-form-item>
        <el-form-item>
          <div class="code-input-wrapper">
            <el-input
              v-model="loginForm.code"
              placeholder="请输入验证码"
              prefix-icon="Lock"
              size="large"
              maxlength="6"
              :disabled="loading"
              @keyup.enter="handleLogin"
            />
            <el-button
              type="primary"
              size="large"
              :disabled="!canSendCode"
              class="send-code-btn"
              @click="handleSendCode"
            >
              {{ buttonText }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  margin-top: 16px;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.login-form {
  margin-top: 24px;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input-wrapper :deep(.el-input) {
  flex: 1;
}

.send-code-btn {
  min-width: 100px;
  padding: 0 16px;
}

.send-code-btn:disabled {
  background-color: #c0c4cc;
  border-color: #c0c4cc;
}

.login-button {
  width: 100%;
}

@media (max-width: 480px) {
  .login-box {
    width: 90%;
    padding: 24px;
  }
  
  .login-header h1 {
    font-size: 18px;
  }
  
  .code-input-wrapper {
    flex-direction: column;
  }
  
  .send-code-btn {
    width: 100%;
  }
}
</style>
