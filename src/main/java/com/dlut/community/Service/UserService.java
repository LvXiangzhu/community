package com.dlut.community.Service;

import com.dlut.community.dao.LoginTicketMapper;
import com.dlut.community.dao.UserMapper;
import com.dlut.community.pojo.LoginTicket;
import com.dlut.community.pojo.User;
import com.dlut.community.util.CommunityConstant;
import com.dlut.community.util.CommunityUtil;
import com.dlut.community.util.MailClient;
import com.dlut.community.util.RedisKeyUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    UserMapper userMapper;
    //注入邮件客户端、模板引擎、域名、虚拟路径
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Value("${server.servlet.context-path}")
    private String contentPath;
    @Value("${community.path.domain}")
    private String domain;
    @Autowired
    RedisTemplate redisTemplate;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null) {
            user = initCache(id);
        }
        return user;
    }

    /*
    * 功能：注册
    * 参数：用户
    * 返回值：注册状态（注册成功、用户名已存在失败等）
    * */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //如果没有user传过来，说明是系统的问题，抛出异常
        if(user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        //如果用户里的参数为空，说明用户操作有误，不用抛异常
        if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        //如果用户输没问题，接下来验证用户名是否已经存在，邮箱是否已经被注册
        //验证账户
        User user1 = userMapper.selectByName(user.getUsername());
        if(user1 != null) {
            map.put("usernameMsg", "用户名已存在！");
            return map;
        }

        //验证邮箱
        User user2 = userMapper.selectByEmail(user.getEmail());
        if(user2 != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0); //默认用户
        user.setStatus(0); //刚开始默认没有激活，需要激活码激活
        user.setActivationCode(CommunityUtil.generateUUID()); // 随机生成激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        //把用户添加到数据库
        userMapper.insertUser(user);

        //给用户发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //http://localhost:8080/community/activation/101/code
        String url = domain + contentPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);


        return map;
    }

    /*
    * 功能：判断激活状态
    * 参数：用户id，激活码
    * 返回值：激活状态
    * */
    public int activation(int userId, String code) { //Service里的这个方法会返回给controller吗
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1) {
            return ACTIVATION_REAPEAT;
        }else if(user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId); //状态有修改，清理缓存
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    /*
    * 方法功能：登录
    * 参数：用户名，密码（需加密），过期时间
    * 返回值：登录状态（成功、账号为空失败、密码错误失败等）
    * */
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值判断
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        //验证账号是否存在
        User user = userMapper.selectByName(username);
        if(user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        //验证状态
        if(user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt()); //用户输入的密码加密后才和数据库存的密码相等
        if(!password.equals(user.getPassword())) {
            map.put("passwordMsg", "密码错误！");
            return map;
        }

        //登录成功，生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket); //redis会把对象序列化为一个字符串

        map.put("ticket", loginTicket.getTicket()); //用数据库代替session
        return map;
    }

    /*
    * 方法功能：退出登录
    * */
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        // LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }

    /*
    * 方法功能：更新头像
    * */
    public int updateHead(int userId, String HeadUrl) {
        int rows = userMapper.updateHeader(userId, HeadUrl);
        clearCache(userId);
        return rows;
    }

    /*
    * 方法功能：修改密码
    * */
    public int updatePassword(int userId, String password) {
        int rows = userMapper.updatePassword(userId, password);
        clearCache(userId);
        return rows;
    }

    /*
    * 方法功能：根据用户名查询用户
    * */
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    //1、优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //2、取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    //3、数据变更时清除缓存
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    /*
    * Security安全认证时返回的用户权限
    * */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
