package com.work.plugin.validator;

import com.opensymphony.util.TextUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 电子邮件地址验证
 *
 * @author Masato Morita
 */
public class EmailValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Boolean, ValidatorException> validate(@Nullable String value)
    {
        if (Objects.isNull(value)){
            return ImmutablePair.of(null, new ValidatorException("value is null."));
        }
        // refer to validateEmailAddress method of com.atlassian.jira.bc.user.UserValidationHelper.Validations
        if (! TextUtils.verifyEmail(value)){
            return ImmutablePair.of(null, new ValidatorException("value is invalid."));
        }
        return ImmutablePair.of(true, null);
    }
}
