package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;

/**
 * Created by work on 2022/3/15.
 */
@Transactional
public interface PowerService {
    /**
     *
     * @param url  url
     * @param description  描述
     * @param bool  标志位
     * @param powerOfType  权限类型
     * @return  生成的权限实例
     */
    PowerEntity setPower(String url,String description,Boolean bool,Integer powerOfType);

    /**
     *
     * @param organizationEntity 菜单对象
     * @param powerEntity 权限对象
     */
    void setItMenu(OrganizationEntity organizationEntity, PowerEntity powerEntity);

    /**
     *
     * @param id  power id
     * @return  powerEntity
     */
    PowerEntity getById(Integer id);

    /**
     *
     * @param powerEntity  权限
     * @param roleEntity   角色
     */
    void  bindPowerToRole(PowerEntity powerEntity, RoleEntity roleEntity);

    /**
     *
     * @param powerEntity 权限
     * @param roleEntity  角色
     */
    void  unbindPowerFromRole(PowerEntity powerEntity,RoleEntity roleEntity);

    PowerEntity[] getAllPowerOfMenu(String url);

    PowerEntity getByDesc(String desc);
}
