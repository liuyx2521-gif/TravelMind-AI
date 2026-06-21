import type { Hotel } from './api'
import { poiLocationParts, poiPhoto, stablePoiId, type AmapPoi } from './amapPoi'
import { hotelPriceText as formatHotelPrice, normalizeHotelPrice } from './hotelPrice'
import { fallbackPlaceImage, placeImagePlaceholder } from './imageFallback'
import { cityCoords, cityDistanceByCoord, cityHotelName, levelText, transitAnchors } from './travelMath'

export type HotelOption = {
  id: string
  name: string
  city: string
  address: string
  desc: string
  reason: string
  route: string
  nearestStation: string
  stationDistance: number
  level: string
  levelLabel: string
  score: number
  price: number
  cover: string
  longitude: number
  latitude: number
}

type HotelBase = Omit<HotelOption, 'reason' | 'route' | 'nearestStation' | 'stationDistance' | 'level' | 'levelLabel'>

export const hotelLevelOptions = [
  { label: '经济型', value: 'budget' },
  { label: '舒适型', value: 'comfort' },
  { label: '高端型', value: 'premium' },
]

export function fallbackHotels(destination: string, hotelLevel: string) {
  const city = destination || '目的地'
  return [0, 1, 2].map(index => {
    const name = cityHotelName(city, index)
    return enrichHotel({
      id: `hotel-${index + 1}`,
      name,
      city,
      address: `${city}核心旅行区`,
      desc: `${city}${levelText(hotelLevel)}酒店`,
      score: [4.8, 4.6, 4.9][index],
      price: normalizeHotelPrice(0, { name, city, price: 0 }, city, index),
      cover: hotelCoverOrPlaceholder('', city, name, hotelLevel),
      longitude: cityCoords[city]?.[0] || 0,
      latitude: cityCoords[city]?.[1] || 0,
    }, { index, hotelLevel, hotelKeyword: '' })
  })
}

export function hotelFromApi(row: Hotel, index: number, destination: string, hotelLevel: string, hotelKeyword: string) {
  return enrichHotel({
    id: `online-${row.id || index}`,
    name: row.name,
    city: row.city || destination,
    address: row.address || '',
    desc: row.address || `${destination}在线酒店`,
    score: Number(row.score || (4.6 + index * 0.1).toFixed(1)),
    price: normalizeHotelPrice(row.price, { name: row.name, city: row.city || destination, price: row.price }, destination, index),
    cover: hotelCoverOrPlaceholder(row.cover, row.city || destination, row.name, hotelLevel),
    longitude: Number(row.longitude || cityCoords[destination]?.[0] || 0),
    latitude: Number(row.latitude || cityCoords[destination]?.[1] || 0),
  }, { index, hotelLevel, hotelKeyword })
}

export function hotelFromPoi(poi: AmapPoi, index: number, destination: string, hotelLevel: string, hotelKeyword: string) {
  const location = poiLocationParts(poi.location)
  return enrichHotel({
    id: `js-${stablePoiId(poi, index, 'hotel', destination)}`,
    name: poi.name || `${destination}酒店`,
    city: poi.cityname || destination,
    address: poi.address || poi.type || '',
    desc: poi.address || poi.type || `${destination}在线酒店`,
    score: Number(poi.biz_ext?.rating || (4.6 + index * 0.1).toFixed(1)),
    price: normalizeHotelPrice(poi.biz_ext?.cost, { name: poi.name || '', city: poi.cityname || destination, price: 0 }, destination, index),
    cover: hotelCoverOrPlaceholder(poiPhoto(poi), poi.cityname || destination, poi.name || `${destination}酒店`, hotelLevel),
    longitude: location.longitude || cityCoords[destination]?.[0] || 0,
    latitude: location.latitude || cityCoords[destination]?.[1] || 0,
  }, { index, hotelLevel, hotelKeyword })
}

export function enrichHotel(hotel: HotelBase, context: { index: number; hotelLevel: string; hotelKeyword: string }): HotelOption {
  const anchor = transitAnchors[hotel.city] || {
    name: `${hotel.city}中心交通站`,
    longitude: cityCoords[hotel.city]?.[0] || hotel.longitude,
    latitude: cityCoords[hotel.city]?.[1] || hotel.latitude,
  }
  const stationDistance = Number(cityDistanceByCoord(hotel.longitude, hotel.latitude, anchor.longitude, anchor.latitude).toFixed(1)) || [0.6, 1.1, 1.8][context.index]
  const levelLabel = levelText(context.hotelLevel)
  const route = stationDistance <= 1
    ? `步行约${Math.max(8, Math.round(stationDistance * 14))}分钟可到`
    : `打车约${Math.max(8, Math.round(stationDistance * 5))}分钟，或换乘公共交通`
  return {
    ...hotel,
    cover: hotelCoverOrPlaceholder(hotel.cover, hotel.city, hotel.name, context.hotelLevel),
    level: context.hotelLevel,
    levelLabel,
    nearestStation: anchor.name,
    stationDistance,
    route,
    reason: `${levelLabel}匹配当前预算，距离${anchor.name}约${stationDistance}km，${route}；到${hotel.city}热门区域更方便，适合${context.hotelKeyword || '旅行'}行程。`,
  }
}

export function hotelImage(hotel: HotelOption, hotelLevel: string) {
  return hotelCoverOrPlaceholder(hotel.cover, hotel.city, hotel.name, hotelLevel)
}

export function repairHotelImage(event: Event, hotel: HotelOption, hotelLevel: string) {
  hotel.cover = hotelCoverOrPlaceholder('', hotel.city, hotel.name, hotelLevel)
  fallbackPlaceImage(event, hotel)
}

export function ensureHotelImage(event: Event, hotel: HotelOption, hotelLevel: string) {
  const img = event.target as HTMLImageElement
  if (img.naturalWidth < 8 || img.naturalHeight < 8) repairHotelImage(event, hotel, hotelLevel)
}

export function hotelPriceText(hotel: Pick<HotelOption, 'name' | 'city' | 'price'>, fallbackCity = '') {
  return formatHotelPrice(hotel, fallbackCity)
}

function hotelCoverOrPlaceholder(url: string | undefined, city: string, name: string, hotelLevel: string) {
  return validHotelImage(url) || placeImagePlaceholder({ name: name || `${city || '酒店'}酒店`, city, tags: levelText(hotelLevel) })
}

function validHotelImage(url?: string) {
  if (!url || !url.trim()) return ''
  if (url.startsWith('data:image/')) return url
  if (!/^https?:\/\//i.test(url)) return ''
  if (url.includes('/v3/staticmap')) return ''
  return url
}
