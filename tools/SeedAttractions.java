import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Locale;

public class SeedAttractions {
    record Place(String name, String province, String city, double lat, double lng, String type) {
    }

    record Type(String name, String desc, String tags, String season, int price, String imageQuery) {
    }

    static final List<Place> PLACES = List.of(
            new Place("北京", "北京", "北京", 39.9042, 116.4074, "city"),
            new Place("上海", "上海", "上海", 31.2304, 121.4737, "city"),
            new Place("杭州", "浙江", "杭州", 30.2741, 120.1551, "lake"),
            new Place("苏州", "江苏", "苏州", 31.2989, 120.5853, "garden"),
            new Place("南京", "江苏", "南京", 32.0603, 118.7969, "history"),
            new Place("厦门", "福建", "厦门", 24.4798, 118.0894, "beach"),
            new Place("青岛", "山东", "青岛", 36.0671, 120.3826, "beach"),
            new Place("三亚", "海南", "三亚", 18.2528, 109.5119, "beach"),
            new Place("广州", "广东", "广州", 23.1291, 113.2644, "food"),
            new Place("深圳", "广东", "深圳", 22.5431, 114.0579, "city"),
            new Place("成都", "四川", "成都", 30.5728, 104.0668, "food"),
            new Place("重庆", "重庆", "重庆", 29.5630, 106.5516, "night"),
            new Place("西安", "陕西", "西安", 34.3416, 108.9398, "history"),
            new Place("桂林", "广西", "桂林", 25.2736, 110.2900, "mountain"),
            new Place("大理", "云南", "大理", 25.6065, 100.2676, "lake"),
            new Place("丽江", "云南", "丽江", 26.8721, 100.2296, "oldtown"),
            new Place("昆明", "云南", "昆明", 25.0389, 102.7183, "flower"),
            new Place("张家界", "湖南", "张家界", 29.1167, 110.4792, "mountain"),
            new Place("长沙", "湖南", "长沙", 28.2282, 112.9388, "food"),
            new Place("武汉", "湖北", "武汉", 30.5928, 114.3055, "lake"),
            new Place("洛阳", "河南", "洛阳", 34.6197, 112.4540, "history"),
            new Place("开封", "河南", "开封", 34.7973, 114.3076, "history"),
            new Place("哈尔滨", "黑龙江", "哈尔滨", 45.8038, 126.5349, "snow"),
            new Place("长春", "吉林", "长春", 43.8171, 125.3235, "snow"),
            new Place("延边", "吉林", "延边", 42.9048, 129.5158, "snow"),
            new Place("沈阳", "辽宁", "沈阳", 41.8057, 123.4315, "history"),
            new Place("大连", "辽宁", "大连", 38.9140, 121.6147, "beach"),
            new Place("天津", "天津", "天津", 39.3434, 117.3616, "city"),
            new Place("秦皇岛", "河北", "秦皇岛", 39.9354, 119.5996, "beach"),
            new Place("承德", "河北", "承德", 40.9515, 117.9634, "history"),
            new Place("济南", "山东", "济南", 36.6512, 117.1201, "spring"),
            new Place("泰安", "山东", "泰安", 36.2000, 117.0876, "mountain"),
            new Place("福州", "福建", "福州", 26.0745, 119.2965, "food"),
            new Place("泉州", "福建", "泉州", 24.8741, 118.6757, "history"),
            new Place("宁波", "浙江", "宁波", 29.8683, 121.5440, "beach"),
            new Place("舟山", "浙江", "舟山", 29.9853, 122.2072, "beach"),
            new Place("黄山", "安徽", "黄山", 30.1408, 118.1660, "mountain"),
            new Place("合肥", "安徽", "合肥", 31.8206, 117.2272, "lake"),
            new Place("南昌", "江西", "南昌", 28.6829, 115.8582, "history"),
            new Place("婺源", "江西", "上饶", 29.2480, 117.8619, "oldtown"),
            new Place("贵阳", "贵州", "贵阳", 26.6470, 106.6302, "mountain"),
            new Place("安顺", "贵州", "安顺", 26.2456, 105.9322, "waterfall"),
            new Place("拉萨", "西藏", "拉萨", 29.6520, 91.1721, "culture"),
            new Place("林芝", "西藏", "林芝", 29.6488, 94.3615, "flower"),
            new Place("西宁", "青海", "西宁", 36.6171, 101.7782, "lake"),
            new Place("海北", "青海", "海北", 36.9541, 100.9000, "lake"),
            new Place("兰州", "甘肃", "兰州", 36.0611, 103.8343, "food"),
            new Place("敦煌", "甘肃", "酒泉", 40.1421, 94.6618, "desert"),
            new Place("银川", "宁夏", "银川", 38.4872, 106.2309, "desert"),
            new Place("乌鲁木齐", "新疆", "乌鲁木齐", 43.8256, 87.6168, "snow"),
            new Place("喀什", "新疆", "喀什", 39.4704, 75.9898, "oldtown"),
            new Place("伊犁", "新疆", "伊犁", 43.9169, 81.3241, "flower"),
            new Place("呼伦贝尔", "内蒙古", "呼伦贝尔", 49.2116, 119.7658, "grassland"),
            new Place("呼和浩特", "内蒙古", "呼和浩特", 40.8426, 111.7492, "grassland"),
            new Place("太原", "山西", "太原", 37.8706, 112.5489, "history"),
            new Place("平遥", "山西", "晋中", 37.1896, 112.1763, "oldtown"),
            new Place("东京", "日本", "东京", 35.6762, 139.6503, "city"),
            new Place("京都", "日本", "京都", 35.0116, 135.7681, "culture"),
            new Place("大阪", "日本", "大阪", 34.6937, 135.5023, "food"),
            new Place("札幌", "日本", "札幌", 43.0618, 141.3545, "snow"),
            new Place("首尔", "韩国", "首尔", 37.5665, 126.9780, "city"),
            new Place("釜山", "韩国", "釜山", 35.1796, 129.0756, "beach"),
            new Place("曼谷", "泰国", "曼谷", 13.7563, 100.5018, "food"),
            new Place("清迈", "泰国", "清迈", 18.7883, 98.9853, "culture"),
            new Place("普吉岛", "泰国", "普吉", 7.8804, 98.3923, "beach"),
            new Place("新加坡", "新加坡", "新加坡", 1.3521, 103.8198, "city"),
            new Place("吉隆坡", "马来西亚", "吉隆坡", 3.1390, 101.6869, "city"),
            new Place("巴厘岛", "印度尼西亚", "巴厘岛", -8.3405, 115.0920, "beach"),
            new Place("河内", "越南", "河内", 21.0278, 105.8342, "oldtown"),
            new Place("胡志明市", "越南", "胡志明市", 10.8231, 106.6297, "city"),
            new Place("暹粒", "柬埔寨", "暹粒", 13.3633, 103.8564, "culture"),
            new Place("马尼拉", "菲律宾", "马尼拉", 14.5995, 120.9842, "city"),
            new Place("长滩岛", "菲律宾", "长滩岛", 11.9674, 121.9248, "beach"),
            new Place("马尔代夫", "马尔代夫", "马累", 4.1755, 73.5093, "beach"),
            new Place("迪拜", "阿联酋", "迪拜", 25.2048, 55.2708, "desert"),
            new Place("伊斯坦布尔", "土耳其", "伊斯坦布尔", 41.0082, 28.9784, "history"),
            new Place("巴黎", "法国", "巴黎", 48.8566, 2.3522, "culture"),
            new Place("伦敦", "英国", "伦敦", 51.5072, -0.1276, "city"),
            new Place("罗马", "意大利", "罗马", 41.9028, 12.4964, "history"),
            new Place("威尼斯", "意大利", "威尼斯", 45.4408, 12.3155, "water"),
            new Place("佛罗伦萨", "意大利", "佛罗伦萨", 43.7696, 11.2558, "culture"),
            new Place("巴塞罗那", "西班牙", "巴塞罗那", 41.3874, 2.1686, "beach"),
            new Place("马德里", "西班牙", "马德里", 40.4168, -3.7038, "culture"),
            new Place("里斯本", "葡萄牙", "里斯本", 38.7223, -9.1393, "beach"),
            new Place("阿姆斯特丹", "荷兰", "阿姆斯特丹", 52.3676, 4.9041, "water"),
            new Place("布拉格", "捷克", "布拉格", 50.0755, 14.4378, "oldtown"),
            new Place("维也纳", "奥地利", "维也纳", 48.2082, 16.3738, "culture"),
            new Place("苏黎世", "瑞士", "苏黎世", 47.3769, 8.5417, "lake"),
            new Place("因特拉肯", "瑞士", "因特拉肯", 46.6863, 7.8632, "mountain"),
            new Place("慕尼黑", "德国", "慕尼黑", 48.1351, 11.5820, "culture"),
            new Place("柏林", "德国", "柏林", 52.5200, 13.4050, "history"),
            new Place("雅典", "希腊", "雅典", 37.9838, 23.7275, "history"),
            new Place("圣托里尼", "希腊", "圣托里尼", 36.3932, 25.4615, "beach"),
            new Place("开罗", "埃及", "开罗", 30.0444, 31.2357, "history"),
            new Place("开普敦", "南非", "开普敦", -33.9249, 18.4241, "beach"),
            new Place("纽约", "美国", "纽约", 40.7128, -74.0060, "city"),
            new Place("洛杉矶", "美国", "洛杉矶", 34.0522, -118.2437, "beach"),
            new Place("旧金山", "美国", "旧金山", 37.7749, -122.4194, "city"),
            new Place("拉斯维加斯", "美国", "拉斯维加斯", 36.1716, -115.1391, "desert"),
            new Place("檀香山", "美国", "檀香山", 21.3069, -157.8583, "beach"),
            new Place("温哥华", "加拿大", "温哥华", 49.2827, -123.1207, "mountain"),
            new Place("班夫", "加拿大", "班夫", 51.1784, -115.5708, "mountain"),
            new Place("多伦多", "加拿大", "多伦多", 43.6532, -79.3832, "city"),
            new Place("墨西哥城", "墨西哥", "墨西哥城", 19.4326, -99.1332, "history"),
            new Place("坎昆", "墨西哥", "坎昆", 21.1619, -86.8515, "beach"),
            new Place("里约热内卢", "巴西", "里约热内卢", -22.9068, -43.1729, "beach"),
            new Place("布宜诺斯艾利斯", "阿根廷", "布宜诺斯艾利斯", -34.6037, -58.3816, "city"),
            new Place("悉尼", "澳大利亚", "悉尼", -33.8688, 151.2093, "beach"),
            new Place("墨尔本", "澳大利亚", "墨尔本", -37.8136, 144.9631, "city"),
            new Place("皇后镇", "新西兰", "皇后镇", -45.0312, 168.6626, "mountain"),
            new Place("奥克兰", "新西兰", "奥克兰", -36.8509, 174.7645, "beach")
    );

