type PlaceImage = {
  name?: string
  title?: string
  city?: string
  tags?: string
  longitude?: number
  latitude?: number
}

export function fallbackPlaceImage(event: Event, place?: PlaceImage) {
  const img = event.target as HTMLImageElement
  img.onerror = null
  img.src = placeImagePlaceholder(place)
}

export function fallbackGenericImage(event: Event, title = 'TravelMind') {
  const img = event.target as HTMLImageElement
  img.onerror = null
  img.src = placeholder(title, '旅行图片暂不可用')
}

export function placeImagePlaceholder(place?: PlaceImage) {
  const title = place?.name || place?.title || 'TravelMind'
  const subtitle = [place?.city, place?.tags].filter(Boolean).join(' · ') || '旅行图片暂不可用'
  return placeholder(title, subtitle)
}

function placeholder(title: string, subtitle: string) {
  const safeTitle = escapeXml(title).slice(0, 32)
  const safeSubtitle = escapeXml(subtitle).slice(0, 42)
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="900" height="520" viewBox="0 0 900 520">
      <defs>
        <linearGradient id="sky" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#dcecff"/>
          <stop offset="0.48" stop-color="#f7fbff"/>
          <stop offset="1" stop-color="#d7e7df"/>
        </linearGradient>
        <linearGradient id="water" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0" stop-color="#7fb6c8"/>
          <stop offset="1" stop-color="#9ed3c0"/>
        </linearGradient>
      </defs>
      <rect width="900" height="520" fill="url(#sky)"/>
      <circle cx="720" cy="118" r="78" fill="#fff4c7" opacity=".95"/>
      <path d="M0 332 C120 268 225 286 346 238 C486 182 576 242 692 198 C780 165 842 174 900 148 L900 520 L0 520 Z" fill="#8aa0b4" opacity=".34"/>
      <path d="M0 383 C130 330 245 348 374 314 C500 281 620 318 742 287 C824 266 870 266 900 250 L900 520 L0 520 Z" fill="#5d7489" opacity=".28"/>
      <rect y="405" width="900" height="115" fill="url(#water)" opacity=".7"/>
      <path d="M96 438 C190 420 278 458 372 438 S560 420 654 438 S812 456 900 438" fill="none" stroke="#ffffff" stroke-width="10" opacity=".45"/>
      <text x="50%" y="48%" text-anchor="middle" font-family="Inter, Arial, sans-serif" font-size="42" font-weight="700" fill="#243447">${safeTitle}</text>
      <text x="50%" y="58%" text-anchor="middle" font-family="Inter, Arial, sans-serif" font-size="20" fill="#667085">${safeSubtitle}</text>
    </svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

function escapeXml(value: string) {
  return value.replace(/[<>&"']/g, x => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;', "'": '&apos;' }[x]!))
}
