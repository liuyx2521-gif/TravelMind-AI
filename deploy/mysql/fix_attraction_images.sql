USE travelmind_ai;

UPDATE attraction
SET cover_image = CASE
  WHEN tags LIKE '%海边%' OR tags LIKE '%海岛%' OR tags LIKE '%beach%' THEN 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%山%' OR tags LIKE '%雪山%' OR tags LIKE '%徒步%' OR tags LIKE '%mountain%' THEN 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%湖%' OR tags LIKE '%water%' OR tags LIKE '%运河%' THEN 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%古镇%' OR tags LIKE '%历史%' OR tags LIKE '%oldtown%' THEN 'https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%博物馆%' OR tags LIKE '%艺术%' OR tags LIKE '%文化%' THEN 'https://images.unsplash.com/photo-1518998053901-5348d3961a04?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%美食%' OR tags LIKE '%夜市%' THEN 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%花%' THEN 'https://images.unsplash.com/photo-1490750967868-88aa4486c946?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%沙漠%' OR tags LIKE '%desert%' THEN 'https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%雪%' OR tags LIKE '%snow%' THEN 'https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%乐园%' THEN 'https://images.unsplash.com/photo-1502136969935-8d8eef54d77b?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%温泉%' THEN 'https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=1200&q=80'
  WHEN tags LIKE '%夜景%' THEN 'https://images.unsplash.com/photo-1519608487953-e999c86e7455?auto=format&fit=crop&w=1200&q=80'
  ELSE 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80'
END
WHERE cover_image LIKE 'https://source.unsplash.com/%';
