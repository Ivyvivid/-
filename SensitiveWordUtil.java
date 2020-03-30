package com.example.ivy.util;

import java.util.*;

public class SensitiveWordUtil {

    /**
     * 敏感词匹配规则
     */
    private static final int MinMatchTYpe = 1;      //最小匹配
    private static final int MaxMatchType = 2;      //最大匹配

    private static final char maskChar = 'X'; // 掩码

    /**
     * 敏感词集合
     */
    private static Map sensitiveWordMap;

    /**
     * 创建敏感词库
     */
    private static void createSensitiveWordMap(Set<String> sensitiveWordSet) {
        //初始化敏感词容器，减少扩容操作
        sensitiveWordMap = new HashMap(sensitiveWordSet.size());
        String key;
        //迭代sensitiveWordSet
        Iterator<String> iterator = sensitiveWordSet.iterator();
        while (iterator.hasNext()) {
            Map nowMap = sensitiveWordMap;
            //关键字
            key = iterator.next();
            for (int i = 0; i < key.length(); i++) {
                //转换成char型
                char keyChar = key.charAt(i);
                //库中获取关键字
                 Object wordMap = nowMap.get(keyChar);
                //如果存在该key，直接赋值，用于下一个循环获取
                if (wordMap != null) {
                    nowMap = (Map) wordMap;
                } else {
                    //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                    Map<String, String> newWordMap = new HashMap<>();
                    //不是最后一个
                    newWordMap.put("isEnd", "0");
                    nowMap.put(keyChar, newWordMap);
                    nowMap = newWordMap;
                }

                if (i == key.length() - 1) {
                    //最后一个
                    nowMap.put("isEnd", "1");
                }
            }
        }
    }

    /**
     * 获取文字中的敏感词
     */
    public static Set<String> getSensitiveWord(String txt, int matchType) {
        Set<String> sensitiveWordList = new HashSet<>();

        for (int i = 0; i < txt.length(); i++) {
            //判断是否包含敏感字符
            int length = checkSensitiveWord(txt, i, matchType);
            if (length > 0) {//存在,加入list中
                sensitiveWordList.add(txt.substring(i, i + length));
                i = i + length - 1;//减1的原因，是因为for会自增
            }
        }

        return sensitiveWordList;
    }


    /**
     * 替换敏感字字符
     */
    public static String replaceSensitiveWord(String txt, int matchType,Map<String,String> replaceWordMap) {
        String resultTxt = txt;
        //获取所有的敏感词
        Set<String> set = getSensitiveWord(txt, matchType);
        Iterator<String> iterator = set.iterator();
        String word;
        String[] splitStr = txt.split("[\" \"|!|,|;|.|:|?]");
        while (iterator.hasNext()) {
            word = iterator.next();
            for (String orgWord :splitStr) {
                if (null == replaceWordMap || replaceWordMap.size() == 0 || !replaceWordMap.containsKey(word)) { // X替换敏感字
                    if (orgWord.endsWith(word) || orgWord.startsWith(word)) {
                        resultTxt = resultTxt.replaceAll(orgWord, getReplaceChars(maskChar, orgWord.length()));
                    }
                } else {// replaceWord替换单词
                    String rw = replaceWordMap.get(word);
                    resultTxt = resultTxt.replaceAll(orgWord, rw);
                }
            }
        }
        return resultTxt;
    }

    /**
     * 生成替换字符串
     */
    private static String getReplaceChars(char replaceChar, int length) {
        StringBuilder replaceStr = new StringBuilder(String.valueOf(replaceChar));
        for (int i = 1; i < length; i++) {
            replaceStr.append(replaceChar);
        }

        return replaceStr.toString();
    }

    /**
     * 检查文字中是否包含敏感字符
     */
    private static int checkSensitiveWord(String txt, int beginIndex, int matchType) {
        //敏感词结束标识位：用于敏感词只有1位的情况
        boolean flag = false;
        //匹配标识数默认为0
        int matchFlag = 0;
        char word;
        Map nowMap = sensitiveWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = txt.charAt(i);
            //获取指定key
            nowMap = (Map) nowMap.get(word);
            if (nowMap != null) {//存在，则判断是否为最后一个
                //找到相应key，匹配标识+1
                matchFlag++;
                //如果为最后一个匹配规则,结束循环
                if ("1".equals(nowMap.get("isEnd"))) {
                    //结束标志位为true
                    flag = true;
                    //最小规则，直接返回,最大规则还需继续查找
                    if (MinMatchTYpe == matchType) {
                        break;
                    }
                }
            } else {//不存在，直接返回
                break;
            }
        }
        if (matchFlag < 2 || !flag) {//长度必须大于等于1，为词
            matchFlag = 0;
        }
        return matchFlag;
    }

    public static void main(String[] args) {
        Set<String> set = new HashSet<>();
        set.add("friend");
        set.add("so");
        SensitiveWordUtil.createSensitiveWordMap(set);
        String str = "Your friend is so friendly";
        String[] splitStr = str.split("[\" \" ; . , : ? !  ]");
        StringBuilder strBuff = new StringBuilder();
        Map<String, String> replaceWordMap = new HashMap<>();
        replaceWordMap.put("friend", "bad");
        for (String s :
                splitStr) {
            strBuff.append(SensitiveWordUtil.replaceSensitiveWord(s, 1,replaceWordMap));
            strBuff.append(" ");
        }
        System.out.println("--->替换前:" + str);
        System.out.println("--->替换后:" + strBuff);
    }


}