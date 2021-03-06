package com.zhuang.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener{

    private Button mButton;
    private ProgressBar mWaiting;
    private ListView mCountries;
    private SimpleAdapter mCountryAdapter;
    private ArrayList<HashMap<String,Object>> myData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mButton = findViewById(R.id.button);
        mWaiting = findViewById(R.id.progressBar);
        mCountries = findViewById(R.id.listView);

        myData = new ArrayList<>();

        mCountryAdapter = new SimpleAdapter(this,myData,
                android.R.layout.simple_list_item_2,new String[]{"name","key"},
                new int[]{android.R.id.text1,android.R.id.text2});
        mCountries.setAdapter(mCountryAdapter);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString("arg1","hahahaha");

//                bundle.putSerializable("list",myData);
//                Intent intent = new Intent();
//                intent.putExtras(bundle);

                MyDialogFragment myDialogFragment = new MyDialogFragment();
                myDialogFragment.setArguments(bundle);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                myDialogFragment.show(ft,"dialog");

            }
        });
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                //Toast.makeText(getApplicationContext(),"ok",Toast.LENGTH_SHORT).show();
                //Log.d("raymond","ok ");
                getDataSync();
                mWaiting.setVisibility(View.VISIBLE);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                //Toast.makeText(getApplicationContext(),"no",Toast.LENGTH_SHORT).show();
                //Log.d("raymond","no. ");
                break;
        }

    }

    public void getDataSync() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://country.io/names.json")
                            .build();
                    Response response = client.newCall(request).execute();

                    Gson gson = new Gson();
                    LinkedTreeMap<String,String> maps = gson.fromJson(response.body().string(),
                            new TypeToken<Map<String,String>>(){}.getType());

                    Iterator it = maps.keySet().iterator();

                    myData.clear();
                    while(it.hasNext()) {
                        String key = it.next().toString();
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("name",maps.get(key)) ;
                        map.put("key",key) ;
                        myData.add(map);
                    }

                    //Log.d("raymond","return:"+myData.toString());

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mWaiting.setVisibility(View.GONE);

                            if(myData.size()>0)
                                mCountryAdapter.notifyDataSetChanged();
                        }
                    });

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.bt_sendMessage:
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() { //　新建一个线程，并新建一个Message的对象，是用Handler的对象发送这个Message
//                        Message msg = new Message();
//                        msg.what = UPDATE_TEXT; // 用户自定义的一个值，用于标识不同类型的消息
//                        hd.sendMessage(msg); // 发送消息
//                    }
//                }).start();
//                break;
//
//            default:
//                break;
//        }
//    }
//
//    // 定义一个内部类继承自Handler，并且覆盖handleMessage方法用于处理子线程传过来的消息
//    class MyHandler extends Handler{
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case UPDATE_TEXT: // 接受到消息之后，对UI控件进行修改
//                    tv.setText("修改成功！");
//                    break;
//
//                default:
//                    break;
//            }
//        }
//    }
}
