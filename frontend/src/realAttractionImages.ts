import type { Attraction } from './api'
import { placeImagePlaceholder } from './imageFallback'

const imageCache = new Map<string, string>()

const brokenHosts = [
  'source.unsplash.com',
]

export function realAttractionImage(item: Pick<Attraction, 'name' | 'coverImage'>) {
  return imageCache.get(item.name) || validImage(item.coverImage) || placeImagePlaceholder(item)
}

export async function loadRealAttractionImages(items: Pick<Attraction, 'name' | 'coverImage'>[]) {
  await Promise.allSettled(items.map(loadRealAttractionImage))
}

async function loadRealAttractionImage(item: Pick<Attraction, 'name' | 'coverImage'>) {
  if (validImage(item.coverImage) && !shouldRefresh(item.coverImage)) return
  const cached = imageCache.get(item.name)
  if (cached) {
    item.coverImage = cached
    return
  }

  const image = await wikiImage(item.name)
  if (image) {
    imageCache.set(item.name, image)
    item.coverImage = image
  }
}

async function wikiImage(title: string) {
  for (const lang of ['zh', 'en']) {
    const controller = new AbortController()
    const timer = window.setTimeout(() => controller.abort(), 3000)
    try {
      const res = await fetch(`https://${lang}.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(title)}`, {
        signal: controller.signal,
      })
      if (!res.ok) continue
      const data = await res.json()
      const image = data?.originalimage?.source || data?.thumbnail?.source
      if (typeof image === 'string' && image.startsWith('http')) return image
    } catch {
      // Keep current image or generated placeholder.
    } finally {
      window.clearTimeout(timer)
    }
  }
  return ''
}

function validImage(url?: string) {
  if (!url || !url.trim()) return ''
  if (url.startsWith('data:image/')) return url
  if (!/^https?:\/\//i.test(url)) return ''
  if (brokenHosts.some(host => url.includes(host))) return ''
  return url
}

function shouldRefresh(url: string) {
  return url.includes('commons.wikimedia.org/wiki/Special:FilePath/')
}
