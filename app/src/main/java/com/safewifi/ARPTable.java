package com.safewifi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiseokWoo
 * ARP 테이블 관리 클래스
 */
public class ARPTable {
    public void getARPTable() {
        // ARPTable 테이블
        // TODO: Android 상위 버전에서 정상 동작하는지 확인 필요
        List<JSONObject> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line = "";
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parse = line.split("\\s+");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("IP", parse[0]);
                jsonObject.put("MAC", parse[3]);
                list.add(jsonObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
