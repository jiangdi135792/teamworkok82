package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Table("MENU_MEMBER_V1")
public interface MemberEntity extends Entity {
    @NotNull
    OrganizationEntity getOrganization();


    @StringLength(50)
    String getMenuId();
    void setMenuId(String menuId);

    int getType();
    void setType(int type);

    void setOrganization(OrganizationEntity organization);
    String getPowerList();
    void  setPowerList(String powerList);
    @NotNull
    String getUserKey();

    enum COLUMN {
        ID, ORGANIZATION_ID, USER_KEY,MENU_ID,TYPE;

        COLUMN() {}
    }
}
