package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by work on 2022/3/15.
 */
@Transactional
public interface RoleService {
    RoleEntity getId(Integer Id);
    List<RoleEntity> getAll();

    /**
     * 获取团队的角色（type = 2�?
     * @return
     */
    List<RoleEntity> getTeamRoles();
    String getPowerByRoleId(Integer id);

    /**
     *
     * @param name  role's  name
     * @param type  role's  type
     * @param desc  role's  description
     * @param order  role's weight
     * @return
     */
    RoleEntity creatRole(String name,Integer type,String desc,Integer order);
    void  maintainRelationshipRoleAndPower(RoleEntity roleEntity, PowerEntity powerEntity);

    /**
     *
     * @param roleEntity  角色
     * @param menu  菜单
     */
    void  setMenuPower(RoleEntity roleEntity, OrganizationEntity menu);
    RoleEntity[] getOrgParentRole(String loginUserName);

    RoleEntity[] getAllDepRole();

    /**
     *
     * @param roleName  角色名字
     * @return 菜单权限列表
     */
    List<String> getMenuPowerByRoleId(String roleName);

    /**
     *
     * @param roleName  角色名字
     * @param menuName  菜单名字
     * @param status  true 添加权限  false  删除 权限
     */
    void updateMenuPower(String roleName,String menuName,boolean status);

    void setRoleSuperior(RoleEntity roleEntity,RoleEntity superiorRole);

    /**
     *
     * @param roleEntity 角色
     * @return  上級角色
     */
    List<RoleEntity> getSuperByRole(RoleEntity roleEntity);

    /**
     *
     * @param roleEntity 角色
     * @return 下级角色
     */
    List<RoleEntity> getLowerByRole(RoleEntity roleEntity);

    void updateRoles(List<Integer> roleIds, Integer teamId, Integer employeeId);
    Integer updateRole(Integer roleId, Integer teamId, Integer employeeId,Integer teamemployeeId);

    /**
     * 根据employeeid获取他所属角色下面的的成�?
     * @return
     */
    List<StrEmployeeOfRole> getRoleByEmployeeId(Integer employeeId);


    RoleEntity getDefaultTeamRole();
    void  setRoleToEmployee(RoleEntity roleEntity, StrEmployee strEmployee);

    /**
     *
     * @param name  角色名字
     * @param desc  角色描述
     * @param order  权重
     * @param type  类型
     * @return   创建的角�?
     */
    RoleEntity creatNewRole(String name,String desc,Integer order,Integer type);

    /**
     *
     * @param roleName 角色名字
     * @param type 角色类型
     */
    boolean delRole(String roleName,Integer type);

    MemberEntity getMenuDetailPower(String roleName, String menuName);

    /**
     *
     * @param roleEntity 角色
     * @param organizationEntity  菜单
     * @param map 权限键值对
     * @param ulo  u owner -1   l lower -2   o other  -3
     */
    void setOrgRoleMenuPower(RoleEntity roleEntity, OrganizationEntity organizationEntity, Map<PowerEntity,Boolean> map,Integer ulo);

    /**
     *
     * @param role    角色�?
     * @param menuName  菜单�?
     * @param str   权限字符�?
     */
    void  changeDetailPower(String role,String menuName,String str);

    void  parseMenuDetailPower(String role,String menuName);

    /**
     * 初始化 给定菜单权限 默认 此菜单下 的权限 为无
     * @param organizationEntity  菜单对象
     * @return
     */
    Map<PowerEntity,Boolean> initMenuPower(OrganizationEntity organizationEntity);

    /**
     *  获取 角色 对象 根据 角色名字
     * @param roleName  角色名
     * @return
     */
    RoleEntity getRoleByName(String roleName);

    /**
     * 删除 此角色的相应菜单下的具体权限
     * @param roleEntity
     * @param organizationEntity
     */
    void  delOrgRoleMenuPower(RoleEntity roleEntity,OrganizationEntity organizationEntity);

    void createProjectRoleOfJiraInnerByCustomRole(RoleEntity roleEntity);

    void deleteProjectRoleOfJiraInnerByCustomRole(RoleEntity roleEntity);

    /**
     *   删除多余的初始化角色 （新角色 存在数据库中的不能重复，国际化做好 就没有冲突）
     * @param strings  角色名字
     */
    void maintainData(String... strings);

}
