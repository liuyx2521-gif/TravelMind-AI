import type { HotelOption } from './hotelPlanner'
import { cityCode, durationText } from './travelMath'

export type TicketOption = {
  id: string
  trainNo: string
  seat: string
  depart: string
  arrive: string
  duration: string
  price: number
}

export type PackingItem = {
  name: string
  checked: boolean
  price?: number
}

export type ItineraryDay = {
  day: number
  title: string
}

export type TripForm = {
  origin: string
  destination: string
  date: number
  people: number
  days: number
  rooms: number
  mealPerPersonDay: number
  localTransportDay: number
}

export function defaultTrip(): TripForm {
  return {
    origin: '杭州',
    destination: '三亚',
    date: Date.now() + 1000 * 60 * 60 * 24 * 14,
    people: 2,
    days: 3,
    rooms: 1,
    mealPerPersonDay: 160,
    localTransportDay: 90,
  }
}

export function emptyTrip(): TripForm {
  return {
    origin: '',
    destination: '',
    date: Date.now() + 1000 * 60 * 60 * 24 * 14,
    people: 1,
    days: 1,
    rooms: 1,
    mealPerPersonDay: 0,
    localTransportDay: 0,
  }
}

export function defaultItinerary(): ItineraryDay[] {
  return [
    { day: 1, title: '抵达后入住酒店，傍晚去海边散步和吃海鲜' },
    { day: 2, title: '核心景点打卡，下午安排拍照时间' },
    { day: 3, title: '轻松返程，预留伴手礼和市区交通' },
  ]
}

export function syncItineraryDays(items: ItineraryDay[], days: number) {
  const count = Math.max(1, days || 1)
  const next = [...items]
  while (next.length < count) next.push({ day: next.length + 1, title: '补充当天行程安排' })
  return next.slice(0, count).map((item, index) => ({ ...item, day: index + 1 }))
}

export function defaultPackingItems(): PackingItem[] {
  return [
    { name: '身份证 / 护照', checked: true },
    { name: '手机与充电线', checked: true },
    { name: '充电宝', checked: true },
    { name: '换洗衣物', checked: true },
    { name: '舒适鞋', checked: true },
    { name: '洗漱用品', checked: true },
    { name: '常用药品', checked: true },
    { name: '雨具', checked: false },
    { name: '拍照服饰', checked: false },
  ]
}

export function defaultPurchaseItems(): PackingItem[] {
  return [
    { name: '防晒霜 / 墨镜', checked: true, price: 120 },
    { name: '一次性用品', checked: true, price: 60 },
    { name: '行李寄存或托运', checked: false, price: 0 },
    { name: '伴手礼预留', checked: false, price: 200 },
  ]
}

export function ticketOptions(origin: string, destination: string, distance: number): TicketOption[] {
  const base = Math.max(80, Math.round(distance * 0.42 + 90))
  return [
    { id: 'g', trainNo: `G${cityCode(origin)}${cityCode(destination)}`, seat: '二等座', depart: '08:12', arrive: '13:46', duration: durationText(distance, 260), price: base },
    { id: 'd', trainNo: `D${cityCode(origin) + 18}`, seat: '一等座', depart: '10:28', arrive: '16:40', duration: durationText(distance, 215), price: Math.round(base * 1.45) },
    { id: 'z', trainNo: `Z${cityCode(destination) + 36}`, seat: '硬卧/动卧', depart: '19:36', arrive: '07:22+1', duration: durationText(distance, 115), price: Math.round(base * 0.78) },
  ]
}

export function tripBudget(
  trip: TripForm,
  nights: number,
  ticket: TicketOption | undefined,
  hotel: Pick<HotelOption, 'price'> | undefined,
  purchaseItems: PackingItem[],
) {
  const people = Math.max(1, trip.people || 1)
  const ticketCost = trip.origin && trip.destination ? (ticket?.price || 0) * 2 * people : 0
  const hotelCost = (hotel?.price || 0) * nights * Math.max(1, trip.rooms || 1)
  const meals = Math.round((trip.mealPerPersonDay || 0) * people * Math.max(1, trip.days || 1))
  const local = Math.round((trip.localTransportDay || 0) * people * Math.max(1, trip.days || 1))
  const luggage = purchaseItems.reduce((sum, item) => sum + Math.max(0, Math.round(item.price || 0)), 0)
  const items = [
    { name: '往返车票', value: ticketCost },
    { name: '酒店住宿', value: hotelCost },
    { name: '餐饮', value: meals },
    { name: '市内交通', value: local },
    { name: '行李准备', value: luggage },
  ].filter(item => item.value > 0)
  const total = items.reduce((sum, item) => sum + item.value, 0)
  return {
    items,
    total,
    perPerson: Math.round(total / people),
    perDay: Math.round(total / Math.max(1, trip.days || 1)),
  }
}

export function planTitle(trip: TripForm) {
  return `${trip.origin}到${trip.destination} · ${trip.people}人 · ${trip.days}日游`
}

export function buildPlanContent(params: {
  trip: TripForm
  nights: number
  budgetTotal: number
  ticket?: TicketOption
  hotel?: HotelOption
  hotelPriceText: string
  itinerary: ItineraryDay[]
  packingItems: PackingItem[]
  purchaseItems: PackingItem[]
}) {
  const { trip, nights, budgetTotal, ticket, hotel, hotelPriceText, itinerary, packingItems, purchaseItems } = params
  const packing = packingItems.map(item => item.name).join('、') || '按需准备'
  const purchases = purchaseItems.map(item => `${item.name}${item.price ? `￥${item.price}` : ''}`).join('、') || '暂无额外购买'
  const days = itinerary.map((day, index) => {
    const isFirst = index === 0
    const isLast = index === itinerary.length - 1
    const transport = isFirst
      ? `${trip.origin}出发，乘坐${ticket?.trainNo || '已选车次'}前往${trip.destination}，抵达后前往酒店办理入住。`
      : isLast
        ? `预留返程时间，从${trip.destination}返回${trip.origin}。`
        : '以市内交通为主，按当天景点位置安排路线。'
    return `Day ${day.day}
交通：${transport}
景点：${day.title || '根据当天状态安排轻量游览。'}
美食：按每人每日￥${trip.mealPerPersonDay || 0}预算安排餐饮，优先选择交通方便、评价稳定的餐厅。
住宿：${hotel?.name || '已选酒店'}，${hotel?.route || '按实际位置规划到达方式'}。`
  }).join('\n\n')
  return `${planTitle(trip)}
预算：￥${budgetTotal}
车票：${ticket?.trainNo || '未选择'}，${trip.origin} ${ticket?.depart || ''} → ${trip.destination} ${ticket?.arrive || ''}
酒店：${hotel?.name || '未选择'}，${nights}晚，${hotelPriceText}
行李：${packing}
需购买：${purchases}

${days}`
}
