package com.qingyou.qingyouclient;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.ParseException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MyUtils {
	static double convert(double value) {
		long l1 = Math.round(value * 100); // 四舍五入
		double ret = l1 / 100.0; // 注意：使用 100.0 而不是 100
		return ret;
	}
	
	static DecimalFormat df = new DecimalFormat("￥0.00");
	static DecimalFormat df2 = new DecimalFormat("0");
	static String cnv_price(double price) {
		return df.format(price);
	}
	
	static String cnv_weight(double w) {
		return df2.format(w);
	}
	
	static SimpleDateFormat mDateFormat = (SimpleDateFormat)DateFormat.getDateInstance();
	static String formatDateTime(long time) {
        mDateFormat.applyPattern("MM-dd HH:mm");
        return mDateFormat.format(new Date(time));
    }
	
	static String formatDate(Date date) {
		mDateFormat.applyPattern("yyyy-MM-dd");
		return mDateFormat.format(date);
	}
	
	static String formatDate(long time) {
		mDateFormat.applyPattern("yyyy-MM-dd");
		return mDateFormat.format(time);
	}
	
	static Date parseDate(String date) {
		Date d = null;
		try {
			mDateFormat.applyPattern("yyyy-MM-dd");
			d = mDateFormat.parse(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}
	
	static void callNetTrans(Context c, int msgid, Bundle data) {
		Intent intent = new Intent();
		intent.setAction("qingyou.net.trans");
		Bundle b = new Bundle(data);
		b.putInt("msgid", msgid);
		intent.putExtras(b);
		c.sendBroadcast(intent);
	}
	
}
