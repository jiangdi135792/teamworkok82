/**
 * Created by admin on 2021/9/19.
 */
$(function () {
    $.ajax({
        type: "get",
        url: contextPath + "/rest/wk-teamwork/latest/orgstr/getOrgAndDept",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (orgTreeData) {
            var zNodes = [];
            $(orgTreeData).each(function (index, item) {
                zNodes.push({
                    id: item.id,
                    name: item.name,
                    pId: item.parent,
                    t: item.name,
                    open: false,
                    iconSkin: item.type,
                    type: item.type
                });
            });

            var setting = {
                data: {
                    key: {
                        title: "t"
                    },
                    simpleData: {
                        enable: true
                    },
                },
                callback: {
                    onClick: function () {
                        var deptId = $.fn.zTree.getZTreeObj("orgTree").getSelectedNodes(true)[0].id;
                        var realDeptId = deptId.replace("d_", "");

                        if (deptId.indexOf("d_") !== -1) {
                            $("#realDeptId").val(realDeptId);//Function Factory Fields
                            var dutyPerson = getDutyPersonByDeptId(deptId);

                            $("#currentDept").attr("style", "display:block;position:absolute");
                            if (dutyPerson == "0" || typeof dutyPerson === "undefined") {//没有分配值班人
                                $("#deptDutyPerson").html("该部门还没有值班人&nbsp;&nbsp;&nbsp;<a style='text-decoration-line: none' href='#'>分配一个</a>")
                                $("#deptDutyPerson a").click(function () {
                                    addDeptDutyPerson(deptId)
                                })
                            } else {
                                $("#employeeListTd").attr("style", "display:none;position:absolute;float: right")//隐藏
                                $("#deptDutyPerson").html(dutyPerson.employeeName + " - " + dutyPerson.email);
                            }
                        } else {
                            $("#employeeListTd").attr("style", "display:none;position:absolute;float: right")//隐藏
                            $("#currentDept").attr("style", "display:none;position:absolute");
                        }
                    }
                },
                check: {
                    enable: false
                }
            }

            $.fn.zTree.init($("#orgTree"), setting, zNodes);
        }
    });

})

function getDutyPersonByDeptId(deptId) {
    var dutyPerson;
    $.ajax({
        type: "get",
        url: contextPath + "/rest/wk-teamwork/latest/department/getDutyPersonByDeptId/" + deptId,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        async: false,
        success: function (data) {
            dutyPerson = data;
        }
    })
    return dutyPerson;
}

function addDeptDutyPerson(deptId) {
    if (deptId.indexOf("d_") !== -1) {
        $.ajax({
            type: "get",
            url: contextPath + "/rest/wk-teamwork/latest/strEmployee/getEmployeeByGroupId/" + deptId,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (data) {
                $("#employeeList").html("")//clear
                var idCount = 0;
                $(data).each(function (index, employee) {
                    $("#employeeList").append("<li><input type='radio' id='" + idCount + "' name='radioGroup' value='" + employee.jiraUserKey + "'>" + employee.employeeName + " - " + employee.email + "</li>");
                    $("#" + idCount + "").change(function () {
                        var selectedJiraUserKey = $(this).val()
                        var blaId = $.fn.zTree.getZTreeObj("orgTree").getSelectedNodes(true)[0].id;
                        var deptName = $.fn.zTree.getZTreeObj("orgTree").getSelectedNodes(true)[0].name;
                        $.ajax({
                            type: "post",
                            url: contextPath + "/rest/wk-teamwork/latest/department/setDeptDutyPerson/" + selectedJiraUserKey + "," + blaId,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data) {
                                if (data == 1) {
                                    var successFlag = AJS.flag({
                                        type: "success",
                                        body: "成功将<font color='#1e90ff'>" + employee.employeeName + "</font>分配为<font color='#1e90ff'>" + deptName + "</font>的值班人"
                                    })
                                    setTimeout(function () {
                                        successFlag.close();
                                    }, 2000)
                                } else {
                                    var errorFlag = AJS.flag({
                                        type: "error",
                                        body: "该成员不是Jira用户"
                                    })
                                    setTimeout(function () {
                                        errorFlag.close();
                                    }, 2000)
                                }
                            }
                        })
                    })
                    idCount++;
                })
                $("#employeeListTd").attr("style", "display:block;float: right")
            }
        })
    }
}

