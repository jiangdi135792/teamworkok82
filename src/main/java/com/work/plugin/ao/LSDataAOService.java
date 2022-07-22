package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

@Transactional
public interface LSDataAOService {


    void delete(int id);
    LSDataEntity getByPP(String lsDataEntitygetP);
    LSDataEntity[] getAll( );
    Pair<LSDataEntity, Map<String, String>>  update(String lsDataEntitygetP,String lsDataEntitygetCompany,String lsDataEntitygetV,String lsDataEntitygetKey);


}
