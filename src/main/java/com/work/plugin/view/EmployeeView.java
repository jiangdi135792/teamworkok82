package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by admin on 2021/7/7.
 */
@RequiredArgsConstructor
public class EmployeeView extends JiraWebActionSupport {

    private final WebResourceManager webResourceManager;

    @Override
    public String execute() throws Exception {

        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:wk-org-ViewEmployee-resources");
        return super.execute();
    }

}
