package com.safewifi;


import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
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
public class MainActivity extends Activity {

    private static final String get_url = "http://172.20.10.8/wifiscan.php";
    private static final String put_url = "http://172.20.10.8/wificonn.php";

    private APInfoAdapter apInfoAdapter;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private List<ScanResult> scanResultList;
    private List<APInfo> apInfoList;
    private ListView listView;
    private APInfo curAP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // APInfo 객체가 저장될 리스트
        apInfoList = new ArrayList<>();

        // 리스트뷰를 위한 adapter 생성
        apInfoAdapter = new APInfoAdapter(getApplicationContext(), R.layout.row, apInfoList);

        listView = (ListView) findViewById(R.id.lv_aplist);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setAdapter(apInfoAdapter);

        // wifi manager 생성
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();

        // 현재 AP 연결중일 경우
        if (wifiInfo.getBSSID() != null) {
            new CheckAP().execute();    // 현재 AP 정보 수집후 서버에 전송
        }

        new ScanAP().execute();         // 주변 AP 스캔후 서버로 정보 조회

    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            curAP = apInfoList.get(position);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = inflater.inflate(R.layout.info, null);

            TextView ssid = (TextView) popupView.findViewById(R.id.tv_ssid);
            TextView mac = (TextView) popupView.findViewById(R.id.tv_mac);
            TextView security = (TextView) popupView.findViewById(R.id.tv_security);
            TextView info = (TextView) popupView.findViewById(R.id.tv_info);
            ssid.setText(curAP.getSSID());
            mac.setText(curAP.getMAC());

            if (curAP.getSecureLevel() != null) {
                if (curAP.getSecureLevel().equals(Command.SECURE_LEVEL_HIGH)) {
                    security.setText("보안도가 높습니다. (" + curAP.getSecureScore() + "점)");
                } else if (curAP.getSecureLevel().equals(Command.SECURE_LEVEL_MEDIUM)) {
                    security.setText("보안도가 보통입니다. (" + curAP.getSecureScore() + "점)");
                } else if (curAP.getSecureLevel().equals(Command.SECURE_LEVEL_LOW)) {
                    security.setText("보안도가 낮습니다. (" + curAP.getSecureScore() + "점)");
                }

                String secure_info = "";

                if (curAP.getInfoEncrypt().equals("OPEN")) {
                    secure_info += "공유기 암호화 설정 안됨\n";
                } else if (curAP.getInfoEncrypt().equals("WEP") || curAP.getInfoEncrypt().equals("WPA")) {
                    secure_info += "공유기 암호화 설정 취약\n";
                }

                if (curAP.getInfoDns() > 0) {
                    secure_info += "DNS 변조 의심 : (" + curAP.getInfoDns() + ")건 탐지\n";
                }

                if (curAP.getInfoArp()) {
                    secure_info += "ARP 테이블 변조 의심\n";
                }

                if (curAP.getInfoPort()) {
                    secure_info += "비정상 포트 오픈";
                }

                info.setText(secure_info);
            } else {
                security.setText("보안도를 알 수 없습니다.");
            }

            final PopupWindow popup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popup.showAtLocation(view, Gravity.CENTER, 0, 0);
            popup.setTouchable(true);
            popup.setFocusable(true);
            popup.setOutsideTouchable(true);
            popup.showAsDropDown(popupView);

