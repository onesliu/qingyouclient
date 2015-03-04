package com.qingyou.qingyouclient;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qingyou.businesslogic.Product;

public class PruductAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater listContainer;
	private List<Product> listItems;
	
	public final class ListItemView { // 自定义控件集合
		public TextView ProductName;
		public TextView Price;
		public TextView Quantity;
		public TextView TotalPrice;
		public TextView PerWeight;
		public TextView WeightTotal;
		public TextView RealWeight;
		public TextView RealTotal;
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
			itemView.Quantity = (TextView)convertView.findViewById(R.id.Quantity);
			itemView.TotalPrice = (TextView)convertView.findViewById(R.id.TotalPrice);
			itemView.PerWeight = (TextView)convertView.findViewById(R.id.PerWeight);
			itemView.WeightTotal = (TextView)convertView.findViewById(R.id.WeightTotal);
			itemView.RealWeight = (TextView)convertView.findViewById(R.id.RealWeight);
			itemView.RealTotal = (TextView)convertView.findViewById(R.id.RealTotal);
			
			convertView.setTag(itemView);
		}
		else {
			itemView = (ListItemView)convertView.getTag();
		}
		
		final Product p = listItems.get(position);

		itemView.ProductName.setText(p.product_name);
		itemView.Price.setText(MyUtils.cnv_price(p.price) + "/" + p.unit);
		itemView.Quantity.setText("" + p.quantity + p.perunit);
		itemView.TotalPrice.setText(context.getString(R.string.label_guest_total) + MyUtils.cnv_price(p.total));
		itemView.PerWeight.setText(context.getString(R.string.label_guest_perweight) + MyUtils.cnv_weight(p.perweight) + p.weightunit);
		itemView.WeightTotal.setText(context.getString(R.string.label_guest_weight) + MyUtils.cnv_weight(p.perweight * p.quantity) + p.weightunit);
		itemView.RealWeight.setText(context.getString(R.string.label_realweight) + MyUtils.cnv_weight(p.realweight) + p.weightunit);
		itemView.RealTotal.setText(context.getString(R.string.label_realtotalpay) + MyUtils.cnv_price(p.realtotal));
		
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
