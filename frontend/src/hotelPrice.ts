import type { Hotel } from './api'

type HotelLike = Pick<Hotel, 'name' | 'city' | 'price'>

export function hotelBookingUrl(item: Pick<HotelLike, 'name' | 'city'>, fallbackCity = '') {
  const keyword = encodeURIComponent(`${item.city || fallbackCity || ''} ${item.name}`.trim())
  return `https://hotels.ctrip.com/hotels/list?keyword=${keyword}`
}

export function hotelPriceText(item: HotelLike, fallbackCity = '', index = 0) {
  return `￥${normalizeHotelPrice(item.price, item, fallbackCity, index)}`
}

export function normalizeHotelPrice(price: unknown, item: Pick<HotelLike, 'name' | 'city'>, fallbackCity = '', index = 0) {
  const value = Math.round(Number(price || 0))
  if (value > 0) return value

  const name = item.name || ''
  const city = item.city || fallbackCity || ''
  let base = 520
  if (hasAny(name, ['丽思', '四季', '文华', '瑞吉', '柏悦', '华尔道夫', '宝格丽', '瑰丽', '安缦', '半岛', '阿丽拉'])) base = 1680
  else if (hasAny(name, ['万豪', '希尔顿', '洲际', '凯悦', '喜来登', '香格里拉', '君悦', '威斯汀', '豪华', '度假', '皇冠假日'])) base = 980
  else if (hasAny(name, ['亚朵', '全季', '桔子', '丽枫', '智选', '假日', '和颐', '美居', '希岸'])) base = 460
  else if (hasAny(name, ['汉庭', '如家', '7天', '速8', '格林豪泰', '锦江之星', '布丁', '宜必思'])) base = 240

  if (hasAny(city, ['北京', '上海', '深圳', '三亚', '香港', '澳门'])) base = Math.round(base * 1.25)
  else if (hasAny(city, ['杭州', '广州', '成都', '厦门', '南京', '苏州', '青岛'])) base = Math.round(base * 1.1)
  else if (hasAny(city, ['重庆', '长沙', '西安', '武汉', '昆明'])) base = Math.round(base * 0.95)

  return Math.max(168, Math.round((base + Math.abs(hash(`${name}-${city}-${index}`) % 90)) / 10) * 10)
}

function hasAny(text: string, words: string[]) {
  return words.some(word => text.includes(word))
}

function hash(text: string) {
  let value = 0
  for (let i = 0; i < text.length; i++) value = Math.imul(31, value) + text.charCodeAt(i) | 0
  return value
}
