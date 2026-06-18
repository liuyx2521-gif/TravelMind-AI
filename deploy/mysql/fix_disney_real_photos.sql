USE travelmind_ai;

SET @shanghai_castle = 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/9a/18-03-12_ShanghaiDisney_013.jpg/1280px-18-03-12_ShanghaiDisney_013.jpg';
SET @hongkong_castle = 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/Hong_Kong_Disneyland_Castle.jpg/1280px-Hong_Kong_Disneyland_Castle.jpg';
SET @tokyo_sea = 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/59/Disney_Sea_in_the_Night.JPG/1280px-Disney_Sea_in_the_Night.JPG';
SET @magic_kingdom = 'https://commons.wikimedia.org/wiki/Special:FilePath/Cinderella_Castle,_Magic_Kingdom_(May_2,_2011).jpg?width=1280';
SET @paris_castle = 'https://commons.wikimedia.org/wiki/Special:FilePath/Sleeping_Beauty_Castle,_Disneyland,_Paris.jpg?width=1280';
SET @california_castle = 'https://commons.wikimedia.org/wiki/Special:FilePath/Sleeping_Beauty_Castle_at_Disneyland_2019.jpg?width=1280';
SET @epcot = 'https://commons.wikimedia.org/wiki/Special:FilePath/Spaceship_Earth_at_Epcot_2022.jpg?width=1280';
SET @theme_park = 'https://images.unsplash.com/photo-1502136969935-8d8eef54d77b?auto=format&fit=crop&w=1400&q=85';
SET @ride = 'https://images.unsplash.com/photo-1513889961551-628c1e5e2ee9?auto=format&fit=crop&w=1400&q=85';
SET @star_wars = 'https://images.unsplash.com/photo-1517976487492-5750f3195933?auto=format&fit=crop&w=1400&q=85';
SET @resort_beach = 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1400&q=85';
SET @winter = 'https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?auto=format&fit=crop&w=1400&q=85';
SET @waterfront = 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1400&q=85';

UPDATE attraction
SET cover_image = CASE
  WHEN name LIKE '上海迪士尼%' THEN @shanghai_castle
  WHEN name LIKE '香港迪士尼%' THEN @hongkong_castle
  WHEN name LIKE '东京迪士尼海洋%' THEN @tokyo_sea
  WHEN name LIKE '东京迪士尼%' THEN @tokyo_sea
  WHEN name LIKE '加州迪士尼%' THEN @california_castle
  WHEN name LIKE '迪士尼加州冒险%' THEN @theme_park
  WHEN name LIKE '奥兰多魔法王国%' THEN @magic_kingdom
  WHEN name LIKE '奥兰多EPCOT%' THEN @epcot
  WHEN name LIKE '奥兰多迪士尼好莱坞影城%' THEN @star_wars
  WHEN name LIKE '奥兰多迪士尼动物王国%' THEN 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1400&q=85'
  WHEN name LIKE '奥兰多潘多拉%' THEN 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1400&q=85'
  WHEN name LIKE '巴黎迪士尼%' THEN @paris_castle
  WHEN name LIKE '夏威夷迪士尼%' THEN @resort_beach
  WHEN tags LIKE '%冰雪奇缘%' THEN @winter
  WHEN tags LIKE '%星球大战%' THEN @star_wars
  WHEN tags LIKE '%过山车%' OR tags LIKE '%刺激%' THEN @ride
  WHEN tags LIKE '%海洋%' THEN @waterfront
  ELSE @theme_park
END
WHERE tags LIKE '%迪士尼%' OR name LIKE '%迪士尼%';
