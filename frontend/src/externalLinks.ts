type PlaceLike = {
  name: string
  city?: string
}

type RailSearch = {
  origin: string
  destination: string
  date: string
  trainNo?: string
  fromCode?: string
  toCode?: string
}

export function placeKeyword(item: PlaceLike, fallbackCity = '') {
  return `${item.city || fallbackCity || ''} ${item.name}`.trim()
}

export function ctripHotelUrl(item: PlaceLike, fallbackCity = '') {
  return `https://hotels.ctrip.com/hotels/list?keyword=${encodeURIComponent(placeKeyword(item, fallbackCity))}`
}

export function fliggyHotelUrl(_item?: PlaceLike, _fallbackCity = '') {
  return 'https://www.fliggy.com/'
}

export function amapMarkerUrl(longitude: number | string, latitude: number | string, title = '目的地位置') {
  return `https://uri.amap.com/marker?position=${longitude},${latitude}&name=${encodeURIComponent(title)}&src=travelmind&coordinate=gaode&callnative=0`
}

export function amapNavigationUrl(from: { longitude: number; latitude: number; name: string }, to: { longitude: number; latitude: number; name: string }) {
  const start = `${from.longitude},${from.latitude},${encodeURIComponent(from.name)}`
  const end = `${to.longitude},${to.latitude},${encodeURIComponent(to.name)}`
  return `https://uri.amap.com/navigation?from=${start}&to=${end}&mode=car&policy=1&src=travelmind&coordinate=gaode&callnative=0`
}

export function amapStaticMapUrl(location: string, key = '') {
  const text = location.trim()
  return text && key ? `https://restapi.amap.com/v3/staticmap?location=${text}&zoom=13&size=600*320&markers=mid,,A:${text}&key=${key}` : ''
}

export function open12306Url(input: RailSearch) {
  if (input.fromCode && input.toCode) {
    const fs = encodeURIComponent(`${input.origin},${input.fromCode}`)
    const ts = encodeURIComponent(`${input.destination},${input.toCode}`)
    return `https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc&fs=${fs}&ts=${ts}&date=${input.date}&flag=N,N,Y`
  }
  const keyword = encodeURIComponent(`${input.origin} ${input.destination} ${input.date} ${input.trainNo || ''}`.trim())
  return `https://www.12306.cn/index/?keyword=${keyword}`
}

export function socialSearchLinks(keyword: string) {
  const text = encodeURIComponent(keyword.trim())
  return [
    { platform: '小红书', type: '搜索入口', title: '小红书图文攻略', url: `https://www.xiaohongshu.com/search_result?keyword=${text}`, summary: '' },
    { platform: '抖音', type: '搜索入口', title: '抖音视频/探店', url: `https://www.douyin.com/search/${text}`, summary: '' },
    { platform: '全网', type: '搜索入口', title: '全网旅行攻略', url: `https://www.bing.com/search?q=${text}`, summary: '' },
  ]
}
