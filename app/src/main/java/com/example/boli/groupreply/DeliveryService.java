package com.example.boli.groupreply;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

public class DeliveryService extends Service {
    public static boolean service_started = false;
    int receiveCount = 0;
    private BroadcastReceiver mSendBroadcastReceiver;
    private BroadcastReceiver mDeliveryBroadcastReceiver;
    public DeliveryService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service_started = true;
        Log.i("LBL", "DeliveryService onCreate");
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        Log.i("LBL", "DeliveryService onStartCommand");
        final int sendListSize = intent.getIntExtra("send_list_size", 0);

        mSendBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i("LBL", "Activity.RESULT_OK");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.i("LBL", "RESULT_ERROR_NO_SERVICE");
                        String address = intent.getStringExtra("address");
                        String sms = intent.getStringExtra("body");
                        long date = intent.getLongExtra("date", 0);
                        String str_uri = intent.getStringExtra("uri");
                        Uri uri = Uri.parse(str_uri);
                        getContentResolver().delete(uri, "*", null);
                        ContentValues values = new ContentValues();
                        values.put("date", date);
                        values.put("read", 0);
                        values.put("type", 5);
                        values.put("status", 128);
                        values.put("address", address);
                        values.put("body", sms);
                        getContentResolver().insert(Uri.parse("content://sms/failed"), values);
                        break;
                }
                receiveCount++;
                if (receiveCount >= sendListSize) {
                    stopSelf();
                }
            }
        };
        this.registerReceiver(mSendBroadcastReceiver, new IntentFilter("SEND_SMS_ACTION"));

        mDeliveryBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String address = intent.getStringExtra("address");
                String sms = intent.getStringExtra("body");
                long date = intent.getLongExtra("date", 0);
                String str_uri = intent.getStringExtra("uri");
                Uri uri = Uri.parse(str_uri);
                getContentResolver().delete(uri, "*", null);
                ContentValues values = new ContentValues();
                values.put("date", date);
                values.put("read", 0);
                values.put("type", 2);
                values.put("status", 0);
                values.put("address", address);
                values.put("body", sms);
                getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                receiveCount++;
                Log.i("LBL", "onReceive intent = " + intent.hashCode() + " context = " + context + " address = " + address
                        + "uri = " + uri.toString());

                if (receiveCount >= sendListSize) {
                    stopSelf();
                }
            }
        };
        this.registerReceiver(mDeliveryBroadcastReceiver, new IntentFilter("DELIVERED_SMS_ACTION"));
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mDeliveryBroadcastReceiver);
        this.unregisterReceiver(mSendBroadcastReceiver);
        service_started = false;
        Log.i("LBL", "DeliveryService onDestroy");
    }
}
