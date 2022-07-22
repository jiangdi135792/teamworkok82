package com.work.plugin.customfield;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Objects;

@RequiredArgsConstructor
public class OrganizationCustomFieldServiceImpl implements OrganizationCustomFieldService {

    private final CustomFieldManager customFieldManager;

    public CustomField getOrganizationCustomField() {
        final CustomField customField = customFieldManager.getCustomFieldObjectByName("Organization");

        if (Objects.isNull(customField))
            return createScoreCustomField();

         return customField;
    }

    private CustomField createScoreCustomField() {
        try {
            return customFieldManager.createCustomField("Organization",
                    "",
                    customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:labels"),
                    customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:labelsearcher"),
                    Lists.newArrayList(GlobalIssueContext.getInstance()),
                    Lists.newArrayList((IssueType) null));

        } catch (GenericEntityException e) {
            e.printStackTrace();
            return null;
        }
    }
}