package com.example.boli.groupreply;

import android.support.v7.app.ActionBarActivity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

class SMS implements Serializable {
    int type = -1;
    long time = 0;
    String smsBody = null;
}

class Conversation{
    String name = null;
    String latestSMS = null;
    String address = null;
    ArrayList<SMS> smsArrayList;
}

class ConversationListAdapter extends BaseAdapter {
    private HashMap<String, Conversation> mItemsMap;
    private LayoutInflater mInflater;
    HashMap<Integer,View> mListViewMap;
    String[] positionArray;
    static HashMap<String, Boolean> mSendMap = new HashMap<>(64);

    class ViewHolder{
        TextView mName = null;
        TextView mSMS = null;
        CheckBox mCheck = null;
    }

    public ConversationListAdapter(HashMap<String, Conversation> mapConversations, String[] position,
                                   Context context){
        mItemsMap = mapConversations;
        mInflater = LayoutInflater.from(context);
        mListViewMap = new HashMap<>(64);
        positionArray = position;
    }

    @Override
    public int getCount() {
        return mItemsMap.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemsMap.get(positionArray[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View mView1;
        final ViewHolder mHolder;

        //Log.i("LBL", "" + intCount++);
        if (mListViewMap.get(position) == null) {
            mView1 = mInflater.inflate(R.layout.conversation_item, null);
            mHolder = new ViewHolder();
            assert mView1 != null;
            mHolder.mName = (TextView) mView1.findViewById(R.id.textView1);
            mHolder.mSMS = (TextView) mView1.findViewById(R.id.textView2);
            mHolder.mCheck = (CheckBox) mView1.findViewById(R.id.checkBox1);
            mListViewMap.put(position, mView1);
            final Conversation mConversation = mItemsMap.get(positionArray[position]);
            mHolder.mName.setText(mConversation.name);
            mHolder.mSMS.setText(mConversation.latestSMS);

            mHolder.mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (isChecked) {
                        mHolder.mCheck.setChecked(true);
                        mSendMap.put(mConversation.address, false);
                        Log.i("LBL", position + " has been checked!");
                    } else {
                        mSendMap.remove(mConversation.address);
                        mHolder.mCheck.setChecked(false);
                        Log.i("LBL", position + " has been unchecked!");
                    }
                }

            });
            mView1.setTag(mHolder);
        }else{
            mView1 = mListViewMap.get(position);
        }

        return mView1;
    }

}

public class MainActivity extends ActionBarActivity {
    public static AlarmManager mAlarmManager;
    public static ProgressDialog mProgressDialog1;
    String[] positionArray;
    HashMap<String, Conversation> mConversationsMap;
    Conversation mConversation;
    SMS mSMS;
    HashMap<String, String> mNum2NameMap;

