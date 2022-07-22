package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by work on 2021/6/22.
 */



@RequiredArgsConstructor
public class DepartmentView extends JiraWebActionSupport {

    private final WebResourceManager webResourceManager;

    @Override
    public String execute() throws Exception {
        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:wk-org-department-resources");
        return super.execute();
    }
}
