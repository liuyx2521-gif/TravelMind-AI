<template>
  <div class="auth-shell flex min-h-screen items-center justify-center px-4">
    <div class="auth-card liquid w-full max-w-sm rounded-[32px] p-6">
      <div class="mb-5 flex items-center justify-between gap-3">
        <div>
          <span class="travel-sticker">新的旅程</span>
          <h1 class="m-0 mt-3 text-2xl">创建账号</h1>
        </div>
        <div class="auth-icon">▧</div>
      </div>
      <n-form>
        <n-form-item label="用户名"><n-input v-model:value="username" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="email" placeholder="name@example.com" /></n-form-item>
        <n-form-item label="密码"><n-input v-model:value="password" type="password" placeholder="设置登录密码" /></n-form-item>
        <n-button type="primary" block round :loading="loading" @click="register">注册</n-button>
        <router-link class="mt-4 block text-center text-sm text-[var(--primary)]" to="/login">已有账号，去登录</router-link>
      </n-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { http } from '../api'

const username = ref('')
const email = ref('')
const password = ref('')
const loading = ref(false)
const router = useRouter()
const toast = useMessage()

async function register() {
  loading.value = true
  try {
    await http.post('/api/auth/register', { username: username.value, email: email.value, password: password.value })
    toast.success('注册成功')
    router.push('/login')
  } catch (e) {
    toast.error((e as Error).message)
  } finally {
    loading.value = false
  }
}
</script>
