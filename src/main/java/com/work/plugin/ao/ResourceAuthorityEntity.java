package com.work.plugin.ao;

import net.java.ao.Entity;

/**
 * Created by Administrator on 2021/7/4.
 */
public interface ResourceAuthorityEntity extends Entity{
    int getId();

    void setId(int id);

    int getFunctionId();

    void setFunctionId(int FunctionId);

    int getUserId();
    void  setUserId(int UserId);

    int getType();
    void  setType(int Type);
}
