package com.safewifi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private ListView lv_wifiList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 리스트뷰를 위한 adapter 생성
        adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);

        // layout의 리스트뷰를 불러와 adapter 매핑
        lv_wifiList = (ListView) findViewById(R.id.lv_wifiList);
        lv_wifiList.setAdapter(adapter);

        // adapter에 item 추가
        adapter.add("1");
        adapter.add("2");
        adapter.add("3");
        adapter.add("4");
        adapter.add("5");
        adapter.add("6");

        // 데이터 업데이트
        adapter.notifyDataSetChanged();
    }
}
