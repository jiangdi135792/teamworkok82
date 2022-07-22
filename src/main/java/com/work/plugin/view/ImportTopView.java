package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * 导入首页动作
 *
 * @author Masato Morita
 */
public class ImportTopView extends JiraWebActionSupport
{
    @Override
    public String execute() throws Exception
    {
        return SUCCESS;
    }
}
