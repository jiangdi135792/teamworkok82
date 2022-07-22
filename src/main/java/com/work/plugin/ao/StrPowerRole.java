package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by work on 2022/3/15.
 */
@Table("Str_Power_Role")
public interface StrPowerRole extends Entity {
    RoleEntity getRoleEntity();
    void setRoleEntity(RoleEntity roleEntity);
    PowerEntity getPowerEntity();
    void setPowerEntity(PowerEntity powerEntity);
}
