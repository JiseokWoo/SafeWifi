package com.safewifi;


import android.app.ListActivity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.safewifi.common.APInfo;
import com.safewifi.common.Command;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by JiseokWoo
 * MainActivity
 */
public class MainActivity extends ListActivity {

    private static final String get_url = "http://172.20.10.8/wifiscan.php";
    private static final String put_url = "http://172.20.10.8/wificonn.php";

    private APInfoAdapter apInfoAdapter;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private List<ScanResult> scanResultList;
    private List<APInfo> apInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();

        /*try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/tcp"));
            String line = "";
            //br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parse = line.split("\\s+");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("IP", parse[0]);
                jsonObject.put("MAC", parse[3]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        // APInfo 객체가 저장될 리스트
        apInfoList = new ArrayList<>();

        // 리스트뷰를 위한 adapter 생성
        apInfoAdapter = new APInfoAdapter(getApplicationContext(), R.layout.row, apInfoList);
        setListAdapter(apInfoAdapter);

        // wifi manager 생성
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();

        // 현재 AP 연결중일 경우
        if (wifiInfo.getBSSID() != null) {
            new CheckAP().execute();    // 현재 AP 정보 수집후 서버에 전송
        }

        new ScanAP().execute();         // 주변 AP 스캔후 서버로 정보 조회


    }

    /**
     * APInfo 클래스와 리스트뷰를 연결해주는 Adapter
     */
    private class APInfoAdapter extends ArrayAdapter<APInfo> {
        private List<APInfo> apInfoList;

        public APInfoAdapter(Context context, int textViewResourceId, List<APInfo> apInfoList) {
            super(context, textViewResourceId, apInfoList);
            this.apInfoList = apInfoList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.row, null);
            }

            APInfo apInfo = apInfoList.get(position);

            if (apInfo != null) {
                TextView tv_ssid = (TextView) view.findViewById(R.id.tv_ssid);
                TextView tv_signal = (TextView) view.findViewById(R.id.tv_signal);
                TextView tv_security = (TextView) view.findViewById(R.id.tv_security);

                if (tv_ssid != null && apInfo.getSSID() != null) {
                    tv_ssid.setText(apInfo.getSSID());
                }
                if (tv_signal != null && apInfo.getSignalLevel() != null) {
                    // TODO: 신호 강도 정보 표시
                    tv_signal.setText(apInfo.getSignalLevel().toString());
                }
                if (tv_security != null && apInfo.getSecureLevel() != null) {
                    // TODO: 보안도 정보 표시
                    tv_security.setText(apInfo.getSecureLevel());
                }
            }

            return view;
        }
    }

    /**
     * APInfo의 SignalLevel을 기준으로 정렬하기 위한 Comparator
     */
    static class LevelAscCompare implements Comparator<APInfo> {

        @Override
        public int compare(APInfo ap1, APInfo ap2) {
            return ap2.getSignalLevel().compareTo(ap1.getSignalLevel());
        }
    }

    /**
     * 주변 AP를 검색해 MAC 기반으로 서버에 정보 조회
     */
    private class ScanAP extends AsyncTask<String, Integer, String> {

        @Override
        protected  void onPreExecute() {
            apInfoAdapter.clear();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO : 단말의 Wifi가 꺼져있을 경우 Wifi 검색이 되지 않음.

            // 와이파이 비활성일 경우 활성화
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            // 와이파이 리스트 스캔
            if (wifiManager.isWifiEnabled() && wifiManager.startScan()) {
                scanResultList = wifiManager.getScanResults();

                // 스캔 결과 adapter에 추가
                if (scanResultList != null && !scanResultList.isEmpty()) {
                    for (ScanResult ap : scanResultList) {
                        APInfo apInfo = null;
                        try {
                            apInfo = getAPInfo(ap.BSSID, ap.SSID, ap.level);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (apInfo != null){
                            apInfoList.add(apInfo);
                        } else {
                            // TODO: 에러 처리
                        }
                    }
                } else {
                    return Command.WIFI_UNAVAILABLE_ERROR;
                }
            } else {
                return Command.WIFI_ENABLE_ERROR;
            }
            return Command.SUCCESS;
        }

        @Override
        protected void onPostExecute(String result) {
            Collections.sort(apInfoList, new LevelAscCompare());
            apInfoAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
    }

    /**
     * 접속중인 AP 정보를 체크해 서버로 전송
     */
    private class CheckAP extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            if (wifiManager != null && wifiInfo != null) {
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

                APInfo apInfo = new APInfo(wifiInfo.getBSSID(), wifiInfo.getSSID(), wifiInfo.getRssi());
                apInfo.setDHCP(dhcpInfo);

                // 서버에 현재 AP 정보 업로드
                putAPInfo(apInfo);

                return Command.SUCCESS;
            }
            return Command.FAIL;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

    }
    }

    /**
     * 서버로부터 AP 정보 조회
     * @param mac
     * @param ssid
     * @return
     * @throws JSONException
     */
    private APInfo getAPInfo(String mac, String ssid, Integer signal) throws JSONException {
        APInfo apInfo = new APInfo(mac, ssid, signal);

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

                // TODO: 에러 처리 로직 개선 필요
                if (response.equals(Command.NO_MAC_ERROR)) {
                    return null;
                } else if (response.equals(Command.DB_INSERT_ERROR) || response.equals(Command.DB_SELECT_ERROR)) {
                    return null;
                } else if (response.equals(Command.EMPTY)) {
                    return null;
                } else {
                    apInfo.setDBInfo(response);
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
    private String putAPInfo(APInfo apInfo) {
        String response = Command.EMPTY;

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
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"), true);
            pw.write(apInfo.toString(Command.PUT));
            pw.flush();

            // 서버로부터 response 수신
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                response = bf.readLine();
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