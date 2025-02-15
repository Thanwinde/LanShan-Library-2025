package com.LanShan.Library.service;

import com.LanShan.Library.controllers.BookController;
import com.LanShan.Library.mapper.UserMapper;
import com.LanShan.Library.pojo.Book;
import com.LanShan.Library.pojo.MyUserDetails;
import com.LanShan.Library.pojo.User;
import com.alibaba.fastjson2.JSONObject;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.util.List;
import java.util.Random;

@Component
public class AdvEmail {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserMapper userMapper;
    @Scheduled(initialDelay = 1000 * 10,fixedDelay = 1000 * 60 * 60 * 24)
    public void sendadv() throws MessagingException {
        System.out.println("开始推广");
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        List<User> users = userMapper.adv_getUsers();
        for (User user : users) {
            String bookId = recommendations(user.getId());
            Book book = userMapper.getBookById(bookId);
            helper.setFrom("2420876526@qq.com");
            helper.setTo(user.getEmail());
            helper.setSubject("TWind的图书推荐");
            String html = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>TWind的每日图书推荐</title>\n" +
                    "    <style>\n" +
                    "        body {\n" +
                    "            font-family: 'Arial', sans-serif;\n" +
                    "            background-color: #f2f2f2;\n" +
                    "            color: #333;\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "        }\n" +
                    "        .email-container {\n" +
                    "            max-width: 600px;\n" +
                    "            margin: 20px auto;\n" +
                    "            background-color: #ffffff;\n" +
                    "            border-radius: 12px;\n" +
                    "            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);\n" +
                    "            overflow: hidden;\n" +
                    "        }\n" +
                    "        .header {\n" +
                    "            background-color: #3498db;\n" +
                    "            color: white;\n" +
                    "            padding: 30px;\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "        .header h1 {\n" +
                    "            margin: 0;\n" +
                    "            font-size: 28px;\n" +
                    "        }\n" +
                    "        .book-info {\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .book-info .book-title {\n" +
                    "            font-size: 22px;\n" +
                    "            font-weight: bold;\n" +
                    "            color: #2c3e50;\n" +
                    "        }\n" +
                    "        .book-info .book-author {\n" +
                    "            font-size: 18px;\n" +
                    "            color: #7f8c8d;\n" +
                    "            margin-bottom: 10px;\n" +
                    "        }\n" +
                    "        .book-info .book-details {\n" +
                    "            font-size: 16px;\n" +
                    "            margin-bottom: 15px;\n" +
                    "            line-height: 1.6;\n" +
                    "        }\n" +
                    "        .book-info .book-details span {\n" +
                    "            font-weight: bold;\n" +
                    "        }\n" +
                    "        .book-image {\n" +
                    "            max-width: 150px;\n" +
                    "            border-radius: 8px;\n" +
                    "            margin-right: 20px;\n" +
                    "            float: left;\n" +
                    "        }\n" +
                    "        .footer {\n" +
                    "            background-color: #f4f4f4;\n" +
                    "            padding: 20px;\n" +
                    "            text-align: center;\n" +
                    "            font-size: 14px;\n" +
                    "            color: #7f8c8d;\n" +
                    "        }\n" +
                    "        .footer a {\n" +
                    "            color: #3498db;\n" +
                    "            text-decoration: none;\n" +
                    "        }\n" +
                    "        .cta-button {\n" +
                    "            background-color: #3498db;\n" +
                    "            color: white;\n" +
                    "            padding: 8px 10px;\n" +
                    "            border-radius: 5px;\n" +
                    "            text-align: center;\n" +
                    "            display: inline-block;\n" +
                    "            text-decoration: none;\n" +
                    "            font-size: 14px;\n" +
                    "            margin-top: 18px;\n" +
                    "            border: #ffffff;\n" +
                    "        }\n" +
                    "        .cta-button:hover {\n" +
                    "            background-color: #2980b9;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"email-container\">\n" +
                    "        <div class=\"header\">\n" +
                    "            <h1> "+user.getUsername()+" 的每日图书推荐</h1>\n" +
                    "            <h2>TWind的蓝山图书馆<h2>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"book-info\">\n" +
                    "            <div>\n" +
                    "                <p class=\"book-title\">"+book.name+"</p>\n" +
                    "                <p class=\"book-author\">by "+book.author+"</p>\n" +
                    "                <div class=\"book-details\">\n" +
                    "                    <p><span>书籍ID:</span>"+book.id+"</p>\n" +
                    "                    <p><span>ISBN:</span> "+book.ISBN + "</p>\n" +
                    "                    <p><span>出版日期:</span>"+book.getPublishedDate()+"</p>\n" +
                    "                    <p><span>主题:</span> "+book.label+"</p>\n" +
                    "                    <p><span>出版社:</span>"+book.publisher+"</p>\n" +
                    "                    <p><span>评分:</span> "+book.score+"</p>\n" +
                    "                    <p><span>剩余本数:</span> "+book.num+"</p>\n" +
                    "                </div>\n" +
                    "                <a href=\"http://121.40.101.83/LanShanLibrary\" class=\"cta-button\">欢迎借阅!</a>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"footer\">\n" +
                    "            <p>会向你每天推荐一本你可能感兴趣的书!</p>\n" +
                    "            <p>愿你找到你想要的书</p>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>\n";


            helper.setText(html, true);
            mailSender.send(mimeMessage);
        }
    }

    public String recommendations(String user_id){
        List<ImmutablePair<String,Long>> te = userMapper.reco_label(user_id);
        if(te == null || te.isEmpty()){
            te = userMapper.reco_random();
            if(te == null || te.isEmpty())
                throw new NoResourceFoundException("无可推广!");
            Random random = new Random();
            int randomIndex = random.nextInt(te.size());
            System.out.println(te.get(randomIndex).getLeft());
            return te.get(randomIndex).getLeft();
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
        return te.get(randomIndex).getLeft();
    }

}
