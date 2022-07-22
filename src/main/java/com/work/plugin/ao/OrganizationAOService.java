package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface OrganizationAOService {

    OrganizationEntity get(int id);

    OrganizationEntity[] all();

    OrganizationEntity create(String function1,String function2,String menuNameid);

    OrganizationEntity update(int id,String function1,String function2,String menuNameid);

    boolean isExistpermissionMgr(String menuId);
   
    void delete(int id);

    /**
     *
     * @param menuNameId 菜单名字(缩写)
     * @return  菜单实例
     */
    OrganizationEntity get(String menuNameId);

    void mainPowerAndMenu(PowerEntity powerEntity, OrganizationEntity organizationEntity);
}
