package com.qingyou.qingyouclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qingyou.businesslogic.Order;
import com.qingyou.businesslogic.OrderList;
import com.qingyou.businesslogic.OrderStatus;
import com.qingyou.businesslogic.ParseBarCode;
import com.qingyou.businesslogic.Product;
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
		int order_status = (int)intent.getIntExtra("order_status", -1);
		int position = (int)intent.getIntExtra("position", -1);
		
		findViews();
		
		orderlist = OrderList.getGlobal();
		order = orderlist.get(order_status, position);
		if (order == null ) {
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
		txtOrderTotalPrice.setText(MyUtils.cnv_price(order.getOrderRealTotal()));

		freshViewStatus();

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
		txtOrderStatus.setText(OrderStatus.getInstance().getStatus(status));
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
	
	public void onClickDelivered(View v) {
    	new AlertDialog.Builder(this)
    	.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert))
    	.setTitle(R.string.ask_deliver)
    	.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				order.setDelivered();
				txtOrderStatus.setText(OrderStatus.getInstance().getStatus(order.order_status));
				finish();
			}
		})
		.setNegativeButton(R.string.label_return, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.show();
	}
}
