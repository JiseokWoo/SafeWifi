package com.safewifi;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chup on 2016-06-06.
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

    public APInfo(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        if (jsonObject.getString(keyMAC) != null) {
            setMac(jsonObject.getString(keyMAC));
        }
        if (jsonObject.getString(keySSID) != null) {
            setSSID(jsonObject.getString(keySSID));
        }
        if (jsonObject.getString(keyPubIP) != null) {
            setPubIP(jsonObject.getString(keyPubIP));
        }
        if (jsonObject.getString(keyDnsIP1) != null) {
            setDnsIP1(jsonObject.getString(keyDnsIP1));
        }
        if (jsonObject.getString(keyDnsIP2) != null) {
            setDnsIP2(jsonObject.getString(keyDnsIP2));
        }
        if (jsonObject.getString(keyInfo) != null) {
            setInfo(jsonObject.getString(keyInfo));
        }
    }

    public String getSSID() {
        return ssid;
    }

    public void setSSID(String SSID) {
        this.ssid = SSID;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
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

    public String toString(int op) {
        String result = "";

        if (getMac() != null) { result += keyMAC + "=" + getMac() + "&"; }
        if (getSSID() != null) { result += keySSID + "=" + getSSID() + "&"; }
        if (op == 1) { return result; }
        if (getPubIP() != null) { result += keyPubIP + "=" + getPubIP() + "&"; }
        if (getDnsIP1() != null) { result += keyDnsIP1 + "=" + getDnsIP1() + "&"; }
        if (getDnsIP2() != null) { result += keyDnsIP2 + "=" + getDnsIP2(); }

        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
}
