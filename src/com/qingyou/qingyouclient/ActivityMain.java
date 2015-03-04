package com.qingyou.qingyouclient;

import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.qingyou.http.HttpThread;
import com.qingyou.qingyouclient.R.id;

public class ActivityMain extends FragmentActivity implements
		ActionBar.TabListener {

	public static final int PAGE_CUR = 0;
	public static final int PAGE_1 = 0;
	public static final int PAGE_2 = 1;
	public static final int PAGE_3 = 2;
	public static final int PAGE_4 = 3;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		StartDataService();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent = new Intent();

		switch (item.getItemId()) {
		case id.action_login:
			intent.setClass(this, ActivityLogin.class);
			break;
		case id.action_settings:
			intent.setClass(this, ActivitySettings.class);
			break;
		}

		startActivity(intent);
		return false;
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
	}

	private Fragment[] fragments = new Fragment[4];

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		FragmentManager fm;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			this.fm = fm;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment frag = OrderListFragment.newInstance(position, "param2");
			fragments[position] = frag;
			return frag;
		}

		private void removeFragment(ViewGroup container, int position) {
			FragmentTransaction ft = fm.beginTransaction();
			String name = getFragmentName(container.getId(), position);
			Fragment fragment = fm.findFragmentByTag(name);
			if (fragment != null) {
				ft.remove(fragment);
				ft.commit();
				fm.executePendingTransactions();
			}
		}

		private String getFragmentName(int viewId, int index) {
			return "android:switcher:" + viewId + ":" + index;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub

			removeFragment(container, position);
			Fragment fragment = (Fragment) super.instantiateItem(container,
					position);
			return fragment;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		unregisterReceiver(transfer);
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction("qingyou.net.trans");
		registerReceiver(transfer, filter);
	}

	// ======================================================================
	// service control
	public static ConnectService dataService = null;

	public void StartDataService() {
		Intent srvIntent = new Intent(this, ConnectService.class);
		startService(srvIntent);
		bindService(srvIntent, mConnection, Context.BIND_AUTO_CREATE);
	}

	ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			dataService = ((ConnectService.MsgBinder) service).getService();

			// 启动登录窗口
			if (PrefConfig.instance(ActivityMain.this).getCfg("account")
					.equals("")) {
				Intent intent = new Intent();
				intent.setClass(ActivityMain.this, ActivityLogin.class);
				startActivity(intent);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			dataService = null;
		}

	};

	private BroadcastReceiver transfer = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("qingyou.net.trans")) {
				Bundle b = intent.getExtras();
				int msgid = b.getInt("msgid");
				AlertToast.showAlert(ActivityMain.this, b.getString("msg"));

				switch (msgid) {
				case HttpThread.NET_ORDER_REFRESH:
					if (fragments[ActivityMain.PAGE_1] != null) {
						((OrderListFragment) fragments[ActivityMain.PAGE_1])
								.setData();
					}
					if (fragments[ActivityMain.PAGE_2] != null) {
						((OrderListFragment) fragments[ActivityMain.PAGE_2])
								.setData();
					}
					if (fragments[ActivityMain.PAGE_3] != null) {
						((OrderListFragment) fragments[ActivityMain.PAGE_3])
								.setData();
					}
					if (fragments[ActivityMain.PAGE_4] != null) {
						((OrderListFragment) fragments[ActivityMain.PAGE_4])
								.setData();
					}
					break;
				case HttpThread.NET_ERROR:
					AlertToast.showAlert(ActivityMain.this, b.getString("msg"));
					break;
				}
			}
		}

	};

}
