package com.leyou.sms.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author 柒
 * @date 2020-02-28 11:32
 * @Description: 测试邮件发送
 */
@SpringBootTest
public class MailTest {

    @Autowired
    private JavaMailSenderImpl mailSender ;

    @Autowired
    private MailProperties mailProperties ;

    @Test
    void contextLoads() {
        SimpleMailMessage message = new SimpleMailMessage();
        // 邮件设置
        message.setText("text");
        message.setSubject("标题");
        message.setTo("1466960699@qq.com");
        message.setFrom("2980372618@qq.com");
        mailSender.send(message);

    }

    @Test
    void test02() throws MessagingException {
        // 复杂邮件类型
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
        messageHelper.setSubject("subject");
        messageHelper.setText("<p>\n" +
                "    <span style=\"color: rgb(51, 51, 51); font-family: 微软雅黑; font-size: 14px; background-color: rgb(255, 255, 255);\">[柒玖氵]：你的验证码是123456,有效期为5分钟。请尽快使用。</span>\n" +
                "</p>",true);
        messageHelper.setTo("1466960699@qq.com");
        messageHelper.setFrom(this.mailProperties.getUsername());
        mailSender.send(message);
    }

}
