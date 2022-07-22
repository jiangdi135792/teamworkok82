package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.work.plugin.api.Organize;
import com.work.plugin.rest.StrOrganizaitonBean;
import com.work.plugin.util.exception.IntegrityConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by admin on 2021/6/22.
 */
@RequiredArgsConstructor
public class StrOrganizeServiceImpl implements StrOrganizeService {
    private final ActiveObjects ao;
    private final StrEmployeeService strEmployeeService;
    private final UserManager userManager;
    private final I18nHelper i18n;

    @Override
    public List<StrOrganize> getAll() {
        return Arrays.asList(ao.find(StrOrganize.class));
    }

    /**
     * 获取组织。部门的集合
     */
    public List<StrOrganizaitonBean> getOrgAndDept() {
        // 获取组织
        List<StrOrganize> orgs = Arrays.asList(ao.find(StrOrganize.class));
        List<StrOrganizaitonBean> result = new ArrayList<>();

        result.addAll(Arrays.asList(orgs.stream().
                map(e -> new StrOrganizaitonBean("o_" + e.getID(), e.getName(), "o_" + e.getParent(), "org")).
                toArray(StrOrganizaitonBean[]::new)));
        // 获取与组织直接关联的部门
        StrOrganizeGroup[] strOrganizeGroups = ao.find(StrOrganizeGroup.class);
        List<Integer> deptIds = Lists.newArrayList();

        Arrays.asList(strOrganizeGroups).stream().forEach(sog -> {
                    DepartmentEntity dept = sog.getGroup();

                    if (dept.getType() != 0) return; // type为0代表部门
                    deptIds.add(dept.getID());
                    result.add(new StrOrganizaitonBean("d_" + dept.getID(), dept.getGroupName(), "o_" + sog.getOrg().getID(), "dept"));
                }
        );
        // 获取部门的子部门
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, "TYPE = 0");
        Lists.newArrayList(departmentEntities).stream().forEach(dept -> {
            if (!deptIds.contains(dept.getID())) {
                result.add(new StrOrganizaitonBean("d_" + dept.getID(), dept.getGroupName(), "d_" + dept.getParent(), "dept"));
            }
        });

