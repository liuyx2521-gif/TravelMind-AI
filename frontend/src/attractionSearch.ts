import { poiPhoto, stablePoiId, type AmapPoi } from './amapPoi'

export function attractionCities(keyword: string, city = '') {
  if (city.trim()) return [city.trim()]
  if (has(keyword, '海边', '海岛', '海滩', '沙滩', '看海', '赶海', '潜水')) return ['三亚', '厦门', '青岛', '舟山', '大连', '威海', '北海', '深圳']
  if (has(keyword, '雪', '滑雪', '冰雪', '雪景')) return ['哈尔滨', '长白山', '阿勒泰', '张家口', '沈阳']
  if (has(keyword, '古镇', '水乡', '江南')) return ['苏州', '嘉兴', '湖州', '杭州', '黄山']
  if (has(keyword, '夜景', '夜市', '灯光')) return ['上海', '重庆', '长沙', '广州', '香港']
  if (has(keyword, '山', '森林', '徒步', '峡谷')) return ['张家界', '黄山', '桂林', '成都', '贵阳']
  if (has(keyword, '迪士尼', '亲子', '乐园', '游乐园')) return ['上海', '香港', '东京', '大阪', '北京']
  return ['']
}

export function attractionKeywords(keyword: string) {
  const base = keyword.trim() || '热门景点'
  const words = [base]
  if (has(base, '海边', '海岛', '海滩', '沙滩', '看海', '赶海', '潜水')) {
    words.push('海滨浴场', '沙滩', '海滩', '海岛景区', '滨海公园', '赶海', '潜水')
  } else if (has(base, '雪', '滑雪', '冰雪', '雪景')) {
    words.push('滑雪场', '冰雪大世界', '雪景景区', '森林雪景', '温泉滑雪')
  } else if (has(base, '古镇', '水乡', '江南')) {
    words.push('古镇', '历史文化街区', '水乡景区', '古街', '老街')
  } else if (has(base, '夜景', '夜市', '灯光')) {
    words.push('夜景', '观景台', '步行街', '夜市', '灯光秀')
  } else if (has(base, '美食', '小吃')) {
    words.push('美食街', '夜市', '小吃街', '步行街')
  } else if (has(base, '山', '森林', '徒步', '峡谷')) {
    words.push('风景区', '森林公园', '国家公园', '山岳景区', '峡谷')
  } else if (has(base, '迪士尼', '亲子', '乐园', '游乐园')) {
    words.push('主题乐园', '游乐园', '迪士尼', '动物园', '海洋公园')
  } else {
    words.push(`${base} 景区`, `${base} 公园`, `${base} 打卡`, `${base} 旅游景点`)
  }
  words.push('热门景点', '风景名胜')
  return [...new Set(words.filter(Boolean))]
}

export function sortTravelPois(pois: AmapPoi[]) {
  const seen = new Set<string>()
  return pois
    .filter(isTravelPoi)
    .filter(poi => {
      const key = String(poi.id || `${poi.name}-${poi.address}`)
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
    .sort((a, b) => attractionRankPoi(b) - attractionRankPoi(a))
}

export function attractionPoiStableId(poi: AmapPoi, index: number, city: string) {
  return stablePoiId(poi, index, 'attraction', city)
}

function isTravelPoi(poi: AmapPoi) {
  const text = `${poi.name || ''} ${poi.type || ''} ${poi.address || ''}`.toLowerCase()
  if (!text.trim()) return false
  if (has(text, '酒店', '宾馆', '停车场', '收费站', '公司', '写字楼', '小区', '住宅', '售楼', '维修', '银行', '医院', '学校')) return false
  return attractionRankPoi(poi) > 0 || has(text, '风景', '景区', '公园', '博物馆', '海滩', '沙滩', '古镇', '乐园', '步行街', '夜市')
}

function attractionRankPoi(poi: AmapPoi) {
  const text = `${poi.name || ''} ${poi.type || ''} ${poi.address || ''}`.toLowerCase()
  let rank = 0
  const popular = ['西湖', '故宫', '迪士尼', '外滩', '环球度假区', '亚龙湾', '天涯海角', '鼓浪屿', '栈桥', '洱海', '丽江古城', '张家界', '九寨沟', '黄山', '泰山', '长白山', '兵马俑', '夫子庙', '宽窄巷子', '洪崖洞', '橘子洲', '青海湖', '颐和园', '圆明园', '东方明珠', '海昌', '长隆']
  if (popular.some(word => text.includes(word.toLowerCase()))) rank += 8
  if (has(text, '风景名胜', '景区', '旅游景点', '国家级', '5a', '4a')) rank += 5
  if (has(text, '海滨浴场', '海滩', '沙滩', '海岛', '滨海公园')) rank += 5
  if (has(text, '公园', '博物馆', '古镇', '古城', '老街', '步行街', '夜市', '乐园', '动物园', '海洋馆')) rank += 3
  if (poiPhoto(poi)) rank += 1
  rank += Number(poi.biz_ext?.rating || 0)
  return rank
}

function has(text: string, ...words: string[]) {
  return words.some(word => text.includes(word.toLowerCase()))
}
