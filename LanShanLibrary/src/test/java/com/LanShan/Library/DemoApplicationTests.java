package com.LanShan.Library;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Date;
import java.util.HashMap;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void testPassword2() {
        String pass = "3g";
        System.out.println("原文:" + pass);
        BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder(4);
        String hashPass = bcryptPasswordEncoder.encode(pass);
        System.out.println("加密后:"+hashPass);
        boolean f = bcryptPasswordEncoder.matches("3g","$2a$10$9snSaVIrk3QLlPwSnQPg.evWdD6W4S9EByjH/TURL2eHqSPmHvfmm");
        System.out.println(f);
    }
    @Test
    void test1() {
        JSONObject json = new JSONObject();
        HashMap map = new HashMap();
        map.put("id", "1");
        map.put("id","2");
        json.put("map",map);
        System.out.println(json);

    }

}
