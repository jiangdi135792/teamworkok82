package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by work on 2022/6/10.
 */
@RequiredArgsConstructor
public class LowerIssuesView extends JiraWebActionSupport {
    private final WebResourceManager webResourceManager;

    @Override
    public String execute() throws Exception {
        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:wk-org-lowerissues-resources");
        return super.execute();
    }
}
