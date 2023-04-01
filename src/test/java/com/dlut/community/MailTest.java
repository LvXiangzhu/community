package com.dlut.community;

import com.dlut.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired()
    private MailClient mailClient;
    @Autowired()
    private TemplateEngine templateEngine;

    @Test
    public void test1() {
        mailClient.sendMail("3471239281@qq.com", "SSM测试", "测试成功");
    }

    @Test
    public void test2() {
        Context context = new Context();
        context.setVariable("username", "sunday"); //name参数对应的就是html里面的${username}，它的值是sunday
        String content = templateEngine.process("/mail/demo", context); //模板引擎用于生成动态网页(为什么路径写成这样？)
        System.out.println(content);
        mailClient.sendMail("3471239281@qq.com", "html格式", content);
    }
}
