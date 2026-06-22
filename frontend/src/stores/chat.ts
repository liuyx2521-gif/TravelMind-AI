import { defineStore } from 'pinia'
import { useUserStore } from './user'
import type { AiConversation } from '../api'

export type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  content: string
}

function defaultMessages(content = '你好，我是 TravelMind AI。') {
  return [{ id: crypto.randomUUID(), role: 'assistant' as const, content }]
}

function jwtSubject(token: string) {
  try {
    return JSON.parse(atob(token.split('.')[1] || '')).sub || token.slice(-12)
  } catch {
    return token.slice(-12)
  }
}

export const useChatStore = defineStore('chat', {
  state: () => ({
    conversationId: undefined as number | undefined,
    conversations: [] as AiConversation[],
    messages: defaultMessages() as ChatMessage[],
    restoring: false,
  }),
  actions: {
    ownerKey() {
      const user = useUserStore()
      if (!user.token) return 'guest'
      return user.user?.id ? `user-${user.user.id}` : `token-${jwtSubject(user.token)}`
    },
    storageKey() {
      return `travelmind-chat-${this.ownerKey()}`
    },
    fallbackStorageKeys() {
      const user = useUserStore()
      const keys = [this.storageKey()]
      if (user.token) keys.push(`travelmind-chat-token-${jwtSubject(user.token)}`)
      return [...new Set(keys)]
    },
    setConversationId(id?: number) {
      this.conversationId = id
    },
    setConversations(rows: AiConversation[]) {
      this.conversations = rows
    },
    removeConversation(id: number) {
      this.conversations = this.conversations.filter(item => item.id !== id)
    },
    setMessages(rows: ChatMessage[]) {
      this.messages = rows.length ? rows : defaultMessages()
    },
    newChat(content = '新会话已开始。') {
      this.conversationId = undefined
      this.messages = defaultMessages(content)
      this.saveCache()
    },
    saveCache() {
      if (this.restoring) return
      const value = JSON.stringify({ conversationId: this.conversationId, messages: this.messages })
      this.fallbackStorageKeys().forEach(key => localStorage.setItem(key, value))
    },
    restoreCache() {
      this.restoring = true
      try {
        for (const key of this.fallbackStorageKeys()) {
          const cache = localStorage.getItem(key)
          if (!cache) continue
          const data = JSON.parse(cache)
          if (Array.isArray(data.messages) && data.messages.length) {
            this.messages = data.messages
            this.conversationId = data.conversationId
            return true
          }
        }
        this.conversationId = undefined
        this.messages = defaultMessages()
        return false
      } catch {
        this.conversationId = undefined
        this.messages = defaultMessages()
        return false
      } finally {
        this.restoring = false
      }
    },
  },
})
