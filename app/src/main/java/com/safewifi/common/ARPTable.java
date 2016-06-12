package com.safewifi.common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by JiseokWoo
 * ARP 테이블 관리 클래스
 */
public class ARPTable {

    private List<JSONObject> table;
    private List<String> macList;

    void ARPTable() {
        table = new ArrayList<>();
    }

    /**
     * 안드로이드 디바이스의 ARP 테이블 정보 저장
     */
    public void getARPTable() {
        // TODO: Android 상위 버전에서 정상 동작하는지 확인 필요
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line = "";
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parse = line.split("\\s+");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("IP", parse[0]);
                jsonObject.put("MAC", parse[3]);
                table.add(jsonObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ARP 테이블에서 중복 MAC 값 여부 체크
     * @return ARP Spoofing 여부
     */
    public boolean checkARPSpoof() {
        // TODO: Android 상위 버전에서 정상 동작하는지 확인 필요
        macList = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line = "";
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parse = line.split("\\s+");
                macList.add(parse[3]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (macList.size() > 2) {
            int len_origin = macList.size();
            HashSet<String> hashSet = new HashSet<>(macList);
            macList = new ArrayList<>(hashSet);

            if (macList.size() != len_origin) {
                return true;
            }
        }

        return false;
    }
}
