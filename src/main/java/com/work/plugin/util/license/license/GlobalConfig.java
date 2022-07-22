package com.work.plugin.util.license.license;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin view of the Global Configuration
 *
 * @author Masato Morita
 * @since 1.0.0
 */
public class GlobalConfig
{
    public GlobalConfig() {
    }
    // true 线下 false 线上
    private static  boolean isOfflineflag = true;
    // true Debug  false 正式上线
    private static  boolean isDebug = true;
    public static boolean getOfflineflag()  {return isOfflineflag;}
    public static boolean getIsDebug()  {return isDebug;}
    public static void printDebug(String str)  {
        if(isDebug){
            varsss = varsss+str;
            System.out.println(str);
        }
    }
    public static List<PRLInfo> glstPRLInfo=new ArrayList<PRLInfo>();

    public static  String varsss ="==" ;

}
