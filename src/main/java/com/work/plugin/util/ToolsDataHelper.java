package com.work.plugin.util;

//import com.atlassian.jira.util.lang.Pair;

/**
 * Created by admin on 2021/6/27.
 */
public  class ToolsDataHelper {
	final static String wordP="/\\[]\" ";
//	final static String word =wordP+"- ={}|;',.<>!@#$%^&*()?_+，。、；：？！…—·ˉˇ¨〃‘’“”々～‖∶＂＇｜｀〔〕〈〉《》「」『』．〖〗【】（）｛｝≈≡≠＝≤≥＜＞≮≯∷±＋－×÷／∫∮∮∝∞∧∨∏∑∪∩∈∵∴⊥∥∠⌒⊙≌∽√§№☆★○●◇◆□℃‰■△▲※→←↑↓〓¤°＃＆＠＼︿＿￣♂♀―";
	final static String word =wordP+"+ ={}|;'''<>!@#$%^&*()?_+，。、；：？！…—·ˉˇ¨〃‘’“”々～‖∶＂＇｜｀〔〕〈〉《》「」『』．〖〗【】（）｛｝≈≡≠＝≤≥＜＞≮≯∷±＋－×÷／∫∮∮∝∞∧∨∏∑∪∩∈∵∴⊥∥∠⌒⊙≌∽√§№☆★○●◇◆□℃‰■△▲※→←↑↓〓¤°＃＆＠＼︿＿￣♂♀―";

	//add new word forword
	//: not use  :use ctrol word

	ToolsDataHelper() {


	}


	public static String setRepalaceString(String text)
	{
		StringBuilder sbStr=new StringBuilder();
         //: not use  :use ctrol word
	//	String text1 =text.replaceAll("[-={}|;',.<>!@#$%^&*()?_+，。、；：？！…—·ˉˇ¨〃‘’“”々～‖∶＂＇｜｀〔〕〈〉《》「」『』．〖〗【】（）｛｝≈≡≠＝≤≥＜＞≮≯∷±＋－×÷／∫∮∮∝∞∧∨∏∑∪∩∈∵∴⊥∥∠⌒⊙≌∽√§№☆★○●◇◆□℃‰■△▲※→←↑↓〓¤°＃＆＠＼︿＿￣♂♀―]","ccccccs$0cccccce");
		String text1 =text.replaceAll("[+={}|;'''<>!@#$%^&*()?_+，。、；：？！…—·ˉˇ¨〃‘’“”々～‖∶＂＇｜｀〔〕〈〉《》「」『』．〖〗【】（）｛｝≈≡≠＝≤≥＜＞≮≯∷±＋－×÷／∫∮∮∝∞∧∨∏∑∪∩∈∵∴⊥∥∠⌒⊙≌∽√§№☆★○●◇◆□℃‰■△▲※→←↑↓〓¤°＃＆＠＼︿＿￣♂♀―]","ccccccs$0cccccce");

		for(int i=0;i<text1.length();i++) {
			int left=text1.indexOf("ccccccs");
			if(left<0){ break;}
			else {
				int right = text1.indexOf("cccccce", left);
				if (right >= 0 && right - left > 7 && right - left < 10) {
					String sub = text1.substring(left + 7, right);
					sbStr.append(text1.substring(0, left) +"ccccccs"+ word.indexOf(sub)+"cccccce");
					text1 = text1.substring(right + 7);
					i = 0;
					int nop = 0;
				} else {   break;}

			}
		}
		if(text1.length()>0)sbStr.append(text1);

		return sbStr.toString().replace("/","ccccccs"+"0"+"cccccce")
				.replace("\\","ccccccs"+"1"+"cccccce")
				.replace("[","ccccccs"+"2"+"cccccce")
				.replace("]","ccccccs"+"3"+"cccccce")
				.replace("\"","ccccccs"+"4"+"cccccce")
				/*.replace(" ","ccccccs"+"5"+"cccccce")*/
				.replace(" ","_");
	}
	public static String getRepalaceString(String text)
	{
		StringBuilder sbStr=new StringBuilder();

		String text1 =text;
		for(int i=0;i<text1.length();i++) {
			int left=text1.indexOf("ccccccs");
			if(left<0){ break;}
			else {
				int right = text1.indexOf("cccccce", left);
				if (right >= 0 && right - left > 7 && right - left < 10) {
					String sub = text1.substring(left + 7, right);
					int len=-1;
					try {
						len=Integer.parseInt(sub);
					}catch (Exception  e){}
					if(len>=0) {
						if(len==29)
							sbStr.append(text1.substring(0, left) +"ccccccs29cccccce");
							else
						sbStr.append(text1.substring(0, left) + "" + word.substring(len, len + 1) + "");
					}
					text1 = text1.substring(right + 7);
					i = 0;
					int nop = 0;
				} else {   break;}

			}
		}
		if(text1.length()>0)sbStr.append(text1);

		return sbStr.toString().replace("_"," ").replace("ccccccs29cccccce","_");
	}
}
