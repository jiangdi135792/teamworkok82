package com.work.plugin.validator;

import com.work.plugin.ao.DepartmentAOService;
import com.work.plugin.ao.StrOrganizeService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 部门关联验证
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class RelationDepartmentValidator
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
     * 验证父部门
     *
     * @param departmentId 父部门编码
     * @return 如果验证成功，返回<code>Pair<True, null></code>。如果失败，返回<code>Pair<null, ValidatorException></code>。
     */
    public Pair<Boolean, ValidatorException> validateDepartmentParent(int departmentId)
    {
        val safeGroup = departmentAOService.getGroup(departmentId);
        if (safeGroup.isPresent())
        {
            if (safeGroup.get().getType() == 0)
                return ImmutablePair.of(true, null);
            else
                return ImmutablePair.of(null, new ValidatorException("upper id is team."));
        }
        return ImmutablePair.of(null, new ValidatorException(String.format("department has id \"%d\" not exist.", departmentId)));
    }
}
