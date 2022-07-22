package web.condition;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.ApplicationProperties;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.xerces.impl.xs.identity.Field;
//import java.util.regex.Pattern;
//import com.sun.org.apache.xerces.internal.impl.xs.identity.Field;
//import org.apache.xerces.impl.xs.identity.Field;
//import com.sun.org.apache.xerces.internal.impl.xs.identity.Field;

public class IsPriorToJiraVersion implements Condition {

    private int maxMajorVersion;
    private int maxMinorVersion;
    private int majorVersion;
    private int minorVersion;

    public IsPriorToJiraVersion(ApplicationProperties applicationProperties) {
        String versionString = applicationProperties.getVersion();
        String versionRegex = "^(\\d+)\\.(\\d+)";
        Pattern versionPattern = Pattern.compile(versionRegex);
        Matcher versionMatcher = versionPattern.matcher(versionString);
        versionMatcher.find();
        majorVersion = Integer.decode(versionMatcher.group(1));
        minorVersion = Integer.decode(versionMatcher.group(2));
    }

    public void init(final Map<String, String> paramMap) throws PluginParseException {
        maxMajorVersion = Integer.decode(paramMap.get("majorVersion"));
        maxMinorVersion = Integer.decode(paramMap.get("minorVersion"));
    }

    public boolean shouldDisplay(final Map<String, Object> context) {
        return (majorVersion < maxMajorVersion) || (majorVersion == maxMajorVersion) && (minorVersion < maxMinorVersion);
    }
}