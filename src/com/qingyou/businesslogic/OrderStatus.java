package com.qingyou.businesslogic;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

//单件记录订单状态映射表
public class OrderStatus implements Parcelable {

	public static final int ORDER_STATUS_WAITING = 1; //待称重
	public static final int ORDER_STATUS_PAYING = 2; //待付款
	public static final int ORDER_STATUS_SCALED = 3; //待配送
	public static final int ORDER_STATUS_FINISHED = 4; //已完成
	public static final int ORDER_STATUS_REFUND = 5; //已退款
	public static final int ORDER_STATUS_CANCEL = 6; //已取消

	private static OrderStatus _self = null;
	private HashMap<Integer, String> status;

	//=========================Parcel======================================
	
	public OrderStatus(Parcel in) {
		status = new HashMap<Integer, String>();
		in.readMap(status, HashMap.class.getClassLoader());
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeMap(status);
	}
	
	public static final Parcelable.Creator<OrderStatus> CREATOR = new Creator<OrderStatus>() {

		@Override
		public OrderStatus createFromParcel(Parcel source) {
			OrderStatus ol = new OrderStatus(source);
			OrderStatus._self = ol;
			return ol;
		}

		@Override
		public OrderStatus[] newArray(int size) {
			return new OrderStatus[size];
		}
	
	};
	
	//===============================================================

	private OrderStatus()
	{
		status = new HashMap<Integer, String>();
	}
	
	public static OrderStatus getInstance()
	{
		if (_self == null)
		{
			_self = new OrderStatus();
		}
		
		return _self;
	}

	public String getStatus(int status) {
		return (this.status.get(status)==null)?"未知状态":this.status.get(status);
	}
	
	public void putStatus(int sid, String sval) {
		status.put(sid, sval);
	}
	
	public int getSize() {
		return status.size();
	}
}
