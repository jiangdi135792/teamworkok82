package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;
import com.work.plugin.api.Group;
import com.work.plugin.rest.DepartmentBean;
import com.work.plugin.util.exception.IntegrityConstraintViolationException;


import java.util.List;
import java.util.Optional;

/**
 * Created by work on 2021/6/21.
 */
@Transactional
public interface DepartmentAOService {



    DepartmentEntity[] query(String groupName);
    DepartmentEntity get(int id);

    void delete(int id) throws IntegrityConstraintViolationException;
    DepartmentEntity update(DepartmentBean bean);
    DepartmentEntity add(DepartmentBean stb);
    List<DepartmentEntity> getAllGroup();
    List<DepartmentEntity> getAllTeam();

    /**
     * 根据组织机构id查询直属的部�?
     * @param orgId
     * @return
     */
    List<DepartmentEntity> getDirectDeptsByOrgId(Integer orgId);
    /**
     * 根据组织机构id查询子部�?
     * @param orgId
     * @return
     */
    List<DepartmentEntity> getSubDeptsByOrgId(Integer orgId);


    /**
     * 根据编码检查团体存�?
     *
     * @param id 团体编码
     * @return 如果团体在，返回true。如果不在，返回false�?
     */
    boolean isGroup(int id);

    /**
     * 根据编码查询团体信息
     * @param id 编码
     * @return 可选团体接�?
     */
    Optional<Group> getGroup(int id);

    /**
     * 根据雇员编码查询所属团体信�?
     * @param employeeId 雇员编码
     * @param type 团体类型
     * @return 团体接口的列�?
     */
    List<Group> getGroupByEmployeeId(int employeeId, int type);

    /**
     * 根据导入更新团体信息
     * 通过组织更新团体信息
     * @param group
     * @return
     */
    Group updateGroupByImport(int orgId,Group group);

    /**
     * 根据导入更新机构和团体关系信�?
     * @param orgId 机构编码
     * @param groupId 团体编码
     */
    void updateOrganizeGroupByImport(int orgId, int groupId);
/*-------------------------------*/
    /**
     * 2021
     * admin
     * 维护组织-部门关系�?
     * @param ownerId
     * @param modifier
     * @param organize
     * @param departmentEntity
     */
    void maintainRelationOrgAndGro(Integer ownerId , Integer modifier, StrOrganize organize, DepartmentEntity departmentEntity);

    /**
     *创建部门
     * @param department
     * @param directoryId
     * @param Organizeid
     * @param status
     * @param type
     * @return
     */
    DepartmentEntity createByInfo(String department,int directoryId,int Organizeid,int status,int type);

    /**
     *
     * @param departName
     * @param parent
     * @return
     */    DepartmentEntity queryByParentId(String departName,String parent);

    /**
     * 根据组织ID 部门名称 获取部门
     * @param OrgId
     * @param DepartName
     * @return
     */
    DepartmentEntity getDepartInfo(int OrgId,String DepartName);

    boolean isGroupByName(int orgId,String groupName);

    void updateDepAndDepRela(int depId,String depParentId);
    /*-------------------------------*/

    /**
     * 验证部门编号是否存在
     * @param deptno 部门编号
     * @return
     */
    boolean isExistDeptNo(String deptno, String groupId);

    /**
     * 传入一个团队，获取它的子团队。
     * 如果没有子团队，则返回自身
     * 如果有，则返回自身和它的子团队
     */
    List<DepartmentEntity> getSubTeamOfTeam(Integer teamId);

    DepartmentEntity getSubDepartInfo(int orgId ,String departName);

    int setDeptDutyPersonByJiraUserKey(String jiraUserKey,String deptId);

    StrEmployee getDutyPersonByDeptId(String deptId);
}
