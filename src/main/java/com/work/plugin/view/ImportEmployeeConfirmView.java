package com.work.plugin.view;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.work.plugin.imports.EmployeeValidationController;
import lombok.RequiredArgsConstructor;
import webwork.action.ServletActionContext;
import webwork.multipart.MultiPartRequestWrapper;

import java.io.File;
import java.util.Map;

/**
 * 导入确认动作
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class ImportEmployeeConfirmView extends JiraWebActionSupport
{
    private final EmployeeValidationController validationController;
    private Map<Integer, Map<Integer,String>> messages;
    private String filePath;

    @Override
    public String execute() throws Exception
    {
        if (! ServletActionContext.getRequest().getMethod().equals("POST"))
            return "invalid";

        MultiPartRequestWrapper requestWrapper = ServletActionContext.getMultiPartRequest();
        if (requestWrapper == null)
            return "invalid";
        String file1 = requestWrapper.getFilesystemName("file");
        String substring =file1.substring(file1.lastIndexOf("."));
        if (org.apache.commons.lang3.StringUtils.isNotBlank(substring)){
            boolean b = ".csv".equalsIgnoreCase(substring);
            if (b){
                File file = requestWrapper.getFile("file");
                filePath = file.toString();
                messages = validationController.execute(filePath);
                return messages==null?"invalid":SUCCESS;
            }else {
                return "invalid";
            }
        }else {
        return "invalid";
        }
    }
    public Map<Integer, Map<Integer,String>> getMessages()
    {
        return messages;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String getResultUrl()
    {
        return "ImportEmployeeResultView.jspa";
    }
}
