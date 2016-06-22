package com.safewifi.common;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by JiseokWoo
 */
public class ConnectWifi {

    public static WifiConfiguration ConfigOpen (String ssid, String mac) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.BSSID = mac;
        config.priority = 1;

        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedAuthAlgorithms.clear();
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        return config;
    }

    public static WifiConfiguration ConfigWEP (String ssid, String mac, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.BSSID = mac;
        config.priority = 1;

        config.status = WifiConfiguration.Status.DISABLED;
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);

        int length = password.length();
        if ((length == 10 || length == 26 || length == 58) && password.matches("[0-9A-Fa-f]*")) {
            config.wepKeys[0] = password;
        } else {
            config.wepKeys[0] = '"' + password + '"';
        }

        return config;
    }

    public static WifiConfiguration ConfigWPA (String ssid, String mac, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.BSSID = mac;
        config.priority = 1;

        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.preSharedKey = "\"" + password + "\"";

        return config;
    }

    public static boolean connect(WifiManager wifiManager, ScanResult ap, String password) {
        WifiConfiguration config = null;

        if (ap.capabilities.contains(Command.ENCRYPT_OPEN)) {
            config = ConfigOpen(ap.SSID, ap.BSSID);
        } else if (ap.capabilities.contains(Command.ENCRYPT_WEP)) {
            config = ConfigWEP(ap.SSID, ap.BSSID, password);
        } else if (ap.capabilities.contains(Command.ENCRYPT_WPA)) {
            config = ConfigWPA(ap.SSID, ap.BSSID, password);
        }

        if (config != null) {
            int networkID = wifiManager.addNetwork(config);
            if (networkID != -1) {
                return wifiManager.enableNetwork(networkID, true);
            }
        }

        return false;
    }
}
