package com.safewifi;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.safewifi.common.APInfo;
import com.safewifi.common.ARPTable;
import com.safewifi.common.Command;
import com.safewifi.common.ConnectWifi;

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
    private ImageButton imageButton;
    private APInfo curAP;

    private ProgressDialog pbScan;
    private ProgressDialog pbCheck;
    private ProgressDialog pbConnect;

    //private WifiReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // 어플 아이콘, 타이틀, 새로고침 버튼
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        View customActionView = LayoutInflater.from(this).inflate(R.layout.action_bar, null);
        actionBar.setCustomView(customActionView);

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

        /*wifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiReceiver, intentFilter);*/

        // ;새로고침 버튼 클릭, 스캔 다시 시작
        imageButton = (ImageButton) findViewById(R.id.scan_refresh);
        imageButton.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View v){
                new ScanAP().execute();
            }
        });

        // 현재 AP 연결중일 경우
        if (wifiInfo.getBSSID() != null) {
           new CheckAP().execute();    // 현재 AP 정보 수집후 서버에 전송
        } else {
            new ScanAP().execute();
        }

    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            curAP = apInfoList.get(position);

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.info, null);

            TextView ssid = (TextView) layout.findViewById(R.id.tv_ssid);
            TextView mac = (TextView) layout.findViewById(R.id.tv_mac);
            TextView security = (TextView) layout.findViewById(R.id.tv_security);
            TextView info = (TextView) layout.findViewById(R.id.tv_info);
            ssid.setText(curAP.getSSID());
            mac.setText(curAP.getMAC());

            if (curAP.getSecureLevel() != null && curAP.getConnCount() > 0) {
                if (curAP.getSecureLevel().equals(Command.SECURE_LEVEL_HIGH)) {
                    security.setText("보안도가 높습니다. (" + curAP.getSecureScore() + "점)");
                } else if (curAP.getSecureLevel().equals(Command.SECURE_LEVEL_MEDIUM)) {
                    security.setText("보안도가 보통입니다. (" + curAP.getSecureScore() + "점)");
                } else if (curAP.getSecureLevel().equals(Command.SECURE_LEVEL_LOW)) {
                    security.setText("보안도가 낮습니다. (" + curAP.getSecureScore() + "점)");
                }

                String secure_info = "";

                if (curAP.getInfoEncrypt().contains(Command.ENCRYPT_OPEN)) {
                    secure_info += "공유기 암호화 설정 안됨\n";
                } else if (curAP.getInfoEncrypt().contains(Command.ENCRYPT_WEP) || (curAP.getInfoEncrypt().contains(Command.ENCRYPT_WPA) && !curAP.getInfoEncrypt().contains(Command.ENCRYPT_WPA2))) {
                    secure_info += "공유기 암호화 설정 취약\n";
                }

                if (curAP.getInfoDns() > 0) secure_info += "DNS 변조 의심 : (" + curAP.getInfoDns() + "건 탐지)\n";
                if (curAP.getInfoArp()) secure_info += "ARP 테이블 변조 의심\n";
                if (curAP.getInfoPort()) secure_info += "비정상 포트 오픈";

                info.setText(secure_info);
            } else {
                security.setText("보안도를 알 수 없습니다.");
            }

            AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
            adBuilder.setTitle(curAP.getSSID() + "의 정보");
            adBuilder.setView(layout);

            adBuilder.setPositiveButton("연결", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (scanResultList != null) {
                        final ScanResult ap = scanResultList.get(curAP.getPosition());

                        if (ap.capabilities.contains(Command.ENCRYPT_OPEN)) {
                            if (ConnectWifi.connect(wifiManager, ap, null)) {
                                // TODO: AP 연결후 처리?
                            } else {
                                errorDialog("연결 실패", ap.SSID + "에 연결하지 못했습니다.", "확인");
                            }
                        } else {

                            WifiConfiguration config = ConnectWifi.findStoredConfig(wifiManager, ap);

                            if (config == null) {
                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                View layout = inflater.inflate(R.layout.connect, null);
                                final EditText et_password = (EditText) layout.findViewById(R.id.et_password);
                                et_password.setPrivateImeOptions("defaultInputmode=english;");

                                AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
                                adBuilder.setTitle(curAP.getSSID() + "에 연결");
                                adBuilder.setView(layout);

                                adBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String password = et_password.getText().toString();

                                        if (ConnectWifi.connect(wifiManager, ap, password)) {
                                            // TODO: AP 연결후 처리?
                                        } else {
                                            errorDialog("연결 실패", ap.SSID + "에 연결하지 못했습니다.", "확인");
                                        }

                                    }
                                });

                                adBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                AlertDialog alertDialog = adBuilder.create();
                                alertDialog.show();
                            } else {
                                ConnectWifi.connect(wifiManager, config);
                            }
                        }
                    } else {
                        errorDialog("AP 정보 확인 에러", "AP 정보를 확인할 수 없습니다.\n새로고침 후 다시 시도해주세요.", "확인");
                    }
                }
            });


            adBuilder.setNegativeButton("취소", null);

            AlertDialog alertDialog = adBuilder.create();
            alertDialog.show();

        }
    };

    private void errorDialog(String title, String msg, String button) {
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
        adBuilder.setTitle(title);
        adBuilder.setMessage(msg);
        adBuilder.setNeutralButton(button, null);
        AlertDialog alertDialog = adBuilder.create();
        alertDialog.show();
    }

    /*public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                if (error == WifiManager.ERROR_AUTHENTICATING) {
                    Toast toast = Toast.makeText(com.safewifi.MainActivity.this, "비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT);
                    toast.show();
                }

                int info = intent.getIntExtra(WifiManager.EXTRA_NEW_STATE, -1);



            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int info = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

                if (info == WifiManager.WIFI_STATE_ENABLING) {
                    pbScan = ProgressDialog.show(MainActivity.this, "", "연결중입니다.");
                }
                if (info == WifiManager.WIFI_STATE_ENABLED) {
                    if (pbScan != null && pbScan.isShowing()) {
                        pbScan.dismiss();
                    }
                }
            }
        }
    }*/

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
            pbScan = ProgressDialog.show(MainActivity.this, "", "스캔중입니다. 잠시만 기다려주세요.");

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
                        if (ap.SSID.equals("")) continue;
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
            pbScan.dismiss();
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
            pbCheck = ProgressDialog.show(MainActivity.this, "", "정보 업로드중입니다. 잠시만 기다려주세요.");
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
                apInfo.setInfoArp(ARPTable.checkARPSpoof());

                // TODO: 테스트용
                apInfo.setInfoPort(true);

                // 서버에 현재 AP 정보 업로드
                putAPInfo(apInfo);

                return Command.SUCCESS;
            }
            return Command.FAIL;
        }

        @Override
        protected void onPostExecute(String result) {
            pbCheck.dismiss();
            new ScanAP().execute();
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
                    apInfo.setInfoEncrypt(encrypt);
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