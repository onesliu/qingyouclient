package com.qingyou.businesslogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.qingyou.qingyouclient.ObjectMap;
import com.qingyou.qingyouclient.SoundPlayer;

//OrderList 只记录状态变化了的订单，包括产品被称重状态的变化
public class OrderList implements Parcelable {

	public List<Order> orders;
	
	//=========================Parcel======================================
	
	public OrderList(Parcel in) {
		orders = new ArrayList<Order>();
		in.readTypedList(orders, Order.CREATOR);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(orders);
	}
	
	public static final Parcelable.Creator<OrderList> CREATOR = new Creator<OrderList>() {

		@Override
		public OrderList createFromParcel(Parcel source) {
			OrderList ol = new OrderList(source);
			return ol;
		}

		@Override
		public OrderList[] newArray(int size) {
			return new OrderList[size];
		}
	
	};
	
	//===============================================================
	
	public static OrderList getGlobal() {
		ObjectMap gobj = ObjectMap.getInstance();
		OrderList olist;
		if (gobj.get("orderlist") == null) {
			olist = new OrderList();
			gobj.put("orderlist", olist);
		}
		else {
			olist = (OrderList)gobj.get("orderlist");
		}
		return olist;
	}
	
	public OrderList() {
		orders = new ArrayList<Order>();
	}
	
	public void clear() {
		orders.clear();
	}
	
	public Order get(long orderid) {
		for(int i = 0; i < orders.size(); i++) {
			if (orders.get(i).order_id == orderid)
				return orders.get(i);
		}
		return null;
	}
	
	public Order get(int order_status, int position) {
		Order o = null;
		int i = 0;
		Iterator<Order> itr = orders.iterator();
		while(itr.hasNext()) {
			Order oo = itr.next();
			if (oo.order_status == order_status) {
				if (i == position) {
					o = oo;
					break;
				}
				else {
					i++;
				}
			}
		}
		return o;
	}
	
	public void add(Order order) {
		orders.add(order);
	}
	
	public int size() {
		return orders.size();
	}
	
	public int size_status(int status) {
		int size = 0;
		Iterator<Order> itr = orders.iterator();
		while(itr.hasNext()) {
			Order o = itr.next();
			if (o.order_status_orign == status)
				size++;
		}
		return size;
	}
	
	public boolean hasChangedOrder() {
		Iterator<Order> itr = orders.iterator();
		while(itr.hasNext()) {
			Order o = itr.next();
			if (o.hasChanged())
				return true;
		}
		return false;
	}
	
	public void mergeOrderList(OrderList newlist) {
		if (newlist == null) return;
		
		//删除上次完成的订单
		clearFinished();
		
		Iterator<Order> itr = orders.iterator();
		while(itr.hasNext()) {
			Order o_old = itr.next();
			Order o_new = newlist.get(o_old.order_id);

			if (o_new == null && o_old.is_delete == false) {
				//有完成订单，报警
				o_old.is_delete = true;
				if (SoundPlayer.getInstance() != null)
					SoundPlayer.getInstance().playSound(3, 1);
			}
		}

		List<Order> new_orders = new ArrayList<Order>(); 
		itr = newlist.orders.iterator();
		while(itr.hasNext()) {
			Order o_new = itr.next();
			Order o_old = this.get(o_new.order_id);
			
			if (o_old == null) {
				//有新订单，报警
				new_orders.add(o_new);
				itr.remove();
				if (SoundPlayer.getInstance() != null)
					SoundPlayer.getInstance().playSound(1, 1);
			}
			else if (o_old != null) {
				if (o_old.order_status_orign != o_new.order_status_orign) {
					//有修改订单，报警
					new_orders.add(o_new);
					itr.remove();
					orders.remove(o_old);
					if (SoundPlayer.getInstance() != null)
						SoundPlayer.getInstance().playSound(2, 1);
				}
			}
		}
		
		if (new_orders.size() > 0) {
			orders.addAll(new_orders);
		}
		
		clearFinished();
	}
	
	public void clearFinished() {
		Iterator<Order> itr = orders.iterator();
		while(itr.hasNext()) {
			Order o = itr.next();
			if (o.is_delete == true) {
				itr.remove();
			}
		}
	}
	
	public void commitAllOrders() {
		Iterator<Order> itr = orders.iterator();
		while(itr.hasNext()) {
			itr.next().commit();
		}
	}
	
	class SortByTime implements Comparator {
		 public int compare(Object o1, Object o2) {
			 Order s1 = (Order) o1;
			 Order s2 = (Order) o2;
			 return s1.order_createtime.compareTo(s2.order_createtime);
		 }
	}

	public void sortByTime() {
		Collections.sort(orders, new SortByTime());
	}

}
