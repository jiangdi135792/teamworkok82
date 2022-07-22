package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by work on 2022/2/3.
 */
@Table("ldap_synctime")
public interface LdapSyncTime extends Entity {
    long getDirectoryId();
    void setDirectoryId(long directoryId);
    int getTime();
    void setTime(int time);
}
