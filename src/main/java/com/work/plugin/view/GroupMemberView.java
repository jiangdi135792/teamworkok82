package com.work.plugin.view;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.work.plugin.ao.RoleEntity;
import com.work.plugin.ao.RoleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Created by admin on 2021/6/30.
 */
@RequiredArgsConstructor
public class GroupMemberView extends JiraWebActionSupport {
    private final PageBuilderService pageBuilderService;
    private final RoleService roleService;

    @Override
    public String execute() throws Exception {
        pageBuilderService.assembler().resources()
                .requireWebResource("com.work.plugin.teamwork:wk-org-gm-resources")
                .requireWebResource("com.work.plugin.teamwork:bootstrap-datepicker")
                .requireWebResource("com.work.plugin.teamwork:bootstrap-validator");
        return super.execute();
    }

    /**
     * 团队默认的角色
     */
    public Object getDefaultRole() throws JSONException {
        RoleEntity defaultTeamRole = roleService.getDefaultTeamRole();

        return ObjectUtils.defaultIfNull(defaultTeamRole, new Object());
    }
}
