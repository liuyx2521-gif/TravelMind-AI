import type { Attraction, Hotel } from './api'

const attractionKey = 'travelmind-online-attraction'
const hotelKey = 'travelmind-online-hotel'

export function saveOnlineAttraction(item: Attraction) {
  sessionStorage.setItem(attractionKey, JSON.stringify(item))
}

export function loadOnlineAttraction(id: string) {
  return loadItem<Attraction>(attractionKey, id)
}

export function saveOnlineHotel(item: Hotel) {
  sessionStorage.setItem(hotelKey, JSON.stringify(item))
}

export function loadOnlineHotel(id: string) {
  return loadItem<Hotel>(hotelKey, id)
}

function loadItem<T extends { id: number | string }>(key: string, id: string) {
  try {
    const item = JSON.parse(sessionStorage.getItem(key) || 'null') as T | null
    return item && String(item.id) === id ? item : undefined
  } catch {
    return undefined
  }
}
