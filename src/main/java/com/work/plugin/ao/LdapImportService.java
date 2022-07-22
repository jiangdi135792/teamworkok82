package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;
import com.work.plugin.rest.LdapImportBean;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2021/8/27.
 */
@Transactional
public interface LdapImportService {
    Map doImport(List<LdapImportBean> list) throws Exception;
}
