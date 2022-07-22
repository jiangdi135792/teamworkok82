package com.work.plugin.validator;

import com.work.plugin.ao.StrEmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 员工唯一值验证试验
 *
 * @author Masato Morita
 */
@Slf4j
@RequiredArgsConstructor
public class UniqueEmployeeValidator implements Validator
{
    private final StrEmployeeService strEmployeeService;

    /**
     *
     * 员工唯一值验证输入值
     *
     * @param value 可空验证对象值
     * @return 如果员工在，返回<code>Pair<False, null></code>。 如果员工不在，返回<code>Pair<True, null></code>。
     *         如果验证失败，返回<code>Pair<null, ValidatorException></code>。
     * @author Masato Morita
     */
    @Override
    public Pair<Boolean, ValidatorException> validate(@Nullable String value)
    {
        if (Objects.isNull(value)){
            return ImmutablePair.of(null, new ValidatorException("value is null."));
        }
        if (strEmployeeService.isEmployeeByName(value))
        {
            //log.info(String.format("the employee already exists. [%s]", value));
            return ImmutablePair.of(null, new ValidatorException("value is null."));
//            return ImmutablePair.of(false, null);
        }
        return ImmutablePair.of(true, null);
    }
}
