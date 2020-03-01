package com.leyou.sms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author 柒
 * @date 2020-02-28 11:38
 * @Description: 邮件发送
 */
@Service
public class MailService {

    @Autowired
    private JavaMailSenderImpl mailSender ;

    @Autowired
    private MailProperties mailProperties ;

    /**
     * 发送邮件
     * @param email
     * @param code
     */
    public void sendMsg(String email, String code) throws MessagingException {
        // 复杂邮件类型
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
        messageHelper.setSubject("subject");
        messageHelper.setText("<p>\n" +
                "    <span style=\"color: rgb(51, 51, 51); font-family: 微软雅黑; font-size: 14px; background-color: rgb(255, 255, 255);\">[柒玖氵]：你的验证码是"+code+",有效期为5分钟。请尽快使用。</span>\n" +
                "</p>",true);
        messageHelper.setFrom(this.mailProperties.getUsername());
        messageHelper.setTo(email);
        mailSender.send(message);
    }
}
