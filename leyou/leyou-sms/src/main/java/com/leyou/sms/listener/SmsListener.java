package com.leyou.sms.listener;

import com.aliyuncs.exceptions.ClientException;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.service.MailService;
import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import java.util.Map;

/**
 * @author 柒
 * @date 2020-02-27 15:39
 * @Description: 监听 进行短信发送
 */
@Component
public class SmsListener {

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private MailService mailService ;

    /**
     * 短信发送
     * 阿里云没钱 发不了短信  蛋疼....
     * @param msg 传入的发送信息 存放手机号，验证码等...
     */
    /*@RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SMS.QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU.SMS.EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"verifycode.sms"}
    ))*/
    public void sendSms(Map<String, String> msg) throws ClientException {
        if (CollectionUtils.isEmpty(msg)) {
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");
        if (StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(code)) {
            this.smsUtils.sendSms(phone,code,this.smsProperties.getSignName(),this.smsProperties.getVerifyCodeTemplate());
        }
    }

    /**
     * 通过邮件验证
     * @param msg
     * @throws MessagingException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SMS.QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU.SMS.EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"verifycode.sms"}
    ))
    public void sendEmails(Map<String,String> msg) throws MessagingException {
        if (CollectionUtils.isEmpty(msg)) {
            return;
        }
        String phone = msg.get("phone");
        String code = msg.get("code");
        this.mailService.sendMsg(phone,code);
    }
}
