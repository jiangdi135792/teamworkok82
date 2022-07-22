package com.work.plugin.ao;

/**
 * Created by work on 2022/3/31.
 */
public interface ImportDataTempService {

    /**
     * 根据传入的每一行创建临时数据到数据库
      * @param field
     * @param hashcode
     */
    void creatTempData(String[] field,long hashcode);

    /**
     *  通过邮箱 和 hashcode 判断是否更新
     * @param email
     * @param hashcode
     * @return
     */
    boolean toUpdateInfo(String email,long hashcode);

    /**
     * 获取要更新的信息，标志是1 的更新，
     * @return
     */
    ImportDataTempEntity[] getToUpdateInfo();

    ImportDataTempEntity getByOrgId(String orgParentId);

    ImportDataTempEntity getByDepId(String orgId);
}
