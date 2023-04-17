package com.dlut.community.Service;

import com.dlut.community.pojo.User;
import com.dlut.community.util.CommunityConstant;
import com.dlut.community.util.HostHolder;
import com.dlut.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /*
    * 关注
    * 参数 userId：当前用户，粉丝
    *     entityType：要关注的实体类型
    *     entityId：要关注的实体id
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId,System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    /*
    * 取消关注
    *
    * */
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    /*
    * 查询用户关注的实体的数量
    * userId：谁关注
    * entityType：关注的实体类型。不同被关注的类型要单独统计，不能混在一起
    * */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey); //统计个数
    }

    /*
    * 查询实体的粉丝数量
    * entityType：实体类型
    * entityId：实体id
    * */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);

    }

    /*
    * 查询当前用户是否已关注实体
    * userId：当前用户id
    * */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /*
    * 查询某用户关注的人
    * userId:某用户的id
    * offset：要分页显示，当前页起始行
    * limit:每页数据限制
    * */
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        List<Map<String, Object>> list = new ArrayList<>();
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if(targetIds == null) {
            return null;
        }
        if(targetIds != null) {
            for (Integer targetId : targetIds) {
                Map<String, Object> map = new HashMap<>();
                User user = userService.findUserById(targetId);
                map.put("user", user);
                Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
                map.put("score", new Date(score.longValue()));
                list.add(map);
            }
        }
        return list;
    }

    /*
    * 查询某用户的粉丝
    * */
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        //注意，set虽然是一个无序集合，但redis内部有自己的set接口的实现类，是可以排序的
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        List<Map<String, Object>> list = new ArrayList<>();
        if(targetIds == null) {
            return null;
        }
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("score", new Date(score.longValue()));
            list.add(map);
        }

        return list;

    }
}
