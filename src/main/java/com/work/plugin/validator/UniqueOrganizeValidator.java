package com.work.plugin.validator;

import com.work.plugin.ao.StrOrganizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 机构唯一值验证试验
 *
 * @author Masato Morita
 */
@Slf4j
@RequiredArgsConstructor
public class UniqueOrganizeValidator implements Validator
{
    private final StrOrganizeService strOrganizeService;

    /**
     *
     * 机构唯一值验证输入值
     *
     * @param value 可空验证对象值
     * @return 如果机构在，返回<code>Pair<False, null></code>。 如果机构不在，返回<code>Pair<True, null></code>。
     *         如果验证失败，返回<code>Pair<null, ValidatorException></code>。
     * @author Masato Morita
     */
    @Override
    public Pair<Boolean, ValidatorException> validate(@Nullable String value)
    {
        if (Objects.isNull(value))
            return ImmutablePair.of(null, new ValidatorException("value is null."));

        try
        {
            Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return ImmutablePair.of(null, new ValidatorException("value is not numeric."));
        }

        if (strOrganizeService.isOrganizeByID(Integer.parseInt(value)))
        {
            log.info(String.format("the employee already exists. [%s]", value));
            return ImmutablePair.of(false, null);
        }

        return ImmutablePair.of(true, null);
    }
}
