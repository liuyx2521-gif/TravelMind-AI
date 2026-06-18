USE travelmind_ai;

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '杭州西子湖四季酒店', '杭州', '灵隐路5号，近西湖景区', 1880, 4.8, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80', 120.1250, 30.2500
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '杭州西子湖四季酒店');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '杭州湖滨银泰亚朵酒店', '杭州', '湖滨商圈，步行可达西湖', 520, 4.6, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80', 120.1650, 30.2550
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '杭州湖滨银泰亚朵酒店');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '青岛海景花园大酒店', '青岛', '东海中路，近海滨步道', 698, 4.7, 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80', 120.3950, 36.0640
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '青岛海景花园大酒店');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '厦门鼓浪屿李家庄酒店', '厦门', '鼓浪屿岛内，近菽庄花园', 760, 4.7, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1200&q=80', 118.0660, 24.4450
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '厦门鼓浪屿李家庄酒店');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '海北青海湖观景客栈', '海北', '青海湖二郎剑景区附近', 380, 4.5, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80', 100.1550, 36.9000
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '海北青海湖观景客栈');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '呼伦贝尔草原度假酒店', '呼伦贝尔', '陈巴尔虎旗草原景区附近', 560, 4.6, 'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1200&q=80', 119.7600, 49.2050
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '呼伦贝尔草原度假酒店');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '九寨沟沟口度假酒店', '阿坝', '九寨沟景区沟口附近', 620, 4.6, 'https://images.unsplash.com/photo-1568084680786-a84f91d1153c?auto=format&fit=crop&w=1200&q=80', 103.9200, 33.2580
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '九寨沟沟口度假酒店');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '张家界武陵源溪布街酒店', '张家界', '武陵源区，近国家森林公园入口', 430, 4.5, 'https://images.unsplash.com/photo-1563911302283-d2bc129e7570?auto=format&fit=crop&w=1200&q=80', 110.4800, 29.1200
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '张家界武陵源溪布街酒店');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '桂林漓江瀑布大酒店', '桂林', '杉湖北路，近漓江和象鼻山', 520, 4.6, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80', 110.2920, 25.2760
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '桂林漓江瀑布大酒店');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '大理洱海海景民宿', '大理', '洱海生态廊道附近', 480, 4.6, 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80', 100.1900, 25.7900
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '大理洱海海景民宿');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '丽江古城花间堂', '丽江', '丽江古城内，近四方街', 690, 4.7, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80', 100.2400, 26.8750
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '丽江古城花间堂');

INSERT INTO hotel (name, city, address, price, score, cover, longitude, latitude)
SELECT '北京王府井文华东方酒店', '北京', '王府井商圈，近故宫', 1680, 4.8, 'https://images.unsplash.com/photo-1561501900-3701fa6a0864?auto=format&fit=crop&w=1200&q=80', 116.4100, 39.9150
WHERE NOT EXISTS (SELECT 1 FROM hotel WHERE name = '北京王府井文华东方酒店');
