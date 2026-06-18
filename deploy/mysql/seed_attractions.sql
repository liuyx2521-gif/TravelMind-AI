USE travelmind_ai;

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '青岛栈桥', '山东', '青岛', '青岛海滨城市名片，适合夏季看海、散步、拍城市海岸线。', 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80', 36.0610, 120.3190, 0, '夏秋', '全天', 4.7, '海边,城市漫步,摄影,夏季'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '青岛栈桥');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '鼓浪屿', '福建', '厦门', '世界文化遗产海岛，适合慢旅行、建筑摄影、咖啡和海风。', 'https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80', 24.4476, 118.0679, 0, '春夏秋', '全天', 4.8, '海岛,文艺,摄影,夏季'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '鼓浪屿');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '青海湖', '青海', '海北', '中国大型内陆湖，夏季油菜花和湖景最出片，适合自驾。', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80', 36.8964, 100.1480, 90, '夏', '08:00-18:00', 4.8, '湖景,自驾,摄影,夏季'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '青海湖');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '呼伦贝尔大草原', '内蒙古', '呼伦贝尔', '夏季草原、蓝天、骑马和边境自驾路线，适合亲子和摄影。', 'https://images.unsplash.com/photo-1470770841072-f978cf4d019e?auto=format&fit=crop&w=1200&q=80', 49.2116, 119.7658, 0, '夏', '全天', 4.9, '草原,自驾,亲子,摄影,夏季'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '呼伦贝尔大草原');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '九寨沟', '四川', '阿坝', '高山湖泊和彩林闻名，夏季清凉，秋季色彩层次最佳。', 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80', 33.2600, 103.9180, 280, '夏秋', '07:30-17:00', 4.9, '山水,避暑,摄影,秋景'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '九寨沟');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '张家界国家森林公园', '湖南', '张家界', '峰林地貌代表，适合山景徒步、玻璃栈道和自然风光摄影。', 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80', 29.1170, 110.4790, 227, '春夏秋', '07:00-18:00', 4.8, '山,徒步,摄影,自然'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '张家界国家森林公园');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '桂林漓江', '广西', '桂林', '喀斯特山水代表，适合乘船、骑行和山水摄影。', 'https://images.unsplash.com/photo-1519451241324-20b4ea2c4220?auto=format&fit=crop&w=1200&q=80', 25.2736, 110.2900, 210, '春夏秋', '08:00-17:30', 4.8, '山水,乘船,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '桂林漓江');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '大理洱海', '云南', '大理', '适合环湖骑行、看日出、民宿度假和轻松慢旅行。', 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80', 25.7920, 100.1860, 0, '春夏秋', '全天', 4.7, '湖景,骑行,古城,慢旅行'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '大理洱海');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '丽江古城', '云南', '丽江', '古城街巷、雪山远景和夜晚氛围适合情侣、摄影和慢游。', 'https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=1200&q=80', 26.8768, 100.2401, 50, '春夏秋', '全天', 4.6, '古城,夜景,情侣,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '丽江古城');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '峨眉山', '四川', '乐山', '中国名山，夏季避暑，冬季云海雪景，适合徒步和祈福。', 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80', 29.5200, 103.3300, 160, '夏冬', '06:00-18:00', 4.7, '山,避暑,雪景,徒步'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '峨眉山');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '故宫博物院', '北京', '北京', '中国古代宫廷建筑群，适合历史文化、建筑摄影和亲子研学。', 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?auto=format&fit=crop&w=1200&q=80', 39.9163, 116.3972, 60, '春秋冬', '08:30-17:00', 4.9, '历史,建筑,亲子,摄影'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '故宫博物院');

INSERT INTO attraction (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
SELECT '长白山天池', '吉林', '延边', '火山口湖和高山景观，夏季清凉，冬季雪景震撼。', 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80', 42.0060, 128.0550, 125, '夏冬', '07:30-17:00', 4.8, '山,湖景,雪景,避暑'
WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = '长白山天池');
