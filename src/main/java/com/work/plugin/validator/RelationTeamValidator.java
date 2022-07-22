package com.work.plugin.validator;

import com.work.plugin.ao.DepartmentAOService;
import com.work.plugin.ao.StrOrganizeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 团队关联验证
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class RelationTeamValidator
{
    private final DepartmentAOService departmentAOService;
    private final StrOrganizeService organizeService;

    /**
     *
     * 验证父机构
     *
     * @param orgId 父机构编码
     * @return 如果验证成功，返回<code>Pair<True, null></code>。如果失败，返回<code>Pair<null, ValidatorException></code>。
     */
    public Pair<Boolean, ValidatorException> validateOrganizeParent(int orgId)
    {
        if (organizeService.getOrganize(orgId).isPresent())
            return ImmutablePair.of(true, null);

        return ImmutablePair.of(null, new ValidatorException(String.format("organize has id \"%d\" not exist.", orgId)));
    }


    /**
     *
     * 验证父团体
     *
     * @param groupId 父团体标志
     * @return 如果验证成功，返回<code>Pair<True, null></code>。如果失败，返回<code>Pair<null, ValidatorException></code>。
     */
    public Pair<Boolean, ValidatorException> validateGroupParent(int groupId)
    {
        if (departmentAOService.getGroup(groupId).isPresent())
            return ImmutablePair.of(true, null);

        return ImmutablePair.of(null, new ValidatorException(String.format("group has id \"%d\" not exist.", groupId)));
    }
}
