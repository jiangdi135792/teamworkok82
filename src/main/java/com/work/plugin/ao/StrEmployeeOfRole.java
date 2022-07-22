package com.work.plugin.ao;

import net.java.ao.Entity;

/**
 * Created by work on 2022/3/15.
 */
public interface StrEmployeeOfRole extends Entity {
    StrEmployee getEmployee();
    void setEmployee(StrEmployee e);
    RoleEntity getRole();
    void setRole(RoleEntity role);

    Integer getTeamId();
    void setTeamId(Integer teamId);

}
