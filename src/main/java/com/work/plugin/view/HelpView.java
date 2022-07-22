package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by Administrator on 2021/9/8.
 */
@RequiredArgsConstructor
public class HelpView extends JiraWebActionSupport {
    private final WebResourceManager webResourceManager;

    @Override
    public String execute() throws Exception {

        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:wk-org-helpView-resources");
        return super.execute();
    }
}


