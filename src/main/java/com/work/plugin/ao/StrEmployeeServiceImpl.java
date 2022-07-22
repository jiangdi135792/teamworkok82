package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.work.plugin.report.GlobalReport;
import com.work.plugin.util.exception.DuplicateKeyException;
import com.work.plugin.api.Employee;
import com.work.plugin.rest.LowerIssuesBean;
import com.work.plugin.rest.LowerIssuesChildBean;
import com.work.plugin.rest.StrEmployeeBean;
import com.work.plugin.util.license.license.GlobalConfig;
import lombok.val;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by admin on 2021/6/21.
 * Update by admin on 2021/6/28.
 */
public class StrEmployeeServiceImpl implements StrEmployeeService {
    private final GroupManager groupManager;
    private final UserManager userManager;
    private final CrowdService crowdService;
    private final ActiveObjects ao;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    public SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");//格式化日�???????

    public StrEmployeeServiceImpl(ActiveObjects ao, GroupManager groupManager, UserManager userManager, CrowdService crowdService, JiraAuthenticationContext jiraAuthenticationContext, RoleService roleService) {
        this.ao = Objects.requireNonNull(ao);//requireNonNull:当传入的参数不为null时，返回参数本身，反之，抛出一个NullPointerException异常
        this.groupManager = groupManager;
        this.userManager = userManager;
        this.crowdService = crowdService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.roleService = roleService;
    }

    /**
     * 根据雇员对象创建雇员
     *
     * @param model 一个雇员对�??????
     * @return StrEmployee 一个创建雇员的接口
     */
    public StrEmployee create(StrEmployeeBean model) throws DuplicateKeyException {
        if (StringUtils.isNotEmpty(model.getJiraUserKey())) {
            if (ao.count(StrEmployee.class,
                    String.format("%s = ?", "JIRA_USER_KEY"),
                    model.getJiraUserKey()) > 0)
                throw new DuplicateKeyException("JIRA_USER_KEY cannot be duplicate.");
        }
        final StrEmployee employee = ao.create(StrEmployee.class);
        employee.setEmail(model.getEmail());
        employee.setCreateDate(format.format(new Date()));//获取当前系统时间，即雇员创建时间
        employee.setEmployeeName(model.getEmployeeName());
        employee.setEmployeeNo(model.getEmployeeNo());
        employee.setEmployeeSex(model.getEmployeeSex());
        employee.setEmploymentStatus(model.getEmploymentStatus());
        employee.setEntryTime(model.getEntryTime());
        employee.setJiraUserName(model.getJiraUserName());
        employee.setJiraUserKey(model.getJiraUserKey());
        employee.setJiraId(model.getJiraId());//JiraId默认�???????0
        employee.setLeaveTime(model.getLeaveTime());
        employee.setMemo(model.getMemo());
        employee.setModifier(model.getOwner());//创建时的创建者即为修改�?
        employee.setModifierDate(format.format(new Date()));//创建时的系统时间即创建时�??????
        employee.setOtherPhone(model.getOtherPhone());
        employee.setOwner(model.getOwner());
        employee.setPhone(model.getPhone());
        employee.setSupervisor(model.getSupervisor());
        if (model.getOrgId() != null) {
            StrOrganize strOrganize = ao.get(StrOrganize.class, model.getOrgId());
            employee.setStrOrganize(strOrganize);
        }
        employee.save();

        String groupId = model.getGroupId();
        if (StringUtils.isNotEmpty(groupId)) {
            StruGroupOfEmployee struGroupOfEmployee = ao.create(StruGroupOfEmployee.class);
            struGroupOfEmployee.setCreateDate(format.format(new Date()));
            struGroupOfEmployee.setEmployee(ao.get(StrEmployee.class, Integer.valueOf(employee.getID())));
            struGroupOfEmployee.setGroup(ao.get(DepartmentEntity.class, Integer.valueOf(model.getGroupId())));
            struGroupOfEmployee.save();
        }
        return employee;
    }

    /**
     * 查询所有雇员信�??????
     *
     * @return List<StrEmployee> 所有雇员接口的集合
     */
    public List<StrEmployee> all() {
        return Arrays.asList(ao.find(StrEmployee.class));
    }

    /**
     * 查询有效的雇员信息（公司字段不为空）
     */
    public List<StrEmployee> getValidStrEmployee() {
        return Arrays.asList(ao.find(StrEmployee.class, "STR_ORGANIZE_ID IS NOT NULL"));
    }


