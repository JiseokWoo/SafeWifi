package com.safewifi;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String get_url = "http://172.30.1.41/wifiscan.php";
    private static final String put_url = "http://172.30.1.41/wificonn.php";

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

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        /*

        // wifi manager 생성
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // 와이파이 비활성일 경우 활성화
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

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

        */


        // thread로 db 서버와 통신
        new Thread() {
            public void run() {
                getAPInfo();
            }
        }.start();


        // 데이터 업데이트
        adapter.notifyDataSetChanged();
    }

    private String getAPInfo() {
        String response = "";

        try {
            // wifiscan connection 생성
            URL url = new URL(get_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // connection post 설정
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // post로 전송할 데이터 생성
            StringBuffer sb = new StringBuffer();
            sb.append("mac").append("=").append("aa:bb:cc:dd:ee:ff:gg").append("&");
            sb.append("ssid").append("=").append("testAP");

            // post로 데이터 전송
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
            pw.write(sb.toString());
            pw.flush();

            // 서버로부터 response 수신
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder buff = new StringBuilder();
                String line = bf.readLine();
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private String putAPInfo() {
        String response = "";

        try {
            // wifiscan connection 생성
            URL url = new URL(put_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // connection post 설정
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // post로 전송할 데이터 생성
            StringBuffer sb = new StringBuffer();
            sb.append("mac").append("=").append("aa:bb:cc:dd:ee:ff:gg").append("&");
            sb.append("ssid").append("=").append("testAP").append("&");
            sb.append("pubIP").append("=").append("testAP").append("&");
            sb.append("dnsIP1").append("=").append("testAP").append("&");
            sb.append("dnsIP2").append("=").append("testAP");

            // post로 데이터 전송
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
            pw.write(sb.toString());
            pw.flush();

            // 서버로부터 response 수신
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder buff = new StringBuilder();
                String line = bf.readLine();
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}