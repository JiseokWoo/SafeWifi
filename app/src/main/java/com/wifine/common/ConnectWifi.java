package com.wifine.common;

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

    public static WifiConfiguration findStoredConfig(WifiManager wifiManager, String ssid) {
        WifiConfiguration config = null;
        List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration configuration : wifiConfigurationList) {
            if (configuration.SSID.equals("\"" + ssid + "\"")) {
                config = configuration;
                break;
            }
        }

        return config;
    }

    public static boolean connect(WifiManager wifiManager, APInfo curAP, String password) {
        WifiConfiguration config = null;

        if (curAP.getInfoEncrypt().contains(Command.ENCRYPT_WEP)) {
            config = ConfigWEP(curAP.getSSID(), curAP.getMAC(), password);
        } else if (curAP.getInfoEncrypt().contains(Command.ENCRYPT_WPA)) {
            config = ConfigWPA(curAP.getSSID(), curAP.getMAC(), password);
        }

        if (config != null) {
            int networkID = wifiManager.addNetwork(config);

            if (networkID != -1) {
                return wifiManager.enableNetwork(networkID, true);
            }
        }

        return false;
    }

    public static boolean connect(WifiManager wifiManager, WifiConfiguration config) {
        return wifiManager.enableNetwork(config.networkId, true);
    }
}
