package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;

import java.util.Objects;

/**
 * Created by work on 2022/2/3.
 */
public class LdapSyncTimeServiceImpl implements LdapSyncTimeService {
    private final ActiveObjects ao;

    public LdapSyncTimeServiceImpl(ActiveObjects ao) {
        this.ao=ao;
    }

    @Override
    public void delete(long directoryId) {
        ao.delete(this.getDirectory(directoryId));
    }

    @Override
    public int getTime(long directoryId) {
        int time=-1;
        LdapSyncTime[] ldapSyncTimes=ao.find(LdapSyncTime.class, String.format(" DIRECTORY_ID = '%d'",directoryId));
        if (ldapSyncTimes == null) {
            return time;
        } else {
            for (LdapSyncTime ldapSyncTime : ldapSyncTimes) {
                time=ldapSyncTime.getTime();
            }
            return time;
        }

    }

    @Override
    public LdapSyncTime getDirectory(long directoryId) {
        LdapSyncTime[] ldapSyncTimes=ao.find(LdapSyncTime.class, String.format(" DIRECTORY_ID  = '%d' ", directoryId));
        for (LdapSyncTime ldapSyncTime:ldapSyncTimes){
            return ldapSyncTime;
        }
        return null;
    }

    @Override
    public LdapSyncTime create(long directoryId,int time) {
        LdapSyncTime ldapSyncTime1=ao.create(LdapSyncTime.class);
        ldapSyncTime1.setDirectoryId(directoryId);
        if (time==0){
            time=1;
        }
        ldapSyncTime1.setTime(time);
        ldapSyncTime1.save();
        return ldapSyncTime1;
    }

    @Override
    public LdapSyncTime update(LdapSyncTime model) {
        LdapSyncTime[] ldapSyncTimes=ao.find(LdapSyncTime.class, String.format(" DIRECTORY_ID = '%d' ", model.getDirectoryId()));
        for (LdapSyncTime ldapSyncTime : ldapSyncTimes) {
            ldapSyncTime.setTime(Objects.nonNull(model.getTime()) ? model.getTime() : ldapSyncTime.getTime());
            ldapSyncTime.save();
            return ldapSyncTime;
        }
        return null;
    }
}
