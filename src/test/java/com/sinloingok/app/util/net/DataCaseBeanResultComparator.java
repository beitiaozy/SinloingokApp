package com.sinloingok.app.util.net;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

/**
 * @author 优化版
 */
public class DataCaseBeanResultComparator {

    private final List<JSONObject> differences = new ArrayList<>();

    public boolean checkJsonMatch(Object predict, Object result) {
        if (predict == null || result == null) {
            addDifference("root", predict, result);
            return false;
        }

        if (predict instanceof Map && result instanceof Map) {
            return checkMapMatch((Map<?, ?>) predict, (Map<?, ?>) result, "");
        } else if (predict instanceof List && result instanceof List) {
            return checkListMatch((List<?>) predict, (List<?>) result, "");
        } else if (!predict.equals(result)) {
            addDifference("", predict, result);
            return false;
        }

        return true;
    }

    private boolean checkMapMatch(Map<?, ?> predictMap, Map<?, ?> resultMap, String path) {
        boolean allMatch = true;
        for (Map.Entry<?, ?> entry : predictMap.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object predictValue = entry.getValue();
            Object resultValue = resultMap.get(key);
            String currentPath = path.isEmpty() ? key : path + "." + key;

            if (!resultMap.containsKey(key)) {
                addDifference(currentPath, predictValue, "缺失");
                allMatch = false;
                continue;
            }

            if (predictValue instanceof Map && resultValue instanceof Map) {
                if (!checkMapMatch((Map<?, ?>) predictValue, (Map<?, ?>) resultValue, currentPath)) {
                    allMatch = false;
                }
            } else if (predictValue instanceof List && resultValue instanceof List) {
                if (!checkListMatch((List<?>) predictValue, (List<?>) resultValue, currentPath)) {
                    allMatch = false;
                }
            } else if (!Objects.equals(predictValue, resultValue)) {
                addDifference(currentPath, predictValue, resultValue);
                allMatch = false;
            }
        }
        return allMatch;
    }

    private boolean checkListMatch(List<?> predictList, List<?> resultList, String path) {
        boolean allMatch = true;
        int minSize = Math.min(predictList.size(), resultList.size());

        for (int i = 0; i < minSize; i++) {
            Object predictElem = predictList.get(i);
            Object resultElem = resultList.get(i);
            String currentPath = path + "[" + i + "]";

            if (predictElem instanceof Map && resultElem instanceof Map) {
                if (!checkMapMatch((Map<?, ?>) predictElem, (Map<?, ?>) resultElem, currentPath)) {
                    allMatch = false;
                }
            } else if (predictElem instanceof List && resultElem instanceof List) {
                if (!checkListMatch((List<?>) predictElem, (List<?>) resultElem, currentPath)) {
                    allMatch = false;
                }
            } else if (!Objects.equals(predictElem, resultElem)) {
                addDifference(currentPath, predictElem, resultElem);
                allMatch = false;
            }
        }

        // 如果 resultList 比 predictList 短，标记缺失项
        for (int i = minSize; i < predictList.size(); i++) {
            String currentPath = path + "[" + i + "]";
            addDifference(currentPath, predictList.get(i), "缺失");
            allMatch = false;
        }

        return allMatch;
    }

    private void addDifference(String field, Object expected, Object actual) {
        JSONObject diff = new JSONObject();
        diff.put("field", field);
        diff.put("expectedValue", expected);
        diff.put("actualValue", actual);
        differences.add(diff);
    }

    public JSONArray getDifferences() {
        return JSONArray.parseArray(JSON.toJSONString(differences));
    }

    public static void main(String[] args) {
        DataCaseBeanResultComparator caseBeanResultComparator = new DataCaseBeanResultComparator();

        String predict = "{\n" +
                "  \"user\": {\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Alice\",\n" +
                "    \"roles\": [\"admin\", \"editor\"],\n" +
                "    \"profile\": {\n" +
                "      \"age\": 30,\n" +
                "      \"email\": \"alice@example.com\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"active\": true,\n" +
                "  \"score\": 98.5,\n" +
                "  \"tags\": [\n" +
                "    { \"id\": 1, \"label\": \"java\" },\n" +
                "    { \"id\": 2, \"label\": \"springboot\" }\n" +
                "  ],\n" +
                "  \"loginHistory\": [1625000000000, 1625100000000]\n," +
                "\"testNull\": \"\" "+
                "}\n";
        String result = "{\n" +
                "  \"user\": {\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Alicia\",\n" +
                "    \"roles\": [\"admin\"],  // 少了一个 role\n" +
                "    \"profile\": {\n" +
                "      \"age\": \"30\",        // 类型不同：String vs Integer\n" +
                "      \"phone\": \"123456789\" // 多余字段\n" +
                "    }\n" +
                "  },\n" +
                "  \"active\": \"true\",          // 类型不同：String vs Boolean\n" +
                "  \"score\": 95.0,             // 数值不同\n" +
                "  \"tags\": [\n" +
                "    { \"id\": 1, \"label\": \"java\" },\n" +
                "    { \"id\": 2, \"label\": \"spring\" },\n" +
                "    { \"id\": 3, \"label\": \"docker\" }  // 多了一个 tag\n" +
                "  ],\n" +
                "  \"loginHistory\": [1625000000000]  // 少了一个时间戳\n" +
                "}\n";
        caseBeanResultComparator.checkJsonMatch(JSONObject.parseObject(predict), JSONObject.parseObject(result));
        System.out.println(caseBeanResultComparator.getDifferences().toJSONString());
    }
}
