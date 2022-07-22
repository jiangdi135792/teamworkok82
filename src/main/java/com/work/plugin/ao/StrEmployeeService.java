package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;
import com.work.plugin.api.Employee;
import com.work.plugin.rest.LowerIssuesChildBean;
import com.work.plugin.rest.StrEmployeeBean;
import com.work.plugin.util.exception.DuplicateKeyException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by admin on 2021/6/21.
 * Update by admin on 2021/6/28.
 */
@Transactional
public interface StrEmployeeService {

	StrEmployee[] getByJiraId(String jiraId);

    /**
     * 根据雇员对象创建雇员
     * @param model 一个雇员对�?????
     * @return StrEmployee 一个创建雇员的接口
     */
    StrEmployee create(StrEmployeeBean model) throws DuplicateKeyException;

    /**
     * 查询所有雇员信�?????
     * @return List<StrEmployee> 所有雇员接口的集合
     */
    List<StrEmployee> all();

    /**
     ** 查询有效的雇员信息（公司字段不为空）
     */
    List<StrEmployee> getValidStrEmployee();

    /**
     * 根据雇员id删除雇员
     * @param id 雇员id(系统分配)
     * @throws SQLException
     */
    void delete(int id) throws SQLException;
    void deleteTeamMember(int id);

    /**
     * 修改雇员信息
     * @param model 传入的雇员信息对�?????
     * @return StrEmployee 一个雇员的接口
     */
    StrEmployee update(StrEmployeeBean model) throws DuplicateKeyException;
    List<StrEmployee> getEmployeeByOrgId(int id);
    StrEmployee[] getEmployeeByGroupId(int id);

    List<StruGroupOfEmployee> getStruGroupOfEmployeeByOrgId(int id);
    List<StruGroupOfEmployee> getStruGroupOfEmployeeByTeamId(int id);
    List<StruGroupOfEmployee> getDirectStruGroupOfEmployeeByTeamId(int id);
    /**
     * 根据雇员id查询一个雇员信�?????
     * @return StrEmployee 一个雇员接口的集合
     */
    StrEmployee getEmployee(int employeeId);

    /**
     * @param EmployeeId
     * @return
     */
    int getByEmployeeId(String EmployeeId);

    List<StrEmployee> getAvailableEmployeesByTeamId(String teamId);
    void addEmployeeToTeam(int teamId, List<String> employeeIds, List<String> roleIds);
    void addEmployeeToTeam(Integer teamId, Integer employeeId, Integer roleId);

    int getCountByUserofJira();

    /**
     * 根据姓名检查雇员存�?????
     *
     * @param name 姓名
     * @return 如果雇员在，返回true。如果不在，返回false�?????
     */
    boolean isEmployeeByName(String name);

    /**
     * 根据名姓查询一个雇员信�?????
     * @param name 姓名
     * @return 可选一个雇员接�?????
     */
    Optional<Employee> getEmployeeByName(String name);

    /**
     * 根据导入更新一个雇员信�?????
     * @param employee 雇员接口
     * @return 更新或创建了一个雇员接�?????
     */
    Employee updateEmployeeByImport(Employee employee);

    /**
     * 根据导入更新雇员和团体关系信�?????
     * @param employeeId 雇员编码
     * @param groupId 团体编码
     */
    void updateGroupOfEmployeeByImport(int employeeId, int groupId);
    void maintainRelationGroupEmployee(StrEmployee e, DepartmentEntity departmentEntity, int modifireId, String createTime, int owner, int postType);
    StruGroupOfEmployee getDepart(int employeeId);

    void deleteRalationEmployeeAndDepart(int employeId);
    List<StrEmployee> allAppoint(long directoryId);

    /**
     * 查询用户
     * @param memberStr
     * @return
     */
    StrEmployee[] queryMember(String memberStr);
    void  setLdapToNull(long directoryId);

    boolean isExistEmployeeNo(String employeeNo, String employeeId);
    StrEmployee getByJiraUserKey(String name);
    void maintainRelationRoleAndEmployee(StrEmployee strEmployee, RoleEntity roleEntity);
    Set<RoleEntity> getRoleByJiraUserKey(String name);
    StrEmployee createOrgUser(String OrgName,String UserName);
    StrEmployee getByEmail(String emial);

    /**
     * 根据雇员ID 组织ID 更新它们关系
     * @param employeeId
     * @param organizeId
     */
    void updateOrgOfEmployeeByImport(int employeeId,int organizeId);

    /**
     * 获取已关联jirauserkey的用户
     * @return
     */
    StrEmployee[] getEmployeeRelatedJirauserkey();

    StrEmployeeOfRole saveEmployeeRole(Integer teamId, Integer roleId, Integer strEmployeeId);

    RoleEntity getRoleByEmployeeAndTeam(Integer teamId, Integer strEmployeeId);

    StrEmployeeOfRole[] listEmployeeRoleByTeamid(Integer teamId);

    List<StrEmployee> getSubEmployee(Integer employeeId);


    /**
     *
     * @param userName 用户�?
     * @return 组织机构中权限角�?
     */
    List<String> getOrgRoleByUserName(String userName);

    /**
     *
     * @param ls  AD域 刪除后 刪除 組織用戶 的 jira 关联
     */
    void setJiraUserKeyNull(long ls);

    /**
     *
     * @param directoryId  id
     * @return  返回当前域下的雇员
     */
    StrEmployee[] getCorrespondingFieldsStrEmployees(long directoryId);

    StrEmployee[]  getEmployeeByJiraUserKeyAndDirectoryId(String ExsitJiraUserKey ,long directoryId);

    /**
     *  Assigning roles to users;
     *  The Timeid field value is  -1  is the organization role;
     * @param strEmployee  org  user
     * @param roleEntitySet  Initialized role or created role
     */
    void setOrgRoleToEmployee(StrEmployee strEmployee, Set<RoleEntity> roleEntitySet);

    /**
     * 获取 当人员 下级的所有issue
     * @return
     */
    List<LowerIssuesChildBean> getAllLowerIssues(StrEmployee strEmployee);

    /**
     *
     * @param name org  user name
     * @return
     */
    StrEmployee creatUserOnly(String name);
    StrEmployee creatUser(String name);
    Set<StrEmployee> listUnmappedToOrgMember();
    Set<StrEmployee> listUnmappedToDeptMember();
}
