package jrx.anydmp.gateway.admin.utils;

import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zhao tingting
 * @date 2018/11/12
 */
public class DateUtils {

	public static String parseToDatetime(long time,String pattern) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Date date = new Date();
		date.setTime(time);
		return simpleDateFormat.format(date);
	}

	public static long DatetimeParseToLong(String date,String pattern) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		long time = 0;
		if (!StringUtils.isEmpty(date)) {
			try {
				time = simpleDateFormat.parse(date).getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return time;
	}
}
