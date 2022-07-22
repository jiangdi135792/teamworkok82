package com.work.plugin.ao;

/**
 * Created by work on 2022/4/24.
 */
public interface WeChatAndJiraUserService {
    /**
     *
     * @param tel 手机号
     * @return
     */
   WeChatAndJiraUserEntity getByTel(String tel);

    /**
     *
     * @param tel           手机号
     * @param userName      账户
     * @param passwd        密码
     */
    Boolean  bindInfo(String tel,String userName,String passwd,String updateTime);

    /**
     *
     * @param tel  手机号
     */
    Boolean  unbindInfo(String tel);

    WeChatAndJiraUserEntity[] getAll();

    void  del(String tel);
}
