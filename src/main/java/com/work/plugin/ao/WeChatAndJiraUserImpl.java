package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.work.plugin.util.Encryption;

/**
 * Created by work on 2022/4/24.
 */
public class WeChatAndJiraUserImpl implements  WeChatAndJiraUserService {
    private final ActiveObjects ao;

    public WeChatAndJiraUserImpl(ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public WeChatAndJiraUserEntity getByTel(String tel) {
        WeChatAndJiraUserEntity[] weChatAndJiraUserEntities = ao.find(WeChatAndJiraUserEntity.class, String.format("TEL = '%s' ", tel));
        if (weChatAndJiraUserEntities.length != 0){
            return weChatAndJiraUserEntities[0];
        }else {
        return null;
        }
    }

    @Override
    public Boolean bindInfo(String tel, String userName, String passwd,String updateTime) {
        WeChatAndJiraUserEntity[] weChatAndJiraUserEntities = ao.find(WeChatAndJiraUserEntity.class, String.format("USER_NAME = '%s' ", userName));
        if (weChatAndJiraUserEntities.length != 0){
            ao.delete(weChatAndJiraUserEntities[0]);
            WeChatAndJiraUserEntity weChatAndJiraUserEntity = ao.create(WeChatAndJiraUserEntity.class);
            weChatAndJiraUserEntity.setTel(tel);
            weChatAndJiraUserEntity.setUpdateTime(updateTime);
            weChatAndJiraUserEntity.setUserName(userName);
            try {
                weChatAndJiraUserEntity.setPassword(Encryption.encrypt(passwd));
            } catch (Exception e) {
                e.printStackTrace();
            }
            weChatAndJiraUserEntity.save();
            return true;
        }else {
            WeChatAndJiraUserEntity weChatAndJiraUserEntity = ao.create(WeChatAndJiraUserEntity.class);
            weChatAndJiraUserEntity.setTel(tel);
            weChatAndJiraUserEntity.setUserName(userName);
            weChatAndJiraUserEntity.setUpdateTime(updateTime);
            try {
                weChatAndJiraUserEntity.setPassword(Encryption.encrypt(passwd));
            } catch (Exception e) {
                e.printStackTrace();
            }
            weChatAndJiraUserEntity.save();
            return true;
        }
    }

    @Override
    public Boolean unbindInfo(String tel) {
        WeChatAndJiraUserEntity[] weChatAndJiraUserEntities = ao.find(WeChatAndJiraUserEntity.class, String.format("TEL = '%s' ", tel));
        if (weChatAndJiraUserEntities.length != 0){
        ao.delete(weChatAndJiraUserEntities[0]);
        return true;
        }else {
            return false;
        }
    }
    @Override
    public WeChatAndJiraUserEntity[] getAll() {
        WeChatAndJiraUserEntity[] weChatAndJiraUserEntities = ao.find(WeChatAndJiraUserEntity.class);
        return weChatAndJiraUserEntities;
    }

    @Override
    public void del(String tel) {
        WeChatAndJiraUserEntity[] weChatAndJiraUserEntities = ao.find(WeChatAndJiraUserEntity.class);
        ao.delete(weChatAndJiraUserEntities[0]);
    }
}
