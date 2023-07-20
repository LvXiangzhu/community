package com.dlut.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWEE = "followee"; //我关注的人
    private static final String PREFIX_FOLLOWER = "follower"; //关注我的人

    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    private static final String PREFIX_UV = "uv"; //访问量
    private static final String PREFIX_DAU = "dau"; //活跃用户

    private static final String PREFIX_POST = "post"; // 分数改变的帖子

    //某个实体的赞
    //like:entity:entityType:entityId -> set(userId) 值用集合存储，谁点了赞就把userId存到这个集合里，方便查看谁点了赞
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    //like:user:userId
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体
    //followee:userId:entityType -> zset(entityId, now)
    /*
    * userId：用户（粉丝）的id
    * entityType：用户关注的实体类型
    *
    * 集合的值存放被关注的实体id
    * */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体拥有的粉丝
    //follower:entityType:entityId -> zset(userId,, now)
    /*
    * entityType：实体类型
    * entityId：实体id
    *
    * 集合的值存放粉丝id
    * */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /*
    * 登录验证码
    * owner：验证码拥有者，用户临时凭证
    * */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /*
    * 获取用户登录凭证关键字
    * ticket:登录凭证
    * */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /*
    * 缓存用户数据所需的key
    * */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    //单日访问量
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    //区间访问量
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
