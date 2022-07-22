package com.work.plugin.util.license.license;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

/**
 * License Valid Condition
 *
 * @author Masato Morita
 * @since 1.0.0
 */

public class LicenseValidCondition extends AbstractWebCondition
{
    private final LicenseService licenseService;

    public LicenseValidCondition(LicenseService licenseService)
    {
        this.licenseService = licenseService;
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper)
    {
      //    return licenseService.hasValidLicense();
      return true;
    }
}