    static final List<Type> TYPES = List.of(
            new Type("历史文化区", "适合了解城市历史、建筑细节和地方故事，安排半天到一天比较舒服。", "历史,建筑,文化,摄影", "春秋冬", 60, "historic landmark"),
            new Type("城市观景台", "适合看城市天际线、夜景和日落，适合情侣和第一次到访。", "城市,夜景,摄影,情侣", "全年", 120, "city skyline"),
            new Type("滨海步道", "适合看海、散步、骑行和拍照，傍晚体验更好。", "海边,日落,骑行,摄影", "夏秋", 0, "beach sunset"),
            new Type("山景徒步线", "适合轻徒步、观景和自然摄影，建议穿舒适鞋并预留体力。", "山,徒步,自然,摄影", "春夏秋", 80, "mountain trail"),
            new Type("湖畔公园", "适合慢旅行、野餐、骑行和亲子游，行程弹性大。", "湖景,公园,亲子,骑行", "春夏秋", 0, "lake park"),
            new Type("古镇街区", "适合逛小店、拍人文照片、吃地方小吃，夜晚更有氛围。", "古镇,小吃,人文,夜景", "春秋", 45, "old town street"),
            new Type("艺术博物馆", "适合雨天、亲子研学和城市文化体验，停留两到三小时。", "博物馆,艺术,亲子,室内", "全年", 50, "museum"),
            new Type("美食夜市", "适合集中体验地方风味，预算可控，晚上氛围最好。", "美食,夜市,夜景,小吃", "全年", 0, "street food night market"),
            new Type("主题乐园", "适合亲子、情侣和朋友出游，建议提前买票并错峰入园。", "乐园,亲子,情侣,刺激", "春夏秋", 299, "theme park"),
            new Type("自然保护区", "适合看自然生态、避暑和轻户外，建议预留完整一天。", "自然,避暑,摄影,户外", "夏秋", 120, "nature reserve"),
            new Type("花海庄园", "适合赏花、拍照和轻度休闲，不赶行程体验更好。", "花海,摄影,情侣,亲子", "春夏", 65, "flower garden"),
            new Type("温泉度假区", "适合放松、亲子和情侣度假，冬春季体验更明显。", "温泉,度假,亲子,情侣", "冬春", 180, "hot spring resort")
    );

    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("MYSQL_URL",
                "jdbc:mysql://localhost:3306/travelmind_ai?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false");
        String user = System.getenv().getOrDefault("MYSQL_USER", "root");
        String password = System.getenv().getOrDefault("MYSQL_PASSWORD", "123456");
        int limit = Integer.parseInt(System.getenv().getOrDefault("ATTRACTION_SEED_LIMIT", "900"));

