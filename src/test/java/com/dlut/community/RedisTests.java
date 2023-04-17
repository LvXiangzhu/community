package com.dlut.community;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        //给redisKey赋值为1
//        redisTemplate.opsForValue().set(redisKey, 1);
        //取redisKey的值
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        //增加redisKey的值
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        //减少redisKey的值
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHashes() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testLists() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "刘备","关羽","张飞","赵云");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSets() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "褚赢", 100);
        redisTemplate.opsForZSet().add(redisKey, "时光", 75);
        redisTemplate.opsForZSet().add(redisKey, "俞亮", 80);
        redisTemplate.opsForZSet().add(redisKey, "洪河", 70);
        redisTemplate.opsForZSet().add(redisKey, "方绪", 85);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey)); //统计个数
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "时光"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "方绪")); //从大到小排名
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
    }

    @Test
    public void testKeys() {
        //删除key
        redisTemplate.delete("test:user");
        //判断key是否存在
        System.out.println(redisTemplate.hasKey("test:user"));
        //设置过期时间:10s
        redisTemplate.expire("test:ids", 10, TimeUnit.SECONDS);
    }

    //多次访问同一个key,可用BoundValueOperations绑定key，这样就不用重复传key了
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";

        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();

    }

    //编程式事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //这个方法在redisTemplate.execute()方法执行时会调用

                String redisKey = "test:tx";
                operations.multi(); //启用事务
                operations.opsForSet().add(redisKey, "刘备");
                operations.opsForSet().add(redisKey, "关羽");
                operations.opsForSet().add(redisKey, "张飞");
                //事务里查询不会返回结果
                System.out.println("查询结果："+operations.opsForSet().members(redisKey));

                //返回值返回给redisTemplate.execute()方法
                return operations.exec(); //提交事务
            }
        });
        System.out.println(obj);
    }
}
