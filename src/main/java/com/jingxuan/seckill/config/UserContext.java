package com.jingxuan.seckill.config;

import com.jingxuan.seckill.pojo.User;

/**
 * @ClassName: UserContext
 */
public class UserContext {

    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public static void setUser(User User) {
        userThreadLocal.set(User);
    }

    public static User getUser() {
        return userThreadLocal.get();
    }
}
