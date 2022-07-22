package com.work.plugin.ao;

import java.util.List;

/**
 * Created by Administrator on 2022/3/8.
 */
public interface TeamProjectService {
    TeamProjectEntity[] getAll();

    TeamProjectEntity save(Integer teamId, Integer projectId);

    int count(Integer teamId, Integer projectId);

    TeamProjectEntity[] getByTeamId(Integer teamId);

    void save(Integer teamId, List<Integer> projectKeys);
}
