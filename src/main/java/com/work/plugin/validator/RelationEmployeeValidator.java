package com.work.plugin.validator;

import com.work.plugin.ao.DepartmentAOService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 工员关联验证
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class RelationEmployeeValidator
{
    private final DepartmentAOService departmentAOService;

    /**
     *
     * 工员关验证
     *
     * @param groupId 团体编码
     * @return 如果验证成功，返回<code>Pair<True, null></code>。如果失败，返回<code>Pair<null, ValidatorException></code>。
     */
    public Pair<Boolean, ValidatorException> validate(int groupId)
    {
        val safeGroup = departmentAOService.getGroup(groupId);
        return safeGroup
                .<Pair<Boolean, ValidatorException>>map(group -> ImmutablePair.of(true, null))
                .orElseGet(() -> ImmutablePair.of(null, new ValidatorException("group not exist.")));

    }
}
