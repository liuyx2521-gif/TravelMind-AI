USE travelmind_ai;

SET @fake_attraction_pattern = '(历史文化区|城市观景台|滨海步道|山景徒步线|湖畔公园|古镇街区|艺术博物馆|美食夜市|主题乐园|自然保护区|花海庄园|温泉度假区)$';

DELETE cp
FROM check_point cp
JOIN attraction a ON a.id = cp.attraction_id
WHERE a.name REGEXP @fake_attraction_pattern;

DELETE f
FROM favorite f
JOIN attraction a ON a.id = f.target_id
WHERE f.target_type = 'ATTRACTION'
  AND a.name REGEXP @fake_attraction_pattern;

DELETE h
FROM browse_history h
JOIN attraction a ON a.id = h.target_id
WHERE h.target_type = 'ATTRACTION'
  AND a.name REGEXP @fake_attraction_pattern;

DELETE FROM attraction
WHERE name REGEXP @fake_attraction_pattern;

DELETE h
FROM browse_history h
LEFT JOIN attraction a ON a.id = h.target_id
WHERE h.target_type = 'ATTRACTION'
  AND a.id IS NULL;

DELETE f
FROM favorite f
LEFT JOIN attraction a ON a.id = f.target_id
WHERE f.target_type = 'ATTRACTION'
  AND a.id IS NULL;

UPDATE attraction
SET tags = IF(tags LIKE '%真实景点%', tags, CONCAT(IFNULL(tags, ''), ',真实景点'))
WHERE deleted = 0;
