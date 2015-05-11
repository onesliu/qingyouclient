package com.qingyou.qingyouclient;

import java.util.Calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class DatePreference extends DialogPreference {
	
	private String mDate = "";

	public DatePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.dialog_datepicker);
	}
	
	public void setDate(String text) {
        final boolean wasBlocking = shouldDisableDependents();
        
        mDate = text;
        
        persistString(text);
        
        final boolean isBlocking = shouldDisableDependents(); 
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }
	
	public String getDate() {
        return mDate;
    }

	@Override
	protected void onBindDialogView(View view) {
		// TODO Auto-generated method stub
		super.onBindDialogView(view);
		DatePicker mPicker = (DatePicker)view.findViewById(R.id.datePicker1);  
        if(mPicker != null) {
        	String []datepart = mDate.split("-");
        	int year, month, day;
        	if (datepart.length != 3) {
        		Calendar calendar=Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH) + 1;
                day = calendar.get(Calendar.DAY_OF_MONTH);
        	}
        	else {
        		year = Integer.parseInt(datepart[0]);
        		month = Integer.parseInt(datepart[1]);
        		day = Integer.parseInt(datepart[2]);
        	}
        	mPicker.init(year, month-1, day, new OnDateChangedListener() {

					@Override
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						mDate = "" + year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
					}
        		
        	});
        }
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		super.onDialogClosed(positiveResult);
		if(positiveResult) {
			if(callChangeListener(mDate)) {
				setDate(mDate);
            }
        }
	}
	
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setDate(restoreValue ? getPersistedString(mDate) : (String) defaultValue);
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mDate) || super.shouldDisableDependents();
    }

}