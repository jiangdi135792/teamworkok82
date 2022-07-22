package com.work.plugin.ao;

/**
 * Created by Administrator on 2022/3/8.
 */
public interface TeamProjectMemberService {
    TeamProjectMember save(Integer teamId, Integer projectId, String jiraUserKey,
                           Integer employeeId, boolean isMapped);

    boolean isExist(Integer teamId, Integer projectId, String jiraUserKey);

    TeamProjectMember[] getUnMappedMemberByTeamId(Integer teamId);
}
