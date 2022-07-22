package com.work.plugin.validator;

import com.atlassian.jira.user.util.UserManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Atlassian用户名验证
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class UserNameValidator implements Validator
{
    /**
     * cited from ccom.atlassian.jira.bc.user.UserValidationHelper package
     */
    private final char[] INVALID_USERNAME_CHARS = new char[]{'<', '>', '&'};
    private final UserManager userManager;

    @Override
    public Pair<Boolean, ValidatorException> validate(@Nullable String value)
    {
        if (Objects.isNull(value))
            return ImmutablePair.of(null, new ValidatorException("value is null."));

        // cited from ccom.atlassian.jira.bc.user.UserValidationHelper.validateUsernamePolicy method
        if (StringUtils.containsAny(value, INVALID_USERNAME_CHARS))
            return ImmutablePair.of(null, new ValidatorException("value is invalid."));

        // cited from ccom.atlassian.jira.bc.user.UserValidationHelper.usernameDoesNotExist method
        if (Objects.nonNull(userManager.getUserByName(value)))
            return ImmutablePair.of(null, new ValidatorException("value already exist."));

        return ImmutablePair.of(true, null);
    }
}
