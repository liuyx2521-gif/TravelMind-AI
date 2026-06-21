import axios, { type AxiosRequestConfig } from 'axios'

type ApiResult<T> = {
  code: number
  message?: string
  data: T
}

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 120000,
})

client.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// @ts-ignore
client.interceptors.response.use(response => {
  const body = response.data as ApiResult<unknown>
  if (body.code !== 0) return Promise.reject(new Error(body.message || '请求失败'))
  return body.data
})

export const http = {
  get: <T = unknown>(url: string, config?: AxiosRequestConfig) => client.get<T, T>(url, config),
  post: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) => client.post<T, T>(url, data, config),
  put: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) => client.put<T, T>(url, data, config),
  delete: <T = unknown>(url: string, config?: AxiosRequestConfig) => client.delete<T, T>(url, config),
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

export type Hotel = {
  id: number
  name: string
  city: string
  address: string
  price: number
  score: number
  cover: string
  longitude: number
  latitude: number
  source?: 'local' | 'online'
}

export type Plan = {
  id: number
  title: string
  destination: string
  budget: number
  days: number
  season?: string
  content?: string
}

export type Note = {
  id: number
  userId?: number
  title: string
  content: string
  cover: string
  viewCount: number
  likeCount: number
  createTime?: string
}

export type Comment = {
  id: number
  noteId: number
  userId: number
  parentId?: number
  replyToUserId?: number
  replyToUsername?: string
  replyToAvatar?: string
  username: string
  avatar?: string
  content: string
  createTime: string
}

export type WeatherInfo = {
  city: string
  province: string
  weather: string
  temperature: string
  windDirection: string
  windPower: string
  humidity: string
  reportTime: string
  forecasts: WeatherForecast[]
}

export type WeatherForecast = {
  date: string
  week: string
  dayWeather: string
  nightWeather: string
  dayTemp: string
  nightTemp: string
}

export type AiConversation = {
  id: number
  userId: number
  title: string
  createTime?: string
  updateTime?: string
}

export type AiMessage = {
  id: number
  conversationId: number
  userId: number
  role: 'user' | 'assistant' | 'system'
  content: string
  createTime?: string
}
