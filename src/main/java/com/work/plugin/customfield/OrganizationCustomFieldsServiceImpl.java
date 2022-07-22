package com.work.plugin.customfield;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
@RequiredArgsConstructor
public class OrganizationCustomFieldsServiceImpl implements OrganizationCustomFieldsService {

    private final CustomFieldManager customFieldManager;

    public List<CustomField> getOrganizationCustomFields(String typePrefix) {
        List<CustomField>  lstCustomField=new ArrayList<CustomField>() {
        };
        {//Organization
            final CustomField customField = customFieldManager.getCustomFieldObjectByName(typePrefix + "Organization");
            if (Objects.isNull(customField)) {
                CustomField cf = createScoreCustomFields(typePrefix+ "Organization");
                lstCustomField.add(cf);

            } else {
                lstCustomField.add(customField);
            }
        }
        {//Department
            final CustomField customField = customFieldManager.getCustomFieldObjectByName(typePrefix + "Department");
            if (Objects.isNull(customField)) {
                CustomField cf = createScoreCustomFields(typePrefix+ "Department");
                lstCustomField.add(cf);

            } else {
                lstCustomField.add(customField);
            }
        }
        {//OrganizationPath
            final CustomField customField = customFieldManager.getCustomFieldObjectByName(typePrefix + "OrganizationPath");
            if (Objects.isNull(customField)) {
                CustomField cf = createScoreCustomFields(typePrefix + "OrganizationPath");
                lstCustomField.add(cf);

            } else {
                lstCustomField.add(customField);
            }
        }
        {//StaffName
            final CustomField customField = customFieldManager.getCustomFieldObjectByName(typePrefix + "StaffName");
            if (Objects.isNull(customField)) {
                CustomField cf = createScoreCustomFields(typePrefix+ "StaffName");
                lstCustomField.add(cf);

            } else {
                lstCustomField.add(customField);
            }
        }

        {//StaffName
            final CustomField customField = customFieldManager.getCustomFieldObjectByName(typePrefix + "OrganizationId");
            if (Objects.isNull(customField)) {
                CustomField cf = createScoreCustomFields(typePrefix+ "OrganizationId");
                lstCustomField.add(cf);

            } else {
                lstCustomField.add(customField);
            }
        }

        {//StaffId
            final CustomField customField = customFieldManager.getCustomFieldObjectByName(typePrefix + "StaffId");
            if (Objects.isNull(customField)) {
                CustomField cf = createScoreCustomFields(typePrefix+ "StaffId");
                lstCustomField.add(cf);

            } else {
                lstCustomField.add(customField);
            }
        }

        {//StaffName
            final CustomField customField = customFieldManager.getCustomFieldObjectByName(typePrefix + "DepartmentId");
            if (Objects.isNull(customField)) {
                CustomField cf = createScoreCustomFields(typePrefix+ "DepartmentId");
                lstCustomField.add(cf);

            } else {
                lstCustomField.add(customField);
            }
        }
        return lstCustomField;
    }

    private CustomField createScoreCustomFields(String prefixAndField) {
        try {
            return customFieldManager.createCustomField(prefixAndField,
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