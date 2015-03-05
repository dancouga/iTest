package com.cht.iTest.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 程式資訊摘要：<P>
 * 類別名稱　　：DateUtils.java<P>
 * 程式內容說明：<P>
 * 程式修改記錄：<P>
 * XXXX-XX-XX：<P>
 *@author wenyangkao
 *@version 1.0
 *@since 1.0
 */
public class DateUtils {
    
    public static final String DEFAULT = "yyyy/MM/dd HH:mm:ss";
    
    /**
     * 取得當下時間，並進行時間調整(可增減年、月、日)，並轉換為default(yyyy-MM-dd)格式之字串
     * 
     * @return String(yyyy-MM-dd)
     */
    public static String getDefaultCurrentWithModify(int year, int month, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT);
        Calendar calendar = Calendar.getInstance();
        modify(calendar, year, month, day, 0);
        return sdf.format(calendar.getTime());
    }
    
    /**
     * 取得當下時間，並轉換為default(yyyy/MM/dd, HH:mm:ss)格式之字串
     * 
     * @return String(yyyy/MM/dd, HH:mm:ss)
     */
    public static String getDefaultCurrent() {
        return getCustomCurrent(DEFAULT);
    }
    
    /**
     * 取得當下時間，並轉換為自定義格式之字串(如:yyyy/MM/dd hh:mm:ss,可參考SimpleDateFormat之API說明)
     * @see java.text.SimpleDateFormat
     *  
     * @param format 時間格式 (ex:yyyy/MM/dd hh:mm:ss)
     * @return String
     */
    public static String getCustomCurrent(String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        return sdf.format(calendar.getTime());
    }
    
    /**
     * 將時間轉為自定義的字串格式。
     * 
     * @param format 字串格式
     * @param date 時間
     * @return String
     */
    public static String getCustomDateStr(String format, Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
    
    /**
     * 將時間轉為預設(yyyy/MM/dd HH:mm:ss)的字串格式。
     * 
     * @param format 字串格式
     * @param date 時間
     * @return String
     */
    public static String getDefaultStr(Date date){
		if (date == null) {
			return "";
		}
    	
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT);
        return sdf.format(date);
    }
    
    /**
     * 將自定義格式之字串轉換為Date物件
     * @see java.text.SimpleDateFormat
     *  
     * @param format 時間格式 (ex:yyyy/MM/dd hh:mm:ss)
     * @param source 符合上述格式之字串
     * @return String
     * @throws ParseException 
     */
	public static Date string2Date(String format, String source) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		try {
			return sdf.parse(source);
		} catch (ParseException e) {

		}

		return null;
	}
    
    /**
     * 取得當下時間，並進行時間調整(可增減年、月、日)，並轉換為default(yyyy-MM-dd)格式之字串
     * 
     * @param format 時間格式 (ex:yyyyMMdd HH:mm:ss)
     * @param year 增減年
     * @param month 增減月
     * @param day 增減日
     * @param hour 增減時
     * @return String
     */
    public static String getCustomCurrentWithModify(
            String format, int year, int month, int day, int hour) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        modify(calendar, year, month, day, hour);
        return sdf.format(calendar.getTime());
    }
    
    /**
     * 對date進行時間調整，並回傳調整後之Date物件
     * 
     * @param date 欲調整之date
     * @param year 增減年
     * @param month 增減月
     * @param day 增減日
     * @param hour 增減時
     * @return Date
     */
    public static Date afterModify(
            Date date, int year, int month, int day, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        modify(calendar, year, month, day, hour);
        return calendar.getTime();
    }
    
    private static void modify(Calendar calendar, int year, int month, int day, int hour) {
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.HOUR, (day * 24) + hour);
    }
    
    public static Date spec(Date date, int hour, int minute, int sec){
    	Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, sec);
		return calendar.getTime();
    }
}
