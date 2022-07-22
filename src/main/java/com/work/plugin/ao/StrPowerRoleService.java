package com.work.plugin.ao;

import java.util.List;

/**
 * Created by work on 2022/7/16.
 */
public interface StrPowerRoleService {
    /**
     *  建立（项目）角色 权限关系
     * @param roleEntity
     * @param powerEntity
     */
    void  createRelationOfPowerAndRole(RoleEntity roleEntity, PowerEntity powerEntity);

    /**
     *  删除（项目）角色 权限关系
     * @param roleEntity
     * @param powerEntity
     */
    void  delRelationOfPowerAndRole(RoleEntity roleEntity,PowerEntity powerEntity);

    /**
     *
     * @param integer  项目角色id
     * @return
     */
    List<PowerEntity> getAllPowerOfSelf(Integer integer);

    /**
     *
     * @return
     */
    List<PowerEntity> getAllPowewr();
}
