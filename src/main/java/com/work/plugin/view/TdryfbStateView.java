package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by admin on 2021/7/4.
 */
@RequiredArgsConstructor
public class TdryfbStateView extends JiraWebActionSupport {

    private final WebResourceManager webResourceManager;

    @Override
    public String execute() throws Exception {

        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:wk-org-tdryfbState-resources");
        return super.execute();
    }

}
