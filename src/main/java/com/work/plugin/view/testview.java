package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by work on 2022/3/28.
 */
@RequiredArgsConstructor
public class testview  extends JiraWebActionSupport{
    private final WebResourceManager webResourceManager;
    @Override
    public String execute() throws Exception {
        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:authManage-scheduler-resources");
        return super.execute();
    }
}
