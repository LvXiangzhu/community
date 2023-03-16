package com.dlut.community;

import com.dlut.community.controller.HomeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ControllerTest {
    @Autowired
    HomeController homeController;
    @Test
    public void HomeControllerTest() {
//        homeController.getIndexPage();
    }
}
