package jrx.anydmp.gateway.admin.utils;

/**
 * @author zhao tingting
 * @date 2018/12/13
 */
public class ValidUtils {
	public static Boolean validUrl(String url) {
		String URL_REGEX = "^(https|http|lb)://(\\w|\\.|-|/)+";
		Boolean result = url.matches(URL_REGEX);
		return  result;
	}
}
