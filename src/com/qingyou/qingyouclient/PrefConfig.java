package com.qingyou.qingyouclient;

import android.content.Context;
import android.content.SharedPreferences;

public final class PrefConfig {

	private static PrefConfig self = null;
	
	private final Context c;
	private String PREF_CFG = "config";
	private SharedPreferences cfgPref;
	
	private PrefConfig(Context c) {
		this.c = c;
	}
	
	public static PrefConfig instance(Context c) {
		if (self == null) {
			self = new PrefConfig(c);
		}
		return self;
	}
	
	public synchronized String getCfg(String key) {
		cfgPref = c.getSharedPreferences(PREF_CFG, 0);
		return cfgPref.getString(key, "");
	}
	
	public synchronized void saveCfg(String key, String val) {
		cfgPref.edit().putString(key, val).commit();
	}
	
}