        return result;
    }


    /**
     * 获取组织。团队的集合
     */
    public List<StrOrganizaitonBean> getOrgAndTeam() {
        // 获取组织
        List<StrOrganize> orgs = Arrays.asList(ao.find(StrOrganize.class));
        List<StrOrganizaitonBean> result = new ArrayList<>();

        result.addAll(Arrays.asList(orgs.stream().
                map(e -> new StrOrganizaitonBean("o_" + e.getID(), e.getName(), "o_" + e.getParent(), "org")).
                toArray(StrOrganizaitonBean[]::new)));
        // 获取与组织直接关联的团队
        StrOrganizeGroup[] strOrganizeGroups = ao.find(StrOrganizeGroup.class);
        List<Integer> deptIds = Lists.newArrayList();

        Map<Integer, TeamProjectEntity> map = Maps.newHashMap();
        TeamProjectEntity[] teamProjectEntities = ao.find(TeamProjectEntity.class);
        Lists.newArrayList(teamProjectEntities).forEach(teamProjectEntity -> {
            Integer teamId = teamProjectEntity.getTeamId();
            map.put(teamId, teamProjectEntity);
        });
        Arrays.asList(strOrganizeGroups).stream().forEach(sog -> {
                    DepartmentEntity dept = sog.getGroup();

                    if (dept.getType() != 1) return; // type为1代表团队
                    deptIds.add(dept.getID());
                    StrOrganizaitonBean strOrganizaitonBean =
                            new StrOrganizaitonBean("d_" + dept.getID(), dept.getGroupName(), "o_" + sog.getOrg().getID(), "team");
                    if (map.keySet().contains(dept.getID())) {
                        TeamProjectEntity teamProjectEntity = map.get(dept.getID());
                        strOrganizaitonBean.setTeamId(dept.getID());
                        Integer projectId = teamProjectEntity.getProjectId();
                        strOrganizaitonBean.setProjectId(projectId);
                    }
                    result.add(strOrganizaitonBean);
                }
        );
        // 获取团队的子团队
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, "TYPE = 1");
        Lists.newArrayList(departmentEntities).stream().forEach(dept -> {
            if (!deptIds.contains(dept.getID())) {
                StrOrganizaitonBean strOrganizaitonBean =
                        new StrOrganizaitonBean("d_" + dept.getID(), dept.getGroupName(), "d_" + dept.getParent(), "team");
                if (map.keySet().contains(dept.getID())) {
                    TeamProjectEntity teamProjectEntity = map.get(dept.getID());
                    Integer projectId = teamProjectEntity.getProjectId();
                    strOrganizaitonBean.setProjectId(projectId);
                    strOrganizaitonBean.setTeamId(dept.getID());
                }

                result.add(strOrganizaitonBean);
            }
        });
        return result;
    }


    /**
     * 获取组织。团队和项目的集合
     */
    public List<StrOrganizaitonBean> getOrgAndTeamAndProject() {
        // 获取组织
        List<StrOrganize> orgs = Arrays.asList(ao.find(StrOrganize.class));
        List<StrOrganizaitonBean> result = new ArrayList<>();

        result.addAll(Arrays.asList(orgs.stream().
                map(e -> new StrOrganizaitonBean("o_" + e.getID(), e.getName(), "o_" + e.getParent(), "org")).
                toArray(StrOrganizaitonBean[]::new)));
        // 获取与组织直接关联的团队
        StrOrganizeGroup[] strOrganizeGroups = ao.find(StrOrganizeGroup.class);
        List<Integer> deptIds = Lists.newArrayList();

        Arrays.asList(strOrganizeGroups).stream().forEach(sog -> {
                    DepartmentEntity dept = sog.getGroup();
                    if (dept.getType() != 1) return; // type为1代表团队
                    int id = dept.getID();
                    deptIds.add(id);
                    StrOrganizaitonBean strOrganizaitonBean =
                            new StrOrganizaitonBean("d_" + id, dept.getGroupName(), "o_" + sog.getOrg().getID(), "team");
                    List<StrOrganizaitonBean> teamProjects = getTeamProjects(id, dept.getGroupName());
                    result.addAll(teamProjects); // 团队与项目的关联关系
                    result.add(strOrganizaitonBean);
                }
        );
        // 获取团队的子团队
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, "TYPE = 1");
        Lists.newArrayList(departmentEntities).stream().forEach(dept -> {
            if (!deptIds.contains(dept.getID())) {
                StrOrganizaitonBean strOrganizaitonBean =
                        new StrOrganizaitonBean("d_" + dept.getID(), dept.getGroupName(), "d_" + dept.getParent(), "team");
                int id = dept.getID();
                deptIds.add(id);
                List<StrOrganizaitonBean> teamProjects = getTeamProjects(id, dept.getGroupName());
                result.add(strOrganizaitonBean);
                result.addAll(teamProjects); // 团队与项目的关联关系
            }
        });
        return result;
    }

    private final TeamProjectService teamProjectService;

    /**
     * 获取团队与项目的关联关系
     *
     * @param teamId
     */
    private List<StrOrganizaitonBean> getTeamProjects(Integer teamId, String teamName) {
        TeamProjectEntity[] teamProjectEntities = teamProjectService.getByTeamId(teamId);
        ProjectManager projectManager = ComponentAccessor.getProjectManager();

        List<StrOrganizaitonBean> list = Lists.newArrayList(teamProjectEntities).stream().map(e -> {
            StrOrganizaitonBean bean = new StrOrganizaitonBean();
            Integer projectId = e.getProjectId();
            bean.setParent("d_" + teamId);
            bean.setTeamId(e.getTeamId());
            bean.setType("teamproject");
            bean.setId(e.getID() + "");
            Project projectObj = projectManager.getProjectObj(projectId.longValue());
            if (projectObj == null) {
                bean.setName("项目丢失");
                return bean;
            }

            bean.setName(projectObj.getName());
            bean.setProjectKey(projectObj.getKey());

            return bean;
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public StrOrganize[] query(String name) {
        return ao.find(StrOrganize.class, MessageFormat.format("NAME like '{0}{1}{2}'", "'%", name, "%'"));
    }

    @Override
    public StrOrganize get(int id) {
        return ao.get(StrOrganize.class, id);
    }

    @Override
    public StrOrganize create(StrOrganizaitonBean bean) {
        StrOrganize entity = ao.create(StrOrganize.class,
                new DBParam(StrOrganize.COLUMN.NAME.name(), bean.getName()),
                new DBParam(StrOrganize.COLUMN.MAPPING_CODE.name(), bean.getMappingCode()),
                new DBParam(StrOrganize.COLUMN.CHARACTER.name(), bean.getCharacter()),
                new DBParam(StrOrganize.COLUMN.MEMO.name(), bean.getMemo()),
                new DBParam(StrOrganize.COLUMN.CREATE_DATE.name(), bean.getCreateDate()),
                new DBParam(StrOrganize.COLUMN.MODIFIER.name(), bean.getModifier()),
                new DBParam(StrOrganize.COLUMN.MODIFIER_DATE.name(), bean.getModifierDate()),
                new DBParam(StrOrganize.COLUMN.TYPE.name(), bean.getType()),
                new DBParam(StrOrganize.COLUMN.PARENT.name(), StringUtils.isNotEmpty(bean.getParent()) ? Integer.valueOf(bean.getParent().replace("o_", "")) : 0),
                new DBParam(StrOrganize.COLUMN.ORG_LEVEL.name(), bean.getOrgLevel()),
                new DBParam(StrOrganize.COLUMN.STATUS.name(), bean.getStatus()),
                new DBParam(StrOrganize.COLUMN.OWNER.name(), bean.getOwner())
        );

        return entity;
    }

    @Override
    public void delete(int id) throws IntegrityConstraintViolationException {
        StrOrganize organize = ao.get(StrOrganize.class, id);
        if (ao.count(StrOrganize.class, MessageFormat.format("PARENT = {0} AND ID <> {0}", id)) > 0 ||
                ao.count(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", id)) > 0)
            throw new IntegrityConstraintViolationException("请先删除该组织的子组织，才可删除该组织！");

        ao.delete(organize);
    }

    @Override
    public StrOrganize update(StrOrganizaitonBean bean) {
        StrOrganize entity = ao.get(StrOrganize.class, Integer.valueOf(bean.getId()));
        entity.setModifier(bean.getModifier());
        entity.setModifierDate(bean.getModifierDate());
        entity.setName(bean.getName());
        entity.setCharacter(bean.getCharacter());
        entity.setMappingCode(bean.getMappingCode());
        entity.setMemo(bean.getMemo());
        entity.setOrgLevel(bean.getOrgLevel());
        entity.setParent(StringUtils.isNotEmpty(bean.getParent()) ? Integer.valueOf(bean.getParent().replace("o_", "")) : 0);
        entity.setStatus(bean.getStatus());
        entity.setType(bean.getType());
        entity.save();
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOrganizeByID(int id) {
        return getOrganize(id).isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Organize> getOrganize(int id) {
        return Optional.ofNullable(ao.get(StrOrganize.class, id)).map(e -> CONVERT_TO_ORGANIZE_API.apply(e));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Organize> getOrganizesByGroupId(int groupId) {
        val ORGANIZE = StrOrganize.class.getSimpleName();
        val ORGANIZE_GROUP = StrOrganizeGroup.class.getSimpleName();
        List<Organize> organizes = Lists.newArrayList();
        val query = Query
                .select(Joiner.on(",").join(StrOrganize.COLUMN.values()))
                .where(String.format("%s.GROUP_ID = ?", ORGANIZE_GROUP), groupId)
                .alias(StrOrganize.class, ORGANIZE)
                .alias(StrOrganizeGroup.class, ORGANIZE_GROUP)
                .join(StrOrganizeGroup.class, String.format("%s.ID = %s.ORG_ID", ORGANIZE, ORGANIZE_GROUP));
        ao.stream(StrOrganize.class, query, e -> organizes.add(CONVERT_TO_ORGANIZE_API.apply(e)));
        return organizes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Organize updateOrganizeByImport(Organize organize) {
        String name = organize.getName();
        if (isOrgByOrganizeName(name)) {
            val entity = ao.find(StrOrganize.class, String.format("NAME = '%s' ", name))[0];
            //entity.setName(Optional.ofNullable(name).orElse(entity.getName()));
            //entity.setType(Optional.ofNullable(organize.getType()).orElse(entity.getType()));
            //entity.setParent(Optional.ofNullable(parent).orElse(entity.getParent()));
            //entity.save();
            return CONVERT_TO_ORGANIZE_API.apply(entity);
        } else {
            val entity = ao.create(
                    StrOrganize.class,
                    new DBParam(StrOrganize.COLUMN.NAME.name(), name),
                    new DBParam(StrOrganize.COLUMN.TYPE.name(), organize.getType()),
                    new DBParam(StrOrganize.COLUMN.PARENT.name(), organize.getParent()),
                    new DBParam(StrOrganize.COLUMN.STATUS.name(), organize.getStatus()),
                    new DBParam(StrOrganize.COLUMN.CHARACTER.name(), organize.getCharacter())
            );
            return CONVERT_TO_ORGANIZE_API.apply(entity);
        }
    }


    private Function<StrOrganize, Organize> CONVERT_TO_ORGANIZE_API =
            e -> Organize.builder().id(e.getID()).name(e.getName()).type(e.getType()).parent(e.getParent())
                    .status(e.getStatus()).level(e.getOrgLevel()).mappingCode(e.getMappingCode()).character(e.getCharacter())
                    .memo(e.getMemo()).owner(e.getOwner()).createDate(e.getCreateDate()).modifier(e.getModifier())
                    .modifierDate(e.getModifierDate()).build();

    /**
     * 获取组织。部门的集合
     */
    private List<StrOrganizaitonBean> getOrgAndDeptOrTeamByType(int type) {
        List<StrOrganize> orgs = Arrays.asList(ao.find(StrOrganize.class));
        List<StrOrganizaitonBean> result = new ArrayList<>();

        result.addAll(Arrays.asList(orgs.stream().
                map(e -> new StrOrganizaitonBean("o_" + e.getID(), e.getName(), "o_" + e.getParent(), "org")).
                toArray(StrOrganizaitonBean[]::new)));
        StrOrganizeGroup[] strOrganizeGroups = ao.find(StrOrganizeGroup.class);
        List<Integer> deptIds = Lists.newArrayList();

        String deptOrTeam = type == 0 ? "dept" : "team";

        Arrays.asList(strOrganizeGroups).stream().forEach(sog -> {
                    DepartmentEntity dept = sog.getGroup();

                    if (dept.getType() != type) return;
                    deptIds.add(dept.getID());
                    result.add(new StrOrganizaitonBean("d_" + dept.getID(), dept.getGroupName(), "o_" + sog.getOrg().getID(), deptOrTeam));
                }
        );

        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, MessageFormat.format("TYPE = {0}", type));
        Lists.newArrayList(departmentEntities).stream().forEach(dept -> {
            if (!deptIds.contains(dept.getID())) {
                result.add(new StrOrganizaitonBean("d_" + dept.getID(), dept.getGroupName(), "d_" + dept.getParent(), deptOrTeam));
            }
        });

        return result;
    }

    public String getCompanyByLicense() {
        int iidd = 0;
        val entitya = ao.find(StrOrganize.class, " PARENT = 0 ");
        for (StrOrganize o : entitya
                ) {
            if (-iidd > -o.getID())
                iidd = o.getID();
        }

        if (iidd > 0) {
            val entity = ao.get(StrOrganize.class, Integer.valueOf(iidd));


            return entity.getName();
        } else {
            return "";

        }

    }

    public boolean updateByLicense(String company) {
        int iidd = 0;
        val entitya = ao.find(StrOrganize.class, " PARENT = 0 ");
        boolean fComanyExsit = false;
        for (StrOrganize o : entitya
                ) {
            if (-iidd > -o.getID())
                iidd = o.getID();
            if (company.equals(o.getName())) {
                fComanyExsit = true;
            }

        }

        if (iidd > 0 && fComanyExsit) {
            /*val entity = ao.get(StrOrganize.class, Integer.valueOf(iidd));

			entity.setName(company);

			entity.save();*/
        } else {
            StrOrganize entity = ao.create(StrOrganize.class,
                    new DBParam(StrOrganize.COLUMN.NAME.name(), company),
                    new DBParam(StrOrganize.COLUMN.MAPPING_CODE.name(), ""),
                    new DBParam(StrOrganize.COLUMN.CHARACTER.name(), 1),
                    new DBParam(StrOrganize.COLUMN.MEMO.name(), ""),
                    new DBParam(StrOrganize.COLUMN.CREATE_DATE.name(), ""),
                    new DBParam(StrOrganize.COLUMN.MODIFIER.name(), 0),
                    new DBParam(StrOrganize.COLUMN.MODIFIER_DATE.name(), ""),
                    new DBParam(StrOrganize.COLUMN.TYPE.name(), ""),
                    new DBParam(StrOrganize.COLUMN.PARENT.name(), 0),
                    new DBParam(StrOrganize.COLUMN.ORG_LEVEL.name(), 1),
                    new DBParam(StrOrganize.COLUMN.STATUS.name(), "0"),
                    new DBParam(StrOrganize.COLUMN.OWNER.name(), 0));

        }
        return true;
    }


    @Override
    public StrOrganize createByInfo(String name, int modifier, long directoryId, String updateTime, int OrgLevel, int parent, String status, String type) {
        StrOrganize strOrganize = ao.create(StrOrganize.class);
        strOrganize.setCharacter(0);
        strOrganize.setName(name);
        strOrganize.setLdapId(directoryId);
        strOrganize.setModifierDate(updateTime);
        strOrganize.setCreateDate(updateTime);
        strOrganize.setOrgLevel(OrgLevel);
        strOrganize.setParent(parent);
        strOrganize.setStatus(status);
        strOrganize.setType(type);
        strOrganize.save();
        return strOrganize;
    }

    @Override
    public StrOrganize getByOrgName(String OrgName) {
        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format(" NAME = '%s' ", OrgName));
        if (strOrganizes.length != 0) {
            return strOrganizes[0];
        }
        return null;
    }
    @Override
    public StrOrganize getByOrgId(int Id) {
        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format(" ID = %d ", Id));
        if (strOrganizes.length != 0) {
            return strOrganizes[0];
        }
        return null;
    }
    @Override
    public boolean isOrgByOrganizeName(String orgName) {
        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format("NAME = '%s' ", orgName));
        if (strOrganizes.length != 0) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public List<DepartmentEntity> getUnmappedTeam() {
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, "TYPE = 1");

        TeamProjectEntity[] teamProjectEntities = ao.find(TeamProjectEntity.class);

        List<Integer> teamids = Lists.newArrayList(teamProjectEntities).stream()
                .map(teamProjectEntity -> teamProjectEntity.getTeamId()).collect(Collectors.toList());


        ArrayList<DepartmentEntity> entityArrayList = Lists.newArrayList(departmentEntities);
        ListIterator<DepartmentEntity> departmentEntityListIterator = entityArrayList.listIterator();
        while (departmentEntityListIterator.hasNext()) {
            DepartmentEntity next = departmentEntityListIterator.next();
            if (teamids.contains(next.getID())) {
                departmentEntityListIterator.remove();
            }
        }

        return entityArrayList;
    }

    @Override
    public void updateOrgOfParentOrgByImport(int orgId, int orgParentId) {
        StrOrganize strOrganize = ao.get(StrOrganize.class, orgId);
        strOrganize.setParent(orgParentId);
        strOrganize.save();
    }



    /**
     * 获取团队以及成员的全部集合
     * <p>
     * ①.属于组织机构和Jira用户，已关联公司或部门
     * ②.属于组织机构和Jira用户，未关联公司或部门
     * ③.不属于组织机构，属于Jira用户
     * <p>
     * 只是要展示并选择人员信息（选择人员名，并关联到jira），暂时使用OrgBean充当EmployeeBean
     *
     * @return
     */
    @Override
    public List<StrOrganizaitonBean> getDetailOrgTree() {

        //组织、部门、子部门
        List<StrOrganize> orgs = Arrays.asList(ao.find(StrOrganize.class));
        StrOrganizeGroup[] strOrganizeGroups = ao.find(StrOrganizeGroup.class);
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, "TYPE = 0");
        List<Integer> deptIds = Lists.newArrayList();

        //要返回的结果集合
        List<StrOrganizaitonBean> result = new ArrayList<>();

        //该节点排在第一位 先取出 ③ 不属于组织机构的jira用户
        result.addAll(Lists.newArrayList(
                StrOrganizaitonBean.builder().id("justJiraUserNotOrgUser").name(i18n.getText("workorg.property.organization.justJiraUserNotOrgUser")).parent(null).type("unmappedmember").build()
        ));
        ArrayList<ApplicationUser> users = (ArrayList<ApplicationUser>) userManager.getUsers();
        StrEmployee[] employees = ao.find(StrEmployee.class);
        boolean flag = true;
        int count = 0;
        for (ApplicationUser user : users) {
            asd:
            for (StrEmployee employee : employees) {
                if (user.getKey().equals(employee.getJiraUserKey())) {
                    flag = false;
                    break asd;
                }
            }
            if (flag) {
                result.add(new StrOrganizaitonBean("handsome_justJiraUserNotOrgUser" + count++, user.getUsername(), "justJiraUserNotOrgUser", "user", user.getUsername(), ""));
            } else {
                flag = true;
            }
        }

        //属于组织机构和Jira用户，未关联公司或部门 ②
        Iterator<StrEmployee> unmappedToDeptMember = strEmployeeService.listUnmappedToDeptMember().iterator();
        Iterator<StrEmployee> unmappedToOrgMember = strEmployeeService.listUnmappedToOrgMember().iterator();
        while (unmappedToDeptMember.hasNext()) {
            StrEmployee strEmployee = unmappedToDeptMember.next();
            if (!("".equals(strEmployee.getJiraUserKey()))) {
                result.add(new StrOrganizaitonBean("handsome_" + strEmployee.getID(), strEmployee.getEmployeeName(), "m_1", "user", strEmployee.getJiraUserKey(), ""));
            }
        }
        while (unmappedToOrgMember.hasNext()) {
            StrEmployee strEmployee = unmappedToOrgMember.next();
            if (!("".equals(strEmployee.getJiraUserKey()))) {
                result.add(new StrOrganizaitonBean("handsome_" + strEmployee.getID(), strEmployee.getEmployeeName(), "m_2", "user", strEmployee.getJiraUserKey(), ""));
            }
        }

        //------------属于组织机构和Jira用户，已关联公司或部门 ①
        for (StrOrganize strOrganize : orgs) {//组织节点
            result.add(new StrOrganizaitonBean("o_" + strOrganize.getID(), strOrganize.getName(), "o_" + strOrganize.getParent(), "org"));
        }

        for (StrOrganizeGroup strOrganizeGroup : strOrganizeGroups) {//部门节点
            DepartmentEntity dept = strOrganizeGroup.getGroup();
            deptIds.add(dept.getID());
            result.add(new StrOrganizaitonBean("d_" + dept.getID(), dept.getGroupName(), "o_" + strOrganizeGroup.getOrg().getID(), "dept"));
        }
        for (DepartmentEntity departmentEntity : departmentEntities) {//子部门节点
            if (!deptIds.contains(departmentEntity.getID())) {
                result.add(new StrOrganizaitonBean("d_" + departmentEntity.getID(), departmentEntity.getGroupName(), "d_" + departmentEntity.getParent(), "dept"));
            }

            StrEmployee[] employeeByGroupId = strEmployeeService.getEmployeeByGroupId(departmentEntity.getID());
            for (StrEmployee strEmployee : employeeByGroupId) {
                if (!("".equals(strEmployee.getJiraUserKey()))) {
                    result.add(new StrOrganizaitonBean("handsome_" + strEmployee.getID(), strEmployee.getEmployeeName(), "d_" + departmentEntity.getID(), "user", strEmployee.getJiraUserKey(), ""));
                }
            }
        }


        return result;
    }


/*lixiang*/
}
