package com.panxiong.instant.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by panxi on 2016/4/22.
 * <p>
 * 字符串操作集合
 */
public class StrUtil {

	/**
	 * 字符串是否为空 true空 / false不空
	 */
	public static boolean isEmpty(String string) {
		if (string == null || string.length() <= 0 || "null".equals(string)) {
			return true;
		}
		return false;
	}

	/* b 转 kb */
	public static double bToKb(double b) {
		String temp = String.valueOf(b / 1024);
		int index = temp.indexOf(".");
		try {
			return Double.valueOf(temp.substring(0, index + 3));
		} catch (Exception e) {
			return Double.valueOf(temp);
		}
	}

	/* 如果最后是逗号 则删除 */
	public static String remoeveLast(String string) {
		if (string == null)
			return "";
		if (string.endsWith(",")) {
			string = string.substring(0, string.length() - 1);
		}
		return string;
	}

	/**
	 * 去掉 null 字符
	 */
	public static String remoeveNull(String string) {
		if (isEmpty(string))
			return "";
		return string.replace("null", "");
	}

	/**
	 * 去掉html标签
	 */
	public static String delHTMLTag(String htmlStr) {
		String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
		String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
		String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
		String regEx_space = "\\s*|\t|\r|\n";// 定义空格回车换行符

		Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(htmlStr);
		htmlStr = m_script.replaceAll(""); // 过滤script标签

		Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
		Matcher m_style = p_style.matcher(htmlStr);
		htmlStr = m_style.replaceAll(""); // 过滤style标签

		Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(htmlStr);
		htmlStr = m_html.replaceAll(""); // 过滤html标签

		Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
		Matcher m_space = p_space.matcher(htmlStr);
		htmlStr = m_space.replaceAll(""); // 过滤空格回车标签
		return htmlStr.trim().replaceAll("&nbsp;", " ").replace("\">", ""); // 返回文本字符串
	}

	/**
	 * 去掉时间字符串的秒字符
	 */
	public static String fromTimeString(String string) {
		if (!isEmpty(string) && string.length() > 18) {
			string = string.substring(0, string.length() - 3);
		}
		return string;
	}

	/**
	 * 去掉时间字符串的时分秒
	 */
	public static String fromTimeString2(String string) {
		if (!isEmpty(string) && string.length() > 10) {
			string = string.substring(0, 10);
		}
		return string;
	}

	/**
	 * 首字符空两格
	 */
	public static String addLastBlank(String string) {
		if (!isEmpty(string)) {
			if (string.startsWith("\n")) {
				string = string.substring("\n".length(), string.length());
			}
			if (!string.startsWith("\u3000\u3000")) {
				string = "\u3000\u3000" + string.replace(" ", "");
			}
		}
		return string;
	}
}
