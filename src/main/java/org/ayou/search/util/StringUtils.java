package org.ayou.search.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * copy from common lang
 */
public class StringUtils {

	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNotBlank(String str) {
		return !StringUtils.isBlank(str);
	}

	//http://www.cnblogs.com/zwm512327/p/3498527.html
	public static boolean ishave(String str, String key) {
		if (str == null || key == null) {
			return false;
		}
		Pattern pattern = Pattern.compile(key);
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}
}
