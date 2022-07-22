package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by admin on 2021/9/3.
 */
@RequiredArgsConstructor
public class ShowOrgTreeView extends JiraWebActionSupport {
    private final WebResourceManager webResourceManager;

    @Override
    public String execute() throws Exception {
        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:show-org-tree");
        return super.execute();
    }
}
