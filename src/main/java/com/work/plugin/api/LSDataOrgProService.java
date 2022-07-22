package com.work.plugin.api;

import com.atlassian.activeobjects.tx.Transactional;
import com.work.plugin.ao.LSDataEntity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

@Transactional
public interface LSDataOrgProService {



    LSDataEntity getByPP(String lsDataEntitygetP);
    LSDataEntity[] getAll();
    Pair<LSDataEntity, Map<String, String>>  update(String lsDataEntitygetP, String lsDataEntitygetCompany, String lsDataEntitygetV, String lsDataEntitygetKey);


}
