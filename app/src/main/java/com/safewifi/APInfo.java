package com.safewifi;

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
    private String info;
    private final static String keyInfo = "info";

    /**
     * 기본 생성자
     */
    public APInfo() {}

    /**
     * MAC 주소와 SSID 값만 설정하는 생성자
     * @param mac
     * @param ssid
     */
    public APInfo(String mac, String ssid) {
        setMAC(mac);
        setSSID(ssid);
    }

    /**
     * JSON 스트링을 받아 객체 생성하는 생성자
     * @param json
     * @throws JSONException
     */
    public APInfo(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        if (jsonObject.getString(keyMAC) != null || !jsonObject.getString(keyInfo).equals("null")) setMAC(jsonObject.getString(keyMAC));
        if (jsonObject.getString(keySSID) != null || !jsonObject.getString(keyInfo).equals("null")) setSSID(jsonObject.getString(keySSID));
        if (jsonObject.getString(keyInfo) != null || !jsonObject.getString(keyInfo).equals("null")) setInfo(jsonObject.getString(keyInfo));
    }

    /**
     * DHCP Info에서 pubIP, DNS1, DNS2 읽어와 객체에 저장
     * @param dhcpInfo
     */
    public void setDHCP(DhcpInfo dhcpInfo) {

        if (dhcpInfo.ipAddress != 0) {
            setPubIP(Formatter.formatIpAddress(dhcpInfo.ipAddress));
        }
        if (dhcpInfo.dns1 != 0) {
            setDnsIP1(Formatter.formatIpAddress(dhcpInfo.dns1));
        }
        if (dhcpInfo.dns2 != 0) {
            setDnsIP2(Formatter.formatIpAddress(dhcpInfo.dns2));
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

        if (getMAC() != null) result += keyMAC + "=" + getMAC() + "&";
        if (getSSID() != null) result += keySSID + "=" + getSSID() + "&";

        if (op.equals(Command.GET)) return result;

        if (getPubIP() != null) result += keyPubIP + "=" + getPubIP() + "&";
        if (getDnsIP1() != null) result += keyDnsIP1 + "=" + getDnsIP1() + "&";
        if (getDnsIP2() != null) result += keyDnsIP2 + "=" + getDnsIP2();

        if (result.endsWith("&")) result = result.substring(0, result.length() - 1);

        return result;
    }
}
