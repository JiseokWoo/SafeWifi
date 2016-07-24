package com.wifine.common;

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

    /**
     * ARP 테이블에서 중복 MAC 값 여부 체크
     * @return ARP Spoofing 여부
     */
    public static boolean checkARPSpoof() {
        // TODO: Android 상위 버전에서 정상 동작하는지 확인 필요
        List<String> macList = new ArrayList<>();

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

        if (macList.size() > 1) {
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
