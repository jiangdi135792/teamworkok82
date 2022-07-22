package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by admin on 2021/6/28.
 */
@RequiredArgsConstructor
public class StrEmployeeView extends JiraWebActionSupport {

    private final WebResourceManager webResourceManager;

    @Override
    public String execute() throws Exception {

        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:wk-org-strEmployee-resources");
        return super.execute();
    }

}
