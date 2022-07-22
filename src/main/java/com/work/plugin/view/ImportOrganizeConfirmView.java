package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.work.plugin.imports.OrganizeValidationController;
import lombok.RequiredArgsConstructor;
import webwork.action.ServletActionContext;
import webwork.multipart.MultiPartRequestWrapper;

import java.util.List;
import java.util.Map;

/**
 * 导入确认动作
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class ImportOrganizeConfirmView extends JiraWebActionSupport
{
    private final OrganizeValidationController validationController;
    private Map<Integer, List<String>> messages;
    private String filePath;

    @Override
    public String execute() throws Exception
    {
        if (! ServletActionContext.getRequest().getMethod().equals("POST"))
            return "invalid";

        MultiPartRequestWrapper requestWrapper = ServletActionContext.getMultiPartRequest();
        if (requestWrapper == null)
            return "invalid";

        filePath = requestWrapper.getFile("file").toString();
        messages = validationController.execute(filePath);

        return SUCCESS;
    }

    public Map<Integer, List<String>> getMessages()
    {
        return messages;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String getResultUrl()
    {
        return "ImportOrganizeResultView.jspa";
    }
}
