<template>
  <main class="landing-shell">
    <div class="landing-glow landing-glow-a"></div>
    <div class="landing-glow landing-glow-b"></div>

    <button
      v-for="(place, index) in places"
      :key="place.name"
      class="landing-photo"
      :style="photoStyle(index)"
      type="button"
      @click="openPlace(place)"
    >
      <img :src="place.image || placeImagePlaceholder(place)" :alt="place.name" @error="fallbackPlaceImage($event, place)" />
    </button>

    <section class="landing-hero">
      <h1>TravelMind AI</h1>
      <p>想旅行，但不想被攻略、预算和路线绕晕？把出发地、天数、预算和想玩的感觉告诉我，我来陪你挑目的地、算花费、找酒店，把每天怎么玩聊成一份清清楚楚的行程。</p>
      <div class="landing-actions">
        <n-button type="primary" round size="large" @click="enterAsGuest">游客进入</n-button>
        <n-button round size="large" @click="goLogin">登录</n-button>
        <n-button round size="large" @click="goRegister">注册</n-button>
      </div>
    </section>

    <n-modal v-model:show="showModal" preset="card" class="max-w-[980px]" :bordered="false">
      <div v-if="selected" class="grid gap-5 md:grid-cols-[1.2fr_.8fr]">
        <img class="h-[360px] w-full rounded-[24px] object-cover" :src="selected.image || placeImagePlaceholder(selected)" :alt="selected.name" @error="fallbackPlaceImage($event, selected)" />
        <div>
          <div class="text-sm font-700 text-[var(--muted)]">{{ selected.city }} · {{ selected.region }}</div>
          <h2 class="m-0 mt-2 text-3xl">{{ selected.name }}</h2>
          <p class="mt-4 text-base leading-8 text-[var(--muted)]">{{ selected.description }}</p>
        </div>
      </div>
    </n-modal>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fallbackPlaceImage, placeImagePlaceholder } from '../imageFallback'

type HeroPlace = {
  name: string
  city: string
  region: string
  wiki: string
  description: string
  image?: string
}

const router = useRouter()

