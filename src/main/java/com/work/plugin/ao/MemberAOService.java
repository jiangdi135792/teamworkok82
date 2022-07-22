package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;

@Transactional
public interface MemberAOService {

    Set<StrOrganize> getBy(String user);

    Pair<MemberEntity, Map<String, String>> create(int organization, String userKey,String menuId,int
                                                    type);

    void delete(int id);

    boolean isExistMenu(String userkey,String menuId);
    MemberEntity[] getRoleList(String mMenuID);
    MemberEntity  getByRoleAndMenu(String roleName ,String menuName);

    MemberEntity[] getMemberByOrganization(OrganizationEntity organizationEntity);
}
