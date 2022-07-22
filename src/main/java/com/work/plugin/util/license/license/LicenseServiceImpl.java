package com.work.plugin.util.license.license;

import com.atlassian.upm.api.license.PluginLicenseManager;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
import com.work.plugin.ao.LSDataAOService;
import com.work.plugin.ao.LSDataEntity;
import com.work.plugin.ao.StrEmployeeService;
import com.work.plugin.ao.StrOrganizeService;
import com.work.plugin.util.encryption.Base64Enc;
import com.work.plugin.util.encryption.CryptUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * License Service Component
 *
 * @author Masato Morita
 * @since 1.0.0
 */
public class LicenseServiceImpl implements LicenseService
{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LicenseServiceImpl.class);
   private final PluginLicenseManager pluginLicenseManager;
    private final LSDataAOService lSDataAOService;
    private final StrEmployeeService strEmployeeService;
    private final StrOrganizeService strOrganizeService;
//    private Gson gson = new Gson();
    public LicenseServiceImpl(PluginLicenseManager pluginLicenseManager,LSDataAOService lSDataAOService,StrEmployeeService strEmployeeService,StrOrganizeService strOrganizeService)
    {
        this.pluginLicenseManager = pluginLicenseManager;
        this.lSDataAOService = lSDataAOService;
        this.strEmployeeService = strEmployeeService;
        this.strOrganizeService = strOrganizeService;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasValidLicense()
    {

        return true;

//        if(GlobalConfig.getOfflineflag()) {
//
//            if(getPLS_A("teamwork")&&getPLS_U("teamwork") && getPLS_C("teamwork")) {
//
//
//
//
//                return true;
//            }
//            else
//                return false;
//        }
//
//        Option<PluginLicense> safeLicense = pluginLicenseManager.getLicense();
//        Option<LicenseError> safeError = safeLicense.flatMap(PluginLicense::getError);
//
//        // Checks this plugin is licensed.
//        if (! safeLicense.isDefined())
//        {
//            log.info("Plugin is not licensed. In order to evaluate this plugin you need to create an evaluation license");
//            return false;
//        }
//
//        // Checks the license has any error.
//        if (safeError.isDefined())
//        {
//            log.info(safeError.toString());
//            return false;
//        }
//
//        // Checks the license is valid.
//        return safeLicense.map(PluginLicense::isValid).getOrElse(false);
    }

    public  String getPLS_A_U_C()
    {
        String rd="";
        if(GlobalConfig.glstPRLInfo.size()==0) {
            GlobalConfig.glstPRLInfo.removeAll(GlobalConfig.glstPRLInfo);

            GlobalConfig.glstPRLInfo.addAll(getLicenseInfos());

        }


        for (PRLInfo o:GlobalConfig.glstPRLInfo
                ) {

            if(!getPLS_A( o.getP())) {rd=rd+o.getP()+"：许可过期 ";}
            if(!getPLS_U( o.getP())) {rd=rd+o.getP()+"：用户数超过 ";}
            if(!getPLS_C( o.getP())) {rd=rd+o.getP()+"：公司名称与许可公司不一致 ";}


        }


     return rd;

    }
    public  boolean getPLS_A(String PP){

        if(GlobalConfig.glstPRLInfo.size()==0) {
            GlobalConfig.glstPRLInfo.removeAll(GlobalConfig.glstPRLInfo);

                GlobalConfig.glstPRLInfo.addAll(getLicenseInfos());

        }


        for (PRLInfo o:GlobalConfig.glstPRLInfo
             ) {
          if(PP.equals(o.getP()))
            {

                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");//设置日期格式



                if( o.getAppend().compareTo(df.format(new Date()))>0||"-1".equals(o.getAppend()))
                {
                        return true;
                }

               else
                return false;



            }

        }
        return false;
    }

    public  boolean getPLS_S(String PP){

        if(GlobalConfig.glstPRLInfo.size()==0) {
            GlobalConfig.glstPRLInfo.removeAll(GlobalConfig.glstPRLInfo);

            GlobalConfig.glstPRLInfo.addAll(getLicenseInfos());

        }


        for (PRLInfo o:GlobalConfig.glstPRLInfo
                ) {
            if(PP.equals(o.getP()))
            {

                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");//设置日期格式




                    String dd=getReleaseData("distribution");
                    if("-1".equals(o.getSvrend())||"".equals(dd)||dd==null||dd.compareTo(o.getSvrend())<=0) {
                        return true;
                    }else
                    {return  false;
                    }



            }

        }
        return false;
    }

    public  boolean getPLS_U(String PP){

        if(GlobalConfig.glstPRLInfo.size()==0) {
            GlobalConfig.glstPRLInfo.removeAll(GlobalConfig.glstPRLInfo);

            GlobalConfig.glstPRLInfo.addAll(getLicenseInfos());

        }


        for (PRLInfo o:GlobalConfig.glstPRLInfo
                ) {
            if(PP.equals(o.getP()))
            {

                if("-1".equals(o.getUser())|| Integer.parseInt(o.getUser())>=strEmployeeService.getCountByUserofJira())
                {

                        return true;

                }

                else
                    return false;



            }

        }
        return false;
    }

    public  boolean getPLS_C(String PP){

        if(GlobalConfig.glstPRLInfo.size()==0) {
            GlobalConfig.glstPRLInfo.removeAll(GlobalConfig.glstPRLInfo);

            GlobalConfig.glstPRLInfo.addAll(getLicenseInfos());

        }


        for (PRLInfo o:GlobalConfig.glstPRLInfo
                ) {
            if(PP.equals(o.getP()))
            {

                if("-1".equals(o.getCompany())|| o.getCompany().equals(strOrganizeService.getCompanyByLicense()))
                {

                    return true;

                }

                else
                    return false;



            }

        }
        return false;
    }



   public   String getReleaseData(String namekey)
    {


        String version = null;

        // try to load from maven properties first
        try {
            Properties p = new Properties();
            InputStream is = getClass().getResourceAsStream("/META-INF/maven/com.work.plugin/teamwork/pom.xml");
            if (is != null) {
               // p.load(is);
               // version = p.getProperty("version", "");
                SAXReader reader = new SAXReader();
                Document document = reader.read(is); //dom4j读取
              /*  String xpath = "/Record/Field[@name='version']";//查询属性type='Creator'
                Element element = (Element) document.selectSingleNode(xpath);//得到name=Creator的元素
                */
                if(document.getRootElement()!=null)
                    if(document.getRootElement().element("licenses")!=null)
                        if(document.getRootElement().element("licenses").element("license")!=null)
                            if(document.getRootElement().element("licenses").element("license").element(namekey)!=null)
                                version= document.getRootElement().element("licenses").element("license").element(namekey).getText();



            //    version= document.getRootElement().element("licenses").element("license").element("distribution").getText();
             //   version= document.getRootElement().element("licenses").element("license").element("comments").getText();


            }
        } catch (Exception e) {
            // ignore
        }

        // fallback to using Java API
      /*  if (version == null) {
            Package aPackage = getClass().getPackage();
            if (aPackage != null) {
                version = aPackage.getImplementationVersion();
                if (version == null) {
                    version = aPackage.getSpecificationVersion();
                }
            }
        }*/

        if (version == null) {
            // we could not compute the version so use a blank
            version = "";
        }

        return version;
    }

public     List<PRLInfo>  getPRLInfofromByte(String keyBytes) {
    List<PRLInfo> lstPRLInfo = new ArrayList<PRLInfo>();
    // -----------------公钥解密--------------------------------
    byte[] encrypted = null;
    try {
        encrypted = Base64Enc.decode(keyBytes);
    } catch (Exception e) {
        e.printStackTrace();
    }
    byte[] decrypted = CryptUtils.decrypt(encrypted);

//    if (decrypted != null) {
//        try {
//            lstPRLInfo = gson.fromJson(new String(decrypted, "utf-8"), (new TypeToken<List<PRLInfo>>() {
//            }).getType());
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//    }
    return lstPRLInfo;
}


    public List<PRLInfo> getLicenseInfos() {
        List<PRLInfo> lstPRLInfo=new ArrayList<PRLInfo>();
        List<PRLInfo> lstPRLInfos=new ArrayList<PRLInfo>();
        String keyBytes =null;

        LSDataEntity[] fLSDataEntity=lSDataAOService.getAll();
        int lleenn=0;
        if(fLSDataEntity!=null)lleenn=fLSDataEntity.length;

        for (int iiii=0;iiii<lleenn;iiii++)
        {
            keyBytes=fLSDataEntity[iiii].getKeykey();

            keyBytes.replace("\n","");

            // -----------------公钥解密--------------------------------
            byte[] encrypted = null;
            try {
                encrypted = Base64Enc.decode(keyBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte[] decrypted = CryptUtils.decrypt(encrypted);
            long seriesno1=0;
            for(int i=0;i<decrypted.length;i++)seriesno1=seriesno1+decrypted[i];
            try {
                System.out.println("init 3 ok ：" );
            } catch (Exception e) {
                e.printStackTrace();
            }
//            try {
//                lstPRLInfo= gson.fromJson(new String(decrypted,"utf-8"),(new TypeToken<List<PRLInfo>>() {
//                }).getType());
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
            int ii=0;
            for (PRLInfo pp :lstPRLInfo
                    ) {
              /*  if( pp.getV().equals("-1"))
                    pp.setV("全版本");
                if( pp.getSvrend().equals("-1"))
                    pp.setSvrend("永久");
                if( pp.getUser().equals("-1"))
                    pp.setUser("无限");
                if( pp.getAppend().equals("-1"))
                    pp.setAppend("永久");*/
                lstPRLInfo.set(ii,pp);ii++;
                if(fLSDataEntity[iiii].getPp().equals(pp.getP()))lstPRLInfos.add(pp);
            }
        }
        return lstPRLInfos;

    }
}
