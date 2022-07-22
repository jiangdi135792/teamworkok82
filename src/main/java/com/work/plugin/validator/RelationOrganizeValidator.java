package com.work.plugin.validator;

import com.work.plugin.ao.StrOrganizeService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 机构关联验证
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class RelationOrganizeValidator
{
    private final StrOrganizeService organizeService;

    /**
     *
     * 验证输入值
     *
     * @param upperId 上级唯一标志
     * @return 如果验证成功，返回<code>Pair<True, null></code>。如果失败，返回<code>Pair<null, ValidatorException></code>。
     */
    public Pair<Boolean, ValidatorException> validate(int upperId)
    {
        val safeOrg = organizeService.getOrganize(upperId);
        return safeOrg
                .<Pair<Boolean, ValidatorException>>map(organize -> ImmutablePair.of(true, null))
                .orElseGet(() -> ImmutablePair.of(null, new ValidatorException("organize which has upper id not exist.")));

    }

}
