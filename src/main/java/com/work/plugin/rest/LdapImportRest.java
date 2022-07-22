package com.work.plugin.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONException;
import com.google.gson.Gson;
import com.google.gson.internal.StringMap;
import com.work.plugin.ao.LdapImportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by Administrator on 2021/8/27.
 */
@Path("employee")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class LdapImportRest {

    private final JiraAuthenticationContext jiraAuthenticationContext;

    private final LdapImportService ldapImportService;

    @POST
    @Path("import")
    public Response doImport(String jsonInfo) throws JSONException, IllegalAccessException, InstantiationException, InvocationTargetException {
        if (null == jiraAuthenticationContext.getLoggedInUser()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        //将json字符串转化为键值对集合
        Gson gson = new Gson();
        List<StringMap> mapList = null;
        Map infoMap = new HashMap();
        try {
            mapList = gson.fromJson(jsonInfo, ArrayList.class);
        } catch (Exception e) {
            infoMap.put("failed", "invalid field");
            return Response.ok(infoMap).build();
        }


        List<LdapImportBean> list = new ArrayList<>();
        for (StringMap stringMap : mapList) {
            LdapImportBean ldapImportBean = new LdapImportBean();
            //调用BeanUtils实例化对象
            BeanUtils.populate(ldapImportBean, stringMap);
            list.add(ldapImportBean);
        }

        try {
            infoMap = ldapImportService.doImport(list);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.ok(infoMap).build();
    }

}