const places = ref<HeroPlace[]>([
  {
    name: '故宫',
    city: '北京',
    region: '中国',
    wiki: '故宫',
    description: '故宫位于北京中轴线中心，是明清两代皇家宫殿建筑群，也是中国古代宫城制度、礼制空间和宫廷建筑艺术的集中体现。它以规模宏大的院落、连续展开的殿宇和丰富的文物收藏闻名，是了解中国历史文化最具代表性的目的地之一。',
  },
  {
    name: '长城',
    city: '北京',
    region: '中国',
    wiki: '长城',
    description: '长城是中国古代重要的军事防御工程，由城墙、敌楼、关隘和烽火台等构成，跨越山地、戈壁与草原等多种地貌。北京周边的八达岭、慕田峪等段落保存较好，兼具历史遗迹和壮阔山景，是中国最具识别度的世界文化遗产之一。',
  },
  {
    name: '西湖',
    city: '杭州',
    region: '中国',
    wiki: '西湖',
    description: '西湖位于浙江杭州，是中国传统山水审美与城市生活结合的经典景观。湖区由湖面、堤桥、园林、寺塔和周边群山共同构成，苏堤春晓、断桥残雪、雷峰夕照等景观长期被视为杭州旅行的代表意象。',
  },
  {
    name: '张家界国家森林公园',
    city: '张家界',
    region: '中国',
    wiki: '张家界国家森林公园',
    description: '张家界国家森林公园以石英砂岩峰林地貌闻名，山峰拔地而起、形态密集，云雾缭绕时层次尤其鲜明。袁家界、金鞭溪、黄石寨等区域展现了独特的山地生态和峡谷景观，是自然风光旅行中的热门目的地。',
  },
  {
    name: '丽江古城',
    city: '丽江',
    region: '中国',
    wiki: '丽江古城',
    description: '丽江古城位于云南丽江，依山就水而建，街巷、水系、民居和商业生活交织在一起。古城保留了纳西族传统聚落格局，也融合了茶马古道历史与高原城市生活氛围，是人文旅行和慢节奏度假的典型目的地。',
  },
  {
    name: '上海外滩',
    city: '上海',
    region: '中国',
    wiki: '外滩',
    description: '外滩位于上海黄浦江西岸，沿线分布着多座近代历史建筑，与浦东陆家嘴天际线隔江相望。这里集中呈现了上海近现代城市发展、金融贸易历史和当代都市景观，是上海夜景和城市漫游最经典的区域之一。',
  },
  {
    name: '颐和园',
    city: '北京',
    region: '中国',
    wiki: '颐和园',
    description: '颐和园是清代皇家园林的代表，以昆明湖、万寿山为主体，融合山水格局、宫殿建筑、长廊彩画和佛香阁等景观。它展现了中国古典园林借景、造景和皇家礼制空间的结合，是北京历史文化旅行的重要目的地。',
  },
  {
    name: '九寨沟',
    city: '阿坝',
    region: '中国',
    wiki: '九寨沟',
    description: '九寨沟以高山湖泊、钙华瀑布、彩林和雪峰景观著称，湖水色彩随季节和光线变化而呈现蓝绿、碧透的层次。景区自然生态保存较好，诺日朗瀑布、五花海、长海等景观构成了极具辨识度的自然旅行体验。',
  },
  {
    name: '黄山',
    city: '黄山',
    region: '中国',
    wiki: '黄山',
    description: '黄山以奇松、怪石、云海、温泉和冬雪闻名，山体形态变化丰富，观景点之间既有险峻山路，也有开阔视野。它长期被视为中国山岳景观的代表，也是摄影、徒步和自然审美旅行的热门目的地。',
  },
  {
    name: '桂林漓江',
    city: '桂林',
    region: '中国',
    wiki: '漓江',
    description: '漓江流经广西桂林至阳朔一带，两岸喀斯特峰林、田园村落和江面倒影共同构成了“桂林山水”的经典画面。乘船或骑行游览时，可以看到山水相映、村镇穿插的南方山水景观。',
  },
  {
    name: '莫高窟',
    city: '敦煌',
    region: '中国',
    wiki: '莫高窟',
    description: '莫高窟位于甘肃敦煌，是以佛教石窟艺术、壁画和彩塑闻名的世界文化遗产。洞窟内容跨越多个历史时期，反映了丝绸之路上的宗教传播、艺术交流和社会生活，是中国古代艺术史的重要宝库。',
  },
  {
    name: '秦始皇兵马俑',
    city: '西安',
    region: '中国',
    wiki: '秦始皇兵马俑',
    description: '秦始皇兵马俑是秦始皇陵的重要陪葬坑，以规模宏大的陶俑军阵闻名。士兵、战车、军马和武器陈列展现了秦代军事组织和陶塑工艺，也使西安成为了解秦汉文明的重要旅行目的地。',
  },
  {
    name: '布达拉宫',
    city: '拉萨',
    region: '中国',
    wiki: '布达拉宫',
    description: '布达拉宫坐落在拉萨红山之上，是西藏最具代表性的宫堡式建筑群。它由白宫、红宫及附属建筑组成，兼具宗教、政治和艺术价值，远望层叠而上的白红建筑，是高原文化旅行的标志性景观。',
  },
  {
    name: '鼓浪屿',
    city: '厦门',
    region: '中国',
    wiki: '鼓浪屿',
    description: '鼓浪屿位于厦门岛西南侧，以海岛街巷、近代建筑、音乐文化和滨海景观著称。岛上保留了多种风格的历史建筑，适合步行游览、看海、拍照和体验较轻松的城市度假氛围。',
  },
  {
    name: '埃菲尔铁塔',
    city: '巴黎',
    region: '法国',
    wiki: '埃菲尔铁塔',
    description: '埃菲尔铁塔位于法国巴黎战神广场，是为1889年世界博览会建造的铁结构塔，也是巴黎最具辨识度的城市地标。铁塔与塞纳河、夏约宫和城市街区共同构成经典视角，是欧洲城市旅行中最受关注的建筑景观之一。',
  },
  {
    name: '富士山',
    city: '山梨 / 静冈',
    region: '日本',
    wiki: '富士山',
    description: '富士山是日本最高峰，也是日本文化中极具象征意义的自然景观。山体轮廓对称清晰，周边的河口湖、忍野八海、新仓山浅间公园等观景点形成了丰富的旅行路线，适合自然风光和摄影旅行。',
  },
  {
    name: '圣托里尼',
    city: '爱琴海',
    region: '希腊',
    wiki: '圣托里尼',
    description: '圣托里尼位于希腊爱琴海，以火山岛地貌、白色房屋、蓝顶教堂和悬崖日落闻名。费拉、伊亚等聚落沿火山口边缘展开，海景、建筑和日落共同构成了地中海海岛旅行的经典画面。',
  },
  {
    name: '泰姬陵',
    city: '阿格拉',
    region: '印度',
    wiki: '泰姬陵',
    description: '泰姬陵位于印度阿格拉，是莫卧儿王朝时期建造的陵墓建筑，以白色大理石、对称布局和精细装饰闻名。它融合了波斯、印度和伊斯兰建筑艺术，被视为世界建筑史上的重要代表。',
  },
  {
    name: '纽约时代广场',
    city: '纽约',
    region: '美国',
    wiki: '时代广场',
    description: '时代广场位于纽约曼哈顿中城，是剧院、商业广告屏幕和城市人流高度集中的区域。这里以大型电子屏、百老汇剧院和跨年倒计时活动闻名，展现了纽约强烈的都市节奏和商业文化。',
  },
  {
    name: '吉萨金字塔',
    city: '开罗',
    region: '埃及',
    wiki: '吉萨金字塔群',
    description: '吉萨金字塔群位于埃及开罗附近，是古埃及文明最具代表性的遗址之一，包括胡夫金字塔、哈夫拉金字塔、孟卡拉金字塔和狮身人面像等。其宏大的石构工程和悠久历史使其成为世界遗产旅行的重要目的地。',
  },
  {
    name: '卢浮宫',
    city: '巴黎',
    region: '法国',
    wiki: '卢浮宫',
    description: '卢浮宫位于巴黎塞纳河右岸，曾是法国王宫，如今是世界知名博物馆。馆藏覆盖古代文明、欧洲绘画、雕塑和装饰艺术等领域，玻璃金字塔入口与历史宫殿形成鲜明对照，是巴黎文化旅行的重要核心。',
  },
  {
    name: '罗马斗兽场',
    city: '罗马',
    region: '意大利',
    wiki: '罗马斗兽场',
    description: '罗马斗兽场是古罗马时期大型圆形竞技场，以拱券结构、看台体系和宏大规模闻名。它见证了古罗马公共娱乐、建筑工程和城市生活，也是罗马历史遗迹游览中最具代表性的建筑之一。',
  },
  {
    name: '马丘比丘',
    city: '库斯科',
    region: '秘鲁',
    wiki: '马丘比丘',
    description: '马丘比丘位于秘鲁安第斯山脉，是印加文明的重要遗址。古城依山势而建，梯田、石墙和山峰云雾共同构成独特景观，既有考古价值，也因壮丽的高山视野成为世界热门旅行目的地。',
  },
  {
    name: '悉尼歌剧院',
    city: '悉尼',
    region: '澳大利亚',
    wiki: '悉尼歌剧院',
    description: '悉尼歌剧院位于悉尼港，是20世纪现代建筑的重要代表。它以帆形屋顶、滨水位置和表演艺术功能闻名，与海港大桥共同构成悉尼城市天际线中最具辨识度的景观。',
  },
  {
    name: '吴哥窟',
    city: '暹粒',
    region: '柬埔寨',
    wiki: '吴哥窟',
    description: '吴哥窟位于柬埔寨暹粒，是吴哥古迹中规模和知名度最高的寺庙建筑。它以中央塔群、回廊浮雕、护城河和对称布局闻名，展现了高棉帝国时期宗教建筑和石刻艺术的高度成就。',
  },
  {
    name: '哈利法塔',
    city: '迪拜',
    region: '阿联酋',
    wiki: '哈利法塔',
    description: '哈利法塔位于迪拜市中心，是世界知名超高层建筑之一。它以极高的建筑高度、观景台和城市综合体景观闻名，周边的迪拜喷泉、购物中心和现代天际线共同构成迪拜旅行的核心区域。',
  },
  {
    name: '大峡谷',
    city: '亚利桑那',
    region: '美国',
    wiki: '大峡谷国家公园',
    description: '大峡谷国家公园位于美国亚利桑那州，以科罗拉多河长期侵蚀形成的巨大峡谷景观闻名。峡壁层理、色彩和尺度极具震撼感，南缘观景点、徒步路线和日出日落景观都深受旅行者喜爱。',
  },
  {
    name: '新天鹅堡',
    city: '巴伐利亚',
    region: '德国',
    wiki: '新天鹅堡',
    description: '新天鹅堡位于德国巴伐利亚阿尔卑斯山麓，是19世纪浪漫主义城堡建筑的代表。它以童话般的外观、山地背景和室内装饰闻名，远眺时城堡与森林、湖泊和雪山形成经典欧洲风景。',
  },
])

