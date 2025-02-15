package com.LanShan.Library;

import com.LanShan.Library.mapper.UserMapper;
import com.LanShan.Library.utils.SnowFlakeGenerator;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private UserMapper userMapper;
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
        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        for(int i = 0;i < 15;i++) {
            String id = IDgeter.generateNextId();
            System.out.println(id);
        }
    }
    @Test
    public void recommendations(){
        String user_id = "1";
        List<ImmutablePair<String,Long>> te = userMapper.reco_label(user_id);
        if(te == null || te.isEmpty()){
            te = userMapper.reco_random();
            if(te == null || te.isEmpty())
                throw new NoResourceFoundException("无可推广!");
            Random random = new Random();
            int randomIndex = random.nextInt(te.size());
            System.out.println(te.get(randomIndex).getLeft());
            return;
        }
        String label = te.getFirst().getLeft();
        te = userMapper.reco_book(label,user_id);
        if(te == null || te.isEmpty())
            te = userMapper.reco_random();
        if(te == null || te.isEmpty())
            throw new NoResourceFoundException("无可推广!");
        Random random = new Random();
        int randomIndex = random.nextInt(te.size());  // 随机生成一个索引
        System.out.println(te.get(randomIndex).getLeft());
        return;
    }

}
