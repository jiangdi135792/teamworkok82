!(function ($, contextPath) {
    $(function () {
        window.onload = controlePower(1);
        AJS.org = {
            init: function () {
                var _this = this;
                //  新增雇员到团队的OK按钮
                $('#dialog-submit-button').click(function () {
                    var employeeids = [];
                    var roleids = [];
                    $('#addEmployee-dialog').find('.dialog-member-in-content').find('tbody tr').each(function (index, item) {
                        employeeids.push($(item).data('employeeid'));
                        var role = $(item).find('.roleSel').auiSelect2('val');
                        roleids.push(role);
                    });
                    var id = _this.getSelectTreeId();
                    $.ajax({
                        type: "post",
                        url: contextPath + "/rest/wk-teamwork/latest/orgstr/addEmployeeToTeam/" + id.replace('d_', ''),
                        contentType: "application/x-www-form-urlencoded; charset=utf-8",
                        data: {
                            employeeIds: employeeids,
                            roleIds: roleids
                        },
                        success: function () {
                            AJS.org.showInfo();
                            AJS.dialog2("#addEmployee-dialog").hide();
                            AJS.org.afterSave();
                        }
                    });
                });
                $('#mapping-submit-btn').click(function () {
                    var selectedNode = AJS.org.getSelectTreeNode();
                    var selectedTeamId = $('#mappingTeam').val();
                    if (!selectedTeamId) {
                        alert('请选择需要映射的团队');
                        return;
                    }
                    $.ajax({
                        type: "get",
                        url: contextPath + "/rest/wk-teamwork/latest/orgstr/mappingTeamProject",
                        contentType: "application/json; charset=utf-8",
                        data: {
                            // projectkey: selectedNode.projectkey,
                            projectId: selectedNode.id,
                            teamId: selectedTeamId
                        },
                        dataType: "json",
                        success: function () {
                            AJS.org.showInfo();
                            AJS.dialog2("#mappingTeam-dialog").hide();
                            loadOrgAndTeamTree();
                        }
                    });
                });
                $('#mapping-cancle-btn').click(function (e) {
                    e.preventDefault();
                    AJS.dialog2("#mappingTeam-dialog").hide();

                })
                $('#role-cancle-btn').click(function (e) {
                    e.preventDefault();
                    // AJS.dialog2("#role-dialog").hide();
                })
                // 页面左侧tab标签切换事件
                $('.tabs-menu .menu-item').click(function () {
                    var treeid = $(this).find('a').data('treeid');
                    var tree = $.fn.zTree.getZTreeObj(treeid);
                    if (treeid == _this.ids.teamTreeId) {
                        controlePower(2);
                        $('button.teamManage').show();
                    } else {
                        controlePower(1);
                        $('button.teamManage').hide();
                    }
                    var nodes = tree.getNodes();
                    if (nodes.length > 0) {
                        $('#' + nodes[0].tId + '_span').click();
                    }
                });

                // 变更团队成员对话
                AJS.dialog2("#addEmployee-dialog").on("show", function () {
                    var _this = $(this);
                    var employeeids = [];
                    var left = _this.find('.dialog-all-member-content');
                    left.empty();
                    var right = _this.find('.dialog-member-in-content');
                    _this.find('button.add').click(function () {
                        //防止重复添加到右�?
                        employeeids = [];
                        right.find('tbody tr').each(function (index, item) {
                            employeeids.push($(item).data('employeeid'));
                        });
                        //添加到右�?
                        left.find('tbody tr.member-selected').each(function (index, item) {
                            if (!~employeeids.indexOf($(item).data('employeeid'))) {
                                $('<td class="role" data-roleid="' + defaultRole.id + '">' + defaultRole.name + '</td>').appendTo(item);
                                $(item).removeClass('member-selected').addClass('normal').appendTo(right.find('tbody'));
                            }
                        });
                    });
                    _this.find('button.addAll').click(function () {
                        //防止重复添加到右�?
                        employeeids = [];
                        right.find('tbody tr').each(function (index, item) {
                            employeeids.push($(item).data('employeeid'));
                        });
                        //全部添加到右侧
                        left.find('tbody tr').each(function (index, item) {
                            if (!~employeeids.indexOf($(item).data('employeeid'))) {
                                $('<td class="role" data-roleid="' + defaultRole.id + '">' + defaultRole.name + '</td>').appendTo(item);
                                $(item).removeClass('member-selected').addClass('normal').appendTo(right.find('tbody'));
                            }
                        });
                    });
                    _this.find('button.remove').click(function () {
                        //防止重复添加到左�?
                        employeeids = [];
                        left.find('tbody tr').each(function (index, item) {
                            employeeids.push($(item).data('employeeid'));
                        });
                        //添加到左�?
                        right.find('tbody tr.member-selected').each(function (index, item) {
                            if (!~employeeids.indexOf($(item).data('employeeid'))) {
                                var memberselected = $(item).removeClass('member-selected');
                                memberselected.find('td.role').remove();
                                memberselected.appendTo(left.find('tbody'));
                            }
                        });
                    });
                    _this.find('button.removeAll').click(function () {
                        //防止重复添加到左�?????
                        employeeids = [];
                        left.find('tbody tr').each(function (index, item) {
                            employeeids.push($(item).data('employeeid'));
                        });
                        // 全部添加到左�?????(只能操作 class �????? normal �????? tr)
                        right.find('tbody tr.normal').each(function (index, item) {
                            if (!~employeeids.indexOf($(item).data('employeeid'))) {
                                var memberselected = $(item).removeClass('member-selected');
                                memberselected.find('td.role').remove();
                                $(item).appendTo(left.find('tbody'));
                            }
                        });
                    });

                    // 加载右侧框的数据
                    var leftData;
                    var rightData;
                    var teamId = AJS.org.getSelectTreeId().substr(2);
                    var memberContent = _this.find('.dialog-member-in-content');
                    memberContent.find('.roleSelDiv').hide();
                    $.ajax({
                        type: "get",
                        url: contextPath + "/rest/wk-teamwork/latest/orgstr/" + teamId + "/getDirectStruGroupOfEmployeeByTeamId",
                        data: {pageNo: 1, pageCount: 10000, type: 1},
                        contentType: "application/json; charset=utf-8",
                        dataType: "json",
                        success: function (data) {
                            rightData = data;
                            memberContent.find('.member-table').empty();
                            var dialogMemberTableHtml = work.Org.Template.dialogMemberRight({data: data});
                            $(dialogMemberTableHtml).appendTo(memberContent.find('.member-table'));
                            memberContent.on(' click', 'tbody tr', function () {
                                var tr = $(this);
                                clickTr = tr;
                                if (tr.hasClass("normal")) {
                                    if (tr.hasClass('member-selected')) {
                                        tr.removeClass('member-selected');
                                    } else {
                                        tr.addClass('member-selected');
                                    }
                                } else {
                                    tr.removeClass('member-selected');
                                }
                            });
                            memberContent.on('click', '.role', function () {
                                memberContent.find('table').hide();
                                memberContent.find('.roleSelDiv').show();
                                memberContent.find('.roleSelDiv').find('.employee-name').text($(this).prev().text());

                                var roleid = ($(this).data('roleid') || '') + '';
                                if (!roleData) {
                                    // 加载角色store
                                    $.ajax({
                                        type: "get",
                                        url: contextPath + "/rest/wk-teamwork/latest/auth/getTeamRole",
                                        contentType: "application/json; charset=utf-8",
                                        dataType: "json",
                                        success: function (data) {
                                            var result = [];
                                            $(data).each(function (idx, item) {
                                                result.push({id: item.id, text: item.name})
                                            });
                                            roleData = result;

                                            $('#roleSel').auiSelect2({
                                                allowClear: true,
                                                data: {results: result, text: 'text'},
                                                width: 280
                                            });
                                            $('#roleSel').auiSelect2('val', roleid);
                                        }
                                    });
                                } else {
                                    $('#roleSel').auiSelect2({
                                        allowClear: true,
                                        data: {results: roleData, text: 'text'},
                                        width: 280
                                    });
                                    $('#roleSel').auiSelect2('val', roleid);
                                }
                            })

                            memberContent.find('a.cancel').click(function () {
                                memberContent.find('.roleSelDiv').hide();
                                memberContent.find('table').show();
                            });
                            memberContent.find('button.save').one('click', function () {
                                var roleData = $('#roleSel').auiSelect2('data');
                                $.ajax({
                                    type: "post",
                                    url: contextPath + "/rest/wk-teamwork/latest/auth/updateRole",
                                    contentType: "application/x-www-form-urlencoded; charset=utf-8",
                                    data: {
                                        roleId: roleData.id,
                                        teamId: teamId,
                                        employeeId: clickTr.data('employeeid'),
                                    },
                                    dataType: "json",
                                    success: function () {
                                        clickTr.find('.role').text(roleData.text).data('roleid', roleData.id);
                                        memberContent.find('a.cancel').click();
                                    }
                                });

                            })
                            try {
                                AJS.tablessortable.setTableSortable(memberContent.find('table'));
                            } catch (e) {
                            }
                        }
                    });
                    // 加载弹出框左侧的组织机构�?
                    $.ajax({
                        type: "get",
                        url: contextPath + "/rest/wk-teamwork/latest/orgstr/getOrgAndDept",
                        contentType: "application/json; charset=utf-8",
                        dataType: "json",
                        success: function (data) {
                            var zNodes = [];
                            $(data).each(function (index, item) {
                                zNodes.push({
                                    id: item.id,
                                    name: item.name,
                                    pId: item.parent,
                                    t: item.name,
                                    open: true,
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
                                    }
                                },
                                callback: {
                                    onClick: function (event, treeId, treeNode) {
                                        $.ajax({
                                            type: "get",
                                            url: contextPath + "/rest/wk-teamwork/latest/orgstr/" + treeNode.id + "/getEmployees",
                                            data: {pageNo: 1, pageCount: 10000, type: 0},
                                            contentType: "application/json; charset=utf-8",
                                            dataType: "json",
                                            success: function (data) {
                                                leftData = data.data;
                                                var arrUnCommon = [];
                                                var arrCommon = [];
                                                //  右侧框中  需要变灰的成员  存入 arrUnCommon
                                                for (var i = 0; i < rightData.length; i++) {
                                                    var exist = false;
                                                    for (var j = 0; j < leftData.length; j++) {
                                                        if (leftData[j].employeeNo == rightData[i].employeeNo) {
                                                            arrCommon.push(rightData[i]);
                                                            exist = true;
                                                            break;
                                                        }
                                                    }
                                                    if (!exist) {
                                                        arrUnCommon.push(rightData[i]);
                                                    }
                                                }

                                                //   左侧框中  需要显示的成员 存入 leftDataToShowInTheLeft
                                                var leftDataToShowInTheLeft = [];
                                                for (var i = 0; i < leftData.length; i++) {
                                                    var exist = false;
                                                    for (var j = 0; j < arrCommon.length; j++) {
                                                        if (arrCommon[j].employeeNo == leftData[i].employeeNo) {
                                                            exist = true;
                                                            break;
                                                        }
                                                    }
                                                    if (!exist) {
                                                        leftDataToShowInTheLeft.push(leftData[i]);
                                                    }
                                                }

                                                // 左侧框显�?????
                                                var memberContent = _this.find('.dialog-all-member-content');
                                                memberContent.empty();
                                                var dialogMemberTableHtml = work.Org.Template.dialogMemberLeft({data: leftDataToShowInTheLeft});
                                                $(dialogMemberTableHtml).appendTo(memberContent);
                                                memberContent.find('tbody tr').click(function () {
                                                    var tr = $(this);
                                                    if (tr.hasClass('member-selected')) {
                                                        tr.removeClass('member-selected');
                                                    } else {
                                                        tr.addClass('member-selected');
                                                    }
                                                })
                                                try {
                                                    AJS.tablessortable.setTableSortable(memberContent.find('table'));
                                                } catch (e) {
                                                }

                                                // 点击组织部门树以�?????, 把右侧框�?????  不属于被点击组织/部门的成�?????  变灰
                                                function becomeGrey(arrCommon, arrUnCommon) {
                                                    // 使用第二个表格模板work.Org.Template.dialogMember2, 分两种颜色重新加载右侧框
                                                    var memberContent = _this.find('.dialog-member-in-content');
                                                    memberContent.find('.member-table').empty();
                                                    memberContent.find('.roleSelDiv').hide();
                                                    var dialogMemberTableHtml = work.Org.Template.dialogMember2({
                                                        data1: arrCommon,
                                                        data2: arrUnCommon
                                                    });
                                                    $(dialogMemberTableHtml).appendTo(memberContent.find('.member-table'));
                                                    // memberContent.find('tbody tr').click(function () {
                                                    //     var tr = $(this);
                                                    //     if (tr.hasClass("normal")) {
                                                    //         if (tr.hasClass('member-selected')) {
                                                    //             tr.removeClass('member-selected');
                                                    //         } else {
                                                    //             tr.addClass('member-selected');
                                                    //         }
                                                    //     } else {
                                                    //         tr.removeClass('member-selected');
                                                    //     }
                                                    // });
                                                    try {
                                                        AJS.tablessortable.setTableSortable(memberContent.find('table'));
                                                    } catch (e) {
                                                    }
                                                }

                                                becomeGrey(arrCommon, arrUnCommon);
                                            }
                                        });
                                    }
                                }
                            };
                            $.fn.zTree.init(_this.find('#dialog-org-tree'), setting, zNodes);
                        }
                    });


                });
                // 未映射项目弹出窗�?
                AJS.dialog2('#mappingTeam-dialog').on('show', function () {
                    $.ajax({
                        type: "get",
                        url: contextPath + "/rest/wk-teamwork/latest/orgstr/unmappedTeam",
                        contentType: "application/json; charset=utf-8",
                        dataType: "json",
                        success: function (data) {
                            var result = [];
                            $(data).each(function (idx, item) {
                                result.push({id: item.id, text: item.groupName})
                            });

                            $('#mappingTeam').auiSelect2({
                                placeholder: "Select a group",
                                allowClear: true,
                                data: {results: result, text: 'text'}
                            });
                        }
                    });
                });
            },
            ids: {
                treeId: 'deptTree',
                teamTreeId: 'teamTree'
            },
            url: {
                deptValid: contextPath + "/rest/wk-teamwork/latest/department/validDeptno",
                employeeValid: contextPath + "/rest/wk-teamwork/latest/strEmployee/validEmployeeno"
            },
            template: {
                option: function (data) {
                    return AJS.template("<option value={key}>{value}</option>").fill(data).toString();
                }
            },
            message: {
                selectOneNode: AJS.I18n.getText('workorg.property.organization.message.selectOneNode'),
                operationSucessed: AJS.I18n.getText('workorg.property.organization.message.operationSucessed'),
                operationFailed: AJS.I18n.getText('workorg.property.organization.message.operationFailed'),
                deleteConfirm: AJS.I18n.getText('workorg.property.organization.message.deleteConfirm'),
                deptCreatedError: AJS.I18n.getText('workorg.property.organization.message.deptCreatedError'),
                teamCreatedError: AJS.I18n.getText('workorg.property.organization.message.deptCreatedError'),
                // selectOneJiraUser: AJS.I18n.getText('workorg.property.organization.selectOrg'),
                selectOneGroup: AJS.I18n.getText('workorg.property.organization.selectOneGroup'),
                selectOneParent: AJS.I18n.getText('workorg.property.organization.selectOneParent'),
                checkFormat: AJS.I18n.getText('workorg.property.form.valid.checkFormat'),
                enterTimeNotNullable: AJS.I18n.getText('workorg.property.message.alert.enterTimeNotNullable'),
                leaveTimeNotNullable: AJS.I18n.getText('workorg.property.message.alert.leaveTimeNotNullable'),
                selectOneOrg: AJS.I18n.getText('workorg.property.organization.selectOneOrg')

            },
            getZtree: function () {
                var treeId = $('.tabs-menu .menu-item.active-tab a').data('treeid');
                if (treeId == this.ids.treeId) {
                    return $.fn.zTree.getZTreeObj(this.ids.treeId);
                } else if (treeId == this.ids.teamTreeId) {
                    return $.fn.zTree.getZTreeObj(this.ids.teamTreeId);
                }
                return {};
            },
            getSelectTreeNode: function (needSelectOneNode) {
                var zTree = this.getZtree();
                var nodes = zTree.getSelectedNodes();
                if (nodes.length == 0 && needSelectOneNode) {
                    alert(AJS.org.message.selectOneNode);
                }
                return nodes[0] || {};
            },
            getSelectTreeId: function (needSelectOneNode) {
                var node = this.getSelectTreeNode(needSelectOneNode);
                return node.id;
            },
            afterSave: function () {
                var nodeId = AJS.org.getSelectTreeNode().tId;
                if (!nodeId) {
                    // nodeId = this.getZtree().getNodes()[0].tId;
                    // 首次创建组织, 重新加载页面
                    //window.location.reload();
                    // 如果没有选中任何节点, 则默认选中第一个节�????
                    nodeId = AJS.org.getZtree().getNodes()[0].tId;
                    //nodeId = this.getZtree().getNodes()[0].tId;
                    // 首次创建组织, 重新加载页面
                    //window.location.reload();
                }
                $('#' + nodeId + "_span").click();
            },
            showInfo: function (options) {
                if (typeof options === "string") {
                    options = {title: options};
                }
                options = options || {};
                var myFlag = AJS.flag({
                    type: options.type || 'info',
                    title: options.title || AJS.org.message.operationSucessed
                });
                setTimeout(function () {
                    myFlag.close()
                }, 1000);
            },
            showError: function (mes) {
                this.showInfo({type: 'error', title: mes || AJS.org.message.operationFailed});
            },
            formValid: {
                dept: function (form, id) {
                    form.validator({
                        fields: {
                            groupName: "required",
                            //groupNo: "required; remote(" + AJS.org.url.deptValid + "?groupId=" + (id || '') + ")"
                            //2018年9月13日11:23:01  去除了 检测 必须条件
                        }
                    });
                },
                org: function (form) {
                    form.validator({
                        fields: {
                            name: "required"
                        }
                    });
                },
                employee: function (form, employeeId) {
                    form.validator({
                        rules: {
                            phone: function (element, params) {
                                return /^[0-9|+|\-|(|)|*|#]{6,40}$/.test(element.value) ||
                                    AJS.org.message.checkFormat; // 只能填写数字 + - ( ) * #
                            }

                        },
                        fields: {
                            employeeName: "required",
                            roleId: "required",
                            email: "email",
                            phone: "phone",
                            employeeNo: "remote(" + AJS.org.url.employeeValid + "?employeeId=" + (employeeId || '') + ")"
                        }
                    });
                }
            }
        };
        AJS.org.init();

        function treeClickFunction(e, treeId, node) {

            // 点击左侧树以�??????, 隐藏"该页面可维护公司�??????, 部门树以及雇员信息�?"
            $("#initial-info").css("display", "none");
            // var node = AJS.org.getSelectTreeNode();
            var id = node.id;
            var pageCount = $('#pageCount').val() || 5;
            var treeId = $('#' + node.tId + '_span').parents('ul.ztree')[0].id;
            // var treeId = $('.tabs-menu .menu-item.active-tab a').data('treeid');
            var deptType = treeId == AJS.org.ids.teamTreeId;

            if (node.type == 'org' || node.type == 'dept' || node.type == 'team' || node.type == 'unmappedmember') {
                !(function getMemberList(pageNo, pageCount, searchStr) {
                    var url;
                    var template;
                    var deleteUrl;
                    if (deptType == 0) {
                        url = contextPath + "/rest/wk-teamwork/latest/orgstr/" + id + "/getEmployees"; // 组织的雇员列�???????
                        template = work.Org.Template.memberList;
                        deleteUrl = contextPath + "/rest/wk-teamwork/latest/strEmployee/";
                    } else {
                        url = contextPath + "/rest/wk-teamwork/latest/orgstr/" + id + "/getStruGroupMembers"; // 团队成员
                        template = work.Org.Template.teamMemberList;
                        deleteUrl = contextPath + "/rest/wk-teamwork/latest/strEmployee/deleteTeamMember/";
                    }

                    $.ajax({
                        type: "get",
                        url: url,
                        data: {pageNo: pageNo, pageCount: pageCount, q: searchStr, type: deptType ? 1 : 0},
                        contentType: "application/json; charset=utf-8",
                        dataType: "json",
                        success: function (data) {
                            //修复成员和公司关系的bug成员只在所属公司显示------------------------
                            /*                            var groupMemberArray = new Array();
                                                        $(data.data).each(function (index, groupMember) {
                                                            var nodeId = node.id;
                                                            var realNodeId = nodeId.substring(nodeId.indexOf("_") + 1);
                                                            if (groupMember.orgId == realNodeId) {
                                                                groupMemberArray.push(groupMember);
                                                            }
                                                        })*/
                            //--------------------------------------------------------------
                            var memberContent = viewMember(true, true);
                            var html = template({
                                /*  data: groupMemberArray,//公司的成员*/
                                data: data.data,//公司的成员
                                pageSize: data.pageSize,
                                pageCount: data.pageCount,
                                total: data.total,//公司的成员数量
                                /* total: groupMemberArray.length,//公司的成员数量*/
                                currentPage: data.currentPage
                            });

                            $(html).appendTo(memberContent);
                            try {
                                AJS.tablessortable.setTableSortable(memberContent.find('table'));
                            } catch (e) {
                            }

                            memberContent.find('a.pageNo').click(function () {
                                // pageCommand 有三种情况: 上一页, 下一页, 或者数字
                                var pageCommand = $(this).text();
                                if (pageCommand == AJS.I18n.getText('workorg.property.organization.page.desc6')) {
                                    // 上一页
                                    getMemberList(parseInt($('#currentPageNo').text()) - 1, $('#pageCount').val(), $('#searchEmployee').val());
                                } else if (pageCommand == AJS.I18n.getText('workorg.property.organization.page.desc7')) {
                                    //下一页
                                    getMemberList(parseInt($('#currentPageNo').text()) + 1, $('#pageCount').val(), $('#searchEmployee').val());
                                } else {
                                    getMemberList($(this).text(), $('#pageCount').val(), $('#searchEmployee').val());
                                }
                            });
                            //跳转
                            memberContent.find('#skipToButton').click(function () {
                                var num = parseInt($('#skipToInput').val());
                                var pageSize = parseInt($('#pageSize').val());
                                if (num <= 0) {
                                    num = 1;
                                }
                                if (num > pageSize) {
                                    num = pageSize;
                                }
                                getMemberList(num, $('#pageCount').val(), $('#searchEmployee').val());
                            });

                            $('#searchBtn').one('click', function () {
                                getMemberList(1, $('#pageCount').val(), $('#searchEmployee').val());
                            });
                            $('select#pageCount').change(function () {
                                getMemberList(1, $(this).val(), $('#searchEmployee').val());
                            });
                            memberContent.find('tr .edit').click(function () {
                                var employeeId = $($(this).parents('tr')[0]).data('employeeid');
                                $.ajax({
                                    type: "get",
                                    url: contextPath + "/rest/wk-teamwork/latest/strEmployee/" + employeeId,
                                    contentType: "application/json; charset=utf-8",
                                    dataType: "json",
                                    success: function (data) {
                                        var memberContent = viewMember(false);
                                        var html = work.Org.Template.memberCreateOrUpdate({data: data});
                                        $(html).appendTo(memberContent);
                                        createOrUpdateEmployee(memberContent, data);
                                    }
                                });
                            });
                            memberContent.find('tr .delete').click(function () {
                                if (!confirm(AJS.org.message.deleteConfirm)) return;
                                var tr = $($(this).parents('tr')[0]);
                                var employeeId = tr.data('employeeid');

                                $.ajax({
                                    type: "delete",
                                    url: deleteUrl + employeeId,
                                    contentType: "application/json; charset=utf-8",
                                    dataType: "json",
                                    success: function (data) {
                                        AJS.org.showInfo({title: data || AJS.org.message.operationSucessed});
                                        AJS.org.afterSave();
                                    },
                                    error: function () {
                                        AJS.org.showError();
                                    }
                                });
                            });
                        }
                    });
                })(1, pageCount, '');
            } else { // 未映射团队操做 TODO
                if (node.type == 'project') { // 弹出一个框，选择映射的团队
                    AJS.dialog2('#mappingTeam-dialog').show();
                } else if (node.type == 'projectroot') { // 不做处理
                } else if (node.type == 'teamproject') { // 团队项目
                    var view = viewMember();
                    view.empty();
                    var teamProjectHtml = work.Org.Template.teamProject();
                    var $teamProject = $(teamProjectHtml).appendTo(view);
                    // team成员 映射到 jira的projectRole
                    $teamProject.find('button.add').click(function () {
                        var teamprojectmemberTree = $.fn.zTree.getZTreeObj("teamprojectmemberTree");
                        var checkedNodes = teamprojectmemberTree.getCheckedNodes();
                        if (checkedNodes.length == 0) {
                            return;
                        }

                        var roles = [];
                        $(checkedNodes).each(function (index, item) {
                            var role = item.role;
                            var projectRoleId = projectRole[role];
                            var users = [];
                            if (!!item.jiraUserKey) {
                                users.push(item.jiraUserKey);
                                $.ajax({
                                    type: "post",
                                    url: contextPath + "/rest/projectconfig/latest/roles/" + node.projectkey + "/" + projectRoleId,
                                    contentType: "application/json; charset=utf-8",
                                    data: JSON.stringify({
                                        groups: [],
                                        users: users
                                    }),
                                    dataType: "json",
                                    success: function (data) {
                                        AJS.org.afterSave();
                                    }
                                });
                            }

                        });

                    });
                    var projectRoleArray = [];
                    var sendData = {};
                    // 一键映射
                    $teamProject.find('button.onekey').click(function () {
                        var data = {};
                        var teamprojectmemberTree = $.fn.zTree.getZTreeObj("teamprojectmemberTree");
                        // 需要一键映射的团队成员
                        var teamnodes = teamprojectmemberTree.getNodesByFilter(function (node) {
                            return !!node.jiraUserKey && !!node.id;
                        });
                        $(teamnodes).each(function (index, item) {
                            var key = item.jiraUserKey + '---' + item.role;
                            if (projectRoleArray.indexOf(key) < 0) { // 未映射过去的
                                var roleId = projectRole[item.role];
                                var sendDatum = sendData[roleId];
                                if (!sendDatum) {
                                    sendDatum = [];
                                    sendData[roleId] = sendDatum;
                                }
                                sendDatum.push(item.jiraUserKey);
                            }
                        })
                        var ajaxReqArray = []
                        for (var projectRoleId in sendData) {
                            var ajaxReq = $.ajax({
                                type: "post",
                                url: contextPath + "/rest/projectconfig/latest/roles/" + node.projectkey + "/" + projectRoleId,
                                contentType: "application/json; charset=utf-8",
                                data: JSON.stringify({
                                    groups: [],
                                    users: sendData[projectRoleId]
                                }),
                                dataType: "json",
                            });
                            ajaxReqArray.push(ajaxReq)
                        }
                        // 从project映射到team
                        var mapToTeamAjax = $.ajax({
                            type: "get",
                            url: contextPath + "/rest/wk-teamwork/latest/orgstr/projectMapToTeam",
                            contentType: "application/json; charset=utf-8",
                            data: {
                                projectKey: node.projectkey,
                                teamId: node.teamId
                            },
                            dataType: "json",
                        });
                        // ajaxReqArray.push(mapToTeamAjax);
                        $.when(ajaxReqArray).then(function () { // 全部请求完成之后，刷新树
                            $.when(mapToTeamAjax).then(function () {
                                AJS.org.afterSave()

                            })
                        })


                    })
                    // jira的projectRole 映射到 team成员
                    $teamProject.find('button.remove').click(function () {
                        var jiraRoleTree = $.fn.zTree.getZTreeObj("jiraRoleTree");
                        var selectedNodes = jiraRoleTree.getSelectedNodes();
                        if (selectedNodes.length > 0) {
                            var selectedNode = selectedNodes[0];
                            if (selectedNode.type == 'user') {
                                var jiaruserkey = selectedNode.id;
                                $.ajax({
                                    type: "post",
                                    url: contextPath + "/rest/wk-teamwork/latest/orgstr/mappingTeam",
                                    contentType: "application/x-www-form-urlencoded; charset=utf-8",
                                    data: {
                                        teamId: node.teamId,
                                        userkey: jiaruserkey,
                                        projectkey: node.projectkey,
                                    },
                                    success: function () {
                                        AJS.org.afterSave();
                                    }
                                });

                            }

                        }


                    });
                    $.ajax({
                        type: "get",
                        url: contextPath + "/rest/wk-teamwork/latest/orgstr/teamprojectMemberRole?teamId=" + node.teamId,
                        contentType: "application/json; charset=utf-8",
                        dataType: "json",
                        success: function (data) {
                            var zNodes = [];
                            $(data).each(function (index, item) {
                                var node = {
                                    id: item.id,
                                    name: item.name,
                                    t: item.name,
                                    open: true,
                                    pId: item.parent,
                                    jiraUserKey: item.jiraUserKey,
                                    role: item.roleName
                                };
                                if (item.type == 'user') {
                                    node.name = item.name + '(' + item.roleName + ')-' + (item.jiraUserKey || '未关联');
                                    node.iconSkin = 'user';
                                } else {
                                    node.nocheck = true;
                                }
                                zNodes.push(node);
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
                                callback: {},
                                check: {
                                    enable: true
                                }

                            }
                            $.fn.zTree.init($('#teamprojectmemberTree'), setting, zNodes);
                        }
                    });

                    $.ajax({
                        type: "get",
                        url: contextPath + "/rest/projectconfig/latest/roles/" + node.projectkey,
                        contentType: "application/json; charset=utf-8",
                        data: {
                            pageNumber: 1,
                            pageSize: 1000,
                        },
                        dataType: "json",
                        success: function (data) {
                            data = data.roles;

                            var zNodes = [];
                            $(data).each(function (index, item) {
                                projectRole[item.name] = item.id;
                                if (item.total == 0) {
                                    return;
                                }
                                var roleName = item.name;
                                var roleId = item.id;
                                var groups = item.groups;
                                var users = item.users;
                                zNodes.push({
                                    id: roleId,
                                    name: roleName,
                                    t: roleName,
                                    open: true,
                                    pId: 0,
                                    type: 'role'
                                });
                                $(groups).each(function (groupindex, group) {
                                    zNodes.push(
                                        {
                                            id: group.id,
                                            name: group.name,
                                            t: group.name,
                                            pId: roleId,
                                            iconSkin: 'group',
                                            nocheck: true,
                                            type: 'group'
                                        });
                                });
                                $(users).each(function (userindex, user) {
                                    projectRoleArray.push(user.key + "---" + item.name);
                                    var name = user.displayName + '(' + user.key + ')';
                                    zNodes.push({
                                        id: user.key,
                                        name: name,
                                        t: name,
                                        pId: roleId,
                                        iconSkin: 'user',
                                        type: 'user'
                                    });

                                })
                            });
                            var setting = {
                                data: {
                                    key: {
                                        title: "t"
                                    },
                                    simpleData: {
                                        enable: true
                                    },
                                    view: {
                                        showIcon: false
                                    },
                                    check: {
                                        enable: true
                                    }
                                },
                                callback: {},
                            }
                            $.fn.zTree.init($('#jiraRoleTree'), setting, zNodes);
                        }
                    });
                } else if (node.type == 'unmappedmember') {

                }
                return;
            }
            var orgOrDeptInfo = $('.orgOrDeptInfo');
            orgOrDeptInfo.children().remove();

            if (node.type == 'org') {
                $.ajax({
                    type: "get",
                    url: contextPath + "/rest/wk-teamwork/latest/orgstr/" + id.substr(2),
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    success: function (data) {
                        var html = work.Org.Template.orgOrDeptInfo({
                            name: data.name,
                            desc: {
                                key: AJS.I18n.getText('workorg.property.organization.orgLevel'),
                                value: data.id
                            },
                            memo: data.memo,
                            addEmployeeShow: deptType
                        });
                        $(html).appendTo(orgOrDeptInfo);
                        if (deptType) {
                            orgOrDeptInfo.find('#addEmployeeToTeam').click(function () {
                                AJS.org.showInfo({title: AJS.I18n.getText('workorg.property.organization.selectTeam')});
                            })
                        }
                    }
                });
            } else if (node.type == 'dept' || node.type == 'team') {
                $.ajax({
                    type: "get",
                    url: contextPath + "/rest/wk-teamwork/latest/department/" + id.substr(2),
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    success: function (data) {
                        var html = work.Org.Template.orgOrDeptInfo({
                            name: data.groupName,
                            desc: {
                                key: AJS.I18n.getText('workorg.property.Department.departNo'),
                                value: data.groupNo || ''
                            },
                            memo: data.memo,
                            addEmployeeShow: deptType
                        });
                        $(html).appendTo(orgOrDeptInfo);
                        if (deptType) { // whether show the button
                            orgOrDeptInfo.find('#addEmployeeToTeam').click(function () {
                                AJS.dialog2('#addEmployee-dialog').show();
                            });
                            $('#dialog-close-button').click(function (e) {
                                e.preventDefault();
                                AJS.dialog2("#addEmployee-dialog").hide();
                            })

                        }
                    }
                });
            }
        }

        /**
         * 加载左侧的组织机构部门树
         */
        function loadTree(callback) {
            loadOrgAndDeptTree(callback);
            loadOrgAndTeamTree(callback);
        }

        /**
         * 加载组织机构部门�??
         * @param callback
         */
        function loadOrgAndDeptTree(callback) {
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/orgstr/getOrgAndDept",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    var zNodes = [];
                    $(data).each(function (index, item) {
                        zNodes.push({
                            id: item.id,
                            name: item.name,
                            pId: item.parent,
                            t: item.name,
                            open: true,
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
                            }
                        },
                        callback: {
                            onClick: treeClickFunction
                        }
                    };
                    $.fn.zTree.init($('#' + AJS.org.ids.treeId), setting, zNodes);
                    if (!!callback && typeof callback == 'function') {
                        callback()
                    }
                }
            });
        }

        /**
         * 加载组织机构团队
         * @param callback
         */
        function loadOrgAndTeamTree(callback) {
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/orgstr/getOrgAndTeamAndProject",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    var zNodes = [];
                    $(data).each(function (index, item) {
                        var node = {
                            id: item.id,
                            name: item.name,
                            pId: item.parent,
                            t: item.name,
                            open: true,
                            iconSkin: item.type,
                            type: item.type,
                            projectkey: item.projectKey,
                            projectName: item.projectName,
                            teamId: item.teamId
                        };

                        zNodes.push(node);
                    });
                    // 未映射项目
                    $.ajax({
                        type: "get",
                        url: contextPath + "/rest/wk-teamwork/latest/orgstr/unmappedProject",
                        contentType: "application/json; charset=utf-8",
                        dataType: "json",
                        success: function (teamData) {
                            $(teamData).each(function (index, item) {
                                zNodes.push({
                                    id: item.id,
                                    name: item.projectName,
                                    pId: item.parent,
                                    t: item.projectName,
                                    open: true,
                                    iconSkin: item.type,
                                    type: item.type
                                    // projectkey: item.projectKey
                                });
                            });
                            var setting = {
                                data: {
                                    key: {
                                        title: "t"
                                    },
                                    simpleData: {
                                        enable: true
                                    }
                                },
                                callback: {
                                    onClick: treeClickFunction
                                },
                                view: {
                                    addDiyDom: addDiyDom
                                }
                            };

                            function addDiyDom(treeId, treeNode) {
                                var aObj = $("#" + treeNode.tId + '_a');
                                if (treeNode.type == 'team') {
                                    var parentNode = treeNode.getParentNode();
                                    if (parentNode.type == 'team') { // 子团队不能挂项目
                                        return;
                                    }
                                    var teamId = treeNode.id;
                                    var editStr = "<span class='demoIcon' id='diyBtn_" + treeNode.id + "' title='"
                                        + treeNode.name + "'><span class='button project-icon'></span></span>";
                                    aObj.append(editStr);
                                    var btn = $("#diyBtn_" + treeNode.id);
                                    btn.bind("click", function (e) {
                                        e.stopPropagation();
                                        var view = viewMember();
                                        view.empty();
                                        var teamManageHtml = work.Org.Template.teamManage(
                                            {
                                                team: {
                                                    teamName: treeNode.name,
                                                    teamId: treeNode.id.substr(2)
                                                }
                                            });
                                        var $teamManage = $(teamManageHtml).appendTo(view);
                                        $teamManage.find('.save').click(function (e) {
                                            e.preventDefault();
                                            var checked = $teamManage.find('input[name=teamradio]:checked');
                                            var projectid = checked.val()
                                            $.ajax({
                                                type: "post",
                                                url: contextPath + "/rest/wk-teamwork/latest/orgstr/saveTeamProject",
                                                data: {
                                                    teamId: teamId.substr(2),
                                                    projectId: projectid
                                                },
                                                contentType: "application/x-www-form-urlencoded; charset=utf-8",
                                                success: function (data) {

                                                    loadOrgAndTeamTree();
                                                    viewMember(true);
                                                }
                                            });

                                        });
                                        $teamManage.find('.cancel').click(function (e) {
                                            viewMember(true);
                                        });

                                        $.ajax({
                                            type: "get",
                                            url: contextPath + "/rest/wk-teamwork/latest/orgstr/projectRelated?teamId=" + teamId.substr(2),
                                            contentType: "application/json; charset=utf-8",
                                            dataType: "json",
                                            success: function (data) {
                                                var teamManageRadioHtml = work.Org.Template.teamManageRadio({data: data});
                                                $(teamManageRadioHtml).appendTo($teamManage.find('.teamManage-left'));
                                            }
                                        });

                                    });
                                }
                            }

                            $.fn.zTree.init($('#' + AJS.org.ids.teamTreeId), setting, zNodes);
                            if (!!callback && typeof callback == 'function') {
                                callback()
                            }
                        }
                    });

                }
            });
        }

        loadTree();

        /**
         * 创建 组织机构/部门 按钮的公用代�???????
         * @param e
         */
        function createFunction(e) {
            // 点击左侧树以�??????, 隐藏"该页面可维护公司�??????, 部门树以及雇员信息�?"
            $("#initial-info").css("display", "none");
            var node = AJS.org.getSelectTreeNode();
            if (!e.data.empty == true) {
                if (node.type == 'dept' && e.data.type == 'org') {
                    AJS.org.showInfo({title: AJS.org.message.deptCreatedError});
                    return;
                }
                if (node.type == 'dept' && e.data.type == 'team') {
                    AJS.org.showInfo({title: AJS.org.message.teamCreatedError});
                    return;
                }
            }

            var operationContent = viewMember(false);
            var orgOrDeptHtml = e.data.template({data: e.data.data || {}});
            var orgOrDept = $(orgOrDeptHtml).appendTo(operationContent);
            var form = orgOrDept.find('form');
            if (!!e.data.valid) {
                e.data.valid(form);
            }

            if (e.data.type == 'org') { // 创建组织时，设置机构类别和分支机构特点二级联�???
                $('#type').on('change', function () {
                    if ($(this).val() == 1 || $(this).val() == 2) {
                        $('#characterDiv').show();
                    } else {
                        $('#characterDiv').hide();
                    }
                })
            }

            loadOrgStore(orgOrDept, e.data.empty == true ? '' : node.id, e.data.type);
            orgOrDept.find('#save-button').click(function () {
                form.isValid(function (v) {
                    if (!v) {
                        return;
                    }

                    var data = form.serializeJson();

                    if (e.data.type != 'org' && !data.parent) {
                        AJS.org.showInfo({title: AJS.org.message.selectOneParent});
                        return;
                    }
                    var str = JSON.stringify(data);

                    $.ajax({
                        type: "POST",
                        url: e.data.url,
                        contentType: "application/json; charset=utf-8",
                        data: str,
                        dataType: "json",
                        success: function (data) {
                            e.data.treeload(function () {
                                var type = e.data.type;
                                var suffix = ''
                                if (type == 'org') {
                                    suffix = 'o_';
                                } else {
                                    suffix = 'd_';
                                }
                                var node = AJS.org.getZtree().getNodeByParam('id', suffix + data.id, null);
                                if (!!node) {
                                    $('#' + node.tId + '_span').click();
                                    AJS.org.getZtree().selectNode(node);
                                } else {
                                    AJS.org.afterSave();
                                }
                            });
                            AJS.org.showInfo();
                        }
                    });
                });
            });
            orgOrDept.find('.cancel').click(function () {
                viewMember(true);
            })
        }

        /** 创建部门 */
        $('button.createDept').bind('click',
            {
                template: work.Org.Template.deptCreateAndUpdate,
                data: {type: 0},
                url: contextPath + "/rest/wk-teamwork/latest/department",
                type: 'dept',
                valid: AJS.org.formValid.dept,
                treeload: loadOrgAndDeptTree
            },
            createFunction);
        /** 创建组织机构 */
        $('button.createOrg').bind('click',
            {
                template: work.Org.Template.orgCreateAndUpdate,
                url: contextPath + "/rest/wk-teamwork/latest/orgstr",
                type: 'org',
                valid: AJS.org.formValid.org,
                treeload: loadOrgAndDeptTree
            },
            createFunction
        );
        /**创建无上级的组织机构**/
        $('button.creategs').bind('click',
            {
                template: work.Org.Template.orgCreateAndUpdate,
                url: contextPath + "/rest/wk-teamwork/latest/orgstr",
                type: 'org',
                valid: AJS.org.formValid.org,
                empty: true,
                treeload: loadOrgAndDeptTree
            },
            createFunction
        );
        /** 创建团队 */
        $('button.createTeam').bind('click',
            {
                template: work.Org.Template.deptCreateAndUpdate,
                data: {type: 1},
                url: contextPath + "/rest/wk-teamwork/latest/department",
                type: 'team',
                valid: AJS.org.formValid.dept,
                treeload: loadOrgAndTeamTree
            },
            createFunction
        );

        /** 创建或编辑雇员公用代�????? */
        function createOrUpdateEmployee($member, $data) {
            var flag = "";
            $('#entryTime, #leaveTime').datepicker({
                language: AJS.I18n.getText('workorg.property.common.language'),
                format: "yyyy-mm-dd",
                keyboardNavigation: false,
                todayHighlight: true,
                autoclose: true
            });
            $('#employmentStatus').on('change', function () {
                var selectVal = $(this).val();
                $member.find('.time-require').hide();
                if (selectVal == 1) {
                    $member.find('.entrytime-require').show();
                } else {
                    $member.find('.leavetime-require').show();
                }
            });
            for (var item in $data) {
                if (item != 'groupId') {
                    $member.find('#' + item).val($data[item]);
                }
            }
            if (!!$data.id) {
                $member.find('.createAnother').hide();
            }
            // 加载组织机构下拉列表
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/orgstr",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (orgData) {
                    var result = [];
                    $(orgData).each(function (idx, item) {
                        result.push({id: item.id, text: item.name})
                    });

                    // 加载部门数据
                    function loadDeptData(orgId, groupId) {
                        $.ajax({
                            type: "get",
                            url: contextPath + "/rest/wk-teamwork/latest/orgstr/" + orgId + "/getSubDeptByOrgId",
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (deptData) {
                                var result = [];
                                result.push({id: "", text: "none"})
                                $(deptData).each(function (idx, item) {
                                    result.push({id: item.id, text: item.groupName})
                                });

                                $('#groupId').auiSelect2({
                                    placeholder: "Select a group",
                                    allowClear: true,
                                    data: {results: result, text: 'text'}
                                });
                                if (!!groupId) {
                                    $member.find('#groupId').auiSelect2('val', groupId);
                                } else {
                                    $member.find('#groupId').auiSelect2('val', '');
                                }
                            }
                        });
                    };

                    $('#orgId').auiSelect2({
                        placeholder: "Select a organization",
                        allowClear: true,
                        data: {results: result, text: 'text'}
                    }).on('change', function () {
                        var orgId = $(this).val();
                        loadDeptData(orgId, $data.groupId);
                    });

                    if (!!$data.orgId) {
                        $member.find('#orgId').val($data.orgId).trigger("change");
                    }
                }
            });
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/role/getAll",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (roleData) {
                    var map = {}
                    map["System Administrator"] = AJS.I18n.getText('workorg.property.permissionMgr.role.sa');
                    map["It Operation"] = AJS.I18n.getText('workorg.property.permissionMgr.role.op');
                    map["Top Executives"] = AJS.I18n.getText('workorg.property.permissionMgr.role.sm');
                    map["Department Manager"] = AJS.I18n.getText('workorg.property.permissionMgr.role.dm');
                    map["Project Managers"] = AJS.I18n.getText('workorg.property.permissionMgr.role.pm');
                    map["General Staff"] = AJS.I18n.getText('workorg.property.permissionMgr.role.gs');
                    map["Product Manager"] = AJS.I18n.getText('workorg.property.permissionMgr.role.pdm');
                    map["Project Manager"] = AJS.I18n.getText('workorg.property.permissionMgr.role.pl');
                    map["Test supervisor"] = AJS.I18n.getText('workorg.property.permissionMgr.role.ts');
                    map["Developer"] = AJS.I18n.getText('workorg.property.permissionMgr.role.dev');
                    map["Product Tester"] = AJS.I18n.getText('workorg.property.permissionMgr.role.pt');
                    var result = [];
                    $(roleData).each(function (idx, item) {
                        result.push({id: item.id, text: (map[item.name] != null ? map[item.name] : item.name)})
                    });
                    $('#roleId').auiSelect2({
                        placeholder: "Select a Role",
                        allowClear: true,
                        data: {results: result, text: 'text'},
                        multiple: true
                    })
                }
            });
            /*=======================================================================================*/
            // 加载雇员下拉列表（直接上司）
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/strEmployee",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    var result = [];
                    $(data).each(function (idx, item) {
                        var text = item.employeeName + '(' + item.orgName;
                        if (!!item.groupName) {
                            text += '-' + item.groupName;
                        }
                        text += ')';
                        result.push({id: item.id, text: text})
                    });

                    $('#supervisor').auiSelect2({
                        placeholder: "Select",
                        allowClear: true,
                        data: {results: result, text: 'text'}
                    });
                    if (!!$data.supervisor) {
                        $member.find('#supervisor').val($data.supervisor).trigger("change");
                    }
                }
            });
            if (!!$data.id) {
                flag = $data.id;
            } else {
                flag = -1;
            }

            function format(item) {
                console.log('fromat')
                var html = '<span >';
                html += item.text + '</span>';
                return $(html);
            }

            $('#jiraUserKey').auiSelect2({
                placeholder: "select a jira user",
                allowClear: true,
                quietMillis: 250,
                formatSelection: format,
                formatResult: format,
                id: function (bond) {
                    console.log('id')
                    console.log(bond.id)
                    return bond.id;
                },
                initSelection: function (element, callback) {
                    console.log('initselection')
                    if (!!$data.jiraUserKey) {
                        $.ajax(contextPath + "/rest/wk-teamwork/latest/strEmployee/getUnrelatedJirauser/" + flag, {
                            dataType: "json"
                        }).done(function (data) {
                            callback(data);
                        });
                    }

                },
                ajax: {
                    url: contextPath + "/rest/wk-teamwork/latest/strEmployee/unrelatedJirauser/" + flag,
                    dataType: 'json',
                    data: function (term, page) {
                        return {
                            q: term, // search term
                        };
                    },
                    results: function (data) {
                        var result = [];
                        $(data).each(function (idx, item) {
                            result.push({
                                id: item.key,
                                text: item.displayName,
                                emailAddress: item.emailAddress,
                                name: item.name
                            })
                        });
                        console.log('result')
                        console.log(result)
                        return {results: result};
                    },
                    cache: true
                }
            }).on('change', function () {
                var data = $(this).auiSelect2('data');
                if (!$member.find('#employeeName').val()) {
                    $member.find('#employeeName').val(data.text);
                }
                if (!$member.find('#email').val()) {
                    $member.find('#email').val(data.emailAddress);
                }
            });

            var form = $member.find('form');
            AJS.org.formValid.employee(form, $data.id);
            $member.find('#savebut').click(function () {
                // alert("验证工号是否重复！！")
                var data = form.serializeJson();
                form.isValid(function (v) { // 有ajax调用，后续逻辑写在回调函数
                   /* if (!v) {
                        return;
                    }*/
                    if (!!$data.id) {
                        data.id = $data.id;
                    }

                    /*组织机构校验*/
                   /* if (!data.orgId) {
                        AJS.org.showInfo({title: AJS.org.message.selectOneOrg});
                        return;
                    }*/
                    var employmentStatus = $('#employmentStatus').val();
                    /*if (employmentStatus == 1 && !$('#entryTime').val()) {
                        AJS.org.showInfo(AJS.org.message.enterTimeNotNullable);
                        return;
                    }
                    if (employmentStatus == 2 && !$('#leaveTime').val()) {
                        AJS.org.showInfo(AJS.org.message.leaveTimeNotNullable);
                        return;
                    }*/
                 //   alert("开始修改！！")
                    var str = JSON.stringify(data);
                    $.ajax({
                        type: !!$data.id ? "PUT" : "POST",
                        url: contextPath + "/rest/wk-teamwork/latest/strEmployee",
                        contentType: "application/json; charset=utf-8",
                        data: str,
                        dataType: "json",
                        complete: function (mes) {
                            if (mes.readyState != 4 || mes.status != 200) {
                                AJS.org.showError(mes.responseText);
                                return;
                            }
                            AJS.org.showInfo();
                           // alert("sddddddddfdfdfdfd")
                            if ($member.find('.createAnotherCheckbox').prop('checked')) { // 继续创建
                                $member.find('#jiraUserKey').auiSelect2('val', '');
                                $member.find('#employeeName').val('');
                                $member.find('#email').val('');
                                $member.find('#employeeNo').val('');
                                $member.find('#phone').val('');
                                $member.find('#otherPhone').val('');
                                // $member.find('#memo').val('');
                            } else {
                                AJS.org.afterSave();
                            }

                        }
                    });
                })
            });
            $member.find('.cancel').click(function () {
                viewMember(true);
            })
        }

        /**
         * 获取部门节点的第一个父组织节点
         */
        function getOrgParentNode(deptNode) {
            var parentNode = deptNode.getParentNode();
            var parentNodeType = parentNode.type;
            if (parentNodeType == 'org') {
                return parentNode;
            } else if (parentNodeType == 'dept') {
                return getOrgParentNode(parentNode);
            }
        }

        /**
         * 创建雇员
         */
        $('button.createMember').click(function () {
            // 点击左侧树以�??????, 隐藏"该页面可维护公司�??????, 部门树以及雇员信息�?"
            $("#initial-info").css("display", "none");
            var node = AJS.org.getSelectTreeNode();

            var memberContent = viewMember(false);
            var memberHtml = work.Org.Template.memberCreateOrUpdate({data: {}});
            var $member = $(memberHtml).appendTo(memberContent);
            var nodeid = node.id;
            var orgId = '';
            var nodetype = node.type;
            if (nodetype == 'org') {
                orgId = nodeid;
            } else if (nodetype == 'dept') {
                var orgParentNode = getOrgParentNode(node);
                orgId = orgParentNode.id;
            }

            createOrUpdateEmployee($member,
                {
                    groupId: (!!nodeid && nodeid.startsWith('d_')) ? node.id.substr(2) : '',
                    orgId: orgId.substr(2)
                }
            );
        });

        /**
         * 视图切换
         * @param flag 是否切换到雇员页�???????
         * @param clear 是否清空雇员列表
         */
        function viewMember(flag, clear) {
            var view = $('section .right-section');
            if (flag) {
                view.find('.right-content').show();
                view.find('.operation-content').children().remove();
                var memberList = view.find('.member-list');
                if (clear) {
                    memberList.children().remove();
                }
                return memberList;
            } else {
                view.find('.right-content').hide();
                return view.find('.operation-content');
            }
        }

        /**
         * 编辑按钮
         */
        $('button.edit').bind('click', function () {
            // 点击左侧树以�??????, 隐藏"该页面可维护公司�??????, 部门树以及雇员信息�?"
            $("#initial-info").css("display", "none");
            var id = AJS.org.getSelectTreeId(true);
            var node = AJS.org.getSelectTreeNode();
            if (!id) return;
            if (!!~id.indexOf("o_")) { // 组织
                $.ajax({
                    type: "get",
                    url: contextPath + "/rest/wk-teamwork/latest/orgstr/" + id.substr(2),
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    success: function (data) {
                        var html = work.Org.Template.orgCreateAndUpdate({data: data});

                        var orgContent = viewMember(false);
                        $(html).appendTo(orgContent);
                        /** 机构类别与分支机构特点二级联�?? start*/
                        if (data.type == 1 || data.type == 2) {
                            $('#characterDiv').show();
                        }
                        $('#type').on('change', function () {
                            if ($(this).val() == 1 || $(this).val() == 2) {
                                $('#characterDiv').show();
                            } else {
                                $('#characterDiv').hide();
                            }
                        });
                        /** 机构类别与分支机构特点二级联�?? end*/
                        loadOrgStore(orgContent, "o_" + data.parent, "org");
                        var form = orgContent.find('form');
                        AJS.org.formValid.org(form); // 加载验证功能
                        orgContent.find('#save-button').click(function () {
                            if (!form.isValid()) {
                                return
                            }
                            var data = form.serializeJson();
                            data.parent = data.parent.substr(2);
                            data.id = id.substr(2);
                            var str = JSON.stringify(data);

                            $.ajax({
                                type: "PUT",
                                url: contextPath + "/rest/wk-teamwork/latest/orgstr",
                                contentType: "application/json; charset=utf-8",
                                data: str,
                                dataType: "json",
                                complete: function (mes) {
                                    if (mes.readyState != 4) {
                                        AJS.org.showError();
                                        return;
                                    }
                                    loadTree(function () {
                                        $('#' + node.tId + '_span').click();
                                    });
                                    AJS.org.showInfo();
                                }
                            });
                        });
                        orgContent.find('.cancel').click(function () {
                            viewMember(true);
                        })
                    }
                });

            } else if (!!~id.indexOf("d_")) { // 部门
                $.ajax({
                    type: "get",
                    url: contextPath + "/rest/wk-teamwork/latest/department/" + id.substr(2),
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    success: function (data) {
                        var html = work.Org.Template.deptCreateAndUpdate({data: data});

                        var deptContent = viewMember(false);
                        $(html).appendTo(deptContent);

                        AJS.org.formValid.dept(deptContent.find('form'), id.substr(2));

                        loadOrgStore(deptContent, data.parent, data.type == 0 ? 'dept' : 'team');

                        deptContent.find('#save-button').click(function () {
                            var form = deptContent.find('form');

                            form.isValid(function (v) { // 有ajax调用，后续逻辑写在回调函数�??
                                if (!v) {
                                    return;
                                }

                                var data = form.serializeJson();
                                data.parent = data.parent;
                                data.id = id.substr(2);
                                var str = JSON.stringify(data);

                                $.ajax({
                                    type: "PUT",
                                    url: contextPath + "/rest/wk-teamwork/latest/department",
                                    contentType: "application/json; charset=utf-8",
                                    data: str,
                                    dataType: "json",
                                    complete: function (mes) {
                                        if (mes.readyState != 4) {
                                            AJS.org.showError();
                                            return;
                                        }
                                        loadTree(function () {
                                            $('#' + node.tId + '_span').click();
                                        });
                                        AJS.org.showInfo();
                                        // AJS.org.afterSave();
                                    }
                                });
                            });
                        });
                        deptContent.find('.cancel').click(function () {
                            viewMember(true);
                        })
                    }
                });
            }
        });

        /**
         * 删除按钮
         */
        $('button.delete').bind('click', function () {
            var node = AJS.org.getSelectTreeNode(true);
            if (!node.id) return;
            if (!confirm(AJS.org.message.deleteConfirm)) return;
            var url;
            var parentOrgId;
            if (node.type == 'org') {
                url = contextPath + "/rest/wk-teamwork/latest/orgstr/" + node.id.substr(2);
                // 删除组织之前, 先获取其父组织的 id (0 或其他数�??????)
                $.ajax({
                    type: "get",
                    url: url,
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    success: function (data) {
                        parentOrgId = data.parent;
                    }
                });
            } else {
                url = contextPath + "/rest/wk-teamwork/latest/department/" + node.id.substr(2);
            }
            // 删除
            $.ajax({
                type: "delete",
                url: url,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function () {
                    function clickParentNodeCallback() {
                        var parentNode = node.getParentNode();
                        if (!parentNode) {
                            AJS.org.afterSave();
                        } else {
                            var tId = parentNode.tId;
                            $('#' + tId + "_span").click();
                        }
                    }

                    loadTree(clickParentNodeCallback);
                    AJS.org.showInfo({title: AJS.org.message.operationSucessed});
                },
                error: function (res) {
                    AJS.org.showError();
                }
            });
        });

        // 下拉列表数据请求
        function loadOrgStore(odj, id, type) {
            var url = contextPath + "/rest/wk-teamwork/latest/orgstr";
            if (type == "dept") {
                url += "/getOrgAndDept";
            } else if (type == "team") {
                url += "/getOrgAndTeam";
            }
            $.ajax({
                type: "get",
                url: url,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    var result = [];
                    $(data).each(function (idx, item) {
                        var id = item.id;
                        if (!~id.indexOf("_")) { // id没前缀，默认为组织
                            id = "o_" + id;
                        }
                        result.push({id: id, text: item.name});
                    });
                    $('#parent').auiSelect2({
                        data: result,
                        allowClear: true
                    });
                    if (!!id) {
                        odj.find('#parent').val(id).trigger("change");
                    }
                }
            })
        }

        $('#updateDate').append('<input type="file" name="file" id="inputUpload" >');
        $('#upload').click(function () {
            $('#inputUpload').trigger('click')
        })
        $('#inputUpload').change(function () {
            $('#updateDate').submit();
        })


        function controlePower(type) {
            $.ajax({
                type: "get",
                cache: false,
                async: false,
                url: contextPath + "/rest/wk-teamwork/latest/orgstr/controlPower",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    //alert(type);
                    if (1 == parseInt(type)) {
                        for (var k in data) {
                            if (k != 'creteam') {
                                if (data[k]) {
                                    $('#' + k).css("display", "inline");
                                }
                            } else {
                                $('#' + k).css("display", "none");
                            }
                        }
                    }
                    if (2 == parseInt(type)) {
                        for (var k in data) {
                            if (k == 'creteam' || k == 'edit' || k == 'del') {
                                if (data[k]) {
                                    $('#' + k).css("display", "inline");
                                }
                            } else {
                                $('#' + k).css("display", "none");
                            }
                        }
                    }
                }
            })
        }
    })
}(AJS.$, AJS.contextPath()));
var roleData;// 角色数据，如果不存在则请求后台获取并存储为本地变量，供之后直接获�?
var clickTr;
var projectRole = {};// 项目角色