package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;
import com.work.plugin.api.Organize;
import com.work.plugin.rest.StrOrganizaitonBean;
import com.work.plugin.util.exception.IntegrityConstraintViolationException;

import java.util.List;
import java.util.Optional;

/**
 * Created by admin on 2021/6/22.
 */
@Transactional
public interface StrOrganizeService {
    List<StrOrganize> getAll();

    List<StrOrganizaitonBean> getOrgAndDept();

    List<StrOrganizaitonBean> getOrgAndTeam();

    List<StrOrganizaitonBean> getOrgAndTeamAndProject();


    StrOrganize[] query(String name);

    StrOrganize get(int id);

    StrOrganize create(StrOrganizaitonBean bean);


    void delete(int id) throws IntegrityConstraintViolationException;

    StrOrganize update(StrOrganizaitonBean bean);

    boolean updateByLicense(String company);

    String getCompanyByLicense();

    /**
     * 根据编码检查机构存�?
     *
     * @param id 机构编码
     * @return 如果机构在，返回true。如果不在，返回false�?
     */
    boolean isOrganizeByID(int id);

    /**
     * 根据编码查询机构信息
     *
     * @param id 机构编码
     * @return 可选机构接�?
     */
    Optional<Organize> getOrganize(int id);

    /**
     * 根据团体编码查询父机构信�?
     *
     * @param groupId 团体编码
     * @return 机构接口的列�?
     */
    List<Organize> getOrganizesByGroupId(int groupId);

    /**
     * 根据导入更新机构信息
     *
     * @param organize 机构接口
     * @return 更新了机构接�?
     */
    Organize updateOrganizeByImport(Organize organize);



    /**
     * 通过信息创建组织
     *
     * @param name
     * @param modifier
     * @param direcotoryId
     * @param updateTime
     * @param OrgLevel
     * @param parent
     * @param status
     * @param type
     * @return
     */
    StrOrganize createByInfo(String name, int modifier, long direcotoryId, String updateTime, int OrgLevel, int parent, String status, String type);

    /**
     * 通过名字获取组织
     *
     * @param OrgName
     * @return
     */
    StrOrganize getByOrgName(String OrgName);
    /**
     * 通过名字获取组织
     *
     * @param Id
     * @return
     */
    StrOrganize getByOrgId(int Id);
    /**
     * 通过组织名字判断是否存在组织
     *
     * @param orgName
     * @return
     */
    boolean isOrgByOrganizeName(String orgName);

    List<DepartmentEntity> getUnmappedTeam();

    /**
     * 通过组织ID 上级组织ID 维护关系，
     *
     * @param orgId
     * @param orgParentId
     */
    void updateOrgOfParentOrgByImport(int orgId, int orgParentId);

    /**
     * 获取组织机构与团队以及成员的全部集合
     * @return
     */
    List<StrOrganizaitonBean> getDetailOrgTree() throws Exception;
}
