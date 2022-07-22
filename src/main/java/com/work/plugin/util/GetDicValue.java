package com.work.plugin.util;

import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//import com.atlassian.jira.util.lang.Pair;

/**
 * Created by admin on 2021/6/27.
 */
public  class GetDicValue {

	private int id;
	private String name;
	static List<Pair<String,String>> lstSex=new ArrayList();
	static List<Pair<String,String>> lstCompanyType=new ArrayList();
	static List<Pair<String,String>> lstBranchCharacter=new ArrayList();
	static List<Pair<String,String>> lstStatus=new ArrayList();
	static List<Pair<String,String>> lstEmploymentStatus=new ArrayList();
	static List<Pair<String,String>> lstGroupType=new ArrayList();


	static{
		lstSex.add(ImmutablePair.of("1", "male"));
		lstSex.add(ImmutablePair.of("2", "female"));

		lstCompanyType.add(ImmutablePair.of("0", "company"));
		lstCompanyType.add(ImmutablePair.of("1", "sub company"));
		lstCompanyType.add(ImmutablePair.of("2", "branch"));
		lstCompanyType.add(ImmutablePair.of("9", "other"));

		lstBranchCharacter.add(ImmutablePair.of("0", "general subsidiary"));
		lstBranchCharacter.add(ImmutablePair.of("1", "independent legal"));

		lstStatus.add(ImmutablePair.of("0", "valid"));
		lstStatus.add(ImmutablePair.of("1", "invalid"));

		lstEmploymentStatus.add(ImmutablePair.of("1", "onJob"));
		lstEmploymentStatus.add(ImmutablePair.of("2", "dimission"));

		lstGroupType.add(ImmutablePair.of("0", "department"));
		lstGroupType.add(ImmutablePair.of("1", "team"));



	}
	GetDicValue() {




	}
	public  static String getDictionaryValue(String type,String key){

		return "";
	}
	public  static List<Pair<String,String>> getDictionaryValueList(String type){

		return null;
	}

	public  static String getDictionaryValue(DicTypeEnum type, String key, I18nHelper i18nHelper, String i18nPrefix){

		String key_next="";
		switch (type)
		{	case SEX:
			for (Pair o:lstSex
					) {
				if(key.equals(o.getLeft()))
				{	key_next=(String) o.getRight();
					key_next=key_next.replace(" ","");
					break;
				}

			}

			break;
			case BRANCHCHARACTER:
				for (Pair o:lstBranchCharacter
					 ) {
					if(key.equals(o.getLeft()))
					{	key_next=(String) o.getRight();
						key_next=key_next.replace(" ","");
						break;
					}

				}

				break;
			case STATUS:
				for (Pair o:lstStatus
						) {
					if(key.equals(o.getLeft()))
					{	key_next=(String) o.getRight();
						key_next=key_next.replace(" ","");
						break;
					}

				}

				break;

			case ORGANIZATIONTYPE:
				for (Pair o:lstCompanyType
						) {
					if(key.equals(o.getLeft()))
					{	key_next=(String) o.getRight();
						key_next=key_next.replace(" ","");
						break;
					}

				}

				break;
			case EMPLOYMENTSTATUS:
				for (Pair o:lstEmploymentStatus
						) {
					if(key.equals(o.getLeft()))
					{	key_next=(String) o.getRight();
						key_next=key_next.replace(" ","");
						break;
					}

				}

				break;
			case GROUPTYPE:
				for (Pair o:lstGroupType
						) {
					if(key.equals(o.getLeft()))
					{	key_next=(String) o.getRight();
						key_next=key_next.replace(" ","");
						break;
					}

				}

				break;

		}
		String rtValue=i18nHelper.getText(i18nPrefix+key_next);
		if(Objects.nonNull(rtValue)||(i18nPrefix+key_next).equals(rtValue))
			key_next=rtValue;//general subsidiary
		return key_next;
	}
	public  static List<Pair<String,String>> getDictionaryValueList(String type,I18nHelper i18nHelper,String i18nPrefix){

		return null;
	}
}
