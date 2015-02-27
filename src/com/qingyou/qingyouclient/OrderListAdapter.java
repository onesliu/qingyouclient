package com.qingyou.qingyouclient;

import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qingyou.businesslogic.Order;
import com.qingyou.businesslogic.OrderList;
import com.qingyou.businesslogic.OrderStatus;

public class OrderListAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater listContainer;
	private OrderList listItems;
	private int page;
	private int order_status;
	private Iterator<Order> itr;

	public final class ListItemView { // 自定义控件集合
		public TextView OrderId;
		public TextView Status;
		public TextView OrderTime;
		public TextView ProductSubject;
		public TextView Customer;
		public TextView Phone;
		public TextView ShippingAddress;
		public TextView ShippingTime;
		public TextView Comment;
	}

	public OrderListAdapter(Context context, OrderList listItems, int page) {
		this.context = context;
		listContainer = LayoutInflater.from(context);
		this.listItems = listItems;
		this.page = page;
		
		itr = listItems.orders.iterator();
		
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

	@Override
	public int getCount() {
		return listItems.size_status(order_status);
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ListItemView itemView = null;
		if (convertView == null) {
			itemView = new ListItemView();
			convertView = listContainer.inflate(R.layout.orderlist_item, null);
			itemView.OrderId = (TextView)convertView.findViewById(R.id.OrderId);
			itemView.OrderTime = (TextView)convertView.findViewById(R.id.OrderTime);
			itemView.Comment = (TextView)convertView.findViewById(R.id.Comment);
			itemView.Customer = (TextView)convertView.findViewById(R.id.Customer);
			itemView.Phone = (TextView)convertView.findViewById(R.id.Phone);
			itemView.ProductSubject = (TextView)convertView.findViewById(R.id.ProductSubject);
			itemView.ShippingAddress = (TextView)convertView.findViewById(R.id.ShippingAddress);
			itemView.ShippingTime = (TextView)convertView.findViewById(R.id.ShippingTime);
			itemView.Status = (TextView)convertView.findViewById(R.id.Status);
			
			convertView.setTag(itemView);
		}
		else {
			itemView = (ListItemView)convertView.getTag();
		}
		
		Order o = null;
		while(itr.hasNext()) {
			o = itr.next();
			if (o.order_status_orign == order_status)
				break;
		}
		
		if (o != null) {
		
			itemView.OrderId.setText(Long.toString(o.order_id));
			itemView.OrderTime.setText(o.order_createtime);
			itemView.Comment.setText(o.comment);
			itemView.Customer.setText(o.shipping_name);
			itemView.Phone.setText(o.customer_phone);
			itemView.ProductSubject.setText(o.productSubject);
			itemView.ShippingAddress.setText(o.shipping_addr);
			itemView.ShippingTime.setText(o.shipping_time);
			itemView.Status.setText(OrderStatus.getInstance().getStatus(o.order_status));
			ActivityOrderDetail.setOrderStatusColor(itemView.Status, o.order_status);
			
			final String phone = itemView.Phone.getText().toString();
			itemView.Phone.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+ phone));
					context.startActivity(intent);
				}
			});
		}

		return convertView;
	}

}
