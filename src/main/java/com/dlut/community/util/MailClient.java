package com.dlut.community.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(from); //发件人
            messageHelper.setTo(to); //收件人
            messageHelper.setSubject(subject); //主题
            messageHelper.setText(content, true); //文本，支持显示html格式
            mailSender.send(messageHelper.getMimeMessage()); //发送邮件
        } catch (MessagingException e) {
            logger.error("发送邮件失败" + e.getMessage());

        }

    }
}
