package com.qingyou.qingyouclient;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.qingyou.http.HttpThread;

public class ConnectService extends Service {
	
	private HttpThread http;
	PrefConfig config = PrefConfig.instance(this);
	
	public ConnectService() {
	}
	
	private BroadcastReceiver transfer = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("qingyou.net.trans")) {
				Bundle b = intent.getExtras();
				int msgid = b.getInt("msgid");
				switch(msgid) {
				case HttpThread.NET_SEND:
					
					break;
				}
			}
		}
		
	};
	
	@Override
	public void onCreate() {
		Log.i(this.getClass().getSimpleName(), "Data Service onCreate");
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("qingyou.net.trans");
		filter.setPriority(1000);
		registerReceiver(transfer, filter);
		
        SoundPlayer.newInstance(this);
        http = HttpThread.getInstance(this);
        http.user = config.getCfg("account");
        http.password = config.getCfg("password");
        http.startDataThread();
        
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.i(this.getClass().getSimpleName(), "Data Service onDestroy");
		
		http.stopDataThread();
		
		unregisterReceiver(transfer);
		
    	stopForeground(true);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(this.getClass().getSimpleName(), "Data Service onBind");
		return new MsgBinder();
	}
	
	@Override
	public void onRebind(Intent intent) {
		Log.i(this.getClass().getSimpleName(), "Data Service onRebind");
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(this.getClass().getSimpleName(), "Data Service onUnbind");
		super.onUnbind(intent);
		return false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(this.getClass().getSimpleName(), "Data Service onStartCommand");
		flags = START_STICKY;
		Notification notification = new Notification(R.drawable.ic_launcher,  
				 getString(R.string.app_name), System.currentTimeMillis());
		PendingIntent pendingintent = PendingIntent.getActivity(this, 0,  
				 new Intent(this, ActivityMain.class), 0);
		notification.setLatestEventInfo(this, "ConnectService", "请保持程序在后台运行",  
				 pendingintent);
		startForeground(0x111, notification);
		return super.onStartCommand(intent, flags, startId);
	}

	//Binder
	public class MsgBinder extends Binder {
		public ConnectService getService() {
			return ConnectService.this;
		}
	}
	
}
