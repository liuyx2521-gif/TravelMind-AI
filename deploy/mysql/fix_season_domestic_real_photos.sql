USE travelmind_ai;

UPDATE attraction SET
  cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Jiuzhaigou_Valley.jpg?width=1400',
  score = 5.0,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '九寨沟';

UPDATE attraction SET
  cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Hulunbuir_grassland.jpg?width=1400',
  score = 5.0,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '呼伦贝尔大草原';

UPDATE attraction SET
  cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Qinghai_Lake.jpg?width=1400',
  score = 5.0,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '青海湖';

UPDATE attraction SET
  cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Li_River_in_Guilin.jpg?width=1400',
  score = 5.0,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '桂林漓江';

UPDATE attraction SET
  cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Heaven_Lake_of_Changbai_Mountain.jpg?width=1400',
  score = 5.0,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '长白山天池';

UPDATE attraction SET
  cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Zhangjiajie_National_Forest_Park.JPG?width=1400',
  score = 5.0,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '张家界国家森林公园';

UPDATE attraction SET
  cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Zhanqiao_Pier_Qingdao.jpg?width=1400',
  score = 4.95,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '青岛栈桥';

UPDATE attraction SET
  cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Gulangyu_Island.jpg?width=1400',
  score = 4.95,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '鼓浪屿';

UPDATE attraction SET
  cover_image = 'https://commons.wikimedia.org/wiki/Special:FilePath/Erhai_Lake.jpg?width=1400',
  score = 4.95,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '大理洱海';

UPDATE attraction SET
  cover_image = 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/West_Lake%2C_Hangzhou_2025.jpg/1280px-West_Lake%2C_Hangzhou_2025.jpg',
  score = 4.95,
  tags = CONCAT(tags, ',国内精选,真实照片')
WHERE name = '西湖';
