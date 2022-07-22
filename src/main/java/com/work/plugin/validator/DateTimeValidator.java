package com.work.plugin.validator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 日期形式值验证
 *
 * @author Masato Morita
 */
public class DateTimeValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Boolean, ValidatorException> validate(@Nullable String value)
    {
        if (Objects.isNull(value))
            return ImmutablePair.of(null, new ValidatorException("value is null."));

        try
        {
            // 输入值需要ISO8601标准格式
            DateTime.parse(value);
            return ImmutablePair.of(true, null);
        }
        catch (IllegalArgumentException e)
        {
            return ImmutablePair.of(null, new ValidatorException("value is invalid."));
        }
    }
}
