export const cityCoords: Record<string, [number, number]> = {
  北京: [116.4074, 39.9042],
  上海: [121.4737, 31.2304],
  广州: [113.2644, 23.1291],
  深圳: [114.0579, 22.5431],
  杭州: [120.1551, 30.2741],
  南京: [118.7969, 32.0603],
  成都: [104.0665, 30.5728],
  重庆: [106.5516, 29.563],
  西安: [108.9398, 34.3416],
  武汉: [114.3054, 30.5928],
  长沙: [112.9388, 28.2282],
  厦门: [118.0894, 24.4798],
  青岛: [120.3826, 36.0671],
  三亚: [109.5119, 18.2528],
  昆明: [102.8329, 24.8801],
  哈尔滨: [126.6425, 45.756],
}

export const stationCodes: Record<string, string> = {
  北京: 'BJP',
  上海: 'SHH',
  杭州: 'HZH',
  广州: 'GZQ',
  深圳: 'SZQ',
  南京: 'NJH',
  成都: 'CDW',
  重庆: 'CQW',
  西安: 'XAY',
  武汉: 'WHN',
  长沙: 'CSQ',
  厦门: 'XMS',
  青岛: 'QDK',
  三亚: 'SEQ',
  昆明: 'KMM',
  哈尔滨: 'HBB',
}

export const transitAnchors: Record<string, { name: string; longitude: number; latitude: number }> = {
  三亚: { name: '三亚站', longitude: 109.5012, latitude: 18.3027 },
  杭州: { name: '龙翔桥地铁站', longitude: 120.1651, latitude: 30.2588 },
  上海: { name: '人民广场地铁站', longitude: 121.4754, latitude: 31.2335 },
  北京: { name: '王府井地铁站', longitude: 116.4115, latitude: 39.9086 },
  成都: { name: '春熙路地铁站', longitude: 104.0809, latitude: 30.6574 },
  广州: { name: '体育西路地铁站', longitude: 113.3214, latitude: 23.131 },
  深圳: { name: '购物公园地铁站', longitude: 114.0538, latitude: 22.5365 },
  重庆: { name: '解放碑地铁站', longitude: 106.5754, latitude: 29.5564 },
  西安: { name: '钟楼地铁站', longitude: 108.9436, latitude: 34.2583 },
}

export function levelText(level: string) {
  return ({ budget: '经济型', comfort: '舒适型', premium: '高端型' } as Record<string, string>)[level] || '舒适型'
}

export function dateText(value: number | null) {
  const date = value ? new Date(value) : new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

export function cityDistance(origin: string, destination: string) {
  const start = cityCoords[origin]
  const end = cityCoords[destination]
  if (!start || !end) return 0
  return cityDistanceByCoord(start[0], start[1], end[0], end[1])
}

export function cityDistanceByCoord(lng1: number, lat1: number, lng2: number, lat2: number) {
  if (!lng1 || !lat1 || !lng2 || !lat2) return 0
  const rad = Math.PI / 180
  const dLat = (lat2 - lat1) * rad
  const dLng = (lng2 - lng1) * rad
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(lat1 * rad) * Math.cos(lat2 * rad) * Math.sin(dLng / 2) ** 2
  return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

export function durationText(km: number, speed: number) {
  const minutes = Math.max(45, Math.round(km / speed * 60))
  return `${Math.floor(minutes / 60)}时${minutes % 60}分`
}

export function cityCode(city: string) {
  let hash = 0
  for (const char of city || 'TM') hash = Math.imul(31, hash) + char.charCodeAt(0) | 0
  return Math.abs(hash % 900) + 100
}

export function cityHotelName(city: string, index: number) {
  const names: Record<string, string[]> = {
    三亚: ['三亚海棠湾民生威斯汀度假酒店', '三亚湾假日度假酒店', '三亚亚特兰蒂斯酒店'],
    杭州: ['杭州西子湖四季酒店', '杭州西湖国宾馆', '杭州城中香格里拉酒店'],
    成都: ['成都博舍酒店', '成都瑞吉酒店', '成都尼依格罗酒店'],
    上海: ['上海和平饭店', '上海浦东丽思卡尔顿酒店', '上海外滩W酒店'],
    北京: ['北京王府井文华东方酒店', '北京饭店诺金', '北京宝格丽酒店'],
  }
  return names[city]?.[index] || `${city}${['中心精选酒店', '舒适商务酒店', '高端度假酒店'][index]}`
}
