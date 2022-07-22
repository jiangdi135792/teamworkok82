package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;

/**
 * 团队 项目 映射关系实体表
 * Created by Administrator on 2022/3/8.
 */
public interface TeamProjectEntity extends Entity {
    @NotNull
    Integer getTeamId();

    void setTeamId(Integer teamId);
    @NotNull
    Integer getProjectId();

    void setProjectId(Integer projectId);

//    String getProjectKey();
//
//    void setProjectKey(String projectKey);


    enum COLUMN {
        ID, TEAM_ID, PROJECT_ID;

        COLUMN() {
        }
    }

}
