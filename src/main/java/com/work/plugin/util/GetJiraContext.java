package com.work.plugin.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by work on 2022/1/25.
 */
@Component
public class GetJiraContext implements ApplicationContextAware {
    public static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(context==null)
        {
            context =applicationContext;
        }
        //System.out.println("ApplicationContext setter is called...");
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }
    public static Object getBean(String name)
    {
        if(context==null)
        {
            context = getApplicationContext();
        }
        return context.getBean(name);
    }
}
