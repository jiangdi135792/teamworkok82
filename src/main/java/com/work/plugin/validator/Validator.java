package com.work.plugin.validator;

import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

/**
 * 验证界面
 *
 * @author Masato Morita
 */
interface Validator
{
    /**
     *
     * 验证输入值
     *
     * @param value 可空验证对象值
     * @return 如果验证成功，返回<code>Pair<True, null></code>。如果失败，返回<code>Pair<null, ValidatorException></code>。
     */
    Pair<Boolean, ValidatorException> validate(@Nullable String value);
}
