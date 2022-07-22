package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;

@Table("MENU_RESOURE_V1")
public interface OrganizationEntity extends Entity {

    String getFunction1();

    void setFunction1(String function1 );

    @NotNull
    @Unique
    String getFunction2();

    void setFunction2(String function2);

    String getMenuNameid();
    void setMenuNameid(String menuNameid);

    @OneToMany
    MemberEntity[] getMembers();

    @OneToMany
    OrganizationAndPowerEntity[] getOrganizationAndPowerEntity();
    enum COLUMN {
        ID, FUNCTION1,FUNCTION2,MENU_NAMEID;

        COLUMN() {}
     }
}
