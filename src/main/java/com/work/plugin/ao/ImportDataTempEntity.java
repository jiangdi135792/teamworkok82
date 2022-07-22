package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by work on 2022/3/31.
 */
@Table("Import_Entity")
public interface ImportDataTempEntity extends Entity {
    String getUserId();
    void setUserId(String userId);

    String getUserName();
    void  setUserName(String userName);

    String getOrgId();
    void setOrgId(String orgId);

    String getOrgName();
    void  setOrgName(String orgName);

    String getEmail();
    void setEmail(String email);

    //1  待更新  0  已更新
    int getStatus();
    void setStatus(int status);

    String getUserParentId();
    void  setUserParentId(String userParentId);

    String getOrgParentId();
    void setOrgParentId(String orgParentId);

    String getDepartId();
    void setDepartId(String departId);

    String getDepartParentId();
    void setDepartParentId(String departParentId);

    String getDepartName();
    void  setDepartName(String departName);

    Long getHashCode();
    void setHashCode(long hashCode);

    String getJiraUserKey();
    void setJiraUserKey(String jiraUserKey);

    int getNewOrgId();
    void setNewOrgId(int newOrgId);

    int getNewDepId();
    void setNewDepId(int newDepId);
}
