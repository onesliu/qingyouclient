package com.qingyou.qingyouclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qingyou.http.HttpThread;

public class ActivityLogin extends Activity {

	TextView txtAccount;
	TextView txtPasswd;
	Button btnLogin;
	PrefConfig config = PrefConfig.instance(this);
	
	HttpThread http = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		txtAccount = (TextView)findViewById(R.id.loginAccount);
		txtPasswd = (TextView)findViewById(R.id.loginPasswd);
		btnLogin = (Button)findViewById(R.id.sign_in_button);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		txtAccount.setText(config.getCfg("account"));
		txtPasswd.setText(config.getCfg("password"));

		http = HttpThread.getInstance(null);
		if (http.isLogin == true) {
			btnLogin.setText(R.string.label_logout);
		}
		else {
			btnLogin.setText(R.string.action_sign_in_short);
		}
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("qingyou.net.trans");
		registerReceiver(transfer, filter);

	}
	
	@Override
	protected void onStop() {
		unregisterReceiver(transfer);
		super.onStop();
	}

	public void OnClickLogin(View v) {
		
		config.saveCfg("account", txtAccount.getText().toString());
		config.saveCfg("password", txtPasswd.getText().toString());
		
		if (http.isLogin == true) {
			http.user = "";
			http.password = "";
			http.isLogin = false;
			btnLogin.setText(R.string.action_sign_in_short);
			AlertToast.showAlert(this, getString(R.string.label_logout));
		}
		else {
			http.user = config.getCfg("account");
			http.password = config.getCfg("password");
			http.conti = true;
		}
	}
	
	private BroadcastReceiver transfer = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("qingyou.net.trans")) {
				Bundle b = intent.getExtras();
				int msgid = b.getInt("msgid");
				AlertToast.showAlert(ActivityLogin.this, b.getString("msg"));
				
				switch(msgid) {
				case HttpThread.LOGIN_OK:
					btnLogin.setText(R.string.label_logout);
					ActivityLogin.this.finish();
					break;
				case HttpThread.LOGIN_FAIL:
					btnLogin.setText(R.string.action_sign_in_short);
					break;
				}
			}
		}
		
	};
}
