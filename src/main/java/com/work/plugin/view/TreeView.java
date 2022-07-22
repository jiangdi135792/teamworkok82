package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by Administrator on 2021/9/13.
 */
@RequiredArgsConstructor
public class TreeView extends JiraWebActionSupport {

    private final WebResourceManager webResourceManager;

    @Override
    public String execute() throws Exception {
        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:wk-org-TreeView-resources");
        return super.execute();
    }
}
