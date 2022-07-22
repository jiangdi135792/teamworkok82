package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import lombok.RequiredArgsConstructor;

/**
 * Created by Administrator on 2021/7/11.
 */
@RequiredArgsConstructor
public class ViewOrganization extends JiraWebActionSupport {
    private final WebResourceManager webResourceManager;

    @Override
    public String execute() throws Exception {
        webResourceManager.requireResourcesForContext("com.work.plugin.teamwork:wk-org-OrgView-resources");
        return super.execute();
    }

}