            Button btn_close = (Button) popupView.findViewById(R.id.btn_close);
            Button btn_conncet = (Button) popupView.findViewById(R.id.btn_connect);
            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup.dismiss();
                }
            });
            //btn_conncet.setOnClickListener(connectListener);
        }
    };

    /*private View.OnClickListener connectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ScanResult ap = scanResultList.get(curAP.getPosition());

            if (ap.capabilities.contains("OPEN")) {
                WifiConfiguration wifiConfiguration = new WifiConfiguration();

                wifiConfiguration.SSID = ap.SSID;
                wifiConfiguration.BSSID = ap.BSSID;
                wifiConfiguration.priority = 1;
                wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiManager.enableNetwork(wifiManager.addNetwork(wifiConfiguration), true);
                wifiManager.saveConfiguration();

            } else {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View popupView = inflater.inflate(R.layout.connect, null);

                final PopupWindow popup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popup.showAtLocation(v, Gravity.CENTER, 0, 0);
                popup.setTouchable(true);
                popup.setFocusable(true);
                popup.setOutsideTouchable(true);
                popup.showAsDropDown(popupView);

                Button btn_confirm = (Button) popupView.findViewById(R.id.btn_confirm);
                Button btn_cancel = (Button) popupView.findViewById(R.id.btn_cancel);
                btn_confirm.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        WifiConfiguration wifiConfiguration = new WifiConfiguration();
                        TextView tv_password = (TextView) v.findViewById(R.id.tv_password);
                        wifiConfiguration.SSID = ap.SSID;
                        wifiConfiguration.BSSID = ap.BSSID;
                        wifiConfiguration.priority = 1;
                        wifiConfiguration.preSharedKey = tv_password.getText().toString();
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        wifiManager.enableNetwork(wifiManager.addNetwork(wifiConfiguration), true);
                        wifiManager.saveConfiguration();
                    }
                });
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                    }
                });
            }


        }
    };*/

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
                TextView tv_mac = (TextView) view.findViewById(R.id.tv_mac);
                TextView tv_info = (TextView) view.findViewById(R.id.tv_info);

                if (tv_ssid != null && apInfo.getSSID() != null) tv_ssid.setText(apInfo.getSSID());
                // TODO: 신호 강도 정보 UI 표시
                if (tv_signal != null && apInfo.getSignalLevel() != null) {
                    // -90 ~ -20
                    tv_signal.setText(apInfo.getSignalLevel().toString());
                }
                // TODO: 보안도 정보 UI 표시
                if (tv_security != null && apInfo.getSecureLevel() != null) tv_security.setText(apInfo.getSecureLevel());
                if (tv_mac != null && apInfo.getMAC() != null) tv_mac.setText(apInfo.getMAC());
                //if (tv_info != null && apInfo.getInfo() != null) tv_info.setText(apInfo.getInfo());
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

            // 와이파이 비활성일 경우 활성화
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO : 단말의 Wifi가 꺼져있을 경우 Wifi 검색이 되지 않음.

            // 와이파이 리스트 스캔
            if (wifiManager.isWifiEnabled() && wifiManager.startScan()) {
                scanResultList = wifiManager.getScanResults();

                // 스캔 결과 adapter에 추가
                if (scanResultList != null && !scanResultList.isEmpty()) {
                    for (ScanResult ap : scanResultList) {
                        APInfo apInfo = null;
                        try {
                            apInfo = getAPInfo(ap.BSSID, ap.SSID, ap.level, ap.capabilities);
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
        protected void onPreExecute() {
            wifiManager.startScan();
            scanResultList = wifiManager.getScanResults();
        }

        @Override
        protected String doInBackground(String... params) {
            if (wifiManager != null && wifiInfo != null) {
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                String encrypt = null;

                for (ScanResult ap : scanResultList) {
                    if (ap.BSSID.equals(wifiInfo.getBSSID())) {
                        encrypt = ap.capabilities;
                        break;
                    }
                }

                APInfo apInfo = new APInfo(wifiInfo.getBSSID(), wifiInfo.getSSID(), wifiInfo.getRssi(), encrypt, apInfoList.size());
                apInfo.setDHCP(dhcpInfo);

                // TODO: 테스트용
                apInfo.setInfoArp(true);
                apInfo.setInfoPort(true);

                // 서버에 현재 AP 정보 업로드
                putAPInfo(apInfo);

                return Command.SUCCESS;
            }
            return Command.FAIL;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            scanResultList = null;
        }
    }

    /**
     * 서버로부터 AP 정보 조회
     * @param mac
     * @param ssid
     * @return
     * @throws JSONException
     */
    private APInfo getAPInfo(String mac, String ssid, Integer signal, String encrypt) throws JSONException {
        APInfo apInfo = new APInfo(mac, ssid, signal, encrypt, apInfoList.size());

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

    private boolean WifiConnect(String mac, String ssid) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = ssid;
        wifiConfiguration.BSSID = mac;
        wifiConfiguration.priority = 1;


        return true;
    }
}