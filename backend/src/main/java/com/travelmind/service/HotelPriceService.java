package com.travelmind.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HotelPriceService {
    public BigDecimal resolve(String name, String city, BigDecimal sourcePrice) {
        if (sourcePrice != null && sourcePrice.compareTo(BigDecimal.ZERO) > 0) {
            return sourcePrice;
        }
        return BigDecimal.valueOf(estimate(name, city));
    }

    private int estimate(String name, String city) {
        var base = 520;
        if (containsAny(name, "丽思", "四季", "文华", "瑞吉", "柏悦", "华尔道夫", "宝格丽", "瑰丽", "安缦", "半岛", "阿丽拉")) {
            base = 1680;
        } else if (containsAny(name, "万豪", "希尔顿", "洲际", "凯悦", "喜来登", "香格里拉", "君悦", "威斯汀", "豪华", "度假", "皇冠假日")) {
            base = 980;
        } else if (containsAny(name, "亚朵", "全季", "桔子", "丽枫", "智选", "假日", "和颐", "美居", "希岸")) {
            base = 460;
        } else if (containsAny(name, "汉庭", "如家", "7天", "速8", "格林豪泰", "锦江之星", "布丁", "宜必思")) {
            base = 240;
        }

        if (containsAny(city, "北京", "上海", "深圳", "三亚", "香港", "澳门")) {
            base = Math.round(base * 1.25f);
        } else if (containsAny(city, "杭州", "广州", "成都", "厦门", "南京", "苏州", "青岛")) {
            base = Math.round(base * 1.1f);
        } else if (containsAny(city, "重庆", "长沙", "西安", "武汉", "昆明")) {
            base = Math.round(base * 0.95f);
        }

        return Math.max(168, Math.round((base + Math.abs((name + city).hashCode() % 90)) / 10f) * 10);
    }

    private boolean containsAny(String text, String... words) {
        return text != null && List.of(words).stream().anyMatch(text::contains);
    }
}
