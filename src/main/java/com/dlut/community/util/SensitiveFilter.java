package com.dlut.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        //从target里读配置文件
        try (
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            //字节流转换成字符流
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while((keyword = reader.readLine()) != null) {
                //把读到的敏感词添加到前缀树
                this.addKeyword(keyword);
            }
        }
        catch (Exception e) {
            logger.error("加载敏感词失败：" + e.getMessage());
        }

    }

    /*
    * 将一个敏感词添加到前缀树
    *
    * */
    private void addKeyword(String keyword) {
        TrieNode trieNode = rootNode;
        for(int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = trieNode.getSubNode(c);
            if(subNode == null) {
                //初始化节点
                subNode = new TrieNode();
                trieNode.addSubNode(c, subNode);
            }
            //指向子节点，进入下一轮循环
            trieNode = subNode;

            //设置结束标志
            if(i == keyword.length() - 1) {
                trieNode.setKeywordEnd(true);
            }
        }
    }

    /*
    * 方法功能：过滤敏感词
    * 参数：待过滤的文本
    * 返回值：替换敏感词后的字符
    * */

    public String filter(String text) {
        if(StringUtils.isBlank(text)) {
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        //指针3优先到达终点，所以用指针3作为结束条件
        while(position < text.length()) {
            char c = text.charAt(position);
            //跳过符号
            if(isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();


    }

    /*
    * 判断是否为特殊符号
    * */
    private boolean isSymbol(Character c) {
        //0x2E80~0x9FFF是东亚文字范围
        //CharUtils.isAsciiAlphanumeric：如果是数字及字母，返回true
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    public class TrieNode {

        //关键词结束标志
        private boolean isKeywordEnd = false;

        //子节点（key是子节点存储的字符，value是子节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }
        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
