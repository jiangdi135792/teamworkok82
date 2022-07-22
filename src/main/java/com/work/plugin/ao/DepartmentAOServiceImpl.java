package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.work.plugin.api.Group;
import com.work.plugin.rest.DepartmentBean;
import com.work.plugin.util.exception.IntegrityConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by work on 2021/6/21.
 */
@RequiredArgsConstructor
public final class DepartmentAOServiceImpl implements DepartmentAOService {

    private final ActiveObjects ao;

    @Override
    public List<DepartmentEntity> getAllGroup() {
        List<DepartmentEntity> groups = Arrays.asList(ao.find(DepartmentEntity.class, "TYPE = 0"));
        return groups;
    }

    @Override
    public List<DepartmentEntity> getAllTeam() {
        List<DepartmentEntity> teams = Arrays.asList(ao.find(DepartmentEntity.class, "TYPE = 1"));
        return teams;
    }

    @Override
    public List<DepartmentEntity> getDirectDeptsByOrgId(Integer orgId) {
        StrOrganizeGroup[] organizeGroups =
                ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", orgId));

        List<DepartmentEntity> depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).collect(Collectors.toList());
        return depts;
    }

    /**
     * 根据组织id查询所有的子部�?
     *
     * @param orgId
     * @return
     */
    @Override
    public List<DepartmentEntity> getSubDeptsByOrgId(Integer orgId) {

        List<StrOrganize> orgList = Lists.newArrayList();
        StrOrganize thisStrOrganize = ao.get(StrOrganize.class, orgId);
        if (thisStrOrganize != null) {
            orgList.add(thisStrOrganize); // 将该组织放到list
        } else {
            return null;
        }

        addSubOrgById(orgId, orgList); // 将子机构放入list

        List<DepartmentEntity> list = Lists.newArrayList();

        orgList.forEach(strOrganize -> {
            StrOrganizeGroup[] organizeGroups = strOrganize.getStrOrganizeGroup(); // 获取该组织的直接子部�?
            Lists.newArrayList(organizeGroups).forEach(organizeGroup -> {
                DepartmentEntity departmentEntity = organizeGroup.getGroup();
                if (departmentEntity.getType() == 1) { // 过滤掉团�?
                    return;
                }
                list.add(departmentEntity); // 直接子部门（一层）

                int id = departmentEntity.getID();
                DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, String.format("PARENT = '%s' and ID <> %d", id, id));
                // 直接子部门下的子部门（多层）
                for (DepartmentEntity dept : departmentEntities) {
                    if (dept.getParent() != null) {
                        addSubDeptById(dept.getID(), list);
                    }
                    list.add(dept);
                }
            });
        });
        return list;
    }

    /**
     * 查询组织机构的子机构
     *
     * @param id
     * @param list
     */
    private void addSubOrgById(int id, List<StrOrganize> list) {
        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format("PARENT = '%s' and ID <> %d", id, id));
        for (StrOrganize organize : strOrganizes) {
            if (organize.getParent() != null) {
                addSubOrgById(organize.getID(), list);
            }
            list.add(organize);
        }
    }

    /**
     * 查询部门的子部门
     *
     * @param id
     * @param list
     */
    private void addSubDeptById(int id, List<DepartmentEntity> list) {
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, String.format("PARENT = '%s' and ID <> %d", id, id));
        for (DepartmentEntity departmentEntity : departmentEntities) {
            if (departmentEntity.getParent() != null) {
                addSubDeptById(departmentEntity.getID(), list);
            }
            list.add(departmentEntity);
        }
    }

    /**
     * query in dept(exclude team)
     *
     * @param name
     * @return
     */
    @Override
    public DepartmentEntity[] query(String name) {
        DepartmentEntity[] result = ao.find(DepartmentEntity.class, MessageFormat.format("NAME like {0}{1}{2} AND TYPE = 0", "'%", name, "%'"));

        return result;
    }

    @Override
    public DepartmentEntity get(int id) {
        DepartmentEntity dept = ao.get(DepartmentEntity.class, id);
        if (StringUtils.isEmpty(dept.getParent())) {
            StrOrganizeGroup[] strOrganizeGroups = ao.find(StrOrganizeGroup.class, String.format("GROUP_ID = '%s'", id));
            if (strOrganizeGroups != null && strOrganizeGroups.length > 0) {
                int parentOrgId = strOrganizeGroups[0].getOrg().getID();
                dept.setParent("o_" + parentOrgId);
            }
        } else {
            dept.setParent("d_" + dept.getParent());
        }

        return dept;
    }

    @Override
    public DepartmentEntity add(DepartmentBean model) {
        String parentId = model.getParent();
        DBParam parent;
        if (parentId.startsWith("o_")) { // 如果父级单位是组织机构，则需要在关联表里添加记录
            model.setParent(parentId.replace("o_", ""));
            parent = new DBParam(DepartmentEntity.COLUMN.PARENT.name(), null);
        } else {
            model.setParent(parentId.replace("d_", ""));
            parent = new DBParam(DepartmentEntity.COLUMN.PARENT.name(), model.getParent());
        }

        final DepartmentEntity section = ao.create(DepartmentEntity.class,
                new DBParam(DepartmentEntity.COLUMN.GROUP_NO.name(), model.getGroupNo()),
                new DBParam(DepartmentEntity.COLUMN.GROUP_NAME.name(), model.getGroupName()),
                parent,
                new DBParam(DepartmentEntity.COLUMN.STATUS.name(), model.getStatus()),
                new DBParam(DepartmentEntity.COLUMN.TYPE.name(), model.getType()),
                new DBParam(DepartmentEntity.COLUMN.MAPPING_CODE.name(), model.getMappingCode()),
                new DBParam(DepartmentEntity.COLUMN.MEMO.name(), model.getMemo()),
                new DBParam(DepartmentEntity.COLUMN.OWNER.name(), model.getOwner()),
                new DBParam(DepartmentEntity.COLUMN.CREATE_DATE.name(), model.getCreateDate()),
                new DBParam(DepartmentEntity.COLUMN.MODIFIER.name(), model.getModifier()),
                new DBParam(DepartmentEntity.COLUMN.MODIFIER_DATE.name(), model.getModifierDate())
        );
        // 需要先创建部门表，才能创建关联�?
        if (parentId.startsWith("o_")) {
            ao.create(StrOrganizeGroup.class,
                    new DBParam(StrOrganizeGroup.COLUMN.GROUP_ID.name(), section.getID()),
                    new DBParam(StrOrganizeGroup.COLUMN.MODIFIER.name(), Integer.valueOf(model.getModifier())),
                    new DBParam(StrOrganizeGroup.COLUMN.ORG_ID.name(), Integer.valueOf(model.getParent())),
                    new DBParam(StrOrganizeGroup.COLUMN.OWNER.name(), Integer.valueOf(model.getOwner())));
        }
        return section;
    }

    @Override
    public void delete(int id) throws IntegrityConstraintViolationException {
        DepartmentEntity departmentEntity = ao.get(DepartmentEntity.class, id);

        if (ao.count(DepartmentEntity.class,
                String.format("PARENT = '%s' AND ID <> %s", id, id)) > 0)
            throw new IntegrityConstraintViolationException("请先删除该部门的子部门，才可删除该部门！");
        if (ao.count(StruGroupOfEmployee.class,
                MessageFormat.format("GROUP_ID = {0}", id)) > 0)
            throw new IntegrityConstraintViolationException("请先删除该部门的雇员，才可删除该部门！");

        if (departmentEntity.getStrOrganizeGroup() != null) {
            ao.delete(departmentEntity.getStrOrganizeGroup());
        }
        ao.delete(departmentEntity);
    }

    @Override
    public DepartmentEntity update(DepartmentBean bean) {
        String parentId = bean.getParent();
        val entity = ao.get(DepartmentEntity.class, bean.getId());
        DepartmentEntity departmentEntity = ao.get(DepartmentEntity.class, bean.getId());
        StrOrganizeGroup strOrganizeGroup = departmentEntity.getStrOrganizeGroup();
        if (parentId.startsWith("o_")) { // 如果父级单位是组织机构，则需要在关联表里添加记录
            if (strOrganizeGroup == null) { // 如果没有关联记录，则新建
                ao.create(StrOrganizeGroup.class,
                        new DBParam(StrOrganizeGroup.COLUMN.GROUP_ID.name(), departmentEntity.getID()),
                        new DBParam(StrOrganizeGroup.COLUMN.MODIFIER.name(), Integer.valueOf(bean.getModifier())),
                        new DBParam(StrOrganizeGroup.COLUMN.ORG_ID.name(), Integer.valueOf(bean.getParent().replace("o_", ""))),
                        new DBParam(StrOrganizeGroup.COLUMN.OWNER.name(), Integer.valueOf(bean.getModifier())));
            } else { // 更新
                strOrganizeGroup.setModifier(Integer.valueOf(bean.getModifier()));
                strOrganizeGroup.setOrg(ao.get(StrOrganize.class, Integer.valueOf(parentId.replace("o_", ""))));
                strOrganizeGroup.save();
            }
            entity.setParent(null); // 部门的父单位为组织时，parent字段设为null，关系由关系表维�?
        } else {
            bean.setParent(parentId.replace("d_", ""));
            entity.setParent(bean.getParent());
            // 删除关联表的数据
            if (strOrganizeGroup != null) {
                ao.delete(strOrganizeGroup);
            }
        }


        entity.setGroupNo(bean.getGroupNo());
        entity.setGroupName(bean.getGroupName());
        entity.setStatus(bean.getStatus());
        entity.setType(bean.getType());
        entity.setMappingCode(bean.getMappingCode());
        entity.setMemo(bean.getMemo());
        entity.setOwner(bean.getOwner());
        entity.setModifier(bean.getModifier());
        entity.setModifierDate(bean.getModifierDate());
        entity.save();
        return entity;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroup(int id) {
        return Objects.nonNull(ao.get(DepartmentEntity.class, id));
    }

    @Override
    public Group updateGroupByImport(int orgid, Group group) {
        if (StringUtils.isNotBlank(group.getName())) {
            String name = group.getName();
            DepartmentEntity departInfo = getSubDepartInfo(orgid, name);
            if (departInfo != null) {
                val entity = ao.get(DepartmentEntity.class, departInfo.getID());
                return CONVERT_TO_DEPARTMENT_API.apply(entity);
            } else {
                val entity = ao.create(
                        DepartmentEntity.class,
                        new DBParam(DepartmentEntity.COLUMN.GROUP_NAME.name(), group.getName())
                );
                return CONVERT_TO_DEPARTMENT_API.apply(entity);
            }
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Group> getGroup(int id) {
        return Optional.ofNullable(ao.get(DepartmentEntity.class, id))
                .map(e -> CONVERT_TO_DEPARTMENT_API.apply(e));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getGroupByEmployeeId(int employeeId, int type) {
        val DEPARTMENT = DepartmentEntity.class.getSimpleName();
        val GROUP_OF_EMPLOYEE = StruGroupOfEmployee.class.getSimpleName();
        List<Group> groups = Lists.newArrayList();
        val query = Query
                .select(Joiner.on(",").join(DepartmentEntity.COLUMN.values()))
                .where(String.format("%s.EMPLOYEE_ID = ? AND %s.TYPE = ?", GROUP_OF_EMPLOYEE, DEPARTMENT), employeeId, type)
                .alias(DepartmentEntity.class, DEPARTMENT)
                .alias(StruGroupOfEmployee.class, GROUP_OF_EMPLOYEE)
                .join(StruGroupOfEmployee.class, String.format("%s.ID = %s.GROUP_ID", DEPARTMENT, GROUP_OF_EMPLOYEE));
        ao.stream(DepartmentEntity.class, query, e -> groups.add(CONVERT_TO_DEPARTMENT_API.apply(e)));
        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOrganizeGroupByImport(int orgId, int groupId) {
        val organize = ao.get(StrOrganize.class, orgId);
        val group = ao.get(DepartmentEntity.class, groupId);

        // 删除父团体信�?
        group.setParent(null);
        group.save();

        // 已经一样关系有，跳�?
        val relations = ao.find(StrOrganizeGroup.class, "ORG_ID = ? AND GROUP_ID = ?", organize.getID(), group.getID());
        if (relations.length > 0)
            return;

        // 若果其他关系有，删除全都
        Stream.of(ao.find(StrOrganizeGroup.class, "GROUP_ID = ?", group.getID())).forEach(ao::delete);

        val relation = ao.create(StrOrganizeGroup.class);
        relation.setOrg(organize);
        relation.setGroup(group);
        relation.save();
    }


    private Function<DepartmentEntity, Group> CONVERT_TO_DEPARTMENT_API =
            e -> Group.builder().id(e.getID()).no(e.getGroupNo()).name(e.getGroupName())
                    .parent(e.getParent()).status(e.getStatus()).type(e.getType()).mappingCode(e.getMappingCode())
                    .memo(e.getMemo()).owner(e.getOwner()).createDate(e.getCreateDate()).modifier(e.getModifier())
                    .modifierDate(e.getModifierDate()).build();
//wangqinglin

    @Override
    public void maintainRelationOrgAndGro(Integer ownerId, Integer modifier, StrOrganize organize, DepartmentEntity departmentEntity) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        String maintainTime = simpleDateFormat.format(new Date());
        StrOrganizeGroup strOrganizeGroup = ao.create(StrOrganizeGroup.class);
        strOrganizeGroup.setCreateDate(maintainTime);
        strOrganizeGroup.setOrg(organize);
        strOrganizeGroup.setGroup(departmentEntity);
        strOrganizeGroup.setModifier(modifier);
        strOrganizeGroup.save();
    }

    @Override
    public DepartmentEntity createByInfo(String department, int modifier, int OrganizeId, int status, int type) {
        DepartmentEntity departmentEntity = ao.create(DepartmentEntity.class);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        String updateTime = simpleDateFormat.format(new Date());
        departmentEntity.setCreateDate(updateTime);
        departmentEntity.setModifierDate(updateTime);
        departmentEntity.setGroupName(department);
        departmentEntity.setStatus(status);
        departmentEntity.setType(type);
        departmentEntity.save();
        //创建过程中维护关�?
        if (OrganizeId != -1) {
            StrOrganize strOrganize = ao.get(StrOrganize.class, OrganizeId);
            this.maintainRelationOrgAndGro(modifier, modifier, strOrganize, departmentEntity);
        }
        return departmentEntity;
    }

    @Override
    public DepartmentEntity queryByParentId(String name, String parentId) {
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, String.format("GROUP_NAME = '%s' AND PARENT = '%S'", name, parentId));
        return departmentEntities[0];
    }

    @Override
    public DepartmentEntity getDepartInfo(int OrgId, String DepartName) {
        StrOrganizeGroup[] strOrganizeGroups = ao.find(StrOrganizeGroup.class, String.format("ORG_ID =  '%d' ", OrgId));
        for (StrOrganizeGroup strOrganizeGroup : strOrganizeGroups) {
            DepartmentEntity group = strOrganizeGroup.getGroup();
            if (DepartName.equals(group.getGroupName())) {
                return group;
            }
            ;
        }
        return null;
    }

    @Override
    public boolean isExistDeptNo(String deptno, String groupId) {
        int count;
        if (StringUtils.isNotEmpty(groupId)) {
            count = ao.count(DepartmentEntity.class,
                    String.format("%s = ? AND %s <> ?", DepartmentEntity.COLUMN.GROUP_NO, DepartmentEntity.COLUMN.ID),
                    deptno, groupId);
        } else {
            count = ao.count(DepartmentEntity.class,
                    String.format("%s = ?", DepartmentEntity.COLUMN.GROUP_NO),
                    deptno);
        }

        return count > 0 ? true : false;
    }

    /**
     * {@inheritDoc}
     *
     * @param teamId
     * @return
     */
    @Override
    public List<DepartmentEntity> getSubTeamOfTeam(Integer teamId) {
        DepartmentEntity department = ao.get(DepartmentEntity.class, teamId);
        ArrayList<DepartmentEntity> departmentEntities = Lists.newArrayList(department);
        addSubDeptById(teamId, departmentEntities);
        return departmentEntities;
    }

    @Override
    public DepartmentEntity getSubDepartInfo(int orgId, String departName) {
        StrOrganizeGroup[] strOrganizeGroups = ao.find(StrOrganizeGroup.class, String.format("ORG_ID =  '%d' ", orgId));
        //组织下所有的部门  一级
        Map<String, Integer> stringIntegerMap = new HashMap<>();
        if (strOrganizeGroups.length > 0) {
            Set<DepartmentEntity> collect = Arrays.stream(strOrganizeGroups).map(strOrganizeGroup -> strOrganizeGroup.getGroup()).collect(Collectors.toSet());
            collect.stream().forEach(departmentEntity -> {
                stringIntegerMap.put(departmentEntity.getGroupName(), departmentEntity.getID());
            });
            collect.stream().forEach(departmentEntity -> {
                getAllSubDepart(departmentEntity, stringIntegerMap);
            });
            if (stringIntegerMap.keySet().contains(departName)) {
                DepartmentEntity departmentEntity = ao.get(DepartmentEntity.class, stringIntegerMap.get(departName));
                return departmentEntity;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean isGroupByName(int orgId, String groupName) {
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, String.format("GROUP_NAME = '%s' ", groupName));
        if (departmentEntities.length > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateDepAndDepRela(int depId, String depParentId) {
        DepartmentEntity departmentEntity = ao.get(DepartmentEntity.class, depId);
        departmentEntity.setParent(depParentId);
        departmentEntity.save();
    }

    private void getAllSubDepart(DepartmentEntity departmentEntity, Map<String, Integer> stringIntegerMap) {
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class);
        List<DepartmentEntity> collect = Arrays.stream(departmentEntities).filter(departmentEntity1 -> {
            if (StringUtils.isNoneBlank(departmentEntity1.getParent())) {
                if (departmentEntity1.getParent().equals(String.valueOf(departmentEntity.getID()))) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        if (collect.size() > 0) {
            collect.stream().forEach(departmentEntity1 -> {
                getAllSubDepart(departmentEntity1, stringIntegerMap);
            });
        }
        stringIntegerMap.put(departmentEntity.getGroupName(), departmentEntity.getID());
    }

    ;

    @Override
    public int setDeptDutyPersonByJiraUserKey(String jiraUserKey, String deptId) {
        if (null == jiraUserKey || "".equals(jiraUserKey)) {
            return 0;
        }
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format(" JIRA_USER_KEY = '%s' ", jiraUserKey));
        if (strEmployees.length == 0) {
            return 0;
        }
        try {
            StrEmployee strEmployee = strEmployees[0];
            DepartmentEntity departmentEntity = ao.find(DepartmentEntity.class, String.format("ID = '%s' ", deptId.replace("d_", "")))[0];
            departmentEntity.setDutyPerson(strEmployee);
            departmentEntity.save();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    @Override
    public StrEmployee getDutyPersonByDeptId(String deptId) {
        DepartmentEntity departmentEntity = ao.find(DepartmentEntity.class, String.format("ID = '%s' ", deptId))[0];

        return departmentEntity.getDutyPerson();
    }
}

