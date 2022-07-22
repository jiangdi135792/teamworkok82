package com.work.plugin.validator;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 组织类型值验证
 *
 * @author Masato Morita
 */
public class OrganizeTypeValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Boolean, ValidatorException> validate(@Nullable String value)
    {
        if (Objects.isNull(value))
            return ImmutablePair.of(null, new ValidatorException("value is null."));

        if (! ImmutableList.of("0", "1", "2", "9").contains(value))
            return ImmutablePair.of(null, new ValidatorException("value is invalid."));

        return ImmutablePair.of(true, null);
    }
}
