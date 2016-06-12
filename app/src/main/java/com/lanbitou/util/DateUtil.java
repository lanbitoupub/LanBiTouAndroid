package com.lanbitou.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间工具类
 * Created by Henvealf on 16-6-8.
 */
public class DateUtil {

    public final static String TODAY = "今天";


    public static boolean isToday(final String dateStr,String formart){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formart);
        try {
            Date date = simpleDateFormat.parse(dateStr);
            return isToday(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isToday(Date date){
        int result = dayBegin(date).compareTo(dayBegin(new Date()));
        return result == 0 ;
    }

    /**
     * 获取指定时间的那天 00:00:00.000 的时间
     * @param date
     * @return
     */
    public static Date dayBegin(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 获取指定时间的那天 23:59:59.999 的时间
     *
     * @param date
     * @return
     */
    public static Date dayEnd(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

}
