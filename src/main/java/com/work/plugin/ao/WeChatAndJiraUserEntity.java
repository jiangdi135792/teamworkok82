package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by work on 2022/4/24.
 */
@Table("WECHAT_JIRAUSER")
public interface WeChatAndJiraUserEntity extends Entity {
    String getTel();
    void  setTel(String tel);

    String getUserName();
    void  setUserName(String userName);

    String getPassword();
    void  setPassword(String passWord);

    String getUpdateTime();
    void  setUpdateTime(String updateTime);
}
