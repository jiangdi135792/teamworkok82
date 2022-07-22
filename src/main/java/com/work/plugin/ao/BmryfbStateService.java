package com.work.plugin.ao;

import com.atlassian.activeobjects.tx.Transactional;
import com.work.plugin.rest.BmryfbInfoBean;
import com.work.plugin.rest.ReportDoChildrenBean;
import com.work.plugin.rest.ReportJoinChildrenBean;

import java.util.List;

/**
 * Created by admin on 2021/7/5.
 */
@Transactional
public interface BmryfbStateService {



    List<ReportJoinChildrenBean> getAllOfBm(String sign);

    List<ReportDoChildrenBean> getAllOfTd(String sign);

    List<BmryfbInfoBean> getInfos(String sign, int type, int pid);

}
