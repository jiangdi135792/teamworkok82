package com.work.plugin.ao;


import com.atlassian.activeobjects.tx.Transactional;

/**
 * Created by work on 2022/2/3.
 */
@Transactional
public interface LdapSyncTimeService {
    void delete(long directoryId);
    int getTime(long directoryId);
    LdapSyncTime getDirectory(long directoryId);
    LdapSyncTime create(long directory,int time);
    LdapSyncTime update(LdapSyncTime ldapSyncTime);
}
