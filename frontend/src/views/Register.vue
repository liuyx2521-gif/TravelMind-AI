<template>
  <div class="flex min-h-screen items-center justify-center px-4">
    <div class="liquid w-full max-w-sm rounded-[28px] p-6">
      <h1 class="m-0 mb-5 text-2xl">创建账号</h1>
      <n-form>
        <n-form-item label="用户名"><n-input v-model:value="username" /></n-form-item>
        <n-form-item label="邮箱"><n-input v-model:value="email" /></n-form-item>
        <n-form-item label="密码"><n-input v-model:value="password" type="password" /></n-form-item>
        <n-button type="primary" block round :loading="loading" @click="register">注册</n-button>
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
