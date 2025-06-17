package com.sinloingok.app.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StringValidationUtil {

    // 两种有效前缀
    private static final Set<String> VALID_PREFIXES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "100100",
            "313030313030303"
    )));

    // 有效的结尾集合
    private static final Set<String> VALID_ENDINGS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "0101", "0102", "0201", "0202", "0401", "0402", "0801", "0802"
    )));

    /**
     * 判断字符串是否符合指定格式
     * 要求：以两种有效前缀之一开头，并且包含有效的4位结尾
     *
     * @param input 要检查的字符串
     * @return 是否符合格式要求
     */
    public static boolean isValidFormat(String input) {
        if (input == null || input.length() < getMinValidLength()) {
            return false;
        }

        // 检查是否以任一有效前缀开头
        String matchedPrefix = getMatchedPrefix(input);
        if (matchedPrefix == null) {
            return false;
        }

        // 检查是否包含有效的4位结尾
        return containsValidEnding(input, matchedPrefix.length());
    }

    /**
     * 判断字符串是否符合指定格式，并返回提取的信息
     *
     * @param input 要检查的字符串
     * @return 包含验证结果和提取信息的对象，如果验证失败返回null
     */
    public static ValidationResult validateAndExtract(String input) {
        if (!isValidFormat(input)) {
            return null;
        }

        String matchedPrefix = getMatchedPrefix(input);
        String ending = extractValidEnding(input, matchedPrefix.length());
        String remainingBeforeEnding = input.substring(matchedPrefix.length(),
                input.indexOf(ending, matchedPrefix.length()));
        String remainingAfterEnding = input.substring(input.indexOf(ending, matchedPrefix.length()) + 4);

        return new ValidationResult(true, matchedPrefix, ending, remainingBeforeEnding, remainingAfterEnding);
    }

    /**
     * 获取匹配的前缀
     *
     * @param input 输入字符串
     * @return 匹配的前缀，如果没有匹配则返回null
     */
    private static String getMatchedPrefix(String input) {
        for (String prefix : VALID_PREFIXES) {
            if (input.startsWith(prefix)) {
                return prefix;
            }
        }
        return null;
    }

    /**
     * 检查字符串是否包含有效的4位结尾（在指定起始位置之后）
     *
     * @param input 输入字符串
     * @param startIndex 开始搜索的索引位置
     * @return 是否包含有效结尾
     */
    private static boolean containsValidEnding(String input, int startIndex) {
        for (int i = startIndex; i <= input.length() - 4; i++) {
            String potentialEnding = input.substring(i, i + 4);
            if (VALID_ENDINGS.contains(potentialEnding)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 提取有效的4位结尾（在指定起始位置之后）
     *
     * @param input 输入字符串
     * @param startIndex 开始搜索的索引位置
     * @return 找到的有效结尾，如果没有找到则返回null
     */
    private static String extractValidEnding(String input, int startIndex) {
        for (int i = startIndex; i <= input.length() - 4; i++) {
            String potentialEnding = input.substring(i, i + 4);
            if (VALID_ENDINGS.contains(potentialEnding)) {
                return potentialEnding;
            }
        }
        return null;
    }

    /**
     * 获取最小有效长度（最短前缀长度 + 4位结尾）
     *
     * @return 最小有效长度
     */
    private static int getMinValidLength() {
        int minPrefixLength = Integer.MAX_VALUE;
        for (String prefix : VALID_PREFIXES) {
            if (prefix.length() < minPrefixLength) {
                minPrefixLength = prefix.length();
            }
        }
        return minPrefixLength + 4;
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String prefix;
        private final String ending;
        private final String remainingBeforeEnding;
        private final String remainingAfterEnding;

        public ValidationResult(boolean valid, String prefix, String ending,
                                String remainingBeforeEnding, String remainingAfterEnding) {
            this.valid = valid;
            this.prefix = prefix;
            this.ending = ending;
            this.remainingBeforeEnding = remainingBeforeEnding;
            this.remainingAfterEnding = remainingAfterEnding;
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getPrefix() { return prefix; }
        public String getEnding() { return ending; }
        public String getRemainingBeforeEnding() { return remainingBeforeEnding; }
        public String getRemainingAfterEnding() { return remainingAfterEnding; }

        @Override
        public String toString() {
            return String.format(
                    "ValidationResult{valid=%s, prefix='%s', ending='%s', beforeEnding='%s', afterEnding='%s'}",
                    valid, prefix, ending, remainingBeforeEnding, remainingAfterEnding);
        }
    }

    /**
     * 获取所有有效结尾
     *
     * @return 有效结尾集合的不可修改副本
     */
    public static Set<String> getValidEndings() {
        return VALID_ENDINGS;
    }

    /**
     * 获取所有有效前缀
     *
     * @return 有效前缀集合的不可修改副本
     */
    public static Set<String> getValidPrefixes() {
        return VALID_PREFIXES;
    }

    /**
     * 检查给定的4位字符串是否是有效的结尾
     *
     * @param ending 要检查的4位字符串
     * @return 是否是有效结尾
     */
    public static boolean isValidEnding(String ending) {
        return VALID_ENDINGS.contains(ending);
    }

    /**
     * 检查给定的字符串是否是有效的前缀
     *
     * @param prefix 要检查的字符串
     * @return 是否是有效前缀
     */
    public static boolean isValidPrefix(String prefix) {
        return VALID_PREFIXES.contains(prefix);
    }
}