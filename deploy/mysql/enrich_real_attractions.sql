USE travelmind_ai;

UPDATE attraction SET
description = '杭州西湖是城市湖景与江南园林气质结合最好的目的地之一，适合慢步行、骑行、坐船和拍照。推荐从断桥、白堤、平湖秋月一路走到苏堤，春季看柳岸花开，秋季看湖面与夕阳，预算友好，公共交通也很方便。',
cover_image = 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/West_Lake%2C_Hangzhou_2025.jpg/1280px-West_Lake%2C_Hangzhou_2025.jpg'
WHERE name = '西湖';

UPDATE attraction SET
description = '三亚湾海岸线开阔，节奏比热门核心景区更轻松，适合预算海边游、亲子散步、看日落和吃海鲜。傍晚沿椰梦长廊走一段体验最好，天气晴朗时海面色彩层次明显，适合安排在到达日或返程前的轻松半日行程。',
cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Sanya_Bay.jpg?width=1400'
WHERE name = '三亚湾';

UPDATE attraction SET description = '青岛栈桥是青岛最经典的海滨地标，连接老城街区、海岸线和远处小青岛景观。这里适合第一次到青岛时快速建立城市印象，也适合清晨拍海鸥、傍晚看海面光影，步行可串联中山路、天主教堂等老城目的地。' WHERE name = '青岛栈桥';
UPDATE attraction SET description = '鼓浪屿兼具海岛、历史建筑和慢生活氛围，适合不赶路地逛巷子、看万国建筑、喝咖啡和拍海景。建议避开正午高峰，选择清晨或傍晚上岛，日光岩、菽庄花园和海边小路可以组合成一条轻松的一日路线。' WHERE name = '鼓浪屿';
UPDATE attraction SET description = '青海湖是高原湖泊和辽阔公路风景的代表，夏季油菜花、蓝色湖面和远山层次非常出片。更适合自驾或包车游玩，建议预留足够时间适应海拔，环湖路线可以搭配茶卡盐湖、祁连草原形成西北环线。' WHERE name = '青海湖';
UPDATE attraction SET description = '呼伦贝尔大草原适合夏季自驾、亲子旅行和自然摄影，草原、湿地、边境小镇和牧场体验层次丰富。建议安排三到五天，选择海拉尔、额尔古纳、满洲里方向串联，日落时段的草原光线尤其适合拍照。' WHERE name = '呼伦贝尔大草原';
UPDATE attraction SET description = '九寨沟以高山湖泊、瀑布和彩林闻名，水色层次极强，夏季清凉，秋季色彩最丰富。景区面积大，建议早入园并按观光车线路分段游玩，长海、五花海、诺日朗瀑布是第一次到访最值得优先安排的点位。' WHERE name = '九寨沟';
UPDATE attraction SET description = '张家界国家森林公园以石英砂岩峰林著称，山体形态独特，适合喜欢自然景观、徒步和高空视角的游客。袁家界、天子山、金鞭溪可以组成经典路线，旺季建议提前规划入口和索道，避免在景区内反复折返。' WHERE name = '张家界国家森林公园';
UPDATE attraction SET description = '桂林漓江代表了中国山水画式的喀斯特风景，乘船或竹筏都能看到江面、群峰和村落的层次变化。经典体验是从桂林到阳朔方向游览，再搭配骑行十里画廊、看日落和逛西街，适合两到三天慢游。' WHERE name = '桂林漓江';
UPDATE attraction SET description = '大理洱海适合环湖骑行、自驾、住民宿和看日出日落，节奏轻松，适合情侣、朋友和家庭旅行。建议选择生态廊道、双廊、喜洲古镇等点位分段游玩，不必追求完整环湖，留出发呆和喝咖啡的时间体验更好。' WHERE name = '大理洱海';
UPDATE attraction SET description = '丽江古城以石板街巷、纳西文化和夜晚灯火氛围著称，适合慢逛、拍照和休闲旅行。建议白天走木府、四方街和小巷，傍晚找高处看古城屋顶与远山，行程可搭配玉龙雪山形成两到三天路线。' WHERE name = '丽江古城';
UPDATE attraction SET description = '峨眉山兼具自然山景、佛教文化、云海和冬季雪景，适合徒步、祈福和避暑旅行。体力充足可以分段徒步，时间紧则通过景区交通上金顶，清晨看云海和日出体验最好，冬季需要注意防滑和保暖。' WHERE name = '峨眉山';
UPDATE attraction SET description = '故宫博物院是北京最核心的历史文化目的地，建筑轴线、宫殿空间和馆藏展览都值得细看。建议提前预约，按中轴线进入，再根据兴趣选择钟表馆、珍宝馆或东西六宫，第一次游览建议预留至少半天。' WHERE name = '故宫博物院';
UPDATE attraction SET description = '长白山天池是火山口湖和高山地貌的代表，夏季清凉，冬季雪景震撼。天池天气变化快，能否看到湖面有一定运气成分，建议留出机动时间，并根据北坡、西坡开放情况选择路线。' WHERE name = '长白山天池';