    /**
     * 根据雇员id删除雇员
     *
     * @param id 雇员id(系统分配)
     * @throws SQLException
     */
    public void delete(int id) throws SQLException {
        StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, String.format("EMPLOYEE_ID = '%d' ", id));
        if (strEmployeeOfRoles.length != 0){
            Arrays.stream(strEmployeeOfRoles).forEach(strEmployeeOfRole -> ao.delete(strEmployeeOfRole));
        }
        StrEmployee strEmployee = ao.get(StrEmployee.class, id);
        ao.delete(strEmployee.getStruGroupOfEmployee());
        ao.delete(strEmployee);
    }

    public void deleteTeamMember(int id) {
        StruGroupOfEmployee strEmployee = ao.get(StruGroupOfEmployee.class, id);
        ao.delete(strEmployee);
    }

    /**
     * 修改雇员信息
     *
     * @param model 传入的雇员信息对�???????
     * @return StrEmployee 一个雇员的接口
     */
    public StrEmployee update(StrEmployeeBean model) throws DuplicateKeyException {
        if (StringUtils.isNotEmpty(model.getJiraUserKey())) {
            if (ao.count(StrEmployee.class,
                    String.format("%s = ? and %s <> ?", "JIRA_USER_KEY", "ID"),
                    model.getJiraUserKey(), model.getId()) > 0)
                throw new DuplicateKeyException("JIRA_USER_KEY cannot be duplicate.");
        }
        final StrEmployee employee = ao.get(StrEmployee.class, model.getId());
        GlobalConfig.printDebug(String.valueOf(employee.getID()));
        employee.setEmail(Objects.nonNull(model.getEmail()) ? model.getEmail() : employee.getEmail());
        employee.setEmployeeName(Objects.nonNull(model.getEmployeeName()) ? model.getEmployeeName() : employee.getEmployeeName());
        employee.setEmployeeNo(Objects.nonNull(model.getEmployeeNo()) ? model.getEmployeeNo() : employee.getEmployeeNo());
        employee.setJiraUserName(Objects.nonNull(model.getJiraUserName()) ? model.getJiraUserName() : employee.getJiraUserName());
        employee.setJiraId(Objects.nonNull(model.getJiraId()) ? model.getJiraId() : employee.getJiraId());
        employee.setJiraUserKey(Objects.nonNull(model.getJiraUserKey()) ? model.getJiraUserKey() : employee.getJiraUserKey());
        employee.setEmployeeSex(Objects.nonNull(model.getEmployeeSex()) ? model.getEmployeeSex() : employee.getEmployeeSex());
        employee.setPhone(Objects.nonNull(model.getPhone()) ? model.getPhone() : employee.getPhone());
        employee.setOtherPhone(Objects.nonNull(model.getOtherPhone()) ? model.getOtherPhone() : employee.getOtherPhone());
        employee.setEmploymentStatus(Objects.nonNull(model.getEmploymentStatus()) ? model.getEmploymentStatus() : employee.getEmploymentStatus());
        employee.setEntryTime(Objects.nonNull(model.getEntryTime()) ? model.getEntryTime() : employee.getEntryTime());
        employee.setLeaveTime(Objects.nonNull(model.getLeaveTime()) ? model.getLeaveTime() : employee.getLeaveTime());
        employee.setMemo(Objects.nonNull(model.getMemo()) ? model.getMemo() : employee.getMemo());
        employee.setModifier(Objects.nonNull(model.getModifier()) ? model.getModifier() : employee.getModifier());
        employee.setSupervisor(Objects.nonNull(model.getSupervisor()) ? model.getSupervisor() : employee.getSupervisor());
        employee.setModifierDate(format.format(new Date()));//修改时的系统时间即为修改时间
        if (model.getOrgId() != null) {
            StrOrganize strOrganize = ao.get(StrOrganize.class, model.getOrgId());
            employee.setStrOrganize(strOrganize);
        }else
        {
            employee.setStrOrganize(null);
        }
        employee.save();

        String groupId = model.getGroupId();
        StruGroupOfEmployee struGroupOfEmployee = getGroupMember(employee.getStruGroupOfEmployee());
        if (struGroupOfEmployee == null) { // 关联�??????
            if (StringUtils.isEmpty(groupId)) { // 部门id为空�?????? 不处�??????
            } else { //  需要创建关联表';

                struGroupOfEmployee = ao.create(StruGroupOfEmployee.class);
                struGroupOfEmployee.setEmployee(employee);
                DepartmentEntity departmentEntity = ao.get(DepartmentEntity.class, Integer.valueOf(groupId));
                struGroupOfEmployee.setGroup(departmentEntity);
                struGroupOfEmployee.setPostType(departmentEntity.getType());
                struGroupOfEmployee.save();
            }
        } else {
            if (StringUtils.isEmpty(groupId)) { // 删除
                ao.delete(struGroupOfEmployee);
                ao.flushAll();
            } else { // 更新
                struGroupOfEmployee.setGroup(ao.get(DepartmentEntity.class, Integer.valueOf(groupId)));
                struGroupOfEmployee.save();
            }
        }

        return employee;
    }

    public StrEmployee[] getByJiraId(String jiraId) {
        StrEmployee[] employee = ao.find(StrEmployee.class, MessageFormat.format("JIRA_USER_KEY = {0}{1}{2}", "'", jiraId, "'"));
        return employee;
    }

    public List<StrEmployee> getEmployeeByOrgId(int id) {
        List<StrOrganize> orgList = Lists.newArrayList();

        StrOrganize thisStrOrganize = ao.get(StrOrganize.class, id);
        if (thisStrOrganize != null) {
            orgList.add(thisStrOrganize); // 将该组织放到list
        }

        addSubOrgById(id, orgList); // 将子机构放入list

        List<StrEmployee> employeeList = Lists.newArrayList();

        orgList.forEach(strOrganize -> {
            StrEmployee[] strEmployees = strOrganize.getStrEmployee();
            employeeList.addAll(Lists.newArrayList(strEmployees));


            StrOrganizeGroup[] organizeGroups = strOrganize.getStrOrganizeGroup(); // 获取该组织的直接子部�????
            Lists.newArrayList(organizeGroups).forEach(organizeGroup -> {
                DepartmentEntity departmentEntity = organizeGroup.getGroup();

                List<Integer> collect = employeeList.stream().map(e -> e.getID()).collect(Collectors.toList());
                List<StrEmployee> groupEmployees = getEmployeeListByGroupId(departmentEntity.getID(), 0);
                groupEmployees.forEach(employee -> {
                    if (!collect.contains(employee.getID())) {
                        employeeList.add(employee);
                    }
                });
            });
        });

        return employeeList;
    }

    public StrEmployee[] getEmployeeByGroupId(int id) {
        return getEmployeeListByGroupId(id, 0).toArray(new StrEmployee[0]);
    }

    @Override
    public List<StruGroupOfEmployee> getStruGroupOfEmployeeByOrgId(int id) {
        List<StrOrganize> orgList = Lists.newArrayList();

        StrOrganize thisStrOrganize = ao.get(StrOrganize.class, id);
        if (thisStrOrganize != null) {
            orgList.add(thisStrOrganize); // 将该组织放到list�??????
        }

        addSubOrgById(id, orgList); // 将子机构放入list�??????

        List<String> orgIdList = Lists.newArrayList();
        List<StruGroupOfEmployee> employeeList = Lists.newArrayList();

        orgList.forEach(strOrganize -> {
            orgIdList.add(String.valueOf(strOrganize.getID()));
            StrOrganizeGroup[] organizeGroups = strOrganize.getStrOrganizeGroup(); // 获取该组织的直接子部�??????
            Lists.newArrayList(organizeGroups).forEach(organizeGroup -> {
                DepartmentEntity departmentEntity = organizeGroup.getGroup();
                employeeList.addAll(getStruGroupOfEmployeeByTeamId(departmentEntity.getID())); // 获取部门以及子部门下的雇�??????
            });
        });

        return employeeList;
    }


    public List<StruGroupOfEmployee>  getStruGroupOfEmployeeByTeamId(int id) {
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0} AND POST_TYPE = {1}", id, 1));
        List<DepartmentEntity> depts = Lists.newArrayList();
        addSubDeptById(id, depts);

        List<StruGroupOfEmployee> strEmployees = Lists.newArrayList(struGroupOfEmployees);

        depts.stream().forEach(dept -> {
            StruGroupOfEmployee[] groupOfEmployees = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0} AND POST_TYPE = {1}", dept.getID(), 1));
            strEmployees.addAll(
                    Lists.newArrayList(groupOfEmployees)
            );
        });
        return strEmployees;
    }

    public List<StruGroupOfEmployee> getDirectStruGroupOfEmployeeByTeamId(int id) {
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0} AND POST_TYPE = {1}", id, 1));
        return Lists.newArrayList(struGroupOfEmployees);
    }

    private List<StrEmployee> getEmployeeListByGroupId(int id, int type) {
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0} AND POST_TYPE = {1}", id, type));
        List<DepartmentEntity> depts = Lists.newArrayList();
        addSubDeptById(id, depts);

        List<StrEmployee> strEmployees = Lists.newArrayList(
                Lists.newArrayList(struGroupOfEmployees).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new));

        depts.stream().forEach(dept -> {
            StruGroupOfEmployee[] groupOfEmployees = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0} AND POST_TYPE = {1}", dept.getID(), type));
            strEmployees.addAll(
                    Lists.newArrayList(
                            Lists.newArrayList(groupOfEmployees).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new)
                    )
            );
        });
        return strEmployees;
    }

    /**
     * 查询组织机构的子机构
     *
     * @param id
     * @param list
     */
    private void addSubOrgById(int id, List<StrOrganize> list) {
        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format(" PARENT = '%s' and ID <> %d", id, id));
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
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, String.format(" PARENT = '%s' and ID <> %d", id, id));
        for (DepartmentEntity departmentEntity : departmentEntities) {
            if (departmentEntity.getParent() != null) {
                addSubDeptById(departmentEntity.getID(), list);
            }
            list.add(departmentEntity);
        }
    }

    public StrEmployee getEmployee(int employeeId) {
        StrEmployee employee = ao.get(StrEmployee.class, employeeId);


        return employee;
    }

    /**
     * @param EmployeeId
     */
    public int getByEmployeeId(String EmployeeId) {
        StruGroupOfEmployee[] struGroupOfEmployee = ao.find(StruGroupOfEmployee.class, MessageFormat.format("EMPLOYEE_ID = {0}", EmployeeId));
        for (StruGroupOfEmployee StruGroupOfEmployeeEntity : struGroupOfEmployee) {
            if (StruGroupOfEmployeeEntity.getGroup() != null) {
                return StruGroupOfEmployeeEntity.getGroup().getID();
            }

        }
        return 0;
    }


    public int getCountByUserofJira() {
        int c = 0;
        c = ao.count(StrEmployee.class,
                "JIRA_USER_KEY <> ''");
        return c;
    }

    @Override
    public List<StrEmployee> getAvailableEmployeesByTeamId(String teamId) {

        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, "POST_TYPE = 1 AND GROUP_ID = " + teamId);
        String[] employeeIds = Lists.newArrayList(struGroupOfEmployees).stream().map(e -> e.getEmployee().getID() + "").toArray(String[]::new);
        String whereSql = "POST_TYPE = 0";
        if (employeeIds.length > 0) {
            whereSql += " AND EMPLOYEE_ID NOT IN (" + StringUtils.join(employeeIds, ",") + ")";
        }
        struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, whereSql);

        return Lists.newArrayList(struGroupOfEmployees).stream().map(e -> e.getEmployee()).collect(Collectors.toList());
    }

    @Override
    public void addEmployeeToTeam(Integer teamId, Integer employeeId, Integer roleId) {
        StruGroupOfEmployee struGroupOfEmployee = ao.create(StruGroupOfEmployee.class);
        struGroupOfEmployee.setCreateDate(format.format(new Date()));
        struGroupOfEmployee.setPostType(1);
        struGroupOfEmployee.setGroup(ao.get(DepartmentEntity.class, teamId));
        struGroupOfEmployee.setEmployee(ao.get(StrEmployee.class, Integer.valueOf(employeeId)));
        struGroupOfEmployee.save();

        saveEmployeeRole(teamId,roleId,employeeId);
    }

    public void addEmployeeToTeam(int teamId, List<String> employeeIds, List<String> roleIds) {
        List<StruGroupOfEmployee> struGroupOfEmployees = getDirectStruGroupOfEmployeeByTeamId(teamId);
        Map<Integer, Integer> map = new HashMap<>();
        struGroupOfEmployees.forEach(struGroupOfEmployee ->
                map.put(struGroupOfEmployee.getEmployee().getID(), struGroupOfEmployee.getID()));

        employeeIds.forEach(employeeId -> {

            if (map.keySet().contains(employeeId)) {
                map.remove(employeeId);
            } else {
                int i = employeeIds.indexOf(employeeId);
                String roleId = roleIds.get(i);

                StruGroupOfEmployee struGroupOfEmployee = ao.create(StruGroupOfEmployee.class);
                struGroupOfEmployee.setCreateDate(format.format(new Date()));
                struGroupOfEmployee.setPostType(1);
                struGroupOfEmployee.setGroup(ao.get(DepartmentEntity.class, teamId));
                struGroupOfEmployee.setEmployee(ao.get(StrEmployee.class, Integer.valueOf(employeeId)));
                struGroupOfEmployee.save();
            }
        });
        map.keySet().stream().forEach(oldEmployeeId -> deleteTeamMember(map.get(oldEmployeeId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmployeeByName(String name) {
        return getEmployeeByName(name).isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Employee> getEmployeeByName(String name) {
        val entities = Stream.of(ao.find(StrEmployee.class, "EMPLOYEE_NAME = ?", name))
                .map(CONVERT_TO_EMPLOYEE_API)
                .collect(Collectors.toList());
        return (entities.size() > 0) ? Optional.of(entities.get(0)) : Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Employee updateEmployeeByImport(Employee employee) {
        String email=employee.getEmail();
        StrEmployee[] strEmployees=ao.find(StrEmployee.class, String.format("EMAIL = '%s' ", email));
        if (strEmployees.length!=0){
            StrEmployee strEmployee=strEmployees[0];
            strEmployee.setStrOrganize(null);
            strEmployee.setEmployeeName(employee.getName());
            strEmployee.save();
            StruGroupOfEmployee[] struGroupOfEmployees=ao.find(StruGroupOfEmployee.class, String.format("EMPLOYEE_ID = '%d' ", strEmployee.getID()));
            if (struGroupOfEmployees.length!=0){
            ao.delete(struGroupOfEmployees[0]);
            }
            return CONVERT_TO_EMPLOYEE_API.apply(strEmployee);
        }
        val safeApi = getEmployeeByName(employee.getName());
        if (safeApi.isPresent()) {
            System.out.println(safeApi.get().getName());
            val entity = ao.get(StrEmployee.class, safeApi.get().getId());
            entity.setEmail(Optional.ofNullable(email).orElse(safeApi.get().getEmail()));
            //entity.setJiraUserKey(Optional.ofNullable(employee.getJiraUserKey()).orElse(safeApi.get().getJiraUserKey()));
            //entity.setPhone(Optional.ofNullable(employee.getPhone()).orElse(safeApi.get().getPhone()));
            entity.setEmploymentStatus(Optional.ofNullable(employee.getStatus()).orElse(safeApi.get().getStatus()));
            //entity.setEntryTime(Optional.ofNullable(employee.getEntryTime()).orElse(safeApi.get().getEntryTime()));
            entity.setEmployeeSex(Optional.ofNullable(employee.getSex()).orElse(safeApi.get().getSex()));
            //entity.setJiraId(Optional.ofNullable(employee.getJiraId()).orElse(safeApi.get().getJiraId()));
            entity.save();
            return CONVERT_TO_EMPLOYEE_API.apply(entity);
        } else {
            val entity = ao.create(
                    StrEmployee.class,
                    new DBParam("EMAIL", email),
                    new DBParam("EMPLOYEE_NAME", employee.getName()),
                    //new DBParam("JIRA_USER_KEY", employee.getJiraUserKey()),
                    new DBParam("EMPLOYEE_SEX", employee.getSex()),
                    //new DBParam("PHONE", employee.getPhone()),
                    new DBParam("EMPLOYMENT_STATUS", employee.getStatus())
                    //new DBParam("ENTRY_TIME", employee.getEntryTime())
            );
            return CONVERT_TO_EMPLOYEE_API.apply(entity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGroupOfEmployeeByImport(int employeeId, int groupId) {
        val employee = ao.get(StrEmployee.class, employeeId);
        val group = ao.get(DepartmentEntity.class, groupId);

        if (group.getType() == 0) {
            // 删除其他部门关系
            val DEPARTMENT = DepartmentEntity.class.getSimpleName();
            val GROUP_OF_EMPLOYEE = StruGroupOfEmployee.class.getSimpleName();
            val query = Query.select()
                    .where(String.format("%s.EMPLOYEE_ID = ? AND %s.TYPE = ?", GROUP_OF_EMPLOYEE, DEPARTMENT), employeeId, 0)
                    .alias(DepartmentEntity.class, DEPARTMENT)
                    .alias(StruGroupOfEmployee.class, GROUP_OF_EMPLOYEE)
                    .join(DepartmentEntity.class, String.format("%s.ID = %s.GROUP_ID", DEPARTMENT, GROUP_OF_EMPLOYEE));
            Stream.of(ao.find(StruGroupOfEmployee.class, query)).forEach(ao::delete);
        } else if (group.getType() == 1) {
            // 已经一样团队关系有，跳�??????
            val relations = ao.find(StruGroupOfEmployee.class, "EMPLOYEE_ID = ? AND GROUP_ID = ?", employee.getID(), group.getID());
            if (relations.length > 0)
                return;
        }

        // 创建关系
        val relation = ao.create(StruGroupOfEmployee.class);
        relation.setEmployee(employee);
        relation.setGroup(group);
        relation.save();
    }


    public static StruGroupOfEmployee getGroupMember(StruGroupOfEmployee[] struGroupOfEmployees) {
        Optional<StruGroupOfEmployee> first =
                Lists.newArrayList(struGroupOfEmployees).stream().filter(e -> e.getPostType() == 0).findFirst();

        return first.orElse(null);

    }

    public static StruGroupOfEmployee[] getTeamMembers(StruGroupOfEmployee[] struGroupOfEmployees) {
        return Lists.newArrayList(struGroupOfEmployees).stream().filter(e -> e.getPostType() == 1).toArray(StruGroupOfEmployee[]::new);
    }

    private Function<StrEmployee, Employee> CONVERT_TO_EMPLOYEE_API =
            e -> Employee.builder().id(e.getID()).email(e.getEmail()).name(e.getEmployeeName()).no(e.getEmployeeNo())
                    .jiraUserName(e.getJiraUserName()).jiraUserKey(e.getJiraUserKey()).jiraId(e.getJiraId())
                    .sex(e.getEmployeeSex()).phone(e.getPhone()).otherPhone(e.getOtherPhone())
                    .status(e.getEmploymentStatus()).entryTime(e.getEntryTime()).leaveTime(e.getLeaveTime())
                    .memo(e.getMemo()).owner(e.getOwner()).createDate(e.getCreateDate()).modifier(e.getModifier())
                    .modifierDate(e.getModifierDate()).build();


//维护部门-雇员间的关系
    @Override
    public void maintainRelationGroupEmployee(StrEmployee e, DepartmentEntity departmentEntity, int modifireId, String createTime, int owner, int postType) {
        if (departmentEntity != null) {
            try {
                String groupName = departmentEntity.getGroupName();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                String maintaintime = simpleDateFormat.format(new Date());
                StruGroupOfEmployee struGroupOfEmployee = ao.create(StruGroupOfEmployee.class);
                struGroupOfEmployee.setCreateDate(createTime);
                struGroupOfEmployee.setEmployee(e);
                struGroupOfEmployee.setGroup(departmentEntity);
                struGroupOfEmployee.setModifierDate(maintaintime);
                struGroupOfEmployee.setPostType(postType);
                struGroupOfEmployee.save();
            } catch (Exception ee) {
            }
        }
    }

    @Override
    public StruGroupOfEmployee getDepart(int employeeId) {
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, String.format(" EMPLOYEE_ID = '%d' ", employeeId));
        if (struGroupOfEmployees.length == 0) {
            return null;
        } else {
            for (StruGroupOfEmployee struGroupOfEmployee : struGroupOfEmployees) {
                return struGroupOfEmployee;
            }
        }
        return null;
    }

    @Override
    public void deleteRalationEmployeeAndDepart(int employeId) {
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, String.format(" EMPLOYEE_ID = '%d' ", employeId));
        if (struGroupOfEmployees.length != 0) {
            for (StruGroupOfEmployee struGroupOfEmployee : struGroupOfEmployees) {
                ao.delete(struGroupOfEmployee);
            }
        }
    }

    @Override
    public List<StrEmployee> allAppoint(long directoryId) {
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format(" LDAP_ID = '%d' ", directoryId));
        return Arrays.asList(strEmployees);
    }

    public StrEmployee[] queryMember(String memberStr) {
        Query select = Query.select();
        if (StringUtils.isNotEmpty(memberStr)) {
            select.where(MessageFormat.format("EMPLOYEE_NAME like {0}{1}{2}", "'%", memberStr, "%'"));
        }

        StrEmployee[] strEmployees = ao.find(StrEmployee.class, select.limit(10));
        return strEmployees;
    }
    @Override
    public void setLdapToNull(long directoryId) {
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format("LDAP_ID = '%d'", directoryId));
        for (StrEmployee strEmployee : strEmployees) {
            strEmployee.setLdapId(0);
            strEmployee.save();
        }
    }

    @Override
    public boolean isExistEmployeeNo(String employeeNo, String employeeId) {
        int count;
/*连接数据库获取，员工号查看是否重复*/
        if (StringUtils.isNotEmpty(employeeId)||!employeeNo.equals("")) {
            count = ao.count(StrEmployee.class,
//                    "EMPLOYEE_NO = ? AND (ID <> ? OR ID IS NULL)",
                    "EMPLOYEE_NO = ? AND ID <> ?",
                    StringUtils.defaultString(employeeNo), Integer.valueOf(employeeId));
        } else {
            count = ao.count(StrEmployee.class,
                    String.format("%s = ?", "EMPLOYEE_NO"),
                    StringUtils.defaultString(employeeNo));
        }
     //return  count>0?true:false;
       return true;
    }
    @Override
    public StrEmployee getByJiraUserKey(String name) {
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format(" JIRA_USER_KEY = '%s' ", name));
        if (strEmployees.length != 0) {
              return  strEmployees[0];
        }
        return null;
    }

    @Override
    public void maintainRelationRoleAndEmployee(StrEmployee strEmployee, RoleEntity roleEntity) {
        StrEmployeeOfRole[] strEmployeeOfRoles=ao.find(StrEmployeeOfRole.class, String.format(" EMPLOYEE_ID = '%d' AND ROLE_ID = '%d' ", strEmployee.getID(), roleEntity.getID()));
        if (strEmployeeOfRoles.length == 0) {
            StrEmployeeOfRole strEmployeeOfRole=ao.create(StrEmployeeOfRole.class);
            strEmployeeOfRole.setEmployee(strEmployee);
            strEmployeeOfRole.setRole(roleEntity);
            strEmployeeOfRole.save();
        }
    }

    @Override
    public Set<RoleEntity> getRoleByJiraUserKey(String name) {
        Set<RoleEntity> roleSet = new HashSet<>();
        StrEmployee byJiraUserKey = getByJiraUserKey(name);
        if (byJiraUserKey == null) {
            if (name.equals("admin")) {
                //TODO  此为测试代码
                roleSet.add(ao.find(RoleEntity.class,String.format("NAME =  '%s' ","System Administrator"))[0]);
                return roleSet;
            }
        } else {
            if (name.equals("admin")) {
                //TODO  此为测试代码
                roleSet.add(ao.find(RoleEntity.class,String.format("NAME =  '%s' ","System Administrator"))[0]);
                return roleSet;
            }else {
            StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, String.format(" EMPLOYEE_ID  = '%d' ", byJiraUserKey.getID()));
            for (StrEmployeeOfRole strEmployeeOfRole : strEmployeeOfRoles) {
                RoleEntity role = strEmployeeOfRole.getRole();
                roleSet.add(role);
            }
            }
        }
        return roleSet;
    }

    @Override
    public StrEmployee createOrgUser(String OrgName, String UserName) {
        StrOrganize[] strOrganizes=ao.find(StrOrganize.class, String.format("NAME = '%s'", OrgName));
        if (strOrganizes.length!=0){
            StrOrganize strOrganize=strOrganizes[0];
            StrEmployee strEmployee=ao.create(StrEmployee.class);
            strEmployee.setJiraUserName(UserName);
            strEmployee.setCreateDate(new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date()));
            strEmployee.setStrOrganize(strOrganize);
        }
        return null;
    }

    @Override
    public StrEmployee getByEmail(String emial) {
        StrEmployee[] strEmployees=ao.find(StrEmployee.class, String.format("EMAIL = '%s'", emial));
        if (strEmployees.length!=0){
            return strEmployees[0];
        }else {
            return null;
        }
    }

    @Override
    public void updateOrgOfEmployeeByImport(int employeeId, int organizeId) {
        StrEmployee strEmployee=ao.get(StrEmployee.class, employeeId);
        StrOrganize strOrganize=ao.get(StrOrganize.class, organizeId);
        strEmployee.setStrOrganize(strOrganize);
        strEmployee.save();
    }

    @Override
    public StrEmployee[] getEmployeeRelatedJirauserkey() {
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, "JIRA_USER_KEY is not null");
        return strEmployees;
    }

    @Override
    public StrEmployeeOfRole saveEmployeeRole(Integer teamId, Integer roleId, Integer strEmployeeId) {
        StrEmployeeOfRole strEmployeeOfRole = ao.create(StrEmployeeOfRole.class);
        strEmployeeOfRole.setTeamId(teamId);
        StrEmployee strEmployee = ao.get(StrEmployee.class, strEmployeeId);
        strEmployeeOfRole.setEmployee(strEmployee);
        RoleEntity roleEntity = ao.get(RoleEntity.class, roleId);
        strEmployeeOfRole.setRole(roleEntity);
        strEmployeeOfRole.save();
        return strEmployeeOfRole;
    }

    @Override
    public RoleEntity getRoleByEmployeeAndTeam(Integer teamId, Integer strEmployeeId) {
        StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, String.format("%s = ? AND %s = ?", "TEAM_ID", "EMPLOYEE_ID"),
                teamId, strEmployeeId
        );
        if (strEmployeeOfRoles.length == 0) {
            return roleService.getDefaultTeamRole();
        } else {
            return strEmployeeOfRoles[0].getRole();
        }

    }

    /**
     * 获取团队的成员
     * @param teamId
     * @return
     */
    @Override
    public StrEmployeeOfRole[] listEmployeeRoleByTeamid(Integer teamId) {
        StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, "TEAM_ID = " + teamId);

        return strEmployeeOfRoles;
    }

    private final RoleService roleService;
    @Override
    public List<StrEmployee> getSubEmployee(Integer employeeId) {
        List<StrEmployee> employeeList = Lists.newArrayList();

        List<StrEmployeeOfRole> strEmployeeOfRoleList = roleService.getRoleByEmployeeId(employeeId);
        strEmployeeOfRoleList.forEach(strEmployeeOfRole -> {
            //获取他的角色
            RoleEntity role = strEmployeeOfRole.getRole();
            int roleId = role.getID();
            if (roleId == 10 || roleId == 11 || roleId == 12) { // 三个基础角色，不用判�??
                return;
            }
            // 它的下级角色
            List<RoleEntity> lowerByRole = roleService.getLowerByRole(role);
            if (lowerByRole == null) {
                return;
            }
            Integer teamId = strEmployeeOfRole.getTeamId();
            List<StruGroupOfEmployee> list = Lists.newArrayList(getDirectStruGroupOfEmployeeByTeamId(teamId));
            list.forEach(struGroupOfEmployee -> {

                StrEmployee employee = struGroupOfEmployee.getEmployee();
                int id = employee.getID();
                if (id == employeeId) {
                    return; // 自己，跳�??
                }
                RoleEntity roleByEmployeeAndTeam = getRoleByEmployeeAndTeam(teamId, id);
                boolean b = lowerByRole.stream().anyMatch(roleEntity ->
                        roleEntity.getID() == roleByEmployeeAndTeam.getID()
                );
                if (b) {
                    employeeList.add(employee);
                }
            });
        });
        return employeeList;
    }


    public List<String> getOrgRoleByUserName(String userName) {
        if(GlobalReport.testKey.size()!=0){
            GlobalReport.testKey.clear();
        }
        GlobalReport.testKey.add("传入的参数uaerName："+userName+",employee表中所有数据如下：");
        StrEmployee[] tests = ao.find(StrEmployee.class);
        for(StrEmployee employee : tests){
            GlobalReport.testKey.add("employee.getID():"+employee.getID()+",");
            GlobalReport.testKey.add("employee.getEmployeeName():"+employee.getEmployeeName()+",");
            GlobalReport.testKey.add("employee.getEmployeeNo():"+employee.getEmployeeNo()+",");
            GlobalReport.testKey.add("employee.getJiraUserKey():"+employee.getJiraUserKey()+",");
            GlobalReport.testKey.add("employee.getJiraUserName():"+employee.getJiraUserName()+",");
            GlobalReport.testKey.add("employee.getJiraId():"+employee.getJiraId()+",");
            GlobalReport.testKey.add("employee.getLdapId():"+employee.getLdapId()+",");
            GlobalReport.testKey.add("employee.getEmploymentStatus():"+employee.getEmploymentStatus()+",");
            GlobalReport.testKey.add("employee.getStrEmployeeOfRole().toString():"+employee.getStrEmployeeOfRole().toString()+",");
            GlobalReport.testKey.add("employee.getStrOrganize().getName():"+employee.getStrOrganize().getName()+"    ###    ");
        }
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format("JIRA_USER_KEY = '%s' ", userName));
        GlobalReport.testKey.add("查询语句："+String.format("JIRA_USER_KEY = '%s' ", userName).toString()+",");
