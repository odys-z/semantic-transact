//package io.odysz.common;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Calendar;
//import java.util.GregorianCalendar;
//
///**@deprecated There are illegal characters in DateFunc.class. ZK client complain about it.
// * @author ly */
//public class DateFunc {
//	public DateFunc(){}
//	
//	public static String GetDate() {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		String sDate = sdf.format(new Date());
//		return sDate;
//	}
//
//    /**
//	 * @param englishDate yyyy-MM-dd
//	 * @param englishDate
//	 * @return
//	 */
//	public static String GetChineseyyyy_M_d_week(String englishDate){
//		String str = FormatDateTime(englishDate, "yyyyNIANMYUERI ");
//		return str + " " + getWeekDayName(englishDate);
//	}
//	
//	/**
//     *  yyyy-MM-dd HH:mm:ss
//     *  @return String
//     */
//	 public static String GetDateTime() {
//		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		 String sDate = sdf.format(new Date());
//		 return sDate;
//	 }
//	 
//	 public static String GetTimeFormat(String strFormat) {
//		 SimpleDateFormat sdf = new SimpleDateFormat(strFormat);
//		 String sDate = sdf.format(new Date());
//		 return sDate;
//	 }
//	 
//	 /**
//	  * @return String
//	  * @throws ParseException
//	  */
//	 public static String SetDateFormat(String myDate,String strFormat) throws ParseException {
//		 SimpleDateFormat sdf = new SimpleDateFormat(strFormat);
//		 String sDate = sdf.format(sdf.parse(myDate));
//		 return sDate;
//	 }
//	 
//	 public static String FormatDateTime(String strDateTime, String strFormat) {
//		 String sDateTime = strDateTime;
//		 try {
//			 Calendar Cal = parseDateTime(strDateTime);
//			 SimpleDateFormat sdf = null;
//			 sdf = new SimpleDateFormat(strFormat);
//			 sDateTime = sdf.format(Cal.getTime());
//		 } catch (Exception e) { }
//		 return sDateTime;
//	 }
//
//	 private static Calendar parseDateTime(String strDateTime) {
//		if (strDateTime == null || strDateTime.trim().equals("")) return null;
//			Date d = formatDateTime(strDateTime);
//			Calendar c = new GregorianCalendar();
//			c.setTime(d);
//			return c;
//	 }
//
//	public static Date formatDateTime(String baseDate) {
//		try{
//			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			return sdf1.parse(baseDate);
//		}
//		catch (Exception e) {}
//		try{
//			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//			return sdf1.parse(baseDate);
//		}
//		catch (Exception e) {}
//		try{
//			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
//			return sdf1.parse(baseDate);
//		}
//		catch (Exception e) {
//			System.out.println("Failed to convert: " + baseDate);
//			return null;
//		}
//	}
//
//	     public static int getDay(String strDate)
//	     {
//	         Calendar cal = parseDateTime(strDate);
//	         return   cal.get(Calendar.DATE);
//	     }
//
//	     public static  int getMonth(String strDate)
//
//	     {
//
//	         Calendar cal = parseDateTime(strDate);
//
//	         return cal.get(Calendar.MONTH) + 1;
//
//	     }
//
//	     public static  int getWeekDay(String strDate)
//
//	     {
//
//	         Calendar cal = parseDateTime(strDate);
//
//	         return cal.get(Calendar.DAY_OF_WEEK);
//
//	     }
//
//	     // FIXME CHINESE!
//	     public static  String getWeekDayName(String strDate) {
//	         String mName[] = { "SUN", "Mon", "Tu", "We", "Thu", "Fri", "Sa" };
//	         int iWeek = getWeekDay(strDate);
//	         iWeek = iWeek - 1;
//	         return "Weekday:" + mName[iWeek];
//	     }
//
//	     public static  int getYear(String strDate) {
//	    	 Calendar cal = parseDateTime(strDate);
//	    	 return cal.get(Calendar.YEAR) + 1900;
//	     }
//
//	     public static  String DateAdd(String strDate, int iCount, int iType) {
//	    	 Calendar Cal = parseDateTime(strDate);
//	         int pType = 0;
//	         if(iType == 0) {
//	        	 pType = 1;
//	         }
//	         else if(iType == 1) {
//	        	 pType = 2;
//	         }
//	         else if(iType == 2) {
//	        	 pType = 5;
//	         }
//	         else if(iType == 3) {
//	             pType = 10;
//	         }
//	         else if(iType == 4) {
//	             pType = 12;
//	         }
//	         else if(iType == 5) {
//	        	 pType = 13;
//	         }
//
//	         Cal.add(pType, iCount);
//	         SimpleDateFormat sdf = null;
//	         if(iType <= 2)
//	        	 sdf = new SimpleDateFormat("yyyy-MM-dd");
//	         else
//	        	 sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//	         String sDate = sdf.format(Cal.getTime());
//	         return sDate;
//	     }
//
//	     public static  String DateAdd(String strOption, int iDays, String strStartDate) {
//	         if(!strOption.equals("d"));
//	         return strStartDate;
//	     }
//
//	     public static  int DateDiff(String strDateBegin, String strDateEnd, int iType) {
//
//	         Calendar calBegin = parseDateTime(strDateBegin);
//
//	         Calendar calEnd = parseDateTime(strDateEnd);
//
//	         long lBegin = calBegin.getTimeInMillis();
//
//	         long lEnd = calEnd.getTimeInMillis();
//
//	         int ss = (int)((lBegin - lEnd) / 1000L);
//
//	         int min = ss / 60;
//
//	         int hour = min / 60;
//
//	         int day = hour / 24;
//
//	         if(iType == 0)
//
//	             return hour;
//
//	         if(iType == 1)
//
//	             return min;
//
//	         if(iType == 2)
//
//	             return day;
//
//	         else
//
//	             return -1;
//
//	     }
//
//	    
//
//	     /*****************************************
//
//	      * @����      �ж�ĳ���Ƿ�Ϊ����
//
//	      * @return   boolean
//
//	      * @throws ParseException
//
//	      ****************************************/
//
//	     public static  boolean isLeapYear(int yearNum){
//
//	      boolean isLeep = false;
//	         if((yearNum % 4 == 0) && (yearNum % 100 != 0)){
//	        	 isLeep = true;
//	         }   else if(yearNum % 400 ==0){
//	        	 isLeep = true;
//	         } else {
//	        	 isLeep = false;
//	         }
//	         return isLeep;
//	     }
//
//	    
//
//	      /*****************************************
//	      * @return   interger
//	      * @throws ParseException
//	      ****************************************/
//	     public static  int getWeekNumOfYear(){
//
//	      Calendar calendar = Calendar.getInstance();
//
//	      int iWeekNum = calendar.get(Calendar.WEEK_OF_YEAR);
//
//	      return iWeekNum;
//
//	     }
//
//	     /*****************************************
//
//	      * @����      ����ָ������ĳ��ĵڼ���
//
//	      * @return   interger
//
//	      * @throws ParseException
//
//	      ****************************************/
//
//	     public static  int getWeekNumOfYearDay(String strDate ) throws ParseException{
//
//	      Calendar calendar = Calendar.getInstance();
//
//	      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//
//	      Date curDate = format.parse(strDate);
//
//	      calendar.setTime(curDate);
//
//	      int iWeekNum = calendar.get(Calendar.WEEK_OF_YEAR);
//
//	      return iWeekNum;
//
//	     }
//
//	     /*****************************************
//
//	      * @����      ����ĳ��ĳ�ܵĿ�ʼ����
//
//	      * @return   interger
//
//	      * @throws ParseException
//
//	      ****************************************/
//
//	     public static  String getYearWeekFirstDay(int yearNum,int weekNum) throws ParseException {
//
//	     
//
//	      Calendar cal = Calendar.getInstance();
//
//	      cal.set(Calendar.YEAR, yearNum);
//
//	      cal.set(Calendar.WEEK_OF_YEAR, weekNum);
//
//	      cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//
//	      //�ֱ�ȡ�õ�ǰ���ڵ��ꡢ�¡���
//
//	      String tempYear = Integer.toString(yearNum);
//
//	      String tempMonth = Integer.toString(cal.get(Calendar.MONTH) + 1);
//
//	      String tempDay = Integer.toString(cal.get(Calendar.DATE));
//
//	      String tempDate = tempYear + "-" +tempMonth + "-" + tempDay;
//
//	      return SetDateFormat(tempDate,"yyyy-MM-dd");
//
//	     
//
//	     }
//
//	     /*****************************************
//
//	      * @����      ����ĳ��ĳ�ܵĽ�������
//
//	      * @return   interger
//
//	      * @throws ParseException
//
//	      ****************************************/
//
//	     public static  String getYearWeekEndDay(int yearNum,int weekNum) throws ParseException {
//
//	      Calendar cal = Calendar.getInstance();
//
//	      cal.set(Calendar.YEAR, yearNum);
//
//	      cal.set(Calendar.WEEK_OF_YEAR, weekNum + 1);
//
//	      cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
//
//	         //�ֱ�ȡ�õ�ǰ���ڵ��ꡢ�¡���
//
//	      String tempYear = Integer.toString(yearNum);
//
//	      String tempMonth = Integer.toString(cal.get(Calendar.MONTH) + 1);
//
//	      String tempDay = Integer.toString(cal.get(Calendar.DATE));
//
//	      String tempDate = tempYear + "-" +tempMonth + "-" + tempDay;
//
//	      return SetDateFormat(tempDate,"yyyy-MM-dd");
//
//	     }
//
//	    
//
//	    
//
//	     /*****************************************
//
//	      * @����      ����ĳ��ĳ�µĿ�ʼ����
//
//	      * @return   interger
//
//	      * @throws ParseException
//
//	      ****************************************/
//
//	     public static  String getYearMonthFirstDay(int yearNum,int monthNum) throws ParseException {
//
//	     
//
//	      String tempYear = Integer.toString(yearNum);
//
//	      String tempMonth = Integer.toString(monthNum);
//
//	      String tempDay = "1";
//
//	      String tempDate = tempYear + "-" +tempMonth + "-" + tempDay;
//
//	      return SetDateFormat(tempDate,"yyyy-MM-dd");
//
//	     
//
//	     }
//
//	     /*****************************************
//
//	      * @return   interger
//
//	      * @throws ParseException
//
//	      ****************************************/
//
//	     public static  String getYearMonthEndDay(int yearNum,int monthNum) throws ParseException {
//
//	      
//
//	        //�ֱ�ȡ�õ�ǰ���ڵ��ꡢ�¡���
//
//	      String tempYear = Integer.toString(yearNum);
//
//	      String tempMonth = Integer.toString(monthNum);
//
//	      String tempDay = "31";
//
//	      if (tempMonth.equals("1") || tempMonth.equals("3") || tempMonth.equals("5") || tempMonth.equals("7") ||tempMonth.equals("8") || tempMonth.equals("10") ||tempMonth.equals("12")) {
//
//	       tempDay = "31";
//
//	      }
//
//	      if (tempMonth.equals("4") || tempMonth.equals("6") || tempMonth.equals("9")||tempMonth.equals("11")) {
//
//	       tempDay = "30";
//
//	      }
//
//	      if (tempMonth.equals("2")) {
//
//	       if (isLeapYear(yearNum)) {
//
//	        tempDay = "29";
//
//	       } else {
//
//	          tempDay = "28";
//
//	       }
//
//	      }
//
//	      //System.out.println("tempDay:" + tempDay);
//
//	      String tempDate = tempYear + "-" +tempMonth + "-" + tempDay;
//
//	      return SetDateFormat(tempDate,"yyyy-MM-dd");
//
//	     }
//
//
//	    /**
//	     * @param srcDate
//	     * @param format
//	     * @return
//	     */
//	    public static String convertDateFormat(String srcDate, String format) {
//	 		try {
//				Calendar c = DateFunc.parseDateTime(srcDate);
//				if (c == null) return "";
//
//				String strFormat = format.replaceAll("yyyy", "\\%1\\$04d");
//				strFormat = strFormat.replaceAll("yy", "\\%1\\$2d");
//				strFormat = strFormat.replaceAll("MM", "\\%2\\$02d");
//				strFormat = strFormat.replaceAll("M", "\\%2\\$d");
//				strFormat = strFormat.replaceAll("DD", "\\%3\\$02d");
//				strFormat = strFormat.replaceAll("dd", "\\%3\\$02d");
//				strFormat = strFormat.replaceAll("D", "\\%3\\$d");
//				strFormat = strFormat.replaceAll("HH", "\\%4\\$02d");
//				strFormat = strFormat.replaceAll("mm", "\\%5\\$02d");
//				strFormat = strFormat.replaceAll("ss", "\\%6\\$02d");
//				strFormat = strFormat.replaceAll("W", "\\%7\\$s");
//				strFormat = strFormat.replaceAll("w", "\\%8\\$d");
//
//		//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD hh24:mm:ss");
//		        String mName[] = {"��", "һ", "��", "��", "��", "��", "��"};
//		        int iWeek = c.get(Calendar.DAY_OF_WEEK);
//				String s = "";
//				s = String.format(strFormat, c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), 
//						c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), 
//						"����" + mName[iWeek - 1], iWeek);
//				return s;
//			}catch (Exception e) {
//				System.out.println("����ʽ���������" + format);
//				e.printStackTrace();
//				return "";
//			}
//	     }
//}
