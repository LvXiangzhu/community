package com.dlut.community;

import com.dlut.community.Service.DataService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.concurrent.*;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    //JDK普通线程池
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    //JDK可执行定时任务线程池
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    // Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    // Spring可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private DataService dataService;

    public void sleep(int sec) {
        try {
            Thread.sleep(sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 1.JDK普通线程池
    @Test
    public void test1() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println("ExecutorService"+Thread.currentThread());
            }
        };
        for(int i = 0; i < 10; i++) {
            executorService.execute(task);
        }

        sleep(10000); //test线程不会等里面创建的线程执行，所以需要手动sleep
    }

    // 2.JDK定时任务线程池
    @Test
    public void test2() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println("ExecutorService"+Thread.currentThread());
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 1000, 1000, TimeUnit.SECONDS);

        sleep(30000);
    }

    // 3.Spring普通线程池
    @Test
    public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskExecutor");
            }
        };

        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }

        sleep(10000);
    }

    // 4.Spring定时任务线程池
    @Test
    public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskScheduler");
            }
        };

        Date startTime = new Date(System.currentTimeMillis() + 10000);
        taskScheduler.scheduleAtFixedRate(task, startTime, 1000);

        sleep(30000);
    }

    // 5.Spring普通线程池(简化)
    @Test
    public void testThreadPoolTaskExecutorSimple() {
        for (int i = 0; i < 10; i++) {
            dataService.execute1();
        }

        sleep(10000);
    }

    // 6.Spring定时任务线程池(简化)
    @Test
    public void testThreadPoolTaskSchedulerSimple() {
        sleep(30000);
    }
}