const selected = ref<HeroPlace | null>(null)
const showModal = ref(false)

onMounted(() => {
  places.value.forEach(loadWikiDetails)
})

function enterAsGuest() {
  router.push('/app')
}

function goLogin() {
  router.push('/login?redirect=/app')
}

function goRegister() {
  router.push('/register')
}

function openPlace(place: HeroPlace) {
  selected.value = place
  showModal.value = true
}

function photoStyle(index: number) {
  const rings = [
    { count: 8, rx: 33, ry: 27, size: '96px', offset: -90, opacity: .1, blur: '3px', z: 1 },
    { count: 8, rx: 43, ry: 36, size: '108px', offset: -67.5, opacity: .32, blur: '1.2px', z: 1 },
    { count: 12, rx: 53, ry: 45, size: '124px', offset: -90, opacity: .72, blur: '0px', z: 2 },
  ]
  let cursor = 0
  let ring = rings[0]
  let localIndex = index
  for (const item of rings) {
    if (index < cursor + item.count) {
      ring = item
      localIndex = index - cursor
      break
    }
    cursor += item.count
  }
  const angle = ((ring.offset + localIndex * (360 / ring.count)) * Math.PI) / 180
  const rotate = Math.sin(angle) * 9
  return {
    '--x': `${Math.cos(angle) * ring.rx}vw`,
    '--y': `${Math.sin(angle) * ring.ry}vh`,
    '--size': ring.size,
    '--photo-opacity': String(ring.opacity),
    '--photo-blur': ring.blur,
    '--photo-z': String(ring.z),
    '--rotate': `${rotate.toFixed(1)}deg`,
    animationDelay: `${-(index % 8) * 0.42}s`,
  } as Record<string, string>
}

async function loadWikiDetails(place: HeroPlace) {
  for (const lang of ['zh', 'en']) {
    const controller = new AbortController()
    const timer = window.setTimeout(() => controller.abort(), 3500)
    try {
      const res = await fetch(`https://${lang}.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(place.wiki)}`, {
        signal: controller.signal,
      })
      if (!res.ok) continue
      const data = await res.json()
      const image = data?.originalimage?.source || data?.thumbnail?.source
      const extract = data?.extract
      if (typeof extract === 'string' && extract.length > 80) {
        place.description = extract
      }
      if (typeof image === 'string' && image.startsWith('http')) {
        place.image = image
        return
      }
    } catch {
      // Keep the local generated placeholder.
    } finally {
      window.clearTimeout(timer)
    }
  }
}
</script>
