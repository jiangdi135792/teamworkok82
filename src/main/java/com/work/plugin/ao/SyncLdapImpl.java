package com.work.plugin.ao;

import com.work.plugin.util.ObjectGUID_Tools;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import java.util.*;

/**
 * Created by work on 2022/1/22.
 */
public class SyncLdapImpl {
    public  static  Map<String,String>  map = new HashMap<>();
    public static LdapContext init(String LDAP_URL, String adminName, String adminPassword) {
        Hashtable env=new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.ldap.attributes.binary", "objectSid objectGUID");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.PROVIDER_URL, LDAP_URL);
        env.put(Context.SECURITY_PRINCIPAL, adminName);
        env.put(Context.SECURITY_CREDENTIALS, adminPassword);
        LdapContext ldapContext=null;
        try {
            ldapContext=new InitialLdapContext(env, null);
            //System.out.println("认证成功");
            map.put(adminName+adminPassword,"success");
        } catch (javax.naming.AuthenticationException e) {
            //System.out.println("认证失败");
            map.put(adminName+adminPassword,"failed");
        } catch (Exception e) {
            //System.out.println("认证出错：" + e);
            map.put(adminName+adminPassword,"error");
            e.printStackTrace();
        }
        return ldapContext;
    }
    public static void close(LdapContext ldapCtx) {
        if (ldapCtx != null) {
            try {
                ldapCtx.close();
                //System.out.println("close success");
            } catch (NamingException e) {
                //System.out.println("NamingException in close():" + e);
            }
        }
    }

    public static Map<String, Map<String, Object>> searchInfoByFilter(LdapContext ldapCtx, String searchBase, String searchFilter, String returnedAtts[]) throws NamingException {
        Map<String,Map<String,Object>> hashmap=new HashMap<String, Map<String, Object>>();
        SearchControls searchCtls=new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(returnedAtts);
        NamingEnumeration<SearchResult> search=ldapCtx.search(searchBase, searchFilter, searchCtls);
        while (search.hasMoreElements()) {
            Map<String,Object> map=new HashMap<String, Object>();
            SearchResult sr=search.next();
            Attributes Attrs=sr.getAttributes();
            if (Attrs != null) {
                NamingEnumeration<?> ne=Attrs.getAll();
                while (ne.hasMore()) {
                    Attribute Attr=(Attribute) ne.next();
                    String name=Attr.getID();
                    NamingEnumeration<?> values=Attr.getAll();
                    if (values!=null){
                        while (values.hasMoreElements()) {
                            String value = "";
                            if ("objectGUID".equals(name)) {
                                //value = UUID.nameUUIDFromBytes((byte[]) values.nextElement()).toString();
                                value=ObjectGUID_Tools.convertToDashedString((byte[]) values.nextElement());
                            } else {
                                value = (String) values.nextElement();
                            }
                            map.put(name,value);
                            if (name.equals(returnedAtts[0])){
                                hashmap.put(value,map);
                            }
                        }
                    }
                }
            }
        }
        return hashmap;
    }

    public static Map<String,Object> searchByAttribute(DirContext dirContext, String searchBaseDN, Attributes matchingAttributes) {
        // 创建搜索控件
        SearchControls cons = new SearchControls();
        // 设置搜索范围
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
       Map map=new HashMap<String,Object>();
        try {
            Name baseName = new LdapName(searchBaseDN);
            NamingEnumeration<SearchResult> nee = dirContext.search(baseName, matchingAttributes);
            SearchResult entry = null;
            while (nee.hasMoreElements()) {
                entry = nee.next();
                Attributes Attrs=entry.getAttributes();
                if (Attrs != null) {
                    NamingEnumeration<?> ne=Attrs.getAll();
                    while (ne.hasMore()) {
                        Attribute Attr=(Attribute) ne.next();
                        String name=Attr.getID();
                        NamingEnumeration<?> values=Attr.getAll();
                        if (values!=null){
                            while (values.hasMoreElements()) {
                                Object object = "";
                                if ("objectGUID".equals(name)) {
                                    object = ObjectGUID_Tools.convertToDashedString((byte[]) values.nextElement());
                                } else {
                                     object=values.nextElement();
                                }
                                map.put(name,object);
                            }
                        }
                    }
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return map;
    }
}
