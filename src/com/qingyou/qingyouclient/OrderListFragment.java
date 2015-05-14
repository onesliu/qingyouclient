package com.qingyou.qingyouclient;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.qingyou.businesslogic.OrderList;
import com.qingyou.businesslogic.OrderStatus;
import com.qingyou.http.HttpThread;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link OrderListFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link OrderListFragment#newInstance} factory
 * method to create an instance of this fragment.
 * 
 */
public class OrderListFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private int page;
	private int order_status;
	private String mParam2;

    private ListView mListView;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return 
	 * @return A new instance of fragment OrderListFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static OrderListFragment newInstance(int page, String param2) {
		OrderListFragment fragment = new OrderListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_PARAM1, page);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		
		return fragment;
	}

	public OrderListFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			page = getArguments().getInt(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);

			switch(page) {
			case ActivityMain.PAGE_1:
				order_status = OrderStatus.ORDER_STATUS_WAITING;
				break;
			case ActivityMain.PAGE_2:
				order_status = OrderStatus.ORDER_STATUS_PAYING;
				break;
			case ActivityMain.PAGE_3:
				order_status = OrderStatus.ORDER_STATUS_SCALED;
				break;
			case ActivityMain.PAGE_4:
				order_status = 0;
				break;
			}
			
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view;
		if (page < ActivityMain.PAGE_4) {
			view = inflater.inflate(R.layout.order_list, container, false);
			mListView = (ListView) view.findViewById(R.id.order_list);
		}
		else {
			view = inflater.inflate(R.layout.query_list, container, false);
			mListView = (ListView) view.findViewById(R.id.query_list);

			Button query_btn = (Button) view.findViewById(R.id.query_btn);
			query_btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					HttpThread http = HttpThread.getInstance(null);
					if (http == null) return;
					Bundle specials = http.GetSpecials();
					
					Intent intent = new Intent();
					intent.setClass(getActivity(), ActivityQuerySettings.class);
					intent.putExtras(specials);
					startActivityForResult(intent, 200);
				}
			});
			
			query_btn2 = (Button) view.findViewById(R.id.query_btn2);
			query_btn2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					queryOrder();
				}
			});
			
			query_prior = (Button) view.findViewById(R.id.query_prior);
			query_prior.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					freshQueryBtn(true, moveDate(query_btn2.getText().toString(), -1));
					queryOrder(query_btn2.getText().toString());
				}
			});
			query_next = (Button) view.findViewById(R.id.query_next);
			query_next.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					freshQueryBtn(true, moveDate(query_btn2.getText().toString(), 1));
					queryOrder(query_btn2.getText().toString());
				}
			});
			
			SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(getActivity());  
			boolean query_order_today = shp.getBoolean("query_order_today", true);
			
			freshQueryBtn(query_order_today, getCurDate());
			if (query_order_today)
				queryOrder(query_btn2.getText().toString());
		}
		return view;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 200) {
			queryOrder();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private OrderListAdapter adapter = null;
	private static OrderList querylist;
	private Button query_btn2;
	private Button query_prior;
	private Button query_next;

	@Override
	public void onStart() {
		super.onStart();
		setData();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setData();
	}
	
	public void freshListView() {
		if (mListView != null) {
			adapter = null;
			setData();
		}
	}
	
	public void setData() {
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		else {
			if (page < ActivityMain.PAGE_4)
				setData(OrderList.getGlobal());
			else if (page == ActivityMain.PAGE_4)
				setData(querylist);
		}
	}

	public void setData(OrderList orderlist) {
		
		final OrderList listItems = orderlist;
		if (listItems == null) {
			mListView.setAdapter(null);
		}
		else {
	        adapter = new OrderListAdapter(getActivity(), listItems, order_status);
	        mListView.setOnItemClickListener(new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
	        		Intent intent = new Intent();
	        		intent.setClass(getActivity(), ActivityOrderDetail.class);
	        		intent.putExtra("order_status", order_status);
	        		intent.putExtra("position", position);
	        		intent.putExtra("order_list", listItems);
	        		startActivity(intent);
	            }
	        });
	        mListView.setAdapter(adapter);
		}
	}
	
	public void queryOrder() {
		Bundle params = new Bundle();
		
		SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(getActivity());  
		boolean query_order_today = shp.getBoolean("query_order_today", true);
		String query_order_date = shp.getString("query_order_date", "");
		int query_order_status = Integer.parseInt(shp.getString("query_order_status", "0"));
		int query_preorder = Integer.parseInt(shp.getString("query_preorder", "0"));
		
		String curdate = getCurDate();
		
		freshQueryBtn(query_order_today, curdate);
		
		if (query_order_today == true) {
			params.putString("date", curdate);
		}
		else {
			if (query_order_date.equals(""))
				query_order_date = curdate;
			params.putString("date", query_order_date);
			if (query_order_status > 0)
				params.putString("status", "" + query_order_status);
			if (query_preorder > 0)
				params.putString("special_id", "" + query_preorder);
		}
		
		HttpThread http = HttpThread.getInstance(null);
		if (http == null) return;
		querylist = http.QueryOrders(params);
		if (querylist != null) {
			setData(querylist);
		}
	}

	public void queryOrder(String qdate) {
		Bundle params = new Bundle();
		params.putString("date", qdate);
		HttpThread http = HttpThread.getInstance(null);
		if (http == null) return;
		querylist = http.QueryOrders(params);
		if (querylist != null) {
			setData(querylist);
		}
	}

	private String getCurDate() {
		Calendar calendar=Calendar.getInstance();
		Date now = calendar.getTime();
		String curdate = MyUtils.formatDate(now);
		return curdate;
	}
	
	private String moveDate(String date, int offset) {
		Date d = MyUtils.parseDate(date);
		if (d == null) return "";
		
		long time = d.getTime() + offset*86400*1000;
		return MyUtils.formatDate(time);
	}
	
	private void freshQueryBtn(boolean today, String qdate) {
		if (today) {
			query_btn2.setText(qdate);
			query_prior.setEnabled(true);
			query_next.setEnabled(true);
		}
		else {
			query_btn2.setText(R.string.label_query);
			query_prior.setEnabled(false);
			query_next.setEnabled(false);
		}
	}


}
