package com.qingyou.qingyouclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.qingyou.businesslogic.OrderList;
import com.qingyou.businesslogic.OrderStatus;

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
			}
			
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.order_list, container, false);
		mListView = (ListView) view.findViewById(R.id.order_list);
		return view;
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
	ObjectMap gobj = ObjectMap.getInstance();

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
			OrderList listItems = OrderList.getGlobal();
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
		        		startActivity(intent);
		            }
		        });
		        mListView.setAdapter(adapter);
			}
			
		}
	}
	

}
