package com.qingyou.qingyouclient;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qingyou.businesslogic.Order;
import com.qingyou.businesslogic.OrderStatus;
import com.qingyou.businesslogic.Product;

public class PruductAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater listContainer;
	private List<Product> listItems;
	private Order order;
	
	public void setOrder(Order order) {
		this.order = order;
	}

	public final class ListItemView { // 自定义控件集合
		public TextView ProductName;
		public TextView Price;
		public TextView Total;
		public TextView RealWeight;
		public TextView RealTotal;
		public TextView Rescan;
		public List<TextView> OrderCount = new ArrayList<TextView>();
	}

	private void resetOrderCount(int count, ListItemView item) {
		for(int i = 0; i < item.OrderCount.size(); i++) {
			if (count > i)
				item.OrderCount.get(i).setVisibility(View.VISIBLE);
			else
				item.OrderCount.get(i).setVisibility(View.INVISIBLE);
			item.OrderCount.get(i).setText(item.ProductName.getText().toString());
			item.OrderCount.get(i).setTextColor(context.getResources().getColor(R.color.black));
			item.OrderCount.get(i).setBackgroundResource(R.drawable.before_scan_circle);
		}
	}
	
	private void setOrderScaned(int count, ListItemView item) {
		if (count > item.OrderCount.size())
			count = item.OrderCount.size();
		for(int i = 0; i < count; i++) {
			if (item.OrderCount.get(i).getVisibility() == View.VISIBLE) {
				item.OrderCount.get(i).setTextColor(context.getResources().getColor(R.color.white));
				item.OrderCount.get(i).setBackgroundResource(R.drawable.after_scan_circle);
			}
		}
	}
	
	public PruductAdapter(Context context, List<Product> listItems) {
		this.context = context;
		listContainer = LayoutInflater.from(context);
		this.listItems = listItems;
	}

	@Override
	public int getCount() {
		return listItems.size();
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
			convertView = listContainer.inflate(R.layout.product_item, null);
			itemView.ProductName = (TextView)convertView.findViewById(R.id.ProductName);
			itemView.Price = (TextView)convertView.findViewById(R.id.Price);
			itemView.Total = (TextView)convertView.findViewById(R.id.Total);
			itemView.RealWeight = (TextView)convertView.findViewById(R.id.RealWeight);
			itemView.RealTotal = (TextView)convertView.findViewById(R.id.RealTotal);
			itemView.OrderCount.add((TextView)convertView.findViewById(R.id.OrderCount1));
			itemView.OrderCount.add((TextView)convertView.findViewById(R.id.OrderCount2));
			itemView.OrderCount.add((TextView)convertView.findViewById(R.id.OrderCount3));
			itemView.OrderCount.add((TextView)convertView.findViewById(R.id.OrderCount4));
			itemView.OrderCount.add((TextView)convertView.findViewById(R.id.OrderCount5));
			itemView.OrderCount.add((TextView)convertView.findViewById(R.id.OrderCount6));
			itemView.OrderCount.add((TextView)convertView.findViewById(R.id.OrderCount7));
			itemView.OrderCount.add((TextView)convertView.findViewById(R.id.OrderCount8));
			itemView.Rescan = (TextView)convertView.findViewById(R.id.Rescan);
			
			convertView.setTag(itemView);
		}
		else {
			itemView = (ListItemView)convertView.getTag();
		}
		
		final Product p = listItems.get(position);

		itemView.ProductName.setText(p.product_name + "：");
		itemView.Price.setText(Double.toString(p.price));
		itemView.Total.setText(Double.toString(p.total));

		resetOrderCount(p.quantity, itemView);
		setOrderScaned(p.scancount, itemView);
		itemView.RealWeight.setText(MyUtils.cnv_weight(p.realweight));
		itemView.RealTotal.setText(MyUtils.cnv_price(p.realtotal));
		
		itemView.Rescan.setVisibility(View.GONE);
		/*
		if (order.order_status >= OrderStatus.ORDER_STATUS_FINISHED) {
			itemView.Rescan.setVisibility(View.GONE);
		}
		else {
			itemView.Rescan.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					p.resetScan();
					order.resetStatus();
					notifyDataSetChanged();
					((ActivityOrderDetail)context).freshViewStatus();
				}
			});
		}
		*/

		return convertView;
	}

}
