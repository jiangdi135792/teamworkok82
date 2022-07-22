package com.work.plugin.customfield;

import com.atlassian.jira.issue.fields.CustomField;

import java.util.List;

public interface OrganizationCustomFieldsService {
    List<CustomField> getOrganizationCustomFields(String typePrefix);
}
