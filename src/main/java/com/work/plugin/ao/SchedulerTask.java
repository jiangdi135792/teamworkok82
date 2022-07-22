package com.work.plugin.ao;

import com.work.plugin.util.GetJiraContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.util.TimerTask;

/**
 * Created by work on 2022/2/3.
 */

@AllArgsConstructor
@NoArgsConstructor
public class SchedulerTask extends TimerTask {
    private long toDoTaskDirectoryId;
    public SchedulerTask (long toDoTaskDirectoryId){
       this.toDoTaskDirectoryId= toDoTaskDirectoryId;
    }
    ApplicationContext applicationContext=GetJiraContext.getApplicationContext();
    StrEmployeeExtendService strEmployeeExtendService=(StrEmployeeExtendService) applicationContext.getBean("strEmployeeExtendService");
    @Override
    public void run() {
        strEmployeeExtendService.updateInfo(toDoTaskDirectoryId);
    }
}
