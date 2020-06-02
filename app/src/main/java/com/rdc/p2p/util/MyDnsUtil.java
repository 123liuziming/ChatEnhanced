package com.rdc.p2p.util;

import com.rdc.p2p.bean.MyDnsBean;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;

public class MyDnsUtil {
    private static HashMap<String, String> nameToIp = new HashMap<>();
    private static HashMap<String, String> ipToName = new HashMap<>();

    // type为1， 转换用户名，为2， 转换ip
    private static String solve(String str, int type) {
        try {
            HashMap<String, String> tmp = nameToIp;
            if(type == 2)
                tmp = ipToName;
            if(tmp.containsKey(str))
                return tmp.get(str);
            List<MyDnsBean> allRes;
            String res;
            if(type == 1) {
                allRes = DataSupport.where("mTargetName = ?", str).find(MyDnsBean.class);
                res = allRes.get(0).getmTargetIp();
            }
            else {
                allRes = DataSupport.where("mTargetIp = ?", str).find(MyDnsBean.class);
                res = allRes.get(0).getmTargetName();
            }
            if(allRes.size() == 0)
                throw new Exception("错误的DNS转换");
            tmp.put(str, res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // 返回用户名对应的ip
    public static String convertUserName(String userName) {
        // 享元模式，在本地留有一份DNS的缓存
        return solve(userName, 1);
    }

    // 反之
    public static String convertUserIp(String userIp) {
        return solve(userIp, 2);
    }

    public static void refresh(String mTargetIp) {
        nameToIp.remove(mTargetIp);
    }

}
