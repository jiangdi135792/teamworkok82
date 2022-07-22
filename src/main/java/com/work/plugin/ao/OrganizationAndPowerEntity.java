package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by work on 2022/4/23.
 */
@Table("MENU_DETAIL_POWER")
public interface OrganizationAndPowerEntity extends Entity {
    OrganizationEntity getOrganization();
    void setOrganization(OrganizationEntity organization);
    PowerEntity getPower();
    void setPower(PowerEntity power);
}
