package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import lombok.RequiredArgsConstructor;

/**
 * Created by admin on 2021/7/4.
 */
@RequiredArgsConstructor
public class SetReportView extends JiraWebActionSupport {

    private final PageBuilderService pageBuilderService;

    @Override
    public String execute() throws Exception {
        pageBuilderService.assembler().resources()
                .requireWebResource("com.work.plugin.teamwork:jquery-easy-ui-resource")
                .requireWebResource("com.work.plugin.teamwork:wk-org-setReport-resources");
        return super.execute();
    }

}
