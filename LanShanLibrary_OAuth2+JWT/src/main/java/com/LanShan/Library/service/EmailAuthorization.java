package com.LanShan.Library.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailAuthorization {

    @Autowired
    private JavaMailSender mailSender;
    public void sendMail(int type,String to,String content) throws Exception {
        if(type == 0)//绑定邮箱
            content = "http://121.40.101.83/LanShanLibrary/user/email/" + content;
        if(type == 1)//邮箱注册
            content = "http://121.40.101.83/LanShanLibrary/user/emailregister/" + content;
        if(type == 2)//邮箱登录
            content = "http://121.40.101.83/LanShanLibrary/user/emaillogin/" + content;

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String html = "<!DOCTYPE html>\n" +
                "<html lang=\"zh\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>邮箱验证地址</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: 'Arial', sans-serif;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            background-color: #f4f4f9;\n" +
                "        }\n" +
                "\n" +
                "        .email-container {\n" +
                "            width: 100%;\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            background-color: #ffffff;\n" +
                "            padding: 20px;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);\n" +
                "        }\n" +
                "\n" +
                "        .email-header {\n" +
                "            text-align: center;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .email-header h2 {\n" +
                "            color: #333;\n" +
                "            font-size: 24px;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "\n" +
                "        .email-body {\n" +
                "            font-size: 16px;\n" +
                "            color: #555;\n" +
                "            line-height: 1.6;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "\n" +
                "        .verification-code {\n" +
                "            font-size: 28px;\n" +
                "            font-weight: bold;\n" +
                "            color: #007bff;\n" +
                "            margin: 20px 0;\n" +
                "            text-align: center;\n" +
                "            padding: 10px 20px;\n" +
                "            background-color: #f1f1f1;\n" +
                "            border-radius: 4px;\n" +
                "        }\n" +
                "\n" +
                "        .footer {\n" +
                "            text-align: center;\n" +
                "            font-size: 14px;\n" +
                "            color: #aaa;\n" +
                "            margin-top: 30px;\n" +
                "        }\n" +
                "\n" +
                "        .footer a {\n" +
                "            color: #007bff;\n" +
                "            text-decoration: none;\n" +
                "        }\n" +
                "\n" +
                "        .footer a:hover {\n" +
                "            text-decoration: underline;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"email-container\">\n" +
                "        <div class=\"email-header\">\n" +
                "            <h2>TWind的蓝山图书馆</h2>\n" +
                "        </div>\n" +
                "        <div class=\"email-body\">\n" +
                "            <p>您好，</p>\n" +
                "            <p>感谢你的请求！这是您的邮箱验证地址，点击即可：</p>\n" +
                "            <div class=\"verification-code\">\n" +
                            content + "\n" +
                "            </div>\n" +
                "            <p>请在1小时内完成验证，如果您没有请求此验证码，请忽略此邮件。</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n";
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom("2420876526@qq.com");
        helper.setTo(to);
        helper.setSubject("Email验证邮件");
        helper.setText(html, true);
        mailSender.send(mimeMessage);
    }

}