//        StrEmployee[] strEmployees = ao.find(StrEmployee.class, Query.select().where(String.format("JIRA_USER_KEY = '%s' ", userName)));
        GlobalReport.testKey.add("查询语句2："+ Query.select().where(String.format("JIRA_USER_KEY = '%s' ", userName)).toString()+",");
        GlobalReport.testKey.add("查询出的数据长度strEmployees.length："+strEmployees.length+",");
        GlobalReport.testKey.add("strEmployees[0].getID()："+strEmployees[0].getID()+",");
        StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, String.format("EMPLOYEE_ID = '%d' ", strEmployees[0].getID()));
        GlobalReport.testKey.add("strEmployeeOfRoles.length："+strEmployeeOfRoles.length+"---  结束 ---");
        List<String> collect = Arrays.stream(strEmployeeOfRoles).map(strEmployeeOfRole -> strEmployeeOfRole.getRole().getName()).collect(Collectors.toList());
        return collect;
    }

    @Override
    public void setJiraUserKeyNull(long ls) {
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format("LDAP_ID = '%d' ", ls));
        Stream.of(strEmployees).forEach(s -> {s.setJiraUserKey(null);s.save();});
    }

    @Override
    public StrEmployee[] getCorrespondingFieldsStrEmployees(long directoryId) {
        return  ao.find(StrEmployee.class, String.format("LDAP_ID =  '%d' ",directoryId));
    }

    @Override
    public StrEmployee[] getEmployeeByJiraUserKeyAndDirectoryId(String ExsitJiraUserKey, long directoryId) {
        return   ao.find(StrEmployee.class, String.format(" JIRA_USER_KEY = '%s' AND  LDAP_ID = '%d' ", ExsitJiraUserKey, directoryId));
    }

    @Override
    public void setOrgRoleToEmployee(StrEmployee strEmployee, Set<RoleEntity> roleEntitySet) {
        StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, String.format("EMPLOYEE_ID = '%d' ", strEmployee.getID()));
        if (strEmployeeOfRoles.length != 0) {
            Set<RoleEntity> exitRole = new HashSet<>();
            Arrays.stream(strEmployeeOfRoles).forEach(s -> {
                exitRole.add(s.getRole());
            });
            Set<RoleEntity> needToCreate = new HashSet<>();
            needToCreate.addAll(roleEntitySet);
            needToCreate.removeAll(exitRole);
            needToCreate.stream().forEach(s -> {
                /*if (s.getName().equals("System Administrator")&& StringUtils.isNotBlank(strEmployee.getJiraUserKey())){
                    addJiraUserToGroup(strEmployee,"System Administrator");
                }*/
                mainRoleAndEmployee(strEmployee, s);
            });
            Set<RoleEntity> needToDel = new HashSet<>();
            needToDel.addAll(exitRole);
            needToDel.removeAll(roleEntitySet);
            needToDel.stream().forEach(s -> {
               /* if (s.getName().equals("System Administrator")&& StringUtils.isNotBlank(strEmployee.getJiraUserKey())){
                    delJiraUserFromGroup(strEmployee,"System Administrator");
                }*/
                StrEmployeeOfRole[] strEmployeeOfRoles1 = ao.find(StrEmployeeOfRole.class, String.format("EMPLOYEE_ID = '%d' AND ROLE_ID = '%d' ", strEmployee.getID(), s.getID()));
                Arrays.stream(strEmployeeOfRoles1).forEach(ss ->{ao.delete(ss);});
            });
        } else {
            roleEntitySet.stream().forEach(s -> {
               /* if (s.getName().equals("System Administrator")&& StringUtils.isNotBlank(strEmployee.getJiraUserKey())){
                    addJiraUserToGroup(strEmployee,"System Administrator");
                }*/
                mainRoleAndEmployee(strEmployee, s);
            });
        }
    }
    private void mainRoleAndEmployee(StrEmployee employee,RoleEntity roleEntity){
        StrEmployeeOfRole strEmployeeOfRole = ao.create(StrEmployeeOfRole.class);
        strEmployeeOfRole.setEmployee(employee);
        strEmployeeOfRole.setRole(roleEntity);
        strEmployeeOfRole.setTeamId(-1);
        strEmployeeOfRole.save();
    }

    public static String getRoleId(StrEmployeeOfRole[] strEmployeeOfRoles){
        final String[] roleId = {""};
        Arrays.stream(strEmployeeOfRoles).forEach(s ->{
            roleId[0] += s.getRole().getID();
            roleId[0] += ",";
        });
        return roleId[0];
    };
    private void addJiraUserToGroup(StrEmployee strEmployee,String roleName){
        ApplicationUser userByKey = userManager.getUserByKey(strEmployee.getJiraUserKey());
        try {
            groupManager.addUserToGroup(userByKey,groupManager.getGroup(roleName));
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        } catch (OperationNotPermittedException e) {
            e.printStackTrace();
        } catch (OperationFailedException e) {
            e.printStackTrace();
        }
    }
    private void delJiraUserFromGroup(StrEmployee strEmployee,String roleName){
        User user = crowdService.getUser(strEmployee.getJiraUserKey());
        try {
            crowdService.removeUserFromGroup(user,groupManager.getGroup(roleName));
        } catch (OperationNotPermittedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public List<LowerIssuesChildBean> getAllLowerIssues(StrEmployee strEmployee){
        List<LowerIssuesChildBean> lowerIssuesChildBeanList = new ArrayList<LowerIssuesChildBean>();
        StrOrganize strOrganize = strEmployee.getStrOrganize();
        String employeeName = strEmployee.getEmployeeName();
       /* if (employeeName.equals("DsdAdmin")){
            return lowerIssuesChildBeanList;
        }*/
        if (strOrganize!=null){
            strOrganize =getSupOrgByOrg(strOrganize);
            lowerIssuesChildBeanList.add(buildChildrenOrg(strOrganize,strEmployee));
        }else {
            strOrganize= getOrgByEmployee(strEmployee);
            lowerIssuesChildBeanList.add(buildChildrenOrg(strOrganize,strEmployee));
        }
        return lowerIssuesChildBeanList;
    }
    Map<Integer ,Map<Integer,List<String>>> mapChild = new HashMap<>();//888  999  / pid /lowerIssueBean
    private LowerIssuesChildBean buildChildrenOrg(StrOrganize strOrganize,StrEmployee strEmployeeLogin){
        mapChild.clear();
        LowerIssuesChildBean issuesChildBean = new LowerIssuesChildBean();
        Set<Integer> orgNum = getOrgNum(strEmployeeLogin);
        DepartmentEntity departmentEntity = getCurrentDep(strEmployeeLogin);
        List<Integer> deplist = new ArrayList();
        List<Integer> depList = new ArrayList();
        if (departmentEntity != null) {
            deplist = getAllDeplist(departmentEntity, deplist);
            deplist.add(departmentEntity.getID());
            depList = getDeplist(departmentEntity, depList);
            List<List> collect = depList.stream().map(s -> toRemoveDepId(s)).collect(Collectors.toList());
            List<Integer> finalDeplist = deplist;
            collect.stream().forEach(list -> finalDeplist.removeAll(list));
            issuesChildBean = getLowerIsseu(strOrganize, strEmployeeLogin, orgNum, finalDeplist);
            List<Integer> allDeplistByOrgId = new ArrayList<>();
            allDeplistByOrgId = allDeplistIdByOrgId(allDeplistByOrgId, strOrganize);
            List<String> strings = new ArrayList<>();
            allDeplistByOrgId.stream().forEach(integer -> {
                if (mapChild.containsKey(888)) {
                    if (mapChild.get(888).containsKey(integer)) {
                        strings.addAll(mapChild.get(888).get(integer));
                    }
                }
            });
            LowerIssuesBean issueInfo = getIssueInfo(strings);
            issuesChildBean.setLowerIssuesBean(issueInfo);
        }else {
            issuesChildBean = getLowerIsseu(strOrganize, strEmployeeLogin, orgNum, depList);
            System.out.println(issuesChildBean);
            List<Integer> allDeplistByOrgId = new ArrayList<>();
            allDeplistByOrgId = allDeplistIdByOrgId(allDeplistByOrgId, strOrganize);
            List<String> strings = new ArrayList<>();
            strings.add(strEmployeeLogin.getJiraUserKey());
            LowerIssuesBean issueInfo = getIssueInfo(strings);
            issuesChildBean.setLowerIssuesBean(issueInfo);
        }
        //   System.out.println(finalDeplist);
        // System.out.println(issuesChildBean);
        return issuesChildBean;
    }
    private LowerIssuesChildBean getLowerIsseu(StrOrganize strOrganize,StrEmployee strEmployee,Set<Integer> orgNum,List<Integer> depList){
        //mapChild.clear();
        LowerIssuesChildBean issuesChildBean = new LowerIssuesChildBean();
        int orgNo = strOrganize.getID();
        issuesChildBean.setName(strOrganize.getName());
        //issuesChildBean.setLowerIssuesBean(getLowerIssuesByOrgNo(orgNo));
        issuesChildBean.setLowerIssuesBean(new LowerIssuesBean(0,0,0,0));
        issuesChildBean.setType(999);
        issuesChildBean.setPid(orgNo);
        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", orgNo, orgNo));//此机构下的所有机构
        if(strOrganizes.length != 0){
            List<LowerIssuesChildBean> temps = new ArrayList<LowerIssuesChildBean>();
            for(StrOrganize org : strOrganizes){
                if (orgNum.contains(org.getID())){
                    orgNum.remove(org.getID());
                    temps.add(getLowerIsseu(org,strEmployee,orgNum,depList));
                }
            }
            issuesChildBean.setChildrens(temps);
        }
        StrOrganizeGroup[] organizeGroups = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", orgNo));
        List<StrOrganizeGroup> collect1 = Arrays.stream(organizeGroups).filter(strOrganizeGroup -> {
            if (strOrganizeGroup.getGroup().getType() == 0) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        //判断机构下是否有直属部门
            List<LowerIssuesChildBean> lists = new ArrayList<LowerIssuesChildBean>();
        if(collect1.size() != 0){
            DepartmentEntity[]  depts = Lists.newArrayList(collect1).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 0).toArray(DepartmentEntity[]::new);
            for(DepartmentEntity dept : depts){
                if (depList.contains(dept.getID())){
                    Set<Integer> collect = depList.stream().collect(Collectors.toSet());
                    collect.remove(dept.getID());
                    depList= collect.stream().collect(Collectors.toList());
                    lists.add(buildChildrenDept(dept,depList));
                }
            }

            if (depList.size() == 0 && orgNo== strEmployee.getStrOrganize().getID() && strEmployee.getStruGroupOfEmployee().length == 0) {
                Map<Integer,List<String>> map=new HashMap<>();
                List<String> list = new ArrayList<>();
                list.add(strEmployee.getJiraUserKey());
                map.put(strEmployee.getID(),list);
                mapChild.put(777,map);
                    LowerIssuesChildBean employeeBmryfb = getEmployeeBmryfb(strEmployee.getID());
                    lists.add(employeeBmryfb);
            }
            if(issuesChildBean.getChildrens() != null){
                lists.addAll(issuesChildBean.getChildrens());
            }
            issuesChildBean.setChildrens(lists);
        }else {
            if (depList.size() == 0 && orgNo== strEmployee.getStrOrganize().getID() && strEmployee.getStruGroupOfEmployee().length == 0) {
                Map<Integer,List<String>> map=new HashMap<>();
                List<String> list = new ArrayList<>();
                list.add(strEmployee.getJiraUserKey());
                map.put(strEmployee.getID(),list);
                mapChild.put(777,map);
                LowerIssuesChildBean employeeBmryfb = getEmployeeBmryfb(strEmployee.getID());
                lists.add(employeeBmryfb);
            }
            if(issuesChildBean.getChildrens() != null){
                lists.addAll(issuesChildBean.getChildrens());
            }
            issuesChildBean.setChildrens(lists);
        }
        issuesChildBean= addDepINfo(issuesChildBean);
        Set<Integer> orgNums = getOrgNum(strEmployee);
        orgNums.stream().forEach(integer ->{
            Map<Integer,List<String>> map=new HashMap<>();
            List<String> list = new ArrayList<>();
            map.put(integer,getUserKeyByOrgId(integer,list));
            if (mapChild.containsKey(999)){
                mapChild.get(999).putAll(map);
            }else {
                mapChild.put(999,map);
            }
        } );
        issuesChildBean =addOrgInfo(issuesChildBean);
        return issuesChildBean;
    }
    private List<String> getUserKeyByOrgId(Integer orgid,List<String> list){
        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format("PARENT = '%d' ", orgid));
        if (strOrganizes.length != 0){
            Arrays.stream(strOrganizes).forEach(strOrganize -> getUserKeyByOrgId(strOrganize.getID(),list));
        }
        StrOrganizeGroup[] strOrganizeGroups = ao.find(StrOrganizeGroup.class, String.format("ORG_ID = '%d' ", orgid));
        Set<Integer> collect = new HashSet<>();
        if (strOrganizeGroups.length != 0 ){
        collect = Arrays.stream(strOrganizeGroups).map(strOrganizeGroup -> strOrganizeGroup.getGroup().getID()).collect(Collectors.toSet());

        }
        if (mapChild.containsKey(888)){
        Set<Integer> integers = mapChild.get(888).keySet();
        Set<Integer> finalCollect = collect;
        integers.stream().forEach(integer ->{
            if (finalCollect.contains(integer)){
                list.addAll(mapChild.get(888).get(integer));
            }
        } );
        }
        return list;
    }
   /* private Set<Integer> getAllDep(Integer integer){
        DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, String.format("PARENT = '%d' ", integer));

    }*/
    private  List getDeplist(DepartmentEntity departmentEntity,List list){
        int depno  = departmentEntity.getID();
        list.add(depno);
        boolean flage = true;
        Set<Integer> listToCycl = new HashSet<>();
        listToCycl.add(depno);
        while (flage) {
            Iterator<Integer> iterator = listToCycl.iterator();
            if (iterator.hasNext()) {
                depno = iterator.next();
            }
            DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", depno, depno));
            listToCycl.remove(depno);
            if (depts.length != 0) {
                Arrays.stream(depts).forEach(departmentEntity1 -> listToCycl.add(departmentEntity1.getID()));
                Arrays.stream(depts).forEach(departmentEntity1 -> list.add(departmentEntity1.getID()));
            }
            if (listToCycl.size() == 0) {
                flage = false;
                break;
            }
        }
        return list;
    }
    private LowerIssuesChildBean addOrgInfo(LowerIssuesChildBean lowerIssuesChildBean){
        List<LowerIssuesChildBean> childrens = lowerIssuesChildBean.getChildrens();
        if (childrens!=null){
            if (childrens.size()>0){
        List<LowerIssuesChildBean> collect = childrens.stream().filter(lowerIssuesChildBean1 -> {
            ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
            StrEmployee strEmployee = ao.find(StrEmployee.class, String.format("JIRA_USER_KEY = '%s' ", loggedInUser.getKey()))[0];
            StruGroupOfEmployee[] struGroupOfEmployee = strEmployee.getStruGroupOfEmployee();
            if (struGroupOfEmployee.length != 0){
                if (lowerIssuesChildBean1.getType() == 999) {
                    int pid = lowerIssuesChildBean1.getPid();
                    if (mapChild.get(999).get(pid) != null) {
                        List<Integer> allDeplistByOrgId =new ArrayList<>();
                        allDeplistByOrgId =allDeplistIdByOrgId(allDeplistByOrgId,ao.get(StrOrganize.class,pid));
                        List<String>  strings = new ArrayList<>();
                        allDeplistByOrgId.stream().forEach(integer -> {
                            if (mapChild.containsKey(888)){
                                if (mapChild.get(888).containsKey(integer)){
                                    strings.addAll(mapChild.get(888).get(integer));
                                }
                            }
                        });
                        LowerIssuesBean issueInfo = getIssueInfo(strings);
                        lowerIssuesChildBean1.setLowerIssuesBean(issueInfo);
                        addOrgInfo(lowerIssuesChildBean1);
                    }
                }
            }else {
                if (lowerIssuesChildBean1.getType() == 999) {
                    int pid = lowerIssuesChildBean1.getPid();
                    if (mapChild.get(999).get(pid) != null) {
                        List<String> strings = new ArrayList<>();
                        strings.add(strEmployee.getEmployeeName());
                        LowerIssuesBean issueInfo = getIssueInfo(strings);
                        lowerIssuesChildBean.setLowerIssuesBean(issueInfo);
                    }
                }
                if (lowerIssuesChildBean1.getType() == 777) {
                    int pid = lowerIssuesChildBean1.getPid();
                    if (mapChild.get(777).get(pid) != null) {
                        List<String> strings = new ArrayList<>();
                        strings.add(strEmployee.getEmployeeName());
                        LowerIssuesBean issueInfo = getIssueInfo(strings);
                        lowerIssuesChildBean.setLowerIssuesBean(issueInfo);
                    }
                }
            }
            return true;
        }).collect(Collectors.toList());
        lowerIssuesChildBean.setChildrens(collect);    }
        }
        return lowerIssuesChildBean;
    }
    private  LowerIssuesChildBean addDepINfo(LowerIssuesChildBean lowerIssuesChildBean){
        List<LowerIssuesChildBean> childrens = lowerIssuesChildBean.getChildrens();
        if(childrens != null){
            if (childrens.size()>0){


        List<LowerIssuesChildBean> collect = childrens.stream().filter(lowerIssuesChildBean1 -> {
            if (lowerIssuesChildBean1.getType() == 999) {
                addDepINfo(lowerIssuesChildBean1);
               // }
            } else if (lowerIssuesChildBean1.getType() == 888) {
                int pidOfDep = lowerIssuesChildBean1.getPid();
                List<Integer> list = new ArrayList<>();
                List<String> toGetInfo= new ArrayList<>();
                List<Integer> deplist = getDeplist(ao.get(DepartmentEntity.class, pidOfDep), list);
                deplist.stream().forEach(integer ->{
                    if (mapChild.containsKey(888)){
                    if (mapChild.get(888).containsKey(integer)){
                        toGetInfo.addAll(mapChild.get(888).get(integer));
                    }
                    }
                } );
                if (toGetInfo.size() != 0){
                LowerIssuesBean issueInfo = getIssueInfo(toGetInfo);
                lowerIssuesChildBean1.setLowerIssuesBean(issueInfo);
                addDepINfo(lowerIssuesChildBean1);
                }
            }
            return true;
        }).collect(Collectors.toList());
        lowerIssuesChildBean.setChildrens(collect);} }
    return lowerIssuesChildBean;
    }
    private  List<Integer> allDeplistIdByOrgId(List<Integer> depList,StrOrganize organize){
        int id = organize.getID();
        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format("PARENT = '%d' ", id));
        if (strOrganizes.length != 0){
            Arrays.stream(strOrganizes).forEach(strOrganize -> {
                allDeplistIdByOrgId(depList,strOrganize);
            });
            StrOrganizeGroup[] strOrganizeG = ao.find(StrOrganizeGroup.class, String.format("ORG_ID = '%d' ", organize.getID()));
            List<Integer> collect = Arrays.stream(strOrganizeG).map(strOrganizeGroup -> strOrganizeGroup.getGroup().getID()).collect(Collectors.toList());
            collect.stream().forEach(integer -> {
                List<Integer> integerList = new ArrayList<>();
                List<Integer> allDeplist = getAllDeplist(ao.get(DepartmentEntity.class, integer), integerList);
                depList.addAll(allDeplist);
            });
            depList.addAll(collect);
        }else {
        StrOrganizeGroup[] strOrganizeG = ao.find(StrOrganizeGroup.class, String.format("ORG_ID = '%d' ", organize.getID()));
        List<Integer> collect = Arrays.stream(strOrganizeG).map(strOrganizeGroup -> strOrganizeGroup.getGroup().getID()).collect(Collectors.toList());
            collect.stream().forEach(integer -> {
                List<Integer> integerList = new ArrayList<>();
                List<Integer> allDeplist = getAllDeplist(ao.get(DepartmentEntity.class, integer), integerList);
                depList.addAll(allDeplist);
            });
        depList.addAll(collect);
        }

        Set<Integer> collect11 = depList.stream().collect(Collectors.toSet());
        List<Integer> collect1 = collect11.stream().collect(Collectors.toList());
        return collect1;
    }
    private List toRemoveDepId(int depId){
        List list = new ArrayList();
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, String.format("GROUP_ID = '%d' ", depId));
        if (struGroupOfEmployees.length != 0){
            List<Integer> list1 = new ArrayList<>();
            Arrays.stream(struGroupOfEmployees).forEach(struGroupOfEmployee -> {
                StrEmployee employee = struGroupOfEmployee.getEmployee();
                if (StringUtils.isNotBlank(employee.getJiraUserKey())){
                    //assignee = test8 OR reporter = adminmm
                    String jqlQuery = "assignee = " + employee.getJiraUserKey() + " or reporter = " + employee.getJiraUserKey() + "";
                    List<Issue> issueByJql = getIssueByJql(jqlQuery);
                    list1.add(issueByJql.size());
                }
            });
            if (list1.size() == 0){
                list.add(depId);
                return list;
            }else {

        return list;
            }
        }else {
            list.add(depId);
            return list;
        }
    }
    public List<Issue> getIssueByJql(String jqlQuery) {//执行jqlQuery查询语句获取issue集合
        List<Issue> issues = new ArrayList<>();
        final SearchService.ParseResult parseResult = searchService.parseQuery(jiraAuthenticationContext.getLoggedInUser(), jqlQuery);
        if (parseResult.isValid()) {
            try {
                SearchResults results = searchService.search(jiraAuthenticationContext.getLoggedInUser(), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                issues = results.getIssues();
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return issues;
    }
    private DepartmentEntity getCurrentDep(StrEmployee strEmployee){
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, String.format("EMPLOYEE_ID = '%d' ", strEmployee.getID()));
        if (struGroupOfEmployees.length != 0){
            return struGroupOfEmployees[0].getGroup();
        }else {
            return null;
        }
    }
    private StrOrganize getOrgByEmployee(StrEmployee strEmployee){
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, String.format("EMPLOYEE_ID = '%d' ", strEmployee.getID()));
        DepartmentEntity group = struGroupOfEmployees[0].getGroup();
        boolean flag=true;
        StrOrganizeGroup strOrganizeGroup = group.getStrOrganizeGroup();
        StrOrganize  strOrganize = strOrganizeGroup.getOrg();
        while (flag){
            if (strOrganize.getParent() ==0){
                flag =false;
                break;
            }else {
                 strOrganize = ao.get(StrOrganize.class, strOrganize.getParent());
            }
        }
        return strOrganize;
    }
    private StrOrganize getSupOrgByOrg(StrOrganize strOrganize){
        boolean flag=true;
        StrOrganize strOrganize1 = strOrganize;
        while (flag){
            if (strOrganize1.getParent() ==0){
                flag =false;
                break;
            }else {
                strOrganize1=ao.get(StrOrganize.class,strOrganize1.getParent());
            }
        }
        return strOrganize1;
    }

    public boolean isSubRole(StrEmployee strEmployee,StrEmployee loginEmployee){

        return false;
    }
    public List<String> orgKeysList = new ArrayList<>();
    public LowerIssuesBean getLowerIssuesByOrgNo(int orgNo){
        if(orgKeysList != null || orgKeysList.size() != 0){
            orgKeysList.clear();
        }
        buildLowerIssuesOrg(orgNo);//调用机构回调函数
        return getIssueInfo(orgKeysList);
    }
    public LowerIssuesBean getBmryfbByDeptNo1(int deptNo){
        if(deptKeys != null || deptKeys.size() != 0){
            deptKeys.clear();
        }
        buildBmryfbDept1(deptNo);//调用部门回调函数
        return CountBmryfb1(deptKeys);
    }
    public LowerIssuesBean CountBmryfb1(List<String> deptKeys){
        List<Long> proLists = new ArrayList<Long>();//存储项目id的集合
        int undoNum = 0;//待办事项数
        int doingNum = 0;//进行中事项数
        int doneNum = 0;//已完成事项数
        int otherNum = 0;//其他事项数

        String str = "(";
        for(String s : deptKeys){
            if(s != null && !"".equals(s) && !" ".equals(s) ){
                str = str + s + ",";
            }
        }
        if(str.length() == 1){
            return new LowerIssuesBean(undoNum,doingNum,doneNum,otherNum);
        }
        String userKey = str.substring(0,str.length()-1) + ")";
        String jqlQuery = "reporter in " + userKey + " or assignee in " + userKey + "";

        final SearchService.ParseResult parseResult = searchService.parseQuery(jiraAuthenticationContext.getLoggedInUser(), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(jiraAuthenticationContext.getLoggedInUser(), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                List<Issue> issues = results.getIssues();
                for(Issue issue : issues){
                    if(!proLists.contains(issue.getProjectId())){
                        proLists.add(issue.getProjectId());
                    }
                    String status = issue.getStatusId();
                    if(status != null && !"".equals(status)){
//                        String summary = issue.getSummary();
//                        String id = issue.getResolutionId();
                        if(issue.getResolutionId() != null){
                            doneNum++;
                        } else if("10000".equals(status)){
                            undoNum++;
                        } else if("3".equals(status) || "4".equals(status)){
                            doingNum++;
                        } else {
                            otherNum++;
                        }
                    }
                }
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return new LowerIssuesBean(undoNum,doingNum,doneNum,otherNum);
    }
    public void buildBmryfbDept1(int deptNo){
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));//获取此部门下的部门
        //判断部门下是否有部门
        if(depts.length != 0){
            for(DepartmentEntity dept : depts){
                buildBmryfbDept1(dept.getID());
            }
        }

        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        //判断此部门下是否有雇员
        if(groupemps.length != 0){
            StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取部门下的雇员
            for(StrEmployee emp : employees){
                if(!"".equals(emp.getJiraUserKey()) && !" ".equals(emp.getJiraUserKey()) && emp.getJiraUserKey() != null){
                    deptKeys.add(emp.getJiraUserKey());
                }
            }
        }
    }
    public LowerIssuesChildBean buildChildrenDept1(DepartmentEntity departmentEntity){
        int deptNo = departmentEntity.getID();
        LowerIssuesChildBean deptChild = new LowerIssuesChildBean();
        deptChild.setName(departmentEntity.getGroupName());
        deptChild.setLowerIssuesBean(getBmryfbByDeptNo1(deptNo));
        deptChild.setType(888);
        deptChild.setPid(deptNo);

        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));//获取此部门下的部门
        //判断部门下是否有部门
        if(depts.length != 0){
            List<LowerIssuesChildBean> temps = new ArrayList<LowerIssuesChildBean>();
            for(DepartmentEntity dept : depts){
                temps.add(buildChildrenDept1(dept));
            }
            deptChild.setChildrens(temps);
        }

        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        //判断此部门下是否有雇员
        if(groupemps.length != 0){
            StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取部门下的雇员
            List<LowerIssuesChildBean> lists = new ArrayList<>();
            for(StrEmployee emp : employees){
                lists.add(getEmployeeBmryfb1(emp.getID()));
            }
            if(deptChild.getChildrens() != null){
                lists.addAll(deptChild.getChildrens());
            }
            deptChild.setChildrens(lists);
        }
        //如果此部门下没有子部门也没有雇员
        if(depts.length == 0 && groupemps.length == 0){
            deptChild.setChildrens(new ArrayList<LowerIssuesChildBean>());
        }
        return deptChild;
    }
    public LowerIssuesChildBean getEmployeeBmryfb1(int empNo){
        LowerIssuesChildBean empChild = new LowerIssuesChildBean();
        StrEmployee employee = ao.get(StrEmployee.class,empNo);
        empChild.setName(employee.getEmployeeName());
        empChild.setLowerIssuesBean(getBmryfbByEmpNo1(empNo));
        empChild.setChildrens(new ArrayList<LowerIssuesChildBean>());
        empChild.setType(777);
        empChild.setPid(empNo);
        return empChild;
    }
    public LowerIssuesBean getBmryfbByEmpNo1(int empNo){
        List<Long> proLists = new ArrayList<Long>();//存储项目id的集合
        int undoNum = 0;//待办事项数
        int doingNum = 0;//进行中事项数
        int doneNum = 0;//已完成事项数
        int otherNum = 0;//其他事项数
        StrEmployee emp = ao.get(StrEmployee.class, empNo);
        String userKey = emp.getJiraUserKey();
        if(userKey == null || "".equals(userKey) || " ".equals(userKey)){
            return new LowerIssuesBean(undoNum,doingNum,doneNum,otherNum);
        }
        String jqlQuery = "reporter = " + userKey + " or assignee = " + userKey + "";
        final SearchService.ParseResult parseResult = searchService.parseQuery(userManager.getUserByKey(userKey), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(userManager.getUserByKey(userKey), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                List<Issue> issues = results.getIssues();
                for(Issue issue : issues){
                    if(!proLists.contains(issue.getProjectId())){
                        proLists.add(issue.getProjectId());
                    }
                    String status = issue.getStatusId();
                    if(status != null && !"".equals(status)){
                        if(issue.getResolutionId() != null){
                            doneNum++;
                        } else if("10000".equals(status)){
                            undoNum++;
                        } else if("3".equals(status) || "4".equals(status)){
                            doingNum++;
                        } else {
                            otherNum++;
                        }
                    }
                }
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return new LowerIssuesBean(undoNum,doingNum,doneNum,otherNum);
    }
    private void buildLowerIssuesOrg(int orgNo){
        StrOrganize[] strOrganize = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", orgNo, orgNo));//此机构下的所有机构
        //判断机构下是否有子机构
        if(strOrganize.length != 0){
            for(StrOrganize org : strOrganize){
                buildLowerIssuesOrg(org.getID());
            }
        }

        StrOrganizeGroup[] organizeGroups = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", orgNo));//机构与部门的对应关系表对象
        //判断机构下是否有直属部门
        if(organizeGroups.length != 0){
            DepartmentEntity[] depts= Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 0).toArray(DepartmentEntity[]::new);
            for(DepartmentEntity dept : depts){
                getBmryfbByDeptNo(dept);
                orgKeysList.addAll(deptKeys);
            }
        }

        //判断此机构有没有直属雇员，即在此机构下找到的雇员没有在部门下
        StrEmployee[] employees = ao.find(StrEmployee.class,MessageFormat.format("STR_ORGANIZE_ID = {0}", orgNo));
        if(employees.length != 0){
            List<LowerIssuesChildBean> emplists = new ArrayList<>();
            for(StrEmployee employee : employees){
                StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("EMPLOYEE_ID = {0}", employee.getID()));
                //为0表示此雇员为机构直属雇员
                if(groupemps.length == 0){
                    orgKeysList.add(employee.getJiraUserKey());
                }
            }

        }

    }
    public LowerIssuesChildBean buildChildrenDept(DepartmentEntity departmentEntity,List<Integer> list){
        int deptNo = departmentEntity.getID();
        LowerIssuesChildBean deptChild = new LowerIssuesChildBean();
        deptChild.setName(departmentEntity.getGroupName());
        //deptChild.setLowerIssuesBean(getBmryfbByDeptNo(departmentEntity));
        deptChild.setLowerIssuesBean(new LowerIssuesBean(0,0,0,0));
        deptChild.setType(888);
        deptChild.setPid(deptNo);
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        StrEmployee strEmployee = ao.find(StrEmployee.class, String.format("JIRA_USER_KEY = '%s' ", loggedInUser.getKey()))[0];
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));//获取此部门下的部门
        //判断部门下是否有部门
        if(depts.length != 0){
            List<LowerIssuesChildBean> temps = new ArrayList<LowerIssuesChildBean>();
            for(DepartmentEntity dept : depts){
                if (list.contains(dept.getID())){
                    Set<Integer> collect = list.stream().collect(Collectors.toSet());
                    collect.remove(dept.getID());
                    list= collect.stream().collect(Collectors.toList());
                     temps.add(buildChildrenDept(dept,list));
                }
            }
            deptChild.setChildrens(temps);
        }
        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        StruGroupOfEmployee[] struGroupOfEmployee = strEmployee.getStruGroupOfEmployee();
        if (struGroupOfEmployee.length != 0){
            List<Integer> integerList = new ArrayList<>();
            DepartmentEntity group = struGroupOfEmployee[0].getGroup();
            integerList= getDeplist(group,integerList);
            if (integerList.contains(deptNo)){
                //判断此部门下是否有雇员
                if(groupemps.length != 0){
                    StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取部门下的雇员
                    List<LowerIssuesChildBean> lists = new ArrayList<LowerIssuesChildBean>();
                    final boolean[] f = {true};
                    Arrays.stream(employees).forEach(strEmployee1 -> {
                        if (strEmployee1.getID()==strEmployee.getID()){
                            f[0] =false;
                        }
                    });
                    if (f[0]){
                        for (StrEmployee emp : employees) {
                            String jiraUserKey = emp.getJiraUserKey();
                            if (jiraUserKey != null) {
                                String jqlQuery = "assignee = " + jiraUserKey + " or reporter = " + jiraUserKey + "";
                                List<Issue> issueByJql = getIssueByJql(jqlQuery);
                                if (issueByJql.size() != 0) {
                                    if (mapChild.containsKey(888)){
                                        if (mapChild.get(888).containsKey(departmentEntity.getID())){
                                            mapChild.get(888).get(departmentEntity.getID()).add(jiraUserKey);
                                        }else {
                                            List<String> list1 = new ArrayList<>();
                                            list1.add(jiraUserKey);
                                            mapChild.get(888).put(departmentEntity.getID(),list1);
                                        }
                                    }else {
                                        Map<Integer,List<String>> map = new HashMap<>();
                                        List<String> list1 = new ArrayList<>();
                                        list1.add(jiraUserKey);
                                        map.put(departmentEntity.getID(),list1);
                                        mapChild.put(888,map);
                                    }
                                    lists.add(getEmployeeBmryfb(emp.getID()));
                                }
                            }
                        }
                    }else {
                        String jiraUserKey = strEmployee.getJiraUserKey();
                        if (jiraUserKey != null) {
                            String jqlQuery = "assignee = " + jiraUserKey + " or reporter = " + jiraUserKey + "";
                            List<Issue> issueByJql = getIssueByJql(jqlQuery);
                            if (issueByJql.size() != 0) {
                                if (mapChild.containsKey(888)){
                                    if (mapChild.get(888).containsKey(departmentEntity.getID())){
                                        mapChild.get(888).get(departmentEntity.getID()).add(jiraUserKey);
                                    }else {
                                        List<String> list1 = new ArrayList<>();
                                        list1.add(jiraUserKey);
                                        mapChild.get(888).put(departmentEntity.getID(),list1);
                                    }
                                }else {
                                    Map<Integer,List<String>> map = new HashMap<>();
                                    List<String> list1 = new ArrayList<>();
                                    list1.add(jiraUserKey);
                                    map.put(departmentEntity.getID(),list1);
                                    mapChild.put(888,map);
                                }
                                lists.add(getEmployeeBmryfb(strEmployee.getID()));
                            }
                        }
                    }

                    if(deptChild.getChildrens() != null){
                        lists.addAll(deptChild.getChildrens());
                    }
                    deptChild.setChildrens(lists);
                }
            }
        }

        return deptChild;
    }
    public List<String> deptKeys = new ArrayList<>();
    public LowerIssuesBean getBmryfbByDeptNo(DepartmentEntity departmentEntity){
        if(deptKeys != null || deptKeys.size() != 0){
            deptKeys.clear();
        }
        buildBmryfbDept(departmentEntity);//调用部门回调函数
        return getIssueInfo(deptKeys);
    }
    public LowerIssuesChildBean getEmployeeBmryfb(int empNo){
        LowerIssuesChildBean empChild = new LowerIssuesChildBean();
        StrEmployee employee = ao.get(StrEmployee.class,empNo);
        empChild.setName(employee.getEmployeeName());
        empChild.setLowerIssuesBean(getBmryfbByEmpNo(empNo));
        empChild.setChildrens(new ArrayList<LowerIssuesChildBean>());
        empChild.setType(777);
        empChild.setPid(empNo);
        return empChild;
    }
    private final SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
    private static final Logger log = LoggerFactory.getLogger(BmryfbStateServiceImpl.class);
    public LowerIssuesBean getBmryfbByEmpNo(int empNo){
        List<Long> proLists = new ArrayList<Long>();//存储项目id的集合
        int issNum = 0;//分配次数
        int repNum = 0;//报告次数
        int repNum1 = 0;//已报告未分配的issue数
        StrEmployee emp = ao.get(StrEmployee.class, empNo);
        String userKey = emp.getJiraUserKey();
        if(userKey == null || "".equals(userKey) || " ".equals(userKey)){
            return new LowerIssuesBean(proLists.size(),issNum,repNum,repNum1);
        }
        String jqlQuery = "reporter = " + userKey + " or assignee = " + userKey + "";
        final SearchService.ParseResult parseResult = searchService.parseQuery(userManager.getUserByKey(userKey), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(userManager.getUserByKey(userKey), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                List<Issue> issues = results.getIssues();
                for(Issue issue : issues){
                    if(!proLists.contains(issue.getProjectId())){
                        proLists.add(issue.getProjectId());
                    }
                    if(userKey.equals(issue.getAssigneeId())){
                        issNum++;
                    }
                    if(userKey.equals(issue.getReporterId())){
                        repNum++;
                        if(issue.getAssigneeId() == null || "".equals(issue.getAssigneeId())){
                            repNum1++;
                        }
                    }
                }
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return new LowerIssuesBean(proLists.size(),issNum,repNum,repNum1);
    }
    public LowerIssuesBean getIssueInfo(List<String> deptKeys){
        List<Long> proLists = new ArrayList<Long>();//存储项目id的集合
        int issNum = 0;//分配次数
        int repNum = 0;//报告次数
        int repNum1 = 0;//已报告未分配的issue数

        String str = "(";
        for(String s : deptKeys){
            if(s != null && !"".equals(s) && !" ".equals(s) ){
                str = str + s + ",";
            }
        }
        if(str.length() == 1){
            return new LowerIssuesBean(proLists.size(),issNum,repNum,repNum1);
        }
        String userKey = str.substring(0,str.length()-1) + ")";
        String jqlQuery = "reporter in " + userKey + " or assignee in " + userKey + "";


        final SearchService.ParseResult parseResult = searchService.parseQuery(jiraAuthenticationContext.getLoggedInUser(), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(jiraAuthenticationContext.getLoggedInUser(), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                List<Issue> issues = results.getIssues();
                for(Issue issue : issues){
                    if(!proLists.contains(issue.getProjectId())){
                        proLists.add(issue.getProjectId());
                    }
                    if(deptKeys.contains(issue.getAssigneeId())){
                        issNum++;
                    }
                    if(deptKeys.contains(issue.getReporterId())){
                        repNum++;
                        if(issue.getAssigneeId() == null || "".equals(issue.getAssigneeId())){
                            repNum1++;
                        }
                    }
                }
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return new LowerIssuesBean(proLists.size(),issNum,repNum,repNum1);
    }
    public void buildBmryfbDept(DepartmentEntity departmentEntity){
        int deptNo = departmentEntity.getID();
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));//获取此部门下的部门
        //判断部门下是否有部门
        if(depts.length != 0){
            for(DepartmentEntity dept : depts){
                buildBmryfbDept(dept);
            }
        }

        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        //判断此部门下是否有雇员
        if(groupemps.length != 0){
            StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取部门下的雇员
            for(StrEmployee emp : employees){
                if(!"".equals(emp.getJiraUserKey()) && !" ".equals(emp.getJiraUserKey()) && emp.getJiraUserKey() != null){
                    deptKeys.add(emp.getJiraUserKey());
                }
            }
        }
    }
    /**
     *   验证
     * @param strEmployee   是上级 返回true
     * @param toVerifyStrEmployee  是下级 返回true
     * @return
     */
    private boolean isSupOrg(StrEmployee strEmployee,StrEmployee toVerifyStrEmployee){

        Map<Integer, Map<String, Set<Integer>>> orgAndDepInfo = getInfo(strEmployee);
        Map<Integer, Map<String, Set<Integer>>> toVerifyOrgAndDepInfo = getInfo(toVerifyStrEmployee);
        boolean orgContain = toVerifyOrgAndDepInfo.get(toVerifyStrEmployee.getID()).get("pro").containsAll(orgAndDepInfo.get(strEmployee.getID()).get("pro"));
        boolean depContain = toVerifyOrgAndDepInfo.get(toVerifyStrEmployee.getID()).get("dep").containsAll(orgAndDepInfo.get(strEmployee.getID()).get("dep"));
        if (orgContain && depContain){
            return true;
        }else {
            return false;
        }


    }
    private  Map<Integer,Map<String,Set<Integer>>> getInfo(StrEmployee strEmployee){
        Map<Integer,Map<String,Set<Integer>>>  stringMapMap=new HashMap<>();
        Map<String,Set<Integer>> stringSetMap = new HashMap<>();
        stringSetMap.put("pro",getOrgNum(strEmployee));
        stringSetMap.put("dep",getDepNum(strEmployee));
        stringMapMap.put(strEmployee.getID(),stringSetMap);
        return stringMapMap;
    }
    private Set<Integer> getOrgNum(StrEmployee strEmployee){
        StrOrganize strOrganize = strEmployee.getStrOrganize();
        if (strOrganize == null){
            strOrganize = getOrgBy(strEmployee);
        }
        Set<Integer> integers = new HashSet<>();
        integers.add(strOrganize.getID());
        boolean flag =true;
        StrOrganize strOr =  strOrganize;
        while (flag){
            Integer parent = strOr.getParent();
            if (parent ==0){
                integers.add(strOr.getID());
                flag = false;
                break;
            }else {
                integers.add(parent);
                strOr=ao.get(StrOrganize.class,parent);
            }
        }
        return integers;
    }
    private StrOrganize getOrgBy(StrEmployee strEmployee){
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, String.format("EMPLOYEE_ID = '%d' ", strEmployee.getID()));
        DepartmentEntity group = struGroupOfEmployees[0].getGroup();
        StrOrganize org = group.getStrOrganizeGroup().getOrg();
        return org;
    }
    private Set<Integer> getDepNum(StrEmployee strEmployee){
        Set<Integer> integers = new HashSet<>();
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, String.format("EMPLOYEE_ID = '%d' ", strEmployee.getID()));
        if (struGroupOfEmployees.length != 0){
        DepartmentEntity group = struGroupOfEmployees[0].getGroup();
        boolean flag =true;
        while (flag){
            String parent = group.getParent();
            if (StringUtils.isBlank(parent)){
                flag = false;
                break;
            }else {
                integers.add(Integer.valueOf(parent));
                group=ao.get(DepartmentEntity.class,Integer.valueOf(parent));
            }
        }
        }
        return integers;
    }
    private List<Integer> getAllDeplist(DepartmentEntity departmentEntity,List<Integer> list){
        int deptNo = departmentEntity.getID();
        boolean flag=true;
        DepartmentEntity department =departmentEntity;
        while (flag){
            String parent = department.getParent();
            if (StringUtils.isNotBlank(parent)){
                list.add(Integer.valueOf(parent));
                department = ao.get(DepartmentEntity.class, Integer.valueOf(parent));
            }else {
                flag=false;
                break;
            }
        }
        boolean flage = true;
        int depno =deptNo;
        Set<Integer> listToCycl = new HashSet<>();
        listToCycl.add(depno);
        while (flage) {
            Iterator<Integer> iterator = listToCycl.iterator();
            if (iterator.hasNext()){
                depno = iterator.next();
            }
            DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", depno, depno));
            listToCycl.remove(depno);
            if (depts.length !=0){
            Arrays.stream(depts).forEach(departmentEntity1 ->listToCycl.add(departmentEntity1.getID()) );
            Arrays.stream(depts).forEach(departmentEntity1 ->list.add(departmentEntity1.getID()) );
            }
            if (listToCycl.size() == 0){
                flage =false;
                break;
            }
        }
        return list;
    }

    @Override
    public StrEmployee creatUserOnly(String name ){
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format("EMPLOYEE_NAME = '%s' ", name));
        StrEmployee strEmployee;
        if (strEmployees.length == 0 ){
         strEmployee = ao.create(StrEmployee.class);
        strEmployee.setEmployeeName(name);
        strEmployee.setEmployeeSex("1");
        strEmployee.setEmploymentStatus("1");
        strEmployee.save();
        }else {
            strEmployee=strEmployees[0];
        }
        return strEmployee;
    }
    @Override
    public StrEmployee creatUser(String name ){
       StrEmployee strEmployee;

            strEmployee = ao.create(StrEmployee.class);
            strEmployee.setEmployeeName(name);
            strEmployee.setEmployeeSex("1");
            strEmployee.setEmploymentStatus("1");
            strEmployee.save();

        return strEmployee;
    }
    /**
     * 未映射到公司的雇员
     * @return
     */
    public Set<StrEmployee> listUnmappedToOrgMember() {
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, "STR_ORGANIZE_ID IS NULL");
        return Arrays.stream(strEmployees).collect(Collectors.toSet());
    }
    /**
     * 未映射到部门的雇员
     * @return
     */
    public Set<StrEmployee> listUnmappedToDeptMember() {
        Query mappedEmployeeQuery = Query.select().alias(StrEmployee.class, "se").alias(StruGroupOfEmployee.class, "sge")
                .join(StruGroupOfEmployee.class, "se.ID = sge.EMPLOYEE_ID").where("sge.POST_TYPE = 0");
        Set<Integer> mappedEmployeeIds = Sets.newHashSet();
        ao.stream(StrEmployee.class, mappedEmployeeQuery, employee -> mappedEmployeeIds.add(employee.getID()));

        StrEmployee[] strEmployees = ao.find(StrEmployee.class,
                Query.select().where("ID NOT IN (" + StringUtils.join(mappedEmployeeIds, ",") + ") AND STR_ORGANIZE_ID IS NOT NULL"));
        return Arrays.stream(strEmployees).collect(Collectors.toSet());
    }
}
