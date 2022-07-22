package com.work.plugin.report;

import com.atlassian.activeobjects.tx.Transactional;

import java.util.List;

/**
 * Created by admin on 2022/3/28.
 */
@Transactional
public interface GetOrgTreeService {

    List<ComboTreeModel> getAllTreeData();

    List<ComboTreeModel> getTeamTreeData(String proname);

}
