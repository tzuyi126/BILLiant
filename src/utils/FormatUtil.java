package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtil {
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	public static String dateToString(Date date) {
		return formatter.format(date);
	}
	
	public static boolean isValidDate(String dateString) {
		try {
			formatter.parse(dateString);
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
}
