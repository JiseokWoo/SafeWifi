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
    private String secure_level;
    private final static String keySecureLevel = "secure_level";
    private Integer secure_score;
    private final static String keySecureScore = "secure_score";
    private String info_encrypt;
    private final static String keyInfoEncrypt = "info_encrypt";
    private Integer info_dns;
    private final static String keyInfoDns = "info_dns";
    private boolean info_arp;
    private final static String keyInfoArp = "info_arp";
    private boolean info_port;
    private final static String keyInfoPort = "info_port";
    private int conn_count;
    private final static String keyConnCount = "conn_count";
    private Integer signalLevel;
    private Integer position;

    /**
     * MAC 주소와 SSID 값만 설정하는 생성자
     * @param mac
     * @param ssid
     */
    public APInfo(String mac, String ssid, Integer signalLevel, String encrypt, Integer position) {
        setMAC(mac);
        setSSID(ssid);
        setPubIP("-");
        setDnsIP1("-");
        setDnsIP2("-");
        setSignalLevel(signalLevel);
        setInfoEncrypt(encrypt);
        setPosition(position);
    }

    /**
     * DB에서 가져온 JSON 스트링을 객체에 세팅
     * @param json
     * @throws JSONException
     */
    public void setDBInfo(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        if (jsonObject.getString(keyMAC) != null && !jsonObject.getString(keyMAC).equals("-")) setMAC(jsonObject.getString(keyMAC));
        if (jsonObject.getString(keySSID) != null && !jsonObject.getString(keySSID).equals("-")) setSSID(jsonObject.getString(keySSID));
        if (jsonObject.getString(keySecureLevel) != null && !jsonObject.getString(keySecureLevel).equals("-")) setSecureLevel(jsonObject.getString(keySecureLevel));
        if (jsonObject.getString(keySecureScore) != null) setSecureScore(jsonObject.getInt(keySecureScore));
        if (jsonObject.getString(keyConnCount) != null) setConnCount(jsonObject.getInt(keyConnCount));
        if (jsonObject.getString(keyInfoDns) != null) setInfoDns(jsonObject.getInt(keyInfoDns));
        if (jsonObject.getString(keyInfoArp) != null && !jsonObject.getString(keyInfoArp).equals("-")) {
            if (jsonObject.getString(keyInfoArp).equals("N")) setInfoArp(false);
            else setInfoArp(true);
        }
        if (jsonObject.getString(keyInfoPort) != null && !jsonObject.getString(keyInfoPort).equals("-")) {
            if (jsonObject.getString(keyInfoPort).equals("N")) setInfoPort(false);
            else setInfoPort(true);
        }
        if (jsonObject.getString(keyInfoDns) != null) setInfoDns(jsonObject.getInt(keyInfoDns));

        setPubIP("-");
        setDnsIP1("-");
        setDnsIP2("-");
    }

    /**
     * DHCP Info에서 pubIP, DNS1, DNS2 읽어와 객체에 저장
     * @param dhcpInfo
     */
    public void setDHCP(DhcpInfo dhcpInfo) {

        if (dhcpInfo.ipAddress != 0) setPubIP(Formatter.formatIpAddress(dhcpInfo.ipAddress));
        else setPubIP("-");

        if (dhcpInfo.dns1 != 0) setDnsIP1(Formatter.formatIpAddress(dhcpInfo.dns1));
        else setDnsIP1("-");

        if (dhcpInfo.dns2 != 0) setDnsIP2(Formatter.formatIpAddress(dhcpInfo.dns2));
        else setDnsIP2("-");
    }

    public String getMAC() {
        return mac;
    }

    public void setMAC(String mac) {
        this.mac = mac;
    }

    public String getSSID() {
        return ssid;
    }

    public void setSSID(String ssid) {
        this.ssid = ssid;
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

    public String getSecureLevel() {
        return secure_level;
    }

    public void setSecureLevel(String secure_level) {
        this.secure_level = secure_level;
    }

    public Integer getSecureScore() {
        return secure_score;
    }

    public void setSecureScore(Integer secure_score) {
        this.secure_score = secure_score;
    }

    public String getInfoEncrypt() {
        return info_encrypt;
    }

    public void setInfoEncrypt(String info_encrypt) {
        this.info_encrypt = info_encrypt;
    }

    public Integer getInfoDns() {
        return info_dns;
    }

    public void setInfoDns(Integer info_dns) {
        this.info_dns = info_dns;
    }

    public boolean getInfoArp() {
        return info_arp;
    }

    public void setInfoArp(boolean info_arp) {
        this.info_arp = info_arp;
    }

    public boolean getInfoPort() {
        return info_port;
    }

    public void setInfoPort(boolean info_port) {
        this.info_port = info_port;
    }

    public int getConnCount() {
        return conn_count;
    }

    public void setConnCount(int conn_count) {
        this.conn_count = conn_count;
    }

    public Integer getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(Integer signalLevel) {
        this.signalLevel = signalLevel;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * post pram 형식의 스트링 반환
     * @param op GET: 서버 데이터 조회용 , PUT: 서버 데이터 업로드 용
     * @return post param
     */
    public String toString(String op) {
        String result = "";
        String encrypt = "-";
        if (getInfoEncrypt() != null) {
            if (getInfoEncrypt().contains(Command.ENCRYPT_OPEN)) {
                encrypt = Command.ENCRYPT_OPEN;
            } else if (getInfoEncrypt().contains(Command.ENCRYPT_WEP)) {
                encrypt = Command.ENCRYPT_WEP;
            } else if (getInfoEncrypt().contains(Command.ENCRYPT_WPA2)) {
                encrypt = Command.ENCRYPT_WPA2;
            } else if (getInfoEncrypt().contains(Command.ENCRYPT_WPA)) {
                encrypt = Command.ENCRYPT_WPA;
            }
        }

        if (op.equals(Command.GET))
            result += keyMAC + "=" + getMAC() + "&" + keySSID + "=" + getSSID() + "&"+ keyInfoEncrypt + "=" + encrypt;
        else if (op.equals(Command.PUT)) {
            result += keyMAC + "=" + getMAC() + "&" + keySSID + "=" + getSSID() + "&" + keyPubIP + "=" + getPubIP() + "&" + keyDnsIP1 + "=" + getDnsIP1() + "&" + keyDnsIP2 + "=" + getDnsIP2() + "&" + keyInfoEncrypt + "=" + encrypt + "&";

            if (getInfoArp())
                result += keyInfoArp + "=Y";
            else
                result += keyInfoArp + "=N";
        }

        return result;
    }


}