        try (var conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            try (var ps = conn.prepareStatement("""
                    INSERT INTO attraction
                    (name, province, city, description, cover_image, latitude, longitude, price, best_season, open_time, score, tags)
                    SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                    WHERE NOT EXISTS (SELECT 1 FROM attraction WHERE name = ? AND city = ?)
                    """)) {
                int count = 0;
                for (Place place : PLACES) {
                    for (Type type : TYPES) {
                        if (count >= limit) break;
                        insert(ps, place, type, count++);
                    }
                    if (count >= limit) break;
                }
                ps.executeBatch();
            }
            conn.commit();
            try (var st = conn.createStatement();
                 var rs = st.executeQuery("SELECT COUNT(*) FROM attraction WHERE deleted = 0")) {
                if (rs.next()) System.out.println("attraction_count=" + rs.getInt(1));
            }
        }
    }

    private static void insert(PreparedStatement ps, Place place, Type type, int index) throws Exception {
        String name = place.name + type.name;
        String description = place.name + type.name + "，" + type.desc + "关键词：" + type.tags + "。";
        String query = (place.type + "," + type.imageQuery).replace(" ", "%20");
        String image = "https://source.unsplash.com/1200x800/?" + query + "&sig=" + index;
        double lat = place.lat + ((index % 9) - 4) * 0.006;
        double lng = place.lng + ((index % 7) - 3) * 0.006;
        double score = 4.2 + (index % 8) * 0.1;

        ps.setString(1, name);
        ps.setString(2, place.province);
        ps.setString(3, place.city);
        ps.setString(4, description);
        ps.setString(5, image);
        ps.setBigDecimal(6, BigDecimal.valueOf(lat).setScale(7, java.math.RoundingMode.HALF_UP));
        ps.setBigDecimal(7, BigDecimal.valueOf(lng).setScale(7, java.math.RoundingMode.HALF_UP));
        ps.setBigDecimal(8, BigDecimal.valueOf(type.price));
        ps.setString(9, type.season);
        ps.setString(10, type.price == 0 ? "全天" : "09:00-18:00");
        ps.setBigDecimal(11, BigDecimal.valueOf(score).setScale(1, java.math.RoundingMode.HALF_UP));
        ps.setString(12, (type.tags + "," + place.type).toLowerCase(Locale.ROOT));
        ps.setString(13, name);
        ps.setString(14, place.city);
        ps.addBatch();
    }
}
