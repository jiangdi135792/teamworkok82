package com.work.plugin.workflow.function.factory;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.work.plugin.ao.DepartmentEntity;
import lombok.RequiredArgsConstructor;
import webwork.action.ActionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2021/9/18.
 */
@RequiredArgsConstructor
public class AssigneeIssueToDepartmentFunctionFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {
    private static final String REAL_DEPT_ID = "realDeptId";

    final private WorkflowManager workflowManager;

    private final ActiveObjects ao;

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> map) {
        Map<String, String[]> myParams = ActionContext.getParameters();
        map.put(REAL_DEPT_ID, myParams.get("realDeptId"));
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {
        getVelocityParamsForInput(map);
        getVelocityParamsForView(map, abstractDescriptor);
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> map, AbstractDescriptor abstractDescriptor) {

        if (!(abstractDescriptor instanceof FunctionDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) abstractDescriptor;
        String message = (String) functionDescriptor.getArgs().get(REAL_DEPT_ID);

        if (message == null) {
            message = "No Message";
        } else {
            DepartmentEntity departmentEntity = ao.find(DepartmentEntity.class, String.format("ID = '%s' ", message))[0];
            message = departmentEntity.getGroupName();
        }

        map.put(REAL_DEPT_ID, message);
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> map) {
        Map params = new HashMap();

        // Process The map
        String message = extractSingleParam(map, REAL_DEPT_ID);
        params.put(REAL_DEPT_ID, message);

        return params;
    }
}
