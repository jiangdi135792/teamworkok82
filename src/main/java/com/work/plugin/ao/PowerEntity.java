package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.Table;

/**
 * Created by work on 2022/3/15.
 */
@Table("POWER_ENTITY")
public interface PowerEntity extends Entity {
    String getUrl();
    void  setUrl(String url);
    String getDescription();
    void  setDescription(String description);
    Boolean getOperation();
    void setOperation(Boolean bool);
    Integer getPowerOfType();
    void  setPowerOfType(Integer type);
    @OneToMany
    StrPowerRole[] getStrPowerRole();
}
