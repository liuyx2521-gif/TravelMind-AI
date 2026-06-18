export type SeasonalPoiQuery = {
  city: string
  keyword: string
}

export function seasonalAttractionKeyword(date = new Date()) {
  const month = date.getMonth() + 1
  if (month >= 3 && month <= 5) return '赏花踏青景点'
  if (month >= 6 && month <= 8) return '避暑海边草原景点'
  if (month >= 9 && month <= 11) return '赏秋自驾景点'
  return '温泉雪景滑雪景点'
}

export function seasonalAttractionTip(date = new Date()) {
  const month = date.getMonth() + 1
  if (month >= 3 && month <= 5) return '当前是春季，默认推荐赏花、踏青、古镇和城市慢游目的地。'
  if (month >= 6 && month <= 8) return '当前是夏季，默认推荐避暑、海边、草原、湖泊等更适合当下出行的目的地。'
  if (month >= 9 && month <= 11) return '当前是秋季，默认推荐赏秋、自驾、山林和古城目的地。'
  return '当前是冬季，默认推荐温泉、雪景、滑雪和暖冬目的地。'
}

export function seasonalAttractionQueries(date = new Date()): SeasonalPoiQuery[] {
  const month = date.getMonth() + 1
  if (month >= 3 && month <= 5) {
    return [
      { city: '杭州', keyword: '赏花景点' },
      { city: '苏州', keyword: '园林景点' },
      { city: '南京', keyword: '踏青景点' },
      { city: '婺源', keyword: '油菜花景点' },
    ]
  }
  if (month >= 6 && month <= 8) {
    return [
      { city: '舟山', keyword: '海边景点' },
      { city: '青岛', keyword: '海边景点' },
      { city: '杭州', keyword: '避暑景点' },
      { city: '呼伦贝尔', keyword: '草原景点' },
      { city: '九寨沟', keyword: '避暑景点' },
    ]
  }
  if (month >= 9 && month <= 11) {
    return [
      { city: '北京', keyword: '赏秋景点' },
      { city: '南京', keyword: '赏秋景点' },
      { city: '杭州', keyword: '秋季景点' },
      { city: '婺源', keyword: '赏秋景点' },
    ]
  }
  return [
    { city: '哈尔滨', keyword: '冰雪景点' },
    { city: '长白山', keyword: '雪景景点' },
    { city: '北京', keyword: '温泉' },
    { city: '三亚', keyword: '海边景点' },
  ]
}
