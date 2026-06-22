import { amapStaticMapUrl } from './externalLinks'

export type AmapPoi = {
  id?: string
  name?: string
  cityname?: string
  pname?: string
  address?: string
  type?: string
  photos?: Array<{ url?: string } | string>
  biz_ext?: { rating?: string | number; cost?: string | number }
  location?: AmapLocation
}

type AmapLocation =
  | string
  | [number, number]
  | {
      lng?: number
      lat?: number
      lnglat?: { lng?: number; lat?: number; getLng?: () => number; getLat?: () => number }
      getLng?: () => number
      getLat?: () => number
    }

export function poiPhoto(poi: AmapPoi) {
  const photos = Array.isArray(poi.photos) ? poi.photos : []
  return photos
    .map(item => typeof item === 'string' ? item : item?.url || '')
    .find(url => url.startsWith('http')) || ''
}

export function staticMap(location: AmapLocation | undefined, key: string) {
  const text = poiLocationText(location)
  return amapStaticMapUrl(text, key)
}

export function poiLocationText(location: AmapLocation | undefined) {
  const { longitude, latitude } = poiLocationParts(location)
  return longitude && latitude ? `${longitude},${latitude}` : ''
}

export function poiLongitude(location: AmapLocation | undefined) {
  return poiLocationParts(location).longitude
}

export function poiLatitude(location: AmapLocation | undefined) {
  return poiLocationParts(location).latitude
}

export function poiLocationParts(location: AmapLocation | undefined) {
  if (!location) return { longitude: 0, latitude: 0 }
  if (typeof location === 'string') {
    const [longitude, latitude] = location.split(',')
    return { longitude: Number(longitude || 0), latitude: Number(latitude || 0) }
  }
  if (Array.isArray(location)) return { longitude: Number(location[0] || 0), latitude: Number(location[1] || 0) }
  return {
    longitude: Number(location.lng ?? location.getLng?.() ?? location.lnglat?.lng ?? location.lnglat?.getLng?.() ?? 0),
    latitude: Number(location.lat ?? location.getLat?.() ?? location.lnglat?.lat ?? location.lnglat?.getLat?.() ?? 0),
  }
}

export function stablePoiId(poi: AmapPoi, index: number, type = 'poi', fallbackCity = '') {
  const source = `${type}-${poi.id || poi.name || index}-${poi.cityname || fallbackCity}-${poi.address || ''}`
  let hash = 0
  for (let i = 0; i < source.length; i++) hash = Math.imul(31, hash) + source.charCodeAt(i) | 0
  return Math.abs(hash)
}
