package io.odysz.common;

import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Date formatting and parsing helper.
 * @author ody
 */
public class DateFormat {
	/**yyyy-MM-dd or %Y-%M-%e*/
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public static SimpleDateFormat yy_MM = new SimpleDateFormat("yyyy_MM");

	/**yyyy-MM-dd-hhmmss or %Y-%M-%e ...*/
	public static SimpleDateFormat sdflong_sqlite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
	public static SimpleDateFormat sdflong_mysql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**yyyy-MM-dd
	 * @param d
	 * @return formatted string
	 */
	static public String format(Date d) { return d == null ? " - - " : sdf.format(d); }

	/**
	 * @param d
	 * @return yyyy_MM
	 */
	static public String formatYYmm(Date d) { return d == null ? " - - " : yy_MM.format(d); }

	static public String formatYYmm(FileTime d) { return d == null ? " - - " : yy_MM.format(new Date(d.toMillis())); }

	static public String formatime(Date d) { return d == null ? " - - : 00.00.00" : sdflong_mysql.format(d); }

	static public String formatime(FileTime d) { return d == null ? " - - : 00.00.00" : sdflong_mysql.format(new Date(d.toMillis())); }

	/**yyyy-MM-dd
	 * @param text
	 * @return formatted date ignoring time (text used as short form)
	 * @throws ParseException
	 */
	public static Date parse(String text) throws ParseException { return sdf.parse(text); }

	/**yyyy-MM-dd HH:mm:ss.0 or yyyy-MM-dd HH:mm:ss
	 * @param text
	 * @return date-time
	 * @throws ParseException
	 */
	public static Date parseDateTime(String text)
			throws ParseException {
		try {
			return sdflong_sqlite.parse(text);
		}
		catch (ParseException e) {
			try {return sdflong_sqlite.parse(text + ".0");}
			catch (ParseException ex) {
				return sdflong_mysql.parse(text);
			}
		}
	}

	public static String incSeconds(dbtype drvType, String date0, int snds) throws ParseException {
		Date d0 = parse(date0);
		d0.setTime(d0.getTime() + snds);
		// return format(d0);
		if (drvType == dbtype.sqlite)
			return sdflong_sqlite.format(d0);
		return sdflong_mysql.format(d0);
	}

	/**@deprecated
	 * https://stackoverflow.com/questions/9474121/i-want-to-get-year-month-day-etc-from-java-date-to-compare-with-gregorian-cal
	 * @param date
	 * @return formatted string
	 */
	public static String GetZhCnYMD(Date date) {
		if (date == null)
			return "---- 年 -- 月 -- 日";
		Calendar c = new GregorianCalendar();
		c.setTime(date);
	    int year = c.get(Calendar.YEAR);
	    int month = c.get(Calendar.MONTH);
	    int day = c.get(Calendar.DAY_OF_MONTH);
	    return String.format("%1$4d年%2$02d月%3$02d日", year, month + 1, day);
	}

	public static String getDayDiff(Date date2, Date date1) {
		if (date2 == null || date1 == null)
			return "-";
		return String.valueOf(getDayDiffInt(date2, date1));
	}

	public static long getDayDiffInt(Date d2, Date d1) {
		if (d2 == null || d1 == null)
			return -1;
		long diff = d2.getTime() - d1.getTime();
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	public static String getTimeStampYMDHms(dbtype drvType) {
		Date now = new Date();
		if (drvType == dbtype.sqlite)
			return sdflong_sqlite.format(now);
		return sdflong_mysql.format(now);
	}
}
