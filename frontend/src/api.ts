import axios from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 120000,
})

client.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(res => {
  const body = res.data
  if (body.code !== 0) return Promise.reject(new Error(body.message || '请求失败'))
  return body.data
})

export const http = {
  get: <T = unknown>(url: string, config?: object) => client.get<T, T>(url, config),
  post: <T = unknown>(url: string, data?: unknown, config?: object) => client.post<T, T>(url, data, config),
  put: <T = unknown>(url: string, data?: unknown, config?: object) => client.put<T, T>(url, data, config),
  delete: <T = unknown>(url: string, config?: object) => client.delete<T, T>(url, config),
}

export type PageResp<T> = { total: number; page: number; size: number; records: T[] }

export type User = { id: number; username: string; email: string; avatar?: string }
export type Attraction = {
  id: number
  name: string
  province: string
  city: string
  description: string
  coverImage: string
  price: number
  bestSeason: string
  openTime: string
  score: number
  tags: string
  longitude: number
  latitude: number
  source?: 'local' | 'online'
}
export type Hotel = { id: number; name: string; city: string; address: string; price: number; score: number; cover: string; longitude: number; latitude: number; source?: 'local' | 'online' }
export type Plan = { id: number; title: string; destination: string; budget: number; days: number; season?: string; content?: string }
export type Note = { id: number; userId?: number; title: string; content: string; cover: string; viewCount: number; likeCount: number; createTime?: string }
export type Comment = { id: number; noteId: number; content: string; createTime: string }
