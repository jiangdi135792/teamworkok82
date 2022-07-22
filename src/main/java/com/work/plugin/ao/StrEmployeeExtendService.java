package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;

/**
 * Created by work on 2022/1/24.
 */
@Transactional
public interface StrEmployeeExtendService {
    void updateInfo(long directoryId);
    long getHashCode(String loginName);
    String getUniqueCode(int employeeId);
    void  delDirectoryInfo(long directoryId);
    void create(Long hash,int Employeeid,String updateTime,String ExsitJiraUserKey,String objectGUID);
}
