package com.example.boli.groupreply;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

class SMSListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<SMS> mList;
    HashMap<Integer,View> hmListViewMap;

    class ViewHolder{
        TextView mSMS = null;
        TextView mDate = null;
    }

    public SMSListAdapter(Context context, ArrayList<SMS> mSMSList) {
        mInflater = LayoutInflater.from(context);
        mList = mSMSList;
        hmListViewMap = new HashMap<Integer,View>();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vView1;
        final ViewHolder vhHolder;
        if (hmListViewMap.get(i) == null) {
            SMS mSms = mList.get(i);
            if (mSms.type == 1) {
                vView1 = mInflater.inflate(R.layout.sms_from_you, null);
            } else {
                vView1 = mInflater.inflate(R.layout.sms_from_me, null);
            }
            vhHolder = new ViewHolder();
            vhHolder.mSMS = (TextView) vView1.findViewById(R.id.textView);
            vhHolder.mDate = (TextView) vView1.findViewById(R.id.textView2);
            hmListViewMap.put(i, vView1);
            vhHolder.mSMS.setText(mSms.smsBody);
            DateFormat mDateFormat = DateFormat.getDateTimeInstance();
            Date d = new Date(mSms.time);
            String strDate = mDateFormat.format(d);
            vhHolder.mDate.setText(strDate);
            vView1.setTag(vhHolder);
        } else {
            vView1 = hmListViewMap.get(i);
        }
        return vView1;
    }
}

public class ConversationActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent mIntent = getIntent();
        ArrayList<SMS> mSMSList = (ArrayList<SMS>) mIntent.getSerializableExtra("smslist");
        SMSListAdapter mSMSListAdapter = new SMSListAdapter(this, mSMSList);
        setListAdapter(mSMSListAdapter);
    }

}
