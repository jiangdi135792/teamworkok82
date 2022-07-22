package com.work.plugin.workflow.function;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import com.work.plugin.ao.DepartmentAOService;
import com.work.plugin.ao.StrEmployee;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Created by admin on 2021/9/19.
 */
@RequiredArgsConstructor
public class AssigneeIssueToDepartmentFunction extends AbstractJiraFunctionProvider {

    private static final String REAL_DEPT_ID = "realDeptId";

    private final DepartmentAOService departmentAOService;

    private final UserManager userManager;


    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);
        String realDeptId = (String) args.get(REAL_DEPT_ID);

        StrEmployee dutyPersonByDeptId = departmentAOService.getDutyPersonByDeptId(realDeptId);

        String jiraUserKey = dutyPersonByDeptId.getJiraUserKey();

        ApplicationUser userByKey = userManager.getUserByKey(jiraUserKey);

        issue.setAssignee(userByKey);

    }
}
