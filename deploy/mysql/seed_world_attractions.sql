USE travelmind_ai;

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '埃菲尔铁塔', '法国', '巴黎', '巴黎地标建筑，适合城市观景、夜景摄影和第一次到访巴黎的经典路线。', 'https://source.unsplash.com/1200x800/?eiffel,tower,paris', 48.8584, 2.2945, 220, '春夏秋', '09:30-23:45', 4.8, '国外,巴黎,地标,夜景,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '埃菲尔铁塔');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '卢浮宫', '法国', '巴黎', '世界级艺术博物馆，适合艺术、历史和雨天室内行程。', 'https://source.unsplash.com/1200x800/?louvre,paris,museum', 48.8606, 2.3376, 135, '全年', '09:00-18:00', 4.9, '国外,巴黎,博物馆,艺术,历史'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '卢浮宫');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '大英博物馆', '英国', '伦敦', '伦敦代表性博物馆，馆藏丰富，适合文化探索和亲子研学。', 'https://source.unsplash.com/1200x800/?british,museum,london', 51.5194, -0.1270, 0, '全年', '10:00-17:00', 4.8, '国外,伦敦,博物馆,文化,亲子'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '大英博物馆');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '伦敦塔桥', '英国', '伦敦', '泰晤士河经典地标，适合城市漫步、夜景和建筑摄影。', 'https://source.unsplash.com/1200x800/?tower,bridge,london', 51.5055, -0.0754, 110, '春夏秋', '09:30-18:00', 4.7, '国外,伦敦,地标,河景,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '伦敦塔桥');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '罗马斗兽场', '意大利', '罗马', '古罗马代表建筑，适合历史文化路线和建筑摄影。', 'https://source.unsplash.com/1200x800/?colosseum,rome', 41.8902, 12.4922, 140, '春秋', '08:30-19:00', 4.8, '国外,罗马,历史,建筑,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '罗马斗兽场');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '圣马可广场', '意大利', '威尼斯', '威尼斯核心广场，适合水城漫步、建筑观赏和日落拍照。', 'https://source.unsplash.com/1200x800/?venice,san,marco', 45.4340, 12.3380, 0, '春秋', '全天', 4.7, '国外,威尼斯,水城,建筑,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '圣马可广场');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '圣托里尼伊亚小镇', '希腊', '圣托里尼', '蓝白建筑和爱琴海日落代表地，适合情侣、海岛和摄影行程。', 'https://source.unsplash.com/1200x800/?santorini,oia,sunset', 36.4618, 25.3753, 0, '夏秋', '全天', 4.9, '国外,海边,海岛,日落,情侣,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '圣托里尼伊亚小镇');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '巴塞罗那圣家堂', '西班牙', '巴塞罗那', '高迪代表建筑，适合建筑、美学和城市文化路线。', 'https://source.unsplash.com/1200x800/?sagrada,familia,barcelona', 41.4036, 2.1744, 210, '春夏秋', '09:00-18:00', 4.8, '国外,巴塞罗那,建筑,文化,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '巴塞罗那圣家堂');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '阿姆斯特丹运河区', '荷兰', '阿姆斯特丹', '适合骑行、运河游船和城市慢旅行，傍晚灯光很出片。', 'https://source.unsplash.com/1200x800/?amsterdam,canal', 52.3676, 4.9041, 0, '春夏秋', '全天', 4.7, '国外,阿姆斯特丹,运河,骑行,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '阿姆斯特丹运河区');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '布拉格老城广场', '捷克', '布拉格', '欧洲古城代表街区，适合建筑摄影、咖啡和夜景漫步。', 'https://source.unsplash.com/1200x800/?prague,old,town', 50.0870, 14.4208, 0, '春秋冬', '全天', 4.8, '国外,布拉格,古城,建筑,夜景'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '布拉格老城广场');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '纽约中央公园', '美国', '纽约', '纽约城市绿洲，适合散步、骑行、亲子和四季城市体验。', 'https://source.unsplash.com/1200x800/?central,park,new,york', 40.7829, -73.9654, 0, '全年', '06:00-01:00', 4.8, '国外,纽约,公园,骑行,亲子'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '纽约中央公园');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '自由女神像', '美国', '纽约', '纽约经典地标，适合第一次到访、海港观景和城市摄影。', 'https://source.unsplash.com/1200x800/?statue,of,liberty', 40.6892, -74.0445, 180, '春夏秋', '09:00-17:00', 4.7, '国外,纽约,地标,海港,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '自由女神像');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '金门大桥', '美国', '旧金山', '旧金山地标桥梁，适合海湾风景、自驾和日落摄影。', 'https://source.unsplash.com/1200x800/?golden,gate,bridge', 37.8199, -122.4783, 0, '春夏秋', '全天', 4.8, '国外,旧金山,地标,海边,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '金门大桥');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '好莱坞星光大道', '美国', '洛杉矶', '洛杉矶热门打卡点，适合城市娱乐、影视文化和街拍。', 'https://source.unsplash.com/1200x800/?hollywood,walk,fame', 34.1016, -118.3267, 0, '全年', '全天', 4.3, '国外,洛杉矶,影视,街拍,打卡'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '好莱坞星光大道');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '班夫露易丝湖', '加拿大', '班夫', '落基山湖泊代表，适合雪山、湖景、徒步和自然摄影。', 'https://source.unsplash.com/1200x800/?lake,louise,banff', 51.4254, -116.1773, 80, '夏秋冬', '全天', 4.9, '国外,班夫,湖景,雪山,徒步,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '班夫露易丝湖');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '悉尼歌剧院', '澳大利亚', '悉尼', '悉尼地标建筑，适合海港漫步、夜景和城市摄影。', 'https://source.unsplash.com/1200x800/?sydney,opera,house', -33.8568, 151.2153, 0, '全年', '全天', 4.8, '国外,悉尼,海港,建筑,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '悉尼歌剧院');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '皇后镇瓦卡蒂普湖', '新西兰', '皇后镇', '湖泊、雪山和户外体验集中地，适合自驾、徒步和摄影。', 'https://source.unsplash.com/1200x800/?queenstown,lake,wakatipu', -45.0312, 168.6626, 0, '夏秋冬', '全天', 4.9, '国外,皇后镇,湖景,雪山,户外'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '皇后镇瓦卡蒂普湖');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '巴厘岛乌鲁瓦图', '印度尼西亚', '巴厘岛', '海崖、日落和冲浪氛围代表地，适合海岛度假和情侣旅行。', 'https://source.unsplash.com/1200x800/?bali,uluwatu,sunset', -8.8291, 115.0849, 50, '夏秋', '08:00-19:00', 4.8, '国外,巴厘岛,海边,日落,情侣'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '巴厘岛乌鲁瓦图');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '曼谷大皇宫', '泰国', '曼谷', '曼谷经典文化地标，适合寺庙建筑、历史文化和初访路线。', 'https://source.unsplash.com/1200x800/?grand,palace,bangkok', 13.7500, 100.4913, 100, '冬春', '08:30-15:30', 4.7, '国外,曼谷,文化,建筑,寺庙'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '曼谷大皇宫');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '新加坡滨海湾花园', '新加坡', '新加坡', '新加坡未来感花园，适合夜景、亲子和城市打卡。', 'https://source.unsplash.com/1200x800/?gardens,by,the,bay,singapore', 1.2816, 103.8636, 120, '全年', '09:00-21:00', 4.8, '国外,新加坡,花园,夜景,亲子'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '新加坡滨海湾花园');