UPDATE attraction SET description = '埃菲尔铁塔是巴黎最具识别度的地标，适合城市观景、夜景摄影和第一次到访巴黎的经典路线。白天可登塔看塞纳河与城市街区，夜晚在夏约宫或战神广场看灯光，搭配塞纳河游船体验更完整。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Tour_Eiffel_Wikimedia_Commons.jpg?width=1400' WHERE name = '埃菲尔铁塔';
UPDATE attraction SET description = '卢浮宫是世界级艺术博物馆，适合艺术、历史和室内行程。馆藏规模巨大，第一次参观建议提前选定重点展区，不必追求全看完，蒙娜丽莎、胜利女神、维纳斯和拿破仑三世套房可以作为经典路线。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Louvre_Museum_Wikimedia_Commons.jpg?width=1400' WHERE name = '卢浮宫';
UPDATE attraction SET description = '大英博物馆是伦敦最重要的博物馆之一，适合文化探索、亲子研学和雨天行程。展馆免费开放但内容密集，建议按古埃及、希腊罗马、亚洲展厅等主题分段参观，给重点展品留出阅读时间。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/British_Museum_from_NE_2.JPG?width=1400' WHERE name = '大英博物馆';
UPDATE attraction SET description = '伦敦塔桥横跨泰晤士河，是伦敦最经典的桥梁地标之一，适合河岸漫步、建筑摄影和夜景。可以从伦敦塔一路走到塔桥，再沿南岸前往市政厅和碎片大厦，傍晚光线最好。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Tower_Bridge_from_Shad_Thames.jpg?width=1400' WHERE name = '伦敦塔桥';
UPDATE attraction SET description = '罗马斗兽场是古罗马文明的代表建筑，适合历史文化路线和建筑摄影。建议和古罗马广场、帕拉蒂尼山一起安排，提前预约时段能减少排队，清晨或傍晚拍摄石材色彩更有层次。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Colosseum_in_Rome%2C_Italy_-_April_2007.jpg?width=1400' WHERE name = '罗马斗兽场';
UPDATE attraction SET description = '圣马可广场是威尼斯的核心公共空间，周围集合了圣马可大教堂、钟楼、总督宫和水城景观。适合清晨避开人群拍建筑，也适合傍晚沿海边步道看水面反光，建议和贡多拉或水上巴士体验搭配。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Piazza_San_Marco%2C_Venice.jpg?width=1400' WHERE name = '圣马可广场';
UPDATE attraction SET description = '圣托里尼伊亚小镇以蓝白建筑、悬崖酒店和爱琴海日落闻名，适合情侣、海岛度假和摄影。日落时段人流密集，建议提前到达观景点，也可以清晨拍摄安静街巷，体验会更轻松。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Oia%2C_Santorini_HDR_sunset.jpg?width=1400' WHERE name = '圣托里尼伊亚小镇';
UPDATE attraction SET description = '巴塞罗那圣家堂是高迪建筑美学的代表，外立面雕塑、彩色玻璃和室内光影都值得慢看。建议提前预约入场时段，上午和下午的光线效果不同，可以和米拉之家、巴特罗之家组成高迪主题路线。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Sagrada_Familia_01.jpg?width=1400' WHERE name = '巴塞罗那圣家堂';
UPDATE attraction SET description = '阿姆斯特丹运河区适合骑行、运河游船和城市慢旅行，桥梁、窄楼和水面倒影构成了典型城市风景。傍晚灯光亮起后氛围很好，建议用步行加电车串联博物馆区和九街街区。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Amsterdam_Canal_Prinsengracht.jpg?width=1400' WHERE name = '阿姆斯特丹运河区';
UPDATE attraction SET description = '布拉格老城广场是欧洲古城氛围浓缩的地点，天文钟、泰恩教堂和周边街巷都适合建筑摄影。白天适合看广场建筑细节，夜晚灯光更有童话感，可步行连接查理大桥和城堡区。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Prague_Old_Town_Square.jpg?width=1400' WHERE name = '布拉格老城广场';
UPDATE attraction SET description = '纽约中央公园是曼哈顿中部的大型城市绿洲，适合散步、骑行、野餐和亲子活动。四季景观不同，春秋尤其舒服，可以和大都会艺术博物馆、第五大道或自然历史博物馆搭配成一天路线。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Central_Park_New_York_City_New_York_23_crop.jpg?width=1400' WHERE name = '纽约中央公园';
UPDATE attraction SET description = '自由女神像是纽约港最具代表性的地标，适合第一次到访纽约时安排。可以选择登岛参观，也可以从炮台公园或斯塔滕岛渡轮远眺，若要登冠需更早预约。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Statue_of_Liberty_7.jpg?width=1400' WHERE name = '自由女神像';
UPDATE attraction SET description = '金门大桥是旧金山湾区最经典的地标，适合自驾、骑行和日落摄影。桥两侧观景点视角不同，雾气天气反而很有城市气质，建议和渔人码头、索萨利托或海岸步道一起安排。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Golden_Gate_Bridge_as_seen_from_Battery_East.jpg?width=1400' WHERE name = '金门大桥';
UPDATE attraction SET description = '好莱坞星光大道适合影视文化打卡和城市街拍，周边有中国剧院、杜比剧院和好莱坞标志远眺点。这里商业化程度较高，建议作为洛杉矶半日城市路线的一站，不必预留过长时间。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Hollywood_Walk_of_Fame.jpg?width=1400' WHERE name = '好莱坞星光大道';
UPDATE attraction SET description = '班夫露易丝湖以蓝绿色湖水、雪山和森林步道闻名，是加拿大落基山最经典的湖泊景观。夏季适合徒步和划船，冬季适合雪景和滑冰，旺季停车紧张，建议早到或乘坐接驳车。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Lake_Louise_17092005.jpg?width=1400' WHERE name = '班夫露易丝湖';
UPDATE attraction SET description = '悉尼歌剧院是澳大利亚最具代表性的建筑，适合海港漫步、建筑摄影和夜景。推荐从环形码头步行到皇家植物园，从不同角度看歌剧院和海港大桥，也可以预约内部导览了解建筑故事。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Sydney_Opera_House_Sails.jpg?width=1400' WHERE name = '悉尼歌剧院';
UPDATE attraction SET description = '皇后镇瓦卡蒂普湖集合了湖泊、雪山和户外体验，适合自驾、徒步、游船和极限运动。湖边步道非常适合傍晚散步，天气好时可以搭配天空缆车俯瞰小镇和湖湾。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Lake_Wakatipu_from_Queenstown.jpg?width=1400' WHERE name = '皇后镇瓦卡蒂普湖';
UPDATE attraction SET description = '巴厘岛乌鲁瓦图以海崖、冲浪海滩和日落氛围著称，适合海岛度假、情侣旅行和拍照。乌鲁瓦图寺和周边悬崖酒吧是经典组合，傍晚交通拥堵，建议提前出发。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Uluwatu_Temple_Bali.jpg?width=1400' WHERE name = '巴厘岛乌鲁瓦图';
UPDATE attraction SET description = '曼谷大皇宫是泰国王室建筑和佛教文化的重要地标，适合初访曼谷的文化路线。建筑细节华丽，参观时需注意着装要求，建议早晨前往避开高温，再搭配卧佛寺、郑王庙形成一日路线。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Grand_Palace_Bangkok.jpg?width=1400' WHERE name = '曼谷大皇宫';
UPDATE attraction SET description = '新加坡滨海湾花园以未来感花园、超级树和夜间灯光秀闻名，适合亲子、夜景和城市打卡。白天可看温室植物，晚上看灯光秀和滨海湾夜景，建议和金沙、鱼尾狮公园一起安排。', cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Gardens_by_the_Bay%2C_Singapore%2C_2012.jpg?width=1400' WHERE name = '新加坡滨海湾花园';

SET @travel_tip = '建议提前查看开放时间、票务和当日天气；如果是热门节假日出行，优先预约门票并预留交通缓冲时间。';
SET @pace_tip = '实际规划时可根据体力和同行人群调整节奏，亲子游客优先减少排队强度，情侣和朋友出行则可以把夜景、表演或特色餐饮放在傍晚之后。';

UPDATE attraction
SET description = REPLACE(description, CONCAT(@travel_tip, ' ', @travel_tip), @travel_tip);

UPDATE attraction
SET description = CONCAT(description, ' ', @travel_tip)
WHERE CHAR_LENGTH(description) < 90
  AND description NOT LIKE CONCAT('%', @travel_tip, '%');

UPDATE attraction
SET description = CONCAT(description, ' ', @pace_tip)
WHERE CHAR_LENGTH(description) < 105
  AND description NOT LIKE CONCAT('%', @pace_tip, '%');
