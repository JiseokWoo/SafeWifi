package com.safewifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lv_wifiList;
    private ArrayAdapter<String> adapter;
    private WifiManager wifiManager;
    private List<ScanResult> scanResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 리스트뷰를 위한 adapter 생성
        adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);

        // 레이아웃의 리스트뷰를 불러와 adapter 매핑
        lv_wifiList = (ListView) findViewById(R.id.lv_wifiList);
        lv_wifiList.setAdapter(adapter);

        // wifi manager 생성
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // 와이파이 비활성일 경우 활성화
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        while (!wifiManager.isWifiEnabled()) {}

        // 와이파이 리스트 스캔
        // TODO : 단말의 Wifi가 꺼져있을 경우 Wifi 검색이 되지 않음.
        if (wifiManager.isWifiEnabled() && wifiManager.startScan()) {
            scanResultList = wifiManager.getScanResults();

            // 스캔 결과 adapter에 추가
            if (scanResultList != null && !scanResultList.isEmpty()) {
                for (ScanResult wifi : scanResultList) {
                    adapter.add(wifi.SSID);
                }
            } else {
                adapter.add("사용 가능한 Wifi가 없습니다.");
            }
        } else {
            adapter.add("Wifi가 정상적으로 작동하지 않습니다.");
        }

        // 데이터 업데이트
        adapter.notifyDataSetChanged();
    }
}