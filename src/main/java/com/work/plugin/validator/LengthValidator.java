package com.work.plugin.validator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 文本多小验证
 *
 * @author Masato Morita
 */
public class LengthValidator implements Validator
{
    /**
     *
     * 验证输入值，标准最大多小255文字
     *
     * @param value 可空验证对象值
     * @return 如果验证成功，返回<code>Pair<True, null></code>。如果失败，返回<code>Pair<null, ValidatorException></code>。
     * @author Masato Morita
     */
    @Override
    public Pair<Boolean, ValidatorException> validate(@Nullable String value)
    {
        return validate(value, 255);
    }

    /**
     *
     * 验证输入值
     *
     * @param value 可空验证对象值
     * @param length 最大多小
     * @return 如果验证成功，返回<code>Pair<True, null></code>。如果失败，返回<code>Pair<null, ValidatorException></code>。
     * @author Masato Morita
     */
    public Pair<Boolean, ValidatorException> validate(@Nullable String value, int length)
    {
        if (Objects.isNull(value)){
            return ImmutablePair.of(null, new ValidatorException("value is null."));
        }
        if (value.length() > length){
            return ImmutablePair.of(null, new ValidatorException("value is invalid."));
        }
        return ImmutablePair.of(true, null);
    }
}
