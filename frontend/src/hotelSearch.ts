import { poiPhoto, stablePoiId, type AmapPoi } from './amapPoi'

const badWords = ['公寓', '民宿', '客栈', '青年旅舍', '青旅', '旅舍', '旅社', '旅馆', '招待所', '住宿', '日租', '短租', 'hostel', 'apartment', 'bnb']
const brandWords = ['希尔顿', '万豪', '洲际', '皇冠', '假日', '香格里拉', '凯悦', '君悦', '柏悦', '喜来登', '威斯汀', '丽思', '文华东方', '四季', '宝格丽', 'W酒店', '万丽', '雅高', '美居', '诺富特', '亚朵', '全季', '桔子', '汉庭', '如家精选', '锦江都城', '丽枫']

export function hotelSearchKeywords(city: string, keyword = '') {
  const prefix = city.trim()
  return [
    keyword.trim(),
    `${prefix}酒店`,
    `${prefix}星级酒店`,
    `${prefix}高端酒店`,
    `${prefix}商务酒店`,
    `${prefix}度假酒店`,
    `${prefix}连锁酒店`,
    `${prefix}饭店`,
  ].filter(Boolean).filter((item, index, arr) => arr.indexOf(item) === index)
}

export function isRegularHotelPoi(poi: AmapPoi) {
  const text = `${poi.name || ''} ${poi.type || ''} ${poi.address || ''}`.toLowerCase()
  if (!text.trim()) return false
  if (badWords.some(word => text.includes(word))) return false
  if (text.includes('宾馆') && !text.includes('国宾馆') && !text.includes('迎宾馆')) return false
  return text.includes('酒店') || text.includes('饭店') || text.includes('度假') || text.includes('hotel') || hotelRankPoi(poi) > 0
}

export function hotelRankPoi(poi: AmapPoi) {
  const text = `${poi.name || ''} ${poi.type || ''}`.toLowerCase()
  let rank = 0
  if (text.includes('酒店') || text.includes('饭店') || text.includes('hotel')) rank += 3
  if (text.includes('度假') || text.includes('resort')) rank += 3
  if (text.includes('星级') || text.includes('豪华') || text.includes('国际')) rank += 2
  if (brandWords.some(word => text.includes(word.toLowerCase()))) rank += 4
  if (poiPhoto(poi)) rank += 1
  rank += Number(poi.biz_ext?.rating || 0)
  return rank
}

export function sortRegularHotelPois(pois: AmapPoi[]) {
  const seen = new Set<string>()
  return pois
    .filter(isRegularHotelPoi)
    .filter(poi => {
      const key = String(poi.id || `${poi.name}-${poi.address}`)
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
    .sort((a, b) => hotelRankPoi(b) - hotelRankPoi(a))
}

export function hotelPoiStableId(poi: AmapPoi, index: number, city: string) {
  return stablePoiId(poi, index, 'hotel', city)
}
