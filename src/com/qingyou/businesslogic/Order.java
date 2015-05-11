package com.qingyou.businesslogic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Order implements Parcelable {

	public long order_id = 0;
	public int order_status = 0;
	public String order_createtime = "";
	public int customer_id = 0;
	public String customer_name = "";
	public String customer_phone = "";
	public String shipping_name = "";
	public String shipping_phone = "";
	public String shipping_addr = "";
	public String shipping_time = "";
	public String comment = "";
	public double costpay = 0;
	public double cashpay = 0;
	public int iscash = 0;
	
	public int order_type = 0;
	public int order_status_orign = 0;
	public String productSubject = "";
	public boolean is_delete = false;

	public List<Product> products;
	
	//=========================Parcel======================================
	
	public Order(Parcel in) {
		order_id = in.readLong();
		order_status = in.readInt();
		order_createtime = in.readString();
		customer_id = in.readInt();
		customer_name = in.readString();
		customer_phone = in.readString();
		shipping_name = in.readString();
		shipping_phone = in.readString();
		shipping_addr = in.readString();
		shipping_time = in.readString();
		comment = in.readString();
		iscash = in.readInt();
		order_type = in.readInt();
		order_status_orign = in.readInt();
		productSubject = in.readString();
		is_delete = (in.readInt()>0)?true:false;
		products = new ArrayList<Product>();
		in.readTypedList(products, Product.CREATOR);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(order_id);
		dest.writeInt(order_status);
		dest.writeString(order_createtime);
		dest.writeInt(customer_id);
		dest.writeString(customer_name);
		dest.writeString(customer_phone);
		dest.writeString(shipping_name);
		dest.writeString(shipping_phone);
		dest.writeString(shipping_addr);
		dest.writeString(shipping_time);
		dest.writeString(comment);
		dest.writeInt(iscash);
		dest.writeInt(order_type);
		dest.writeInt(order_status_orign);
		dest.writeString(productSubject);
		dest.writeInt((is_delete)?1:0);
		dest.writeTypedList(products);
	}
	
	public static final Parcelable.Creator<Order> CREATOR = new Creator<Order>() {

		@Override
		public Order createFromParcel(Parcel source) {
			Order o = new Order(source);
			return o;
		}

		@Override
		public Order[] newArray(int size) {
			return new Order[size];
		}
	
	};
	
	//===============================================================

	public Order() {
		products = new ArrayList<Product>();
	}
	
	public void add_product(Product p) {
		products.add(p);
	}
	
	public Product get(int productid) {
		Iterator<Product> itr = products.iterator();
		Product p = null, ret = null;
		while(itr.hasNext()) {
			p = itr.next();
			if (p.product_id == productid) {
				ret = p;
				break;
			}
		}
		return ret;
	}
	
	public Product get(String barcode) {
		Iterator<Product> itr = products.iterator();
		ParseBarCode pbc = new ParseBarCode();
		Product p = null, ret = null;
		int id1, id2;

		if (pbc.parseWeightCode(barcode) == false)
			return null;
		id1 = pbc.productId;
		
		while(itr.hasNext()) {
			p = itr.next();
			if (pbc.parseWeightCode(p.ean) == false)
				continue;
			id2 = pbc.productId;
			
			if (id1 == id2) {
				ret = p;
				break;
			}
		}
		return ret;
	}
	
	public int product_size() {
		return products.size();
	}
	
	public void initStatus(int status) {
		order_status = order_status_orign = status;
	}
	
	public void resetStatus() {
		order_status = order_status_orign;
	}
	
	public boolean hasChanged() {
		if (order_status != order_status_orign)
			return true;
		return false;
	}
	
	public void commit() {
		order_status_orign = order_status;
	}
	
	public boolean hasScanedOver() {
		if (order_status >= OrderStatus.ORDER_STATUS_FINISHED)
			return true;
		
		Iterator<Product> itr = products.iterator();
		while(itr.hasNext()) {
			if (itr.next().hasScanned() == false)
				return false;
		}
		
		if (order_type == 0)
			order_status = OrderStatus.ORDER_STATUS_SCALED;
		else
			order_status = OrderStatus.ORDER_STATUS_PAYING;

		return true;
	}
	
	public void setDelivered() {
		order_status = OrderStatus.ORDER_STATUS_FINISHED;
	}
	
	public double getOrderTotal() {
		Iterator<Product> itr = products.iterator();
		double total = 0.0;
		while(itr.hasNext()) {
			total += itr.next().total;
		}
		return total;	
	}
	
	public double getOrderRealTotal() {
		Iterator<Product> itr = products.iterator();
		double total = 0.0;
		while(itr.hasNext()) {
			total += itr.next().realtotal;
		}
		return total;	
	}
}
