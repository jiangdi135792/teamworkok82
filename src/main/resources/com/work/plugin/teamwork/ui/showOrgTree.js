/**
 * Created by admin on 2021/9/3.
 */
var orgTreeData;
(function ($, contextPath) {
    //预请求一下
    $.get(contextPath + "/rest/wk-teamwork/latest/orgstr/getDetailOrgTree");
    $(window).load(function () {

        bindEvents()
    })
})(AJS.$, AJS.contextPath());

bindEvents = function () {
    setInterval(function () {
        //暂时没有好的解决jira覆盖事件的办法
        try {
            var $reporterVar = $("#reporter-single-select").data("events")["click"];
            if (typeof($reporterVar) == "undefined") {
                pageRightReporterContext()
            }
        } catch (e) {
        }
        try {
            var $assigneeVar = $("#assignee-single-select").data("events")["click"];
            if (typeof($assigneeVar) == "undefined") {
                pageRightAssigneeContext()
            }
        } catch (e) {
        }
    }, 500)
}

pageRightAssigneeContext = function () {
    $("#assignee-single-select").click(function () {

        $.ajax({
            type: "get",
            url: contextPath + "/rest/wk-teamwork/latest/orgstr/getDetailOrgTree",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (data) {
                orgTreeData = data;
                dealAssignee(data)
            }
        });
    })
}

pageRightReporterContext = function () {
    $("#reporter-single-select").click(function () {
        $.ajax({
            type: "get",
            url: contextPath + "/rest/wk-teamwork/latest/orgstr/getDetailOrgTree",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (data) {
                orgTreeData = data;
                dealReporter(data)
            }
        });
    })
}

dealAssignee = function (orgTreeData) {
    var zNodes = [];
    $(orgTreeData).each(function (index, item) {
        zNodes.push({
            id: item.id,
            name: showName(name, item),
            employeeName: item.name,
            jiraUserKey: item.characters,
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
                try {
                    var nodeId = $.fn.zTree.getZTreeObj("orgTreeToInit").getSelectedNodes(true)[0].id;
                    if (nodeId.indexOf("handsome") != -1) {
                        var employeeName = $.fn.zTree.getZTreeObj("orgTreeToInit").getSelectedNodes(true)[0].employeeName;
                        var jiraUserKey = $.fn.zTree.getZTreeObj("orgTreeToInit").getSelectedNodes(true)[0].jiraUserKey;
                        $("#assignee-field").val(employeeName)
                        //对页面右侧
                        $("#assignee-form select").find("option[selected='selected']").val(jiraUserKey)
                        //选择到自己的时候，相当于assignee to me 上面的操作相当于无效，还需要current-user
                        $("#assignee-form select").find("option[class='current-user']").val(jiraUserKey)
                        //对assign-issue按钮
                        $("#assign-issue select").find("option[selected='selected']").val(jiraUserKey)
                        $("#assign-issue select").find("option[class='current-user']").val(jiraUserKey)
                        //对create、edit等
                        $("form[name='jiraform'] select[id='assignee']").find("option[selected='selected']").val(jiraUserKey)

                    }

                } catch (e) {
                }
            }
        },
        check: {
            enable: false
        }
    }

    $("#assignee-suggestions").find("h5").html("组织机构信息")
    $("#orgTreeToInit").remove()

    $("#assignee-suggestions").find("h5").eq(1).remove()
    $("#all-users").remove()
    $("#suggestions").remove()

    $("<ul id='orgTreeToInit' class='ztree'></ul>").insertAfter($("#assignee-suggestions").find("h5"))
    $.fn.zTree.init($("#orgTreeToInit"), setting, zNodes);
}

/**
 * 如果是组织机构节点就只显示item.name
 * 如果是雇员节点，就显示employeeName+jiraUserKey
 * @param name 要处理的节点名
 * @param item 根据item.characters是否未定义来判断节点类型
 * @returns {*} 返回处理后的节点名name
 */
showName = function (name, item) {
    if (typeof (item.characters) == "undefined") {
        name = item.name
    } else {
        name = item.name + " - (" + item.characters + ")"
    }
    return name;
}

dealReporter = function (orgTreeData) {
    var zNodes = [];
    $(orgTreeData).each(function (index, item) {

        zNodes.push({
            id: item.id,
            name: showName(name, item),
            employeeName: item.name,
            jiraUserKey: item.characters,
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
                try {
                    var nodeId = $.fn.zTree.getZTreeObj("orgTreeToInit2").getSelectedNodes(true)[0].id;
                    if (nodeId.indexOf("handsome") != -1) {
                        //要显示的雇员名
                        var employeeName = $.fn.zTree.getZTreeObj("orgTreeToInit2").getSelectedNodes(true)[0].employeeName;
                        //要提交表单的jira名
                        var jiraUserKey = $.fn.zTree.getZTreeObj("orgTreeToInit2").getSelectedNodes(true)[0].jiraUserKey;

                        $("#reporter-field").val(employeeName)
                        //jira的表单 这个与assignee位置不同
                        $("#reporter-form").find("option").eq(0).val(jiraUserKey);
                        $("form[name='jiraform'] select").find("option").eq(0).val(jiraUserKey)
                    }

                } catch (e) {
                }
            }
        },
        check: {
            enable: false
        }
    }

    $("#reporter-suggestions").find("li").html("<h5>组织机构信息</h5>")
    $("#orgTreeToInit2").remove()

    $("<ul id='orgTreeToInit2' class='ztree'></ul>").insertAfter($("#reporter-suggestions").find("li"))
    $.fn.zTree.init($("#orgTreeToInit2"), setting, zNodes);

}


