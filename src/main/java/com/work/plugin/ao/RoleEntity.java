package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.Table;

/**
 * Created by work on 2022/3/15.
 */
@Table("ROLE_ENTITY")
public interface RoleEntity extends Entity {
    String getName();
    void setName(String name);
    //type=1  组织机构角色  =2 团队角色
    Integer getType();
    void setType(Integer type);
    Integer getParentId();
    void setParentId(Integer parentId);
    //true exit false  deleted
    Integer getStatus();
    void  setStatus(Integer status);
    Integer getOrder();
    void  setOrder(Integer order);
    String getDesc();
    void  setDesc(String desc);
    @OneToMany
    StrEmployeeOfRole[] getStrEmployeeOfRole();
}