    private HashMap<String, Conversation> getSmsInPhone() {
        final String SMS_URI_ALL = "content://sms/";
        //final String SMS_URI_INBOX = "content://sms/inbox";
        mConversationsMap = new HashMap<>(128);
        positionArray = new String[500];
        Cursor cursor;
        int count = 0;

        Uri uri = Uri.parse(SMS_URI_ALL);
        String[] projection = new String[] {"address", "body", "date", "type"};
        cursor = getContentResolver().query(uri, projection, null, null, "date desc");
        if (cursor != null) {
            dumpNumber();
            try {
                if(cursor.moveToFirst()) {
                    //int index_Address = cursor.getColumnIndex("address");
                    int index_Address = 0;
                    //int index_Person = cursor.getColumnIndex("person");
                    //int index_Body = cursor.getColumnIndex("body");
                    int index_Body = 1;
                    //int index_type = cursor.getColumnIndex("type");
                    int index_type = 3;
                    //int index_Date = cursor.getColumnIndex("date");
                    int index_Date = 2;
                    //Log.i("LBL", "" + index_Date);
                    do{
                        String address = handleNumber(cursor.getString(index_Address));
                        if (address == null) {
                            continue;
                        }
                        String name;
                        String smsBody = cursor.getString(index_Body);
                        int type = cursor.getInt(index_type);
                        long date = cursor.getLong(index_Date);
                        name = mNum2NameMap.get(address);
                        if(name == null){
                            name = address;
                        }
                        if(!mConversationsMap.containsKey(name)){
                            mConversation = new Conversation();
                            mConversation.name = name;
                            mConversation.latestSMS = smsBody;
                            mConversation.address = address;
                            mConversation.smsArrayList = new ArrayList<>();
                            mSMS = new SMS();
                            mSMS.smsBody = smsBody;
                            mSMS.type = type;
                            mSMS.time = date;
                            mConversation.smsArrayList.add(mSMS);
                            mConversationsMap.put(name, mConversation);
                            positionArray[count++] = name;
                        } else {
                            mConversation = mConversationsMap.get(name);
                            mSMS = new SMS();
                            mSMS.smsBody = smsBody;
                            mSMS.type = type;
                            mSMS.time = date;
                            mConversation.smsArrayList.add(mSMS);
                        }
                    }while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return mConversationsMap;
    }

    private String handleNumber(String num){
        String tmp;
        tmp = num.replace(" ", "");
        if(tmp.charAt(0) == '+') {
            if (tmp.charAt(1) != '8' || tmp.charAt(2) != '6') {
                return null;
            }
            return tmp.substring(3);
        }
        return tmp;
    }

    private void dumpNumber(){
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        String name;
        String number;
        mNum2NameMap = new HashMap<>(128);
        try {
            if(cursor != null && cursor.moveToFirst()){
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                do{
                    name = cursor.getString(nameIndex);
                    number = handleNumber(cursor.getString(numIndex));
                    if (number == null) {
                        continue;
                    }
                    mNum2NameMap.put(number, name);
                }while(cursor.moveToNext());
            }
        } finally {
            assert cursor != null;
            cursor.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final HashMap<String, Conversation> mConversationsMap = getSmsInPhone();

        ConversationListAdapter mListAdapter = new ConversationListAdapter(mConversationsMap, positionArray,
                this);
        ListView mConversationListView = (ListView)findViewById(R.id.conversations);
        mConversationListView.setAdapter(mListAdapter);
        mConversationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("LBL", i + " has been selected!");
                Conversation mConversation = mConversationsMap.get(positionArray[i]);
                Intent mIntent = new Intent(MainActivity.this, ConversationActivity.class);
                Bundle mData = new Bundle();
                mData.putSerializable("smslist", mConversation.smsArrayList);
                mIntent.putExtras(mData);
                startActivity(mIntent);
            }
        });

        final EditText etEditText1 = (EditText)findViewById(R.id.editText1);
        etEditText1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String text = etEditText1.getText().toString();
                if (text.equals(getString(R.string.input_hint)) && b) {
                    etEditText1.setText("");
                }
            }
        });

        Button mButton1 = (Button)findViewById(R.id.button1);
        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int estimate_time;
                final int send_map_size = ConversationListAdapter.mSendMap.size();


                if (!SendService.service_started) {
                    if (sendSMS() == 0) {
                        if (send_map_size > 10) {
                            mProgressDialog1 = new ProgressDialog(MainActivity.this);
                            mProgressDialog1.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            mProgressDialog1.setTitle(getString(R.string.sending));
                            if ((send_map_size % 10) == 0) {
                                estimate_time = (send_map_size / 10) * 5;
                            } else {
                                estimate_time = (send_map_size / 10) * 5 + 5;
                            }
                            mProgressDialog1.setMessage(String.format(getString(R.string.send_message),
                                    send_map_size,
                                    estimate_time));
                            mProgressDialog1.setIndeterminate(false);
                            mProgressDialog1.setCancelable(false);
                            mProgressDialog1.setMax(send_map_size);
                            mProgressDialog1.setButton(BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mProgressDialog1.getProgress() == send_map_size) {
                                        mProgressDialog1.cancel();
                                    }
                                }
                            });
                            mProgressDialog1.setButton(BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mProgressDialog1.getProgress() < send_map_size) {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle(getString(R.string.confirm_cancel_send_title))
                                                .setMessage(getString(R.string.confirm_cancel_send_message))
                                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        cancelSending();
                                                    }
                                                })
                                                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        mProgressDialog1.show();
                                                    }
                                                })
                                                .create()
                                                .show();
                                    } else {
                                        mProgressDialog1.cancel();
                                    }

                                }
                            });
                            mProgressDialog1.show();
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.sending))
                                    .setMessage(getString(R.string.successful_send))
                                    .setPositiveButton(getString(R.string.ok), null)
                                    .create()
                                    .show();
                        }
                    }
                } else {
                    mProgressDialog1.show();
                }
            }
        });
    }

    private void cancelSending() {
        if (SendService.service_started) {
            Intent mSendIntent = new Intent(MainActivity.this, SendService.class);
            PendingIntent mPendingIntent = PendingIntent.getService(MainActivity.this, 0,
                    mSendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mAlarmManager.cancel(mPendingIntent);
            stopService(mSendIntent);
        }

        if (DeliveryService.service_started) {
            Intent mDeliveryIntent = new Intent(MainActivity.this, DeliveryService.class);
            stopService(mDeliveryIntent);
        }
    }

    private int sendSMS() {
        if (ConversationListAdapter.mSendMap.size() == 0) {
            Toast.makeText(MainActivity.this,
                    getString(R.string.need_selection),
                    Toast.LENGTH_SHORT).show();
            return -1;
        }

        EditText etEditText1 = (EditText)findViewById(R.id.editText1);
        String strSMS = etEditText1.getText().toString();

        if(strSMS.equals("")){
            Toast.makeText(MainActivity.this,
                    getString(R.string.need_input),
                    Toast.LENGTH_SHORT).show();
            return -2;
        }

        cancelSending();

        mAlarmManager = (AlarmManager) MainActivity.this
                .getSystemService(MainActivity.ALARM_SERVICE);

        //包装需要执行Service的Intent
        Intent mSendIntent = new Intent(MainActivity.this, SendService.class);
        ArrayList<String> send_list = new ArrayList<>(64);
        for(String strAddress : ConversationListAdapter.mSendMap.keySet()){
            send_list.add(strAddress);
        }

        mSendIntent.putExtra("send_list", send_list);
        mSendIntent.putExtra("sms", strSMS);
        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0,
                mSendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //触发服务的起始时间
        long triggerAtTime = SystemClock.elapsedRealtime();

        //使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
                10 * 1000, pendingIntent);

        Intent mDeliveryIntent = new Intent(MainActivity.this, DeliveryService.class);
        mDeliveryIntent.putExtra("send_list_size", ConversationListAdapter.mSendMap.size());
        startService(mDeliveryIntent);

        etEditText1.setText("");
        return 0;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i("LBL", "-------onResume------");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.i("LBL", "-------onPause------");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Log.i("LBL", "-------onStop------");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ConversationListAdapter.mSendMap.clear();
        Log.i("LBL", "-------onDestroy------");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            Intent mIntent = new Intent(MainActivity.this, ActivityAbout.class);
            startActivity(mIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
