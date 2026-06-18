USE travelmind_ai;

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '上海迪士尼乐园', '上海', '上海', '中国内地迪士尼度假区核心乐园，适合亲子、情侣、巡游、烟花和沉浸式主题游玩。', 'https://images.unsplash.com/photo-1545579133-99bb5ab189bd?auto=format&fit=crop&w=1200&q=80', 31.1434, 121.6579, 475, '春秋冬', '08:30-21:30', 4.8, '迪士尼,主题乐园,亲子,情侣,烟花,上海'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '上海迪士尼乐园');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '上海迪士尼奇幻童话城堡', '上海', '上海', '上海迪士尼标志性城堡，适合拍照、夜景和烟花前后打卡。', 'https://images.unsplash.com/photo-1560617544-b4f287789e24?auto=format&fit=crop&w=1200&q=80', 31.1440, 121.6576, 0, '全年', '随乐园开放', 4.9, '迪士尼,城堡,摄影,夜景,上海'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '上海迪士尼奇幻童话城堡');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '上海迪士尼疯狂动物城', '上海', '上海', '全球首个疯狂动物城主题园区，适合沉浸式街区拍照和热门项目体验。', 'https://images.unsplash.com/photo-1502136969935-8d8eef54d77b?auto=format&fit=crop&w=1200&q=80', 31.1450, 121.6568, 0, '全年', '随乐园开放', 4.8, '迪士尼,疯狂动物城,亲子,打卡,上海'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '上海迪士尼疯狂动物城');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '上海迪士尼创极速光轮', '上海', '上海', '明日世界高速过山车项目，适合喜欢刺激和未来感夜景的游客。', 'https://images.unsplash.com/photo-1513889961551-628c1e5e2ee9?auto=format&fit=crop&w=1200&q=80', 31.1455, 121.6591, 0, '全年', '随乐园开放', 4.7, '迪士尼,过山车,刺激,夜景,上海'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '上海迪士尼创极速光轮');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '上海迪士尼加勒比海盗沉落宝藏之战', '上海', '上海', '沉浸式室内船 ride，视效和故事体验强，适合多数年龄段。', 'https://images.unsplash.com/photo-1533104816931-20fa691ff6ca?auto=format&fit=crop&w=1200&q=80', 31.1428, 121.6558, 0, '全年', '随乐园开放', 4.8, '迪士尼,加勒比海盗,室内项目,亲子,上海'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '上海迪士尼加勒比海盗沉落宝藏之战');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '上海迪士尼小镇', '上海', '上海', '乐园外商业街区，适合餐饮、购物、看湖景和不入园轻打卡。', 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=80', 31.1417, 121.6604, 0, '全年', '10:00-22:00', 4.6, '迪士尼,美食,购物,夜景,上海'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '上海迪士尼小镇');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '香港迪士尼乐园', '中国香港', '香港', '香港大屿山迪士尼乐园，适合亲子、城堡打卡、巡游和烟花。', 'https://images.unsplash.com/photo-1545579133-99bb5ab189bd?auto=format&fit=crop&w=1200&q=80', 22.3130, 114.0413, 640, '秋冬春', '10:00-21:00', 4.7, '迪士尼,香港,主题乐园,亲子,烟花'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '香港迪士尼乐园');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '香港迪士尼冰雪奇缘世界', '中国香港', '香港', '冰雪奇缘主题园区，适合亲子、情侣、角色互动和城堡风格拍照。', 'https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?auto=format&fit=crop&w=1200&q=80', 22.3141, 114.0426, 0, '全年', '随乐园开放', 4.8, '迪士尼,香港,冰雪奇缘,亲子,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '香港迪士尼冰雪奇缘世界');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '香港迪士尼迷离庄园', '中国香港', '香港', '香港迪士尼原创室内项目，故事性强，适合不喜欢强刺激的游客。', 'https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=1200&q=80', 22.3125, 114.0433, 0, '全年', '随乐园开放', 4.7, '迪士尼,香港,室内项目,故事,亲子'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '香港迪士尼迷离庄园');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '香港迪士尼灰熊山谷', '中国香港', '香港', '西部矿山主题区域，适合轻刺激项目、家庭游和拍照。', 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80', 22.3120, 114.0440, 0, '全年', '随乐园开放', 4.6, '迪士尼,香港,过山车,山景,亲子'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '香港迪士尼灰熊山谷');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '东京迪士尼乐园', '日本', '东京', '东京迪士尼度假区经典乐园，适合亲子、城堡、巡游和日式服务体验。', 'https://images.unsplash.com/photo-1545579133-99bb5ab189bd?auto=format&fit=crop&w=1200&q=80', 35.6329, 139.8804, 460, '春秋冬', '09:00-21:00', 4.9, '迪士尼,日本,东京,主题乐园,亲子'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '东京迪士尼乐园');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '东京迪士尼海洋', '日本', '东京', '以海洋冒险为主题的迪士尼乐园，氛围独特，适合情侣、朋友和深度玩家。', 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80', 35.6267, 139.8851, 460, '春秋冬', '09:00-21:00', 4.9, '迪士尼,日本,东京,海洋,情侣,主题乐园'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '东京迪士尼海洋');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '东京迪士尼美女与野兽城堡奇缘', '日本', '东京', '东京迪士尼热门沉浸式项目，适合童话主题和拍照打卡。', 'https://images.unsplash.com/photo-1560617544-b4f287789e24?auto=format&fit=crop&w=1200&q=80', 35.6337, 139.8798, 0, '全年', '随乐园开放', 4.8, '迪士尼,日本,美女与野兽,城堡,亲子'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '东京迪士尼美女与野兽城堡奇缘');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '东京迪士尼海洋梦幻泉乡', '日本', '东京', '东京迪士尼海洋新主题港区，适合冰雪奇缘、长发公主和彼得潘主题体验。', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80', 35.6279, 139.8816, 0, '全年', '随乐园开放', 4.8, '迪士尼,日本,东京,梦幻泉乡,海洋'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '东京迪士尼海洋梦幻泉乡');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '加州迪士尼乐园', '美国', '阿纳海姆', '全球首座迪士尼乐园，适合经典城堡、怀旧项目和迪士尼粉丝朝圣。', 'https://images.unsplash.com/photo-1545579133-99bb5ab189bd?auto=format&fit=crop&w=1200&q=80', 33.8121, -117.9190, 760, '春秋冬', '08:00-24:00', 4.9, '迪士尼,美国,加州,主题乐园,城堡'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '加州迪士尼乐园');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '迪士尼加州冒险乐园', '美国', '阿纳海姆', '皮克斯、漫威和加州风情主题乐园，适合刺激项目和夜间秀。', 'https://images.unsplash.com/photo-1513889961551-628c1e5e2ee9?auto=format&fit=crop&w=1200&q=80', 33.8059, -117.9189, 760, '春秋冬', '08:00-22:00', 4.8, '迪士尼,美国,加州,漫威,皮克斯,过山车'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '迪士尼加州冒险乐园');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '加州迪士尼星球大战银河边缘', '美国', '阿纳海姆', '星球大战沉浸式主题区，适合科幻迷、夜景和高完成度场景打卡。', 'https://images.unsplash.com/photo-1517976487492-5750f3195933?auto=format&fit=crop&w=1200&q=80', 33.8146, -117.9216, 0, '全年', '随乐园开放', 4.9, '迪士尼,美国,星球大战,科幻,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '加州迪士尼星球大战银河边缘');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '奥兰多魔法王国', '美国', '奥兰多', '华特迪士尼世界核心乐园，城堡、巡游和烟花体验最经典。', 'https://images.unsplash.com/photo-1560617544-b4f287789e24?auto=format&fit=crop&w=1200&q=80', 28.4177, -81.5812, 850, '冬春秋', '09:00-22:00', 4.9, '迪士尼,美国,奥兰多,魔法王国,城堡,烟花'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '奥兰多魔法王国');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '奥兰多EPCOT', '美国', '奥兰多', '科技、世界文化和节庆美食主题乐园，适合成年人、情侣和慢游。', 'https://images.unsplash.com/photo-1518998053901-5348d3961a04?auto=format&fit=crop&w=1200&q=80', 28.3747, -81.5494, 850, '冬春秋', '09:00-21:00', 4.8, '迪士尼,美国,奥兰多,EPCOT,美食,文化'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '奥兰多EPCOT');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '奥兰多迪士尼好莱坞影城', '美国', '奥兰多', '星球大战、玩具总动员和影视主题项目集中，适合热门项目打卡。', 'https://images.unsplash.com/photo-1517976487492-5750f3195933?auto=format&fit=crop&w=1200&q=80', 28.3575, -81.5583, 850, '冬春秋', '09:00-21:00', 4.8, '迪士尼,美国,奥兰多,影视,星球大战,玩具总动员'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '奥兰多迪士尼好莱坞影城');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '奥兰多迪士尼动物王国', '美国', '奥兰多', '动物、自然和潘多拉主题结合，适合亲子、自然和沉浸式景观。', 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80', 28.3554, -81.5904, 850, '冬春秋', '08:00-19:00', 4.8, '迪士尼,美国,奥兰多,动物王国,自然,亲子'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '奥兰多迪士尼动物王国');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '奥兰多潘多拉阿凡达世界', '美国', '奥兰多', '动物王国热门主题区，夜景和飞行项目体验强，适合摄影和沉浸式游玩。', 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80', 28.3550, -81.5920, 0, '全年', '随乐园开放', 4.9, '迪士尼,美国,奥兰多,阿凡达,夜景,自然'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '奥兰多潘多拉阿凡达世界');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '巴黎迪士尼乐园', '法国', '巴黎', '欧洲经典迪士尼乐园，适合城堡、巡游、童话街区和亲子旅行。', 'https://images.unsplash.com/photo-1560617544-b4f287789e24?auto=format&fit=crop&w=1200&q=80', 48.8738, 2.7758, 650, '春夏秋', '09:30-22:00', 4.7, '迪士尼,法国,巴黎,城堡,亲子,主题乐园'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '巴黎迪士尼乐园');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '巴黎迪士尼冒险世界', '法国', '巴黎', '巴黎迪士尼第二园区，聚合漫威、皮克斯、冰雪奇缘等沉浸式区域。', 'https://images.unsplash.com/photo-1502136969935-8d8eef54d77b?auto=format&fit=crop&w=1200&q=80', 48.8674, 2.7797, 650, '春夏秋', '09:30-21:00', 4.7, '迪士尼,法国,巴黎,冒险世界,漫威,皮克斯,冰雪奇缘'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '巴黎迪士尼冒险世界');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '巴黎迪士尼冰雪奇缘世界', '法国', '巴黎', '巴黎迪士尼冰雪奇缘主题区，适合亲子、情侣和城堡湖景打卡。', 'https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?auto=format&fit=crop&w=1200&q=80', 48.8679, 2.7814, 0, '全年', '随乐园开放', 4.8, '迪士尼,法国,巴黎,冰雪奇缘,亲子,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '巴黎迪士尼冰雪奇缘世界');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '巴黎迪士尼复仇者联盟园区', '法国', '巴黎', '漫威主题区，适合超级英雄粉丝、互动体验和年轻游客。', 'https://images.unsplash.com/photo-1513889961551-628c1e5e2ee9?auto=format&fit=crop&w=1200&q=80', 48.8668, 2.7790, 0, '全年', '随乐园开放', 4.7, '迪士尼,法国,巴黎,漫威,复仇者联盟,打卡'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '巴黎迪士尼复仇者联盟园区');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '夏威夷迪士尼奥拉尼度假村', '美国', '檀香山', '迪士尼海岛度假酒店，适合亲子、海边、泳池和轻松度假。', 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80', 21.3399, -158.1237, 0, '全年', '全天', 4.7, '迪士尼,美国,夏威夷,海边,度假,亲子'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '夏威夷迪士尼奥拉尼度假村');
