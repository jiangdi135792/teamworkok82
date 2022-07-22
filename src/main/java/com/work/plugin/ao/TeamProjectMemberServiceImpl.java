package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import lombok.AllArgsConstructor;

/**
 * Created by Administrator on 2022/5/2.
 */
@AllArgsConstructor
public class TeamProjectMemberServiceImpl implements TeamProjectMemberService {

    private final ActiveObjects ao;

    @Override
    public TeamProjectMember save(Integer teamId, Integer projectId, String jiraUserKey,
                                  Integer employeeId, boolean isMapped) {
        TeamProjectMember teamProjectMember = ao.create(TeamProjectMember.class);
        teamProjectMember.setEmployeeId(employeeId);
        teamProjectMember.setMapped(isMapped);
        teamProjectMember.setTeamId(teamId);
        teamProjectMember.setProjectId(projectId);
        teamProjectMember.setJiraUserKey(jiraUserKey);
        teamProjectMember.save();

        return teamProjectMember;
    }

    @Override
    public boolean isExist(Integer teamId, Integer projectId, String jiraUserKey) {
        int count = ao.count(TeamProjectMember.class,
                "TEAM_ID = ? AND PROJECT_ID = ? AND JIRA_USER_KEY = ? AND MAPPED = false",
                teamId, projectId, jiraUserKey
        );

        return count > 0 ? true : false;
    }


    /**
     * 查询团队项目未映射的成员
     * @param teamId
     */
    public TeamProjectMember[] getUnMappedMemberByTeamId(Integer teamId) {
        TeamProjectMember[] teamProjectMembers = ao.find(TeamProjectMember.class,
                "TEAM_ID = ? AND MAPPED = false",
                teamId);
        return teamProjectMembers;
    }
}
