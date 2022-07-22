package web.contextproviders;


import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.ApplicationProperties;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.xerces.impl.xs.identity.Field;
//import java.util.regex.Pattern;
//import com.sun.org.apache.xerces.internal.impl.xs.identity.Field;
//import org.apache.xerces.impl.xs.identity.Field;
//import com.sun.org.apache.xerces.internal.impl.xs.identity.Field;

public class GetSessionInfo extends AbstractJiraContextProvider  {

    private String mUser;
    private String mGroup;
    private int majorVersion;
    private int minorVersion;

    public GetSessionInfo(ApplicationProperties applicationProperties) {

        String versionString = applicationProperties.getVersion();
        String versionRegex = "^(\\d+)\\.(\\d+)";
        Pattern versionPattern = Pattern.compile(versionRegex);
        Matcher versionMatcher = versionPattern.matcher(versionString);
        versionMatcher.find();
        majorVersion = Integer.decode(versionMatcher.group(1));
        minorVersion = Integer.decode(versionMatcher.group(2));


    }
    /*
    public Map getContextMap(GadgetRequestContext.User user, JiraHelper jiraHelper)
    {
        int historyIssues = 0;
        if (jiraHelper != null && jiraHelper.getRequest() != null)
        {
            UserHistory history = (UserHistory) jiraHelper.getRequest().getSession().getAttribute(SessionKeys.USER_ISSUE_HISTORY);
            if (history != null)
            {
                historyIssues = history.getIssues().size();
            }
        }
        int logoHeight = TextUtils.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT));
        String historyHeight = String.valueOf(80 + logoHeight + (25 * historyIssues));
        String filterHeight = String.valueOf(205 + logoHeight);
        return EasyMap.build("historyWindowHeight", historyHeight,
                "filtersWindowHeight", filterHeight);
    }

    public void init(final Map<String, String> paramMap) throws PluginParseException {
        mUser = paramMap.get("user");
        mGroup = paramMap.get("group");
    }
*/



    public Map getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        String history = (String) jiraHelper.getRequest().getSession().getValue("user");
        String historyHeight="122";
        String filterHeight ="34";
        return EasyMap.build("historyWindowHeight", historyHeight,
                "filtersWindowHeight", filterHeight);

    }
}