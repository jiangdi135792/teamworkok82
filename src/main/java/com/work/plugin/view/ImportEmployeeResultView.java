package com.work.plugin.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.work.plugin.imports.data.EmployeeDataProcess;
import lombok.RequiredArgsConstructor;
import lombok.val;
import webwork.action.ServletActionContext;

import java.util.Map;
import java.util.stream.Stream;

/**
 * 导入结果动作
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class ImportEmployeeResultView extends JiraWebActionSupport
{
    private final EmployeeDataProcess dataProcess;
    private Map<Integer, String> results;

    @Override
    public String execute() throws Exception
    {
        if (! ServletActionContext.getRequest().getMethod().equals("POST"))
            return "invalid";

        val filePath = ServletActionContext.getRequest().getParameter("filePath");
        val rows = Stream.of(ServletActionContext.getRequest().getParameterValues("rows"))
                .map(Integer::parseInt).toArray(Integer[]::new);

        results = dataProcess.execute(filePath, rows);

        return SUCCESS;
    }

    public Map<Integer, String> getResults()
    {
        return results;
    }
}
