package com.work.plugin.impl;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.work.plugin.ao.LSDataAOService;
import com.work.plugin.ao.LSDataEntity;
import com.work.plugin.api.LSDataOrgProService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

//import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LSDataOrgProServiceImpl implements LSDataOrgProService {
	private final ActiveObjects ao;
	private final CrowdService crowdService;
	private final LSDataAOService lSDataAOService;


	public LSDataEntity[] getAll( ) {


			return lSDataAOService.getAll();

	}

	public LSDataEntity getByPP(String lsDataEntitygetP) {

			return lSDataAOService.getByPP(lsDataEntitygetP);
	}
	public Pair<LSDataEntity, Map<String, String>>  update(String lsDataEntitygetP,String lsDataEntitygetCompany,String lsDataEntitygetV,String lsDataEntitygetKey) {


		return lSDataAOService.update(lsDataEntitygetP, lsDataEntitygetCompany, lsDataEntitygetV, lsDataEntitygetKey);
	}


}
