package com.safewifi;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiseokWoo
 * MainActivity
 */
public class MainActivity extends AppCompatActivity {

    private static final String get_url = "http://172.20.10.8/wifiscan.php";
    private static final String put_url = "http://172.20.10.8/wificonn.php";

    private ListView lv_wifiList;
    private ArrayAdapter<String> adapter;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private List<ScanResult> scanResultList;
    private List<APInfo> apInfoList;

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
        apInfoList = new ArrayList<>();

        wifiInfo = wifiManager.getConnectionInfo();

        /*// 현재 AP에 접속중일 경우 데이터 업로드
        if (wifiInfo.getBSSID() != null) {
            new Thread() {
                public void run() {
                    checkAP();
                }
            }.start();
        }*/

        new ScanAP().execute();

    }

    private class ScanAP extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO : 단말의 Wifi가 꺼져있을 경우 Wifi 검색이 되지 않음.

            // 와이파이 비활성일 경우 활성화
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            apInfoList.clear();
            adapter.clear();

            // 와이파이 리스트 스캔
            if (wifiManager.isWifiEnabled() && wifiManager.startScan()) {
                scanResultList = wifiManager.getScanResults();

                // 스캔 결과 adapter에 추가
                if (scanResultList != null && !scanResultList.isEmpty()) {
                    for (ScanResult ap : scanResultList) {
                        APInfo apInfo = null;
                        try {
                            apInfo = getAPInfo(ap.BSSID, ap.SSID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        apInfoList.add(apInfo);
                        adapter.add(apInfo.getSSID());
                    }
                } else {
                    return Command.WIFI_UNAVAILABLE_ERROR;
                }
            } else {
                // TODO:  ErrorCode 작성
                return Command.WIFI_ENABLE_ERROR;
            }

            return Command.SUCCESS;
        }

        @Override
        protected void onPostExecute(String result) {
            adapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
    }

    /**
     * 주변 AP 정보 스캔 후 apInfoList에 저장
     * @throws JSONException
     */
    /*private String scanAP() throws JSONException {
        // TODO : 단말의 Wifi가 꺼져있을 경우 Wifi 검색이 되지 않음.

        // 와이파이 비활성일 경우 활성화
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        apInfoList.clear();
        adapter.clear();

        // 와이파이 리스트 스캔
        if (wifiManager.isWifiEnabled() && wifiManager.startScan()) {
            scanResultList = wifiManager.getScanResults();

            // 스캔 결과 adapter에 추가
            if (scanResultList != null && !scanResultList.isEmpty()) {
                for (ScanResult ap : scanResultList) {
                    APInfo apInfo = getAPInfo(ap.BSSID, ap.SSID);
                    apInfoList.add(apInfo);
                    adapter.add(apInfo.getSSID());
                }
            } else {
                return Command.WIFI_UNAVAILABLE_ERROR;
            }
        } else {
            // TODO:  ErrorCode 작성
            return Command.WIFI_ENABLE_ERROR;
        }

        return Command.SUCCESS;
    }*/

    /**
     * 현재 AP 정보 체크해 서버에 업로드
     */
    private void checkAP() {
        if (wifiManager != null && wifiInfo != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

            APInfo apInfo = new APInfo(wifiInfo.getBSSID(), wifiInfo.getSSID());
            apInfo.setDHCP(dhcpInfo);

            // 서버에 현재 AP 정보 업로드
            putAPInfo(apInfo);
        }
    }

    /**
     * 서버로부터 AP 정보 조회
     * @param mac
     * @param ssid
     * @return
     * @throws JSONException
     */
    private APInfo getAPInfo(String mac, String ssid) throws JSONException {
        APInfo apInfo = new APInfo(mac, ssid);

        try {
            // wifiscan connection 생성
            URL url = new URL(get_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // connection post 설정
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // post로 데이터 전송
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"), true);
            pw.write(apInfo.toString(Command.GET));
            pw.flush();

            // 서버로부터 response 수신
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String response = bf.readLine();

                if (response != null) {
                    apInfo = new APInfo(response);
                }
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return apInfo;
    }

    /**
     * 서버로 AP 정보 업로드
     * @param apInfo
     */
    private void putAPInfo(APInfo apInfo) {

        try {
            // wifiscan connection 생성
            URL url = new URL(put_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // connection post 설정
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // post로 데이터 전송
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
            pw.write(apInfo.toString(Command.PUT));
            pw.flush();

            /*// 서버로부터 response 수신
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = bf.readLine();
                if (response == null) {

                }
            }*/

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}