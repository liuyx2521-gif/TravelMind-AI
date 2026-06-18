<template>
  <div class="flex min-h-screen items-center justify-center px-4">
    <div class="liquid w-full max-w-sm rounded-[28px] p-6">
      <h1 class="m-0 mb-5 text-2xl">登录 TravelMind</h1>
      <n-form>
        <n-form-item label="账号">
          <n-input v-model:value="account" placeholder="用户名或邮箱" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="password" type="password" placeholder="密码" />
        </n-form-item>
        <n-button type="primary" block round :loading="loading" @click="login">登录</n-button>
        <router-link class="mt-4 block text-center text-sm text-[var(--primary)]" to="/register">没有账号，去注册</router-link>
      </n-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useUserStore } from '../stores/user'

const account = ref('')
const password = ref('')
const loading = ref(false)
const route = useRoute()
const router = useRouter()
const toast = useMessage()
const user = useUserStore()

async function login() {
  loading.value = true
  try {
    await user.login(account.value, password.value)
    router.push(String(route.query.redirect || '/'))
  } catch (e) {
    toast.error((e as Error).message)
  } finally {
    loading.value = false
  }
}
</script>
