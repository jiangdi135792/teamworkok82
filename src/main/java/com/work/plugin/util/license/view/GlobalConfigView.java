package com.work.plugin.util.license.view;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.work.plugin.ao.LSDataAOService;
import com.work.plugin.ao.LSDataEntity;
import com.work.plugin.ao.StrOrganizeService;
import com.work.plugin.util.encryption.Base64Enc;
import com.work.plugin.util.encryption.CryptUtils;
import com.work.plugin.util.license.license.PRLInfo;
import com.work.plugin.util.license.license.GlobalConfig;
import com.work.plugin.util.license.license.LicenseService;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Admin view of the Global Configuration
 *
 * @author Masato Morita
 * @since 1.0.0
 */
//@RequiredArgsConstructor
public class GlobalConfigView extends JiraWebActionSupport
{
    private final LSDataAOService lSDataAOService;
    private final LicenseService licenseService;
    private final StrOrganizeService strOrganizeService;

    public GlobalConfigView(LicenseService licenseService,LSDataAOService lSDataAOService,StrOrganizeService strOrganizeService)
    {
        this.licenseService = licenseService;
        this.lSDataAOService = lSDataAOService;
        this.strOrganizeService = strOrganizeService;
    }
    private Gson gson = new Gson();
    private String key;
    private String company;
    private String serialno;
    private String companyy;

    public String getCompanyy() {
        return companyy;
    }



    public String getKey() {
        return key;
    }

    public String getCompany() {
        return company;
    }

    public String getSerialno() {
        return serialno;
    }

    public void setSerialno(String serialno) {
        this.serialno = serialno;
    }


    /**
     * Executes a view procedure
     *
     * @return a view name by success
     */
    @Override
    public String execute() throws Exception
    {
        Object act = getHttpRequest().getParameterValues("act");
        if(act!=null)act = getHttpRequest().getParameterValues("act")[0];
if(act!=null&&act.toString().equals("add")) {
    //  Object scompany = getHttpRequest().getParameterValues("company");
    // Object sserialno =   getHttpRequest().getParameterValues("serialno");
    // Object skey =  getHttpRequest().getParameterValues("key");
    if (company != null && serialno != null && key != null)
        if (saveValidLicense(key.trim().replace("\n",""), serialno.trim(), company)) {
            return SUCCESS;
        } else {


            addError("key", "你的输入有误，请联系供应商.");

            return SUCCESS;
        }


    else {
        return SUCCESS;
    }
}else if(act!=null&&act.toString().equals("close"))
{
    return SUCCESS;
}else
{
    return SUCCESS;
}

    }
    @Override
    public boolean hasAnyErrors()
    {

        return true;
    }
    @Override
    public void validate()
    {

        addError("Password", "Please enter password.");
    }
/*public boolean invalidInput()
        {

      //  addError("Password", "Please enter password.");
         return    false;
        }
        */
@Override
public void doValidation()
        {

        addError("key", "Please enter password.");

        }
public void setKey(String key)
    {
        this.key = key;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }
    public void setCompanyy(String companyy)
    {
        this.companyy = companyy;
    }

    public String hasValidLicenseError() {
        String rd="";
        if(GlobalConfig.getOfflineflag()) {
            rd=licenseService.getPLS_A_U_C();
        }
        return rd;
    }

    /**
     * Checks whether this application has the valid license in order to use this plugin.
     *
     * @return true if this application has the valid license, false otherwise
     */
    public boolean hasValidLicense()
    {


        return licenseService.hasValidLicense();




    }


    public boolean hasOffline()
    {

        return GlobalConfig.getOfflineflag();




    }

    public String hasLicenseCompanyInfo()
    {

       String rtcode="";
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
                System.out.println("init 1 ok");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                lstPRLInfo= gson.fromJson(new String(decrypted,"utf-8"),(new TypeToken<List<PRLInfo>>() {
                }).getType());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int ii=0;
            for (PRLInfo pp :lstPRLInfo
                    ) {
                if( !pp.getCompany().equals("-1"))rtcode=pp.getCompany();

            }
        }
     //   LSDataEntity getByPP(String lsDataEntitygetP);
       if("".equals(rtcode))rtcode="试用客户";
        return rtcode;

    }
    public List<PRLInfo> hasLicenseInfo() throws UnsupportedEncodingException {
        List<PRLInfo> lstPRLInfo=new ArrayList<PRLInfo>();
        List<PRLInfo> lstPRLInfos=new ArrayList<PRLInfo>();
        lstPRLInfo=licenseService.getLicenseInfos();
        for (PRLInfo pp :lstPRLInfo
             ) {
            if( pp.getV().equals("-1"))
                pp.setV("全版本");
            if(!licenseService.getPLS_C(pp.getP()))
                pp.setV( pp.getV()+"(公司名称与许可不一致)");

            if( pp.getSvrend().equals("-1"))
                pp.setSvrend("永久");
            if(!licenseService.getPLS_S(pp.getP()))
            pp.setSvrend( pp.getSvrend()+"(服务过期，不能升级)");

            if( pp.getUser().equals("-1"))
                pp.setUser("无限");
            if(!licenseService.getPLS_U(pp.getP()))
                pp.setUser( pp.getUser()+"(超许可用户数)");

            if( pp.getAppend().equals("-1"))
                pp.setAppend("永久");

            if(!licenseService.getPLS_A(pp.getP()))
                pp.setAppend( pp.getAppend()+"(许可过期)");

            lstPRLInfos.add(pp);
        }

        return lstPRLInfos;

    }
    public boolean saveValidLicense(String keyBytes,String sserialsno,String scompany) throws UnsupportedEncodingException
    {
        List<PRLInfo> lstPRLInfo=new ArrayList<PRLInfo>();
        // -----------------公钥解密--------------------------------
        byte[] encrypted = null;
        try {
            encrypted = Base64Enc.decode(keyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] decrypted = CryptUtils.decrypt(encrypted);
        long seriesno2=0;
        if(decrypted!=null)
        for(int i=0;i<decrypted.length;i++)seriesno2=seriesno2+decrypted[i];
        String scc=null;
     if(sserialsno.equals(String.valueOf(seriesno2))){
         lstPRLInfo= gson.fromJson(new String(decrypted,"utf-8"),(new TypeToken<List<PRLInfo>>() {
         }).getType());
         for (PRLInfo pp :lstPRLInfo
                 ) {
                String cc=pp.getCompany();
            // if(cc!=null && !cc.equals("-1")&& !cc.equals("")) 可以继续无公司试用
                 if(cc!=null && !cc.equals(""))
             {
                 scc=cc;
             }
         }
         if(scompany.equals(scc))
         {

             for (PRLInfo ppinput :lstPRLInfo
                     ) {
                 saveAnupdatedorg(ppinput.getP(),ppinput.getCompany(),ppinput.getV(),keyBytes);

             }

             GlobalConfig.glstPRLInfo.removeAll(GlobalConfig.glstPRLInfo);
             GlobalConfig.glstPRLInfo.addAll(licenseService.getLicenseInfos());
             return true;
         }
         else
         {
             return false;
         }
     }
        else
     {
         return false;
     }



    }

private boolean    saveAnupdatedorg(String ppinputetP,String ppinputgetCompany,String ppinputgetV,String keyBytesg){
        lSDataAOService.update(ppinputetP,ppinputgetCompany,ppinputgetV,keyBytesg);
        if(!("-1").equals(ppinputgetCompany)&&!("").equals(ppinputgetCompany)) strOrganizeService.updateByLicense(ppinputgetCompany);

    return true;
    }
}


