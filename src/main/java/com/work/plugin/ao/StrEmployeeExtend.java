package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by work on 2022/1/24.
 */
@Table("extend_employee")
public interface StrEmployeeExtend extends Entity{
    int getEmployeeId();
    void setEmployeeId(int employeeId);
    String getSourcePart();
    void setSourcePart(String sourcePart);

    String getLoginName();
    void  setLoginName(String loginName);

    Long getHashValue();
    void setHashValue(Long hashValue);

    String getUpdateTime();
    void  setUpdateTime(String updateTime);
}
