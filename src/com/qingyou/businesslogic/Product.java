package com.qingyou.businesslogic;

import java.math.BigDecimal;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
	public int product_id = 0;
	public int plu_serial_no = 0;
	public String product_name = "";
	public int product_type = 0;
	public String ean = "";
	public double price = 0;
	public double perprice = 0;
	public double perweight = 0;
	public String unit = "";
	public String perunit = "";
	public String weightunit = "";
	public int quantity = 0;
	public double total = 0;
	public double realweight = 0;
	public double realtotal = 0;
	public String image = "";
	
	public int scancount = 0;

	//=========================Parcel======================================
	
	public Product(Parcel in) {
		product_id = in.readInt();
		plu_serial_no = in.readInt();
		product_name = in.readString();
		product_type = in.readInt();
		ean = in.readString();
		price = in.readDouble();
		perprice = in.readDouble();
		perweight = in.readDouble();
		unit = in.readString();
		perunit = in.readString();
		weightunit = in.readString();
		quantity = in.readInt();
		total = in.readDouble();
		realweight = in.readDouble();
		realtotal = in.readDouble();
		image = in.readString();
		scancount = in.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(product_id);
		dest.writeInt(plu_serial_no);
		dest.writeString(product_name);
		dest.writeInt(product_type);
		dest.writeString(ean);
		dest.writeDouble(price);
		dest.writeDouble(perprice);
		dest.writeDouble(perweight);
		dest.writeString(unit);
		dest.writeString(perunit);
		dest.writeString(weightunit);
		dest.writeInt(quantity);
		dest.writeDouble(total);
		dest.writeDouble(realweight);
		dest.writeDouble(realtotal);
		dest.writeString(image);
		dest.writeInt(scancount);
	}
	
	public static final Parcelable.Creator<Product> CREATOR = new Creator<Product>() {

		@Override
		public Product createFromParcel(Parcel source) {
			Product p = new Product(source);
			return p;
		}

		@Override
		public Product[] newArray(int size) {
			return new Product[size];
		}
	
	};
	
	//===============================================================

	public Product() {
	}
	
	public boolean hasScanned() {
		if (scancount < quantity) return false;
		return true;
	}
	
	public void resetScan() {
		scancount = 0;
	}
	
	public void finishScan() {
		scancount = quantity;
	}
	
	public void addScan(double weight) {
		if (scancount == 0) {
			realweight = realtotal = 0;
		}
		else if (scancount >= quantity) {
			return;
		}
		
		realweight += weight;
		BigDecimal bg = new BigDecimal(realweight * price);
		realtotal = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(); //金额2位小数四舍五入
		scancount++;
	}
}