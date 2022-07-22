package com.work.plugin.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

/**
 * 数字值验证
 *
 * @author Masato Morita
 */
public class NumberValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Boolean, ValidatorException> validate(@Nullable String value)
    {
        if (StringUtils.isBlank(value))
            return ImmutablePair.of(null, new ValidatorException("value is null."));

        try
        {
            Integer.parseInt(value);
            return ImmutablePair.of(true, null);
        }
        catch (NumberFormatException e)
        {
            return ImmutablePair.of(null, new ValidatorException("value is invalid."));
        }
    }
}
