package com.work.plugin.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 必须值验证
 *
 * @author Masato Morita
 */
public class RequiredValidator implements Validator
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
        if (StringUtils.isEmpty(value)){
            return ImmutablePair.of(null, new ValidatorException("value is invalid."));
        }
        return ImmutablePair.of(true, null);
    }
}
