type CachedCard = {
  targetId: number
  targetType: string
  title: string
  cover: string
  path: string
}

const key = 'travelmind-favorite-cards'

export function saveFavoriteCard(card: CachedCard) {
  const cache = loadCache()
  cache[cacheKey(card.targetType, card.targetId)] = card
  localStorage.setItem(key, JSON.stringify(cache))
}

export function removeFavoriteCard(targetType: string, targetId: number) {
  const cache = loadCache()
  delete cache[cacheKey(targetType, targetId)]
  localStorage.setItem(key, JSON.stringify(cache))
}

export function getFavoriteCard(targetType: string, targetId: number) {
  return loadCache()[cacheKey(targetType, targetId)]
}

function loadCache(): Record<string, CachedCard> {
  try {
    return JSON.parse(localStorage.getItem(key) || '{}')
  } catch {
    return {}
  }
}

function cacheKey(type: string, id: number) {
  return `${type}-${id}`
}
