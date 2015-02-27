package com.qingyou.qingyouclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

public class AlertToast {

	private static Toast toast = null;

	public static void showAlert(Context c, String msg) {
        if (toast == null) {
            toast = Toast.makeText(c , msg, Toast.LENGTH_LONG);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
	
}
