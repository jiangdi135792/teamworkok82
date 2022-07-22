package com.work.plugin.ao;

import net.java.ao.Entity;

/**
 * 团队 项目 成员 映射关系实体表
 * Created by Administrator on 2022/3/8.
 */
public interface TeamProjectMember extends Entity {
    Integer getTeamId();

    void setTeamId(Integer teamId);

    Integer getProjectId();

    void setProjectId(Integer projectId);

    String getJiraUserKey();

    void setJiraUserKey(String jiraUserKey);

    Integer getEmployeeId();

    void setEmployeeId(Integer employeeId);

    boolean isMapped();

    void setMapped(boolean mapped);

    enum COLUMN {
        TEAM_ID, PROJECT_ID;

        COLUMN() {
        }
    }

}
