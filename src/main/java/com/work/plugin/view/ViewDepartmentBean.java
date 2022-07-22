package com.work.plugin.view;

/**
 * Created by work on 2021/6/21.
 */
import lombok.RequiredArgsConstructor;

/**
 * Created by work on 2021/6/21.
 */
@RequiredArgsConstructor
public class ViewDepartmentBean {
    String groupNo;
    String groupName;
    String status;
    String type;

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
