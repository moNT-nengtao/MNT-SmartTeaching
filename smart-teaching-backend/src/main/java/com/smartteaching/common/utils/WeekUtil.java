package com.smartteaching.common.utils;

import com.alibaba.fastjson2.JSON;

import java.util.*;

/**
 * 周次工具类
 *
 * 功能：
 * 1. jsonToRangeStr      - JSON字符串转范围字符串（如 "[1,2,3,4,9,10,11,12]" → "1-4,9-12"）
 * 2. rangeStrToJson      - 范围字符串转JSON字符串（如 "1-4,9-12" → "[1,2,3,4,9,10,11,12]"）
 * 3. hasIntersection     - 判断两个JSON周次是否有交集（冲突检测）
 *
 * @author SmartTeaching
 * @since 2026-08-22
 */
public class WeekUtil {

    /**
     * 1. JSON字符串转范围字符串
     */
    public static String jsonToRangeStr(String weekJson) {
        List<Integer> weekList = parseJson(weekJson);
        if (weekList.isEmpty()) {
            return "";
        }
        return toRangeStr(weekList);
    }

    /**
     * 2. 范围字符串转JSON字符串
     */
    public static String rangeStrToJson(String rangeStr) {
        List<Integer> weekList = parseRange(rangeStr);
        if (weekList.isEmpty()) {
            return "[]";
        }
        return JSON.toJSONString(weekList);
    }

    /**
     * 3. 判断两个JSON周次是否有交集（冲突检测）
     */
    public static boolean hasIntersection(String json1, String json2) {
        if (json1 == null || json2 == null || json1.isEmpty() || json2.isEmpty()) {
            return false;
        }
        // 去掉 [ ] 和空格，按逗号分割
        String[] arr1 = json1.replace("[", "").replace("]", "").replace(" ", "").split(",");
        String[] arr2 = json2.replace("[", "").replace("]", "").replace(" ", "").split(",");

        Set<String> set = new HashSet<>(Arrays.asList(arr1));
        for (String week : arr2) {
            if (set.contains(week)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 私有方法 ====================

    private static List<Integer> parseJson(String weekJson) {
        if (weekJson == null || weekJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return JSON.parseArray(weekJson, Integer.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static List<Integer> parseRange(String rangeStr) {
        if (rangeStr == null || rangeStr.isEmpty()) {
            return new ArrayList<>();
        }
        // 规范化常见非 ASCII 连字符与空白（例如 Excel 导出时可能包含非断连字符 U+2011）
        String s = rangeStr.replaceAll("[\\s\\u00A0\\u2007\\u202F]", ""); // 去除各种空格
        s = s.replaceAll("[，]", ","); // 全角逗号
        // 把多种连字符替换成 ASCII '-'，涵盖常见 Unicode 破折号/短横/减号
        s = s.replaceAll("[\u2010\u2011\u2012\u2013\u2014\u2212\uFF0D]", "-");
        // 移除中文的'周'字前缀（例如 '周1-16'）
        s = s.replaceAll("周", "");

        List<Integer> weekList = new ArrayList<>();
        String[] parts = s.split(",");
        for (String part : parts) {
            if (part == null) continue;
            part = part.trim();
            if (part.isEmpty()) continue;
            try {
                if (part.contains("-")) {
                    String[] nums = part.split("-", 2);
                    int start = Integer.parseInt(nums[0].trim());
                    int end = Integer.parseInt(nums[1].trim());
                    for (int i = start; i <= end; i++) {
                        weekList.add(i);
                    }
                } else {
                    weekList.add(Integer.parseInt(part));
                }
            } catch (Exception e) {
                // 忽略解析错误的段落，继续处理其他部分
            }
        }
        return weekList;
    }

    private static String toRangeStr(List<Integer> weekList) {
        if (weekList == null || weekList.isEmpty()) {
            return "";
        }
        Collections.sort(weekList);
        List<String> ranges = new ArrayList<>();
        int start = weekList.get(0);
        int end = weekList.get(0);
        for (int i = 1; i < weekList.size(); i++) {
            if (weekList.get(i) == end + 1) {
                end = weekList.get(i);
            } else {
                ranges.add(start == end ? String.valueOf(start) : start + "-" + end);
                start = weekList.get(i);
                end = weekList.get(i);
            }
        }
        ranges.add(start == end ? String.valueOf(start) : start + "-" + end);
        return String.join(",", ranges);
    }
}