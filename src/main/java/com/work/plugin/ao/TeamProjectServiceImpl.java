package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.java.ao.DBParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2022/3/8.
 */
@AllArgsConstructor
public class TeamProjectServiceImpl implements TeamProjectService {

    private final ActiveObjects ao;

    @Override
    public TeamProjectEntity[] getAll() {
        TeamProjectEntity[] teamProjectEntities = ao.find(TeamProjectEntity.class);
        return teamProjectEntities;
    }

    @Override
    public TeamProjectEntity save(Integer teamId, Integer projectId) {

        TeamProjectEntity[] teamProjectEntities = ao.find(TeamProjectEntity.class,
                String.format("%s = ?", TeamProjectEntity.COLUMN.TEAM_ID),
                teamId);
        TeamProjectEntity teamProjectEntity;
        if (teamProjectEntities.length == 1) {
            teamProjectEntity = teamProjectEntities[0];
            teamProjectEntity.setProjectId(projectId);
            teamProjectEntity.save();
        } else {
            if (teamProjectEntities.length > 1) { //  有冗余数据，全部删除
                Arrays.stream(teamProjectEntities).forEach(entity -> ao.delete(entity));
            }

            teamProjectEntity = ao.create(TeamProjectEntity.class,
                    new DBParam(TeamProjectEntity.COLUMN.PROJECT_ID.name(), projectId),
                    new DBParam(TeamProjectEntity.COLUMN.TEAM_ID.name(), teamId)
            );
            teamProjectEntity.save();
        }




        return teamProjectEntity;
    }

    @Override
    public int count(Integer teamId, Integer projectId) {
        int count = ao.count(
                TeamProjectEntity.class,
                String.format("%s = ? AND %s = ?",
                        TeamProjectEntity.COLUMN.PROJECT_ID,
                        TeamProjectEntity.COLUMN.TEAM_ID
                ),
                projectId,
                teamId
        );
        return count;
    }

    @Override
    public TeamProjectEntity[] getByTeamId(Integer teamId) {
        TeamProjectEntity[] teamProjectEntities = ao.find(TeamProjectEntity.class,
                String.format("%s = ?", TeamProjectEntity.COLUMN.TEAM_ID),
                teamId);

        return teamProjectEntities;
    }

    @Override
    public void save(Integer teamId, List<Integer> projectIds) {
        TeamProjectEntity[] teamProjectEntities = ao.find(TeamProjectEntity.class, String.format("%s = ?", TeamProjectEntity.COLUMN.TEAM_ID),
                teamId);
        ArrayList<TeamProjectEntity> teamProjectEntityArrayList = Lists.newArrayList(teamProjectEntities);
        List<Integer> projectIdRecord = teamProjectEntityArrayList.stream().
                map(teamProjectEntity -> teamProjectEntity.getProjectId()).collect(Collectors.toList());

        // newValue[] - oldValue[] => 添加
        List<Integer> waitToAddProjectIds =
                projectIds.stream().filter(projectId -> !projectIdRecord.contains(projectId)).collect(Collectors.toList());
        waitToAddProjectIds.forEach(projectId -> {
            TeamProjectEntity teamProjectEntity = ao.create(TeamProjectEntity.class);
            teamProjectEntity.setTeamId(teamId);
            teamProjectEntity.setProjectId(projectId);
            teamProjectEntity.save();
        });
        // oldValue[] - newValue[] => 删除
        List<Integer> waitToDelProjectIds =
                projectIdRecord.stream().filter(projectid -> !projectIds.contains(projectid)).collect(Collectors.toList());
        waitToDelProjectIds.forEach(projectId -> {
            ao.deleteWithSQL(TeamProjectEntity.class,
                    String.format("%s = ? AND %s = ?", TeamProjectEntity.COLUMN.TEAM_ID, TeamProjectEntity.COLUMN.PROJECT_ID),
                    teamId, projectId);
        });
    }
}
