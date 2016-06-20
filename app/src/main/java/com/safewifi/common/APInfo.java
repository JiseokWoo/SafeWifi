package com.safewifi.common;

import android.net.DhcpInfo;
import android.text.format.Formatter;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by JiseokWoo
 * AP 정보를 관리하기 위한 클래스
 */
public class APInfo {
    private String mac;
    private final static String keyMAC = "mac";
    private String ssid;
    private final static String keySSID = "ssid";
    private String pubIP;
    private final static String keyPubIP = "pubIP";
    private String dnsIP1;
    private final static String keyDnsIP1 = "dnsIP1";
    private String dnsIP2;
    private final static String keyDnsIP2 = "dnsIP2";
    private String encrypt;
    private final static String keyEncrypt = "encrypt";
    private Integer signalLevel;
    private final static String keySignalLevel = "signalLevel";
    private String secure_level;
    private final static String keySecureLevel = "secure_level";
    private String info;
    private final static String keyInfo = "info";
    private int position;

    /**
     * 기본 생성자
     */
    public APInfo() {
        setSSID("-");
        setMAC("-");
        setPubIP("-");
        setDnsIP1("-");
        setDnsIP2("-");
        setPosition(0);
    }

    /**
     * MAC 주소와 SSID 값만 설정하는 생성자
     * @param mac
     * @param ssid
     */
    public APInfo(String mac, String ssid, Integer signalLevel, String encrypt, int position) {
        setMAC(mac);
        setSSID(ssid);
        setPubIP("-");
        setDnsIP1("-");
        setDnsIP2("-");
        setSignalLevel(signalLevel);
        setEncrypt(encrypt);
        setPosition(position);
    }

    /**
     * DB에서 가져온 JSON 스트링을 객체에 세팅
     * @param json
     * @throws JSONException
     */
    public void setDBInfo(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        if (jsonObject.getString(keyMAC) != null || !jsonObject.getString(keyInfo).equals("null")) setMAC(jsonObject.getString(keyMAC));
        if (jsonObject.getString(keySSID) != null || !jsonObject.getString(keyInfo).equals("null")) setSSID(jsonObject.getString(keySSID));
        if (jsonObject.getString(keySecureLevel) != null || !jsonObject.getString(keySecureLevel).equals("null")) setSecureLevel(jsonObject.getString(keySecureLevel));
        if (jsonObject.getString(keyInfo) != null || !jsonObject.getString(keyInfo).equals("null")) setInfo(jsonObject.getString(keyInfo));

        setPubIP("-");
        setDnsIP1("-");
        setDnsIP2("-");
    }

    /**
     * DHCP Info에서 pubIP, DNS1, DNS2 읽어와 객체에 저장
     * @param dhcpInfo
     */
    public void setDHCP(DhcpInfo dhcpInfo) {

        if (dhcpInfo.ipAddress != 0) {
            setPubIP(Formatter.formatIpAddress(dhcpInfo.ipAddress));
        } else {
            setPubIP("-");
        }
        if (dhcpInfo.dns1 != 0) {
            setDnsIP1(Formatter.formatIpAddress(dhcpInfo.dns1));
        } else {
            setDnsIP1("-");
        }
        if (dhcpInfo.dns2 != 0) {
            setDnsIP2(Formatter.formatIpAddress(dhcpInfo.dns2));
        } else {
            setDnsIP2("-");
        }
    }

    public String getSSID() {
        return ssid;
    }

    public void setSSID(String SSID) {
        this.ssid = SSID;
    }

    public String getMAC() {
        return mac;
    }

    public void setMAC(String mac) {
        this.mac = mac;
    }

    public String getPubIP() {
        return pubIP;
    }

    public void setPubIP(String pubIP) {
        this.pubIP = pubIP;
    }

    public String getDnsIP1() {
        return dnsIP1;
    }

    public void setDnsIP1(String dnsIP1) {
        this.dnsIP1 = dnsIP1;
    }

    public String getDnsIP2() {
        return dnsIP2;
    }

    public void setDnsIP2(String dnsIP2) {
        this.dnsIP2 = dnsIP2;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        if (encrypt != null) {
            if (encrypt.contains("OPEN")) {
                this.encrypt = "OPEN";
            } else if (encrypt.contains("WEP")) {
                this.encrypt = "WEP";
            } else if (encrypt.contains("WPA")) {
                if (encrypt.contains("2")){
                    this.encrypt = "WPA2";
                } else {
                    this.encrypt = "WPA";
                }
            } else if (encrypt.contains("ESS")) {
                this.encrypt = "WPA2";
            } else {
                this.encrypt = "UNKNOWN";
            }
        }

    }

    public Integer getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public String getSecureLevel() {
        return secure_level;
    }

    public void setSecureLevel(String secure_level) {
        this.secure_level = secure_level;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * post pram 형식의 스트링 반환
     * @param op GET: 서버 데이터 조회용 , PUT: 서버 데이터 업로드 용
     * @return post param
     */
    public String toString(String op) {
        String result = "";
        if (op.equals(Command.GET))
            result += keyMAC + "=" + getMAC() + "&" + keySSID + "=" + getSSID() + "&"+ keyEncrypt + "=" + getEncrypt();
        else if (op.equals(Command.PUT)) {
            result += keyMAC + "=" + getMAC() + "&" + keySSID + "=" + getSSID() + "&" + keyPubIP + "=" + getPubIP() + "&" + keyDnsIP1 + "=" + getDnsIP1() + "&" + keyDnsIP2 + "=" + getDnsIP2();
        }

        return result;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
