package com.qingyou.qingyouclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qingyou.businesslogic.Order;
import com.qingyou.businesslogic.OrderList;
import com.qingyou.businesslogic.OrderStatus;
import com.qingyou.businesslogic.ParseBarCode;
import com.qingyou.businesslogic.Product;
import com.qingyou.http.HttpThread;
import com.qingyou.qr_codescan.MipcaActivityCapture;

public class ActivityOrderDetail extends Activity {

	TextView txtOrderId;
	TextView txtOrderStatus;
	TextView txtOrderTime;
	TextView txtCustomer;
	TextView txtPhone;
	TextView txtShippingAddress;
	TextView txtShippingTime;
	TextView txtComment;
	TextView txtOrderTotalPrice;
	TextView txtPreTotal;
	ListView listProduct;
	Button btnDeliver;
	PruductAdapter adapter;
	ScrollView mainScroll;
	
	OrderList orderlist;
	Order order;
	Product product;
	
	private final static int SCANNIN_GREQUEST_CODE = 1;
	private ParseBarCode parseBarCode = new ParseBarCode();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail);
	}
	
	@Override
	protected void onStart() {
		Intent intent = getIntent();
		int order_status = (int)intent.getIntExtra("order_status", 0);
		int position = (int)intent.getIntExtra("position", -1);
		
		findViews();
		
		orderlist = intent.getParcelableExtra("order_list");
		if (order_status > 0 && position >= 0)
			order = orderlist.get(order_status, position);
		else
			order = orderlist.orders.get(position);
		if (order == null ) {
			super.onStart();
			finish();
			return;
		}
		
		adapter = new PruductAdapter(this, order.products);
		listProduct.setAdapter(adapter);
		setListViewHeightBasedOnChildren(listProduct);
		mainScroll.smoothScrollTo(0,20);
		
		txtOrderId.setText(Long.toString(order.order_id));
		txtOrderTime.setText(order.order_createtime);
		txtCustomer.setText(order.shipping_name);
		txtPhone.setText(order.shipping_phone);
		txtShippingAddress.setText(order.shipping_addr);
		txtShippingTime.setText(order.shipping_time);
		txtComment.setText(order.comment);
		
		txtPreTotal.setText(MyUtils.cnv_price(order.getOrderTotal()));
		txtOrderTotalPrice.setText(MyUtils.cnv_price(order.getOrderRealTotal()));

		freshViewStatus();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("qingyou.net.trans");
		registerReceiver(transfer, filter);

		super.onStart();
	}
	
	public void setListViewHeightBasedOnChildren(ListView listView) {    
        ListAdapter listAdapter = listView.getAdapter();    
        if (listAdapter == null) {    
            return;    
        }    
        int totalHeight = 0;    
        for (int i = 0; i < listAdapter.getCount(); i++) {    
            View listItem = listAdapter.getView(i, null, listView);    
            listItem.measure(0, 0);   
            totalHeight += listItem.getMeasuredHeight();    
        }    
        ViewGroup.LayoutParams params = listView.getLayoutParams();    
        params.height = totalHeight    
                + (listView.getDividerHeight() * (listAdapter.getCount() + 1));    
        listView.setLayoutParams(params);    
    }

	public void freshViewStatus() {
		
		changeStatusText(order.order_status);
		
		if (order.order_status == OrderStatus.ORDER_STATUS_SCALED)
			btnDeliver.setEnabled(true);
		else
			btnDeliver.setEnabled(false);
	}

	private void findViews() {
		txtOrderId = (TextView)findViewById(R.id.dtOrderId);
		txtOrderStatus = (TextView)findViewById(R.id.dtStatus);
		txtOrderTime = (TextView)findViewById(R.id.dtOrderTime);
		txtCustomer = (TextView)findViewById(R.id.dtCustomer);
		txtPhone = (TextView)findViewById(R.id.dtPhone);
		txtShippingAddress = (TextView)findViewById(R.id.dtShippingAddress);
		txtShippingTime = (TextView)findViewById(R.id.dtShippingTime);
		txtComment = (TextView)findViewById(R.id.dtComment);
		btnDeliver = (Button)findViewById(R.id.btnDeliver);
		txtOrderTotalPrice = (TextView)findViewById(R.id.dtOrderTotalPrice);
		txtPreTotal = (TextView)findViewById(R.id.dtPreTotal);

		listProduct = (ListView)findViewById(R.id.listViewProduct);
		mainScroll = (ScrollView)findViewById(R.id.dtMainScroll);
		/*
		listProduct.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				startCodeScan();
			}
		});
		*/
		
	}
	
	public void startCodeScan() {
		if (order.hasScanedOver()) {
			AlertToast.showAlert(this, getString(R.string.alert_scaled));
			return;
		}
		
		Intent intent = new Intent();
		intent.setClass(this, MipcaActivityCapture.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				if (parseBarCode.parseWeightCode(bundle.getString("result")) == false) {
					AlertToast.showAlert(ActivityOrderDetail.this, getString(R.string.alert_errcode) + " " + bundle.getString("result"));
				}
				
				product = order.get(bundle.getString("result"));
				if (product == null) {
					AlertToast.showAlert(ActivityOrderDetail.this, getString(R.string.alert_nocode) + " " + bundle.getString("result"));
				}
				else if (product.hasScanned()) {
					AlertToast.showAlert(ActivityOrderDetail.this, getString(R.string.alert_scaned) + " " + bundle.getString("result"));
				}
				else {
					product.addScan(parseBarCode.weight);
					adapter.notifyDataSetChanged();
					Log.v(product.product_id + " " + product.product_name);
				}

				if (order.hasScanedOver() == false) {
					startCodeScan();
				}

				finish();
				//freshViewStatus();
			}
			break;
		}
	}

	public void changeStatusText(int status) {
		String cashpay = "";
		if (order.iscash > 0) cashpay = getString(R.string.label_cashpay);
		txtOrderStatus.setText(OrderStatus.getInstance().getStatus(status) + cashpay);
		setOrderStatusColor(txtOrderStatus, status);
	}

	public static void setOrderStatusColor(TextView txtOrderStatus, int status) throws NotFoundException {
		Context c = txtOrderStatus.getContext();
		switch(status) {
		case OrderStatus.ORDER_STATUS_WAITING:
			txtOrderStatus.setTextColor(c.getResources().getColor(R.color.red));
			break;
		case OrderStatus.ORDER_STATUS_PAYING:
			txtOrderStatus.setTextColor(c.getResources().getColor(R.color.orange));
			break;
		case OrderStatus.ORDER_STATUS_SCALED:
			txtOrderStatus.setTextColor(c.getResources().getColor(R.color.blue));
			break;
		case OrderStatus.ORDER_STATUS_REFUND:
		case OrderStatus.ORDER_STATUS_CANCEL:
			txtOrderStatus.setTextColor(c.getResources().getColor(R.color.gray));
			break;
		case OrderStatus.ORDER_STATUS_FINISHED:
			txtOrderStatus.setTextColor(c.getResources().getColor(R.color.black));
			break;
		default:
			txtOrderStatus.setTextColor(c.getResources().getColor(R.color.red));
		}
	}
	
	private void commit() {
		HttpThread http = HttpThread.getInstance(null);
		if (http == null) return;
		http.commitOrder(order, OrderStatus.ORDER_STATUS_FINISHED);
	}
	
	public void onClickDelivered(View v) {
		final EditText input = new EditText(this);
		//input.setInputType(type);
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
    	.setTitle(R.string.ask_deliver)
    	.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (order.iscash > 0) {
					
					if (input.getText().toString().equals("")) {
						AlertToast.showAlert(ActivityOrderDetail.this, "到付订单必须输入收款金额");
						return;
					}
					
					double cashpay = 0;
					try {
						cashpay = Double.parseDouble(input.getText().toString());
					}
					catch(Exception e) {
						AlertToast.showAlert(ActivityOrderDetail.this, "收款金额输入不正确");
						return;
					}
					
					if (cashpay <= 0) {
						AlertToast.showAlert(ActivityOrderDetail.this, "收款金额输入不正确");
						return;
					}
					
					order.cashpay = cashpay;
					
					double offset = Math.abs(order.cashpay - order.getOrderRealTotal());
					if (offset > 1) {
						new AlertDialog.Builder(ActivityOrderDetail.this)
						.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
						.setTitle("实收金额与订单金额差距" + offset + "元,继续吗？")
						.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								commit();
							}
						})
						.setNegativeButton(R.string.label_return, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.show();
					}
					else {
						commit();
					}
				}
				else {
					commit();
				}
			}
		})
		.setNegativeButton(R.string.label_return, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		if (order.iscash > 0)
			builder.setView(input);
		builder.show();
	}
	
	@Override
	protected void onStop() {
		unregisterReceiver(transfer);
		super.onStop();
	}
	
	private BroadcastReceiver transfer = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("qingyou.net.trans")) {
				Bundle b = intent.getExtras();
				int msgid = b.getInt("msgid");
				
				switch(msgid) {
				case HttpThread.COMMIT_ORDER:
					AlertToast.showAlert(ActivityOrderDetail.this, b.getString("msg"));
					ActivityOrderDetail.this.finish();
					break;
				case HttpThread.NET_ERROR:
					AlertToast.showAlert(ActivityOrderDetail.this, b.getString("msg"));
					break;
				}
			}
		}
		
	};
}
