!(function ($, contextPath) {
    $(function () {
        showRelationInfo("allOrgParentRole", "1", "orgRoleTable","menuPermit");
        showRelationInfo("allProRole","2","depRoleTable","depmenuPermit");
        function showRelationInfo(url, type, roleTbodyId,editId) {
            showMenu(type);
            var org = type;
            $.ajax({
                type: "post",
                cache: false,
                url: contextPath + "/rest/wk-teamwork/latest/power/" + url,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    var  map={}
                    map["System Administrator"]=AJS.I18n.getText('workorg.property.permissionMgr.role.sa');
                    map["It Operation"]=AJS.I18n.getText('workorg.property.permissionMgr.role.op');
                    map["Top Executives"]=AJS.I18n.getText('workorg.property.permissionMgr.role.sm');
                    map["Department Manager"]=AJS.I18n.getText('workorg.property.permissionMgr.role.dm');
                    map["Project Managers"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pm');
                    map["General Staff"]=AJS.I18n.getText('workorg.property.permissionMgr.role.gs');
                    map["Product Manager"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pdm');
                    map["Project Manager"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pl');
                    map["Test supervisor"]=AJS.I18n.getText('workorg.property.permissionMgr.role.ts');
                    map["Developer"]=AJS.I18n.getText('workorg.property.permissionMgr.role.dev');
                    map["Product Tester"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pt');

                    map["System Administrator Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.sades');
                    map["It Operation Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.opdes');
                    map["Department Manager Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.dmdes');
                    map["Top Executives Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.smdes');
                    map["Project Managers Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pmdes');
                    map["General Staff Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.gsdes');
                    map["Product Manager Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pdmdes');
                    map["Quality Manager Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.tsdes');
                    map["Developer Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.devdes');
                    map["Test supervisor Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.ptdes');
                    map["Project Manager Des"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pldes');
                    var length = data.length;
                    var str = "";
                    for (var m = 0; m < length; m++) {
                        str += "<tr><td id='" + data[m].id + "' style='cursor: pointer;padding: 2px;text-align:center'>";
                        str += (map[data[m].name] != null?map[data[m].name]:data[m].name) + "</td><td style='padding: 2px;text-align:center'>";
                        str += data[m].order + "</td><td style='padding: 2px;text-align:center'>";
                        str += (map[data[m].desc] != null?map[data[m].desc]:data[m].desc) + "</td><td style='padding: 2px;text-align: center'>";
                        str += "<button name='delRole"+type+"'" ;
                        var defaultRole=new Array(11);
                        defaultRole[1]="System Administrator";
                        defaultRole[2]="It Operation"
                        defaultRole[3]="Top Executives"
                        defaultRole[4]="Department Manager";
                        defaultRole[5]="Project Managers";
                        defaultRole[6]="General Staff";
                        defaultRole[7]="Product Manager";
                        defaultRole[8]="Project Manager";
                        defaultRole[9]="Test supervisor";
                        defaultRole[10]="Developer";
                        defaultRole[11]="Product Tester";
                        if (isInArray(defaultRole, data[m].name)) {
                            str += "disabled ";
                        } else {
                        }
                        str += " class='aui-icon aui-icon-small aui-iconfont-delete' style='display: none' >del</button>&nbsp;<button name='addRole"+type+"'  class='aui-icon aui-icon-small aui-iconfont-add' style='display: none'>add</button> <button name='saveRole"+type+"' class='aui-icon aui-icon-small aui-iconfont-success' style='display: none'>save</button></td>";
                        str += "</tr>";
                    }
                    $("#"+roleTbodyId).append(str);
                    controlePower();
                    for (var n = 0; n < length; n++) {
                        if(org == 1){
                        $('#' + data[n].id).bind('click', {roleName: data[n].name}, loadMenuPermite)
                        //$('#' + data[n].id).bind('dblclick', {roleName: data[n].name,desc:data[n].desc,weigh:data[n].order}, editRole)
                        }else {
                            $('#' + data[n].id).bind('click', {id: data[n].id}, proLoadMenuPermit)
                        }
                    }
                    bindDelRole(org);
                    bindAddRole();
                    bindCreateRole(org);
                    function editRole(event) {
                        var roleName = event.data.roleName;
                        var desc = event.data.desc;
                        var order = event.data.weigh;
                        alert(roleName);
                        alert(order);
                        alert(desc);
                    }
                    function proLoadMenuPermit(event) {
                        $('#proPermisson').show();
                        $('#proPermisson').empty();
                        var id = event.data.id;
                        $(this).parent().siblings().css("background","#ffffff");
                        $(this).parent().css("background","	#C0C0C0")
                       // var allpermissions = AJS.I18n.getText('workorg.property.permissionMgr.allpermissions');
                        var allpermissionsproject = AJS.I18n.getText('workorg.property.permissionMgr.allpermissionsproject');
                        var subpermissions = AJS.I18n.getText('workorg.property.permissionMgr.subpermissions');
                        var suppermissions = AJS.I18n.getText('workorg.property.permissionMgr.suppermissions');
                        $.ajax({
                            type: "get",
                            cache: false,
                            url: contextPath + "/rest/wk-teamwork/latest/power/getProjectRolePower/" + id,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data) {
                                var str = "";
       /*                         str += "<tr><td>";
                                str += allpermissions;
                                str += "</td>";
                                str += "<td><aui-label for='allpermissions'></aui-label><aui-toggle id='allpermissions' label='allpermissions' name='proPermi' "
                                for (var k in data) {
                                    if (data[k].name == "allpermissions") {
                                        console.info("============================")
                                        if (eval(data[k].exit)) {
                                            str += "checked";
                                        }
                                    }
                                }
                                str +=" ></aui-toggle></td></tr>"*/
                                str += "<tr><td>";
                                str += allpermissionsproject;
                                str += "</td>";
                                str += "<td><aui-label for='allpermissionsproject'></aui-label><aui-toggle id='allpermissionsproject' name='proPermi' label='allpermissionsproject'" ;
                                for (var k in data) {
                                    if (data[k].name == "allpermissionsproject") {
                                        if (eval(data[k].exit)) {
                                            str += "checked";
                                        }
                                    }
                                }
                                str += "></aui-toggle></td></tr>"
                                str += "<tr><td>";
                                str += subpermissions;
                                str += "</td>";
                                str += "<td><aui-label for='subpermissions'></aui-label><aui-toggle id='subpermissions' name='proPermi' label='subpermissions'" ;
                                for (var k in data) {
                                    if (data[k].name == "subpermissions") {
                                        if (eval(data[k].exit)) {
                                            str += "checked";
                                        }
                                    }
                                }
                                str +="></aui-toggle></td></tr>"
                                str += "<tr><td>";
                                str += suppermissions;
                                str += "</td>";
                                str += "<td><aui-label for='suppermissions'></aui-label><aui-toggle id='suppermissions'  name='proPermi' label='suppermissions'" ;
                                for (var k in data) {
                                    if (data[k].name == "suppermissions") {
                                        if (eval(data[k].exit)) {
                                            str += "checked";
                                        }
                                    }
                                }
                                str +="></aui-toggle></td></tr>"
                                $('#proPermisson').empty();
                                $('#proPermisson').append(str)
                                $("aui-toggle[name='proPermi']").bind('click', {
                                    id: id,
                                }, changeProPermission)
                            }
                        });
                    }
                    function changeProPermission(event) {
                        var id = event.data.id;
                        var attr = $(this).attr("id");
                        var chek = $(this).prop("checked");
                        $.ajax({
                            type: "get",
                            cache: false,
                            url: contextPath + "/rest/wk-teamwork/latest/power/changeProPermission/" + id+"/"+attr+"/"+chek,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success:function (data) {

                            }
                        })
                    }
                    function loadMenuPermite(event) {
                        $(this).parent().siblings().css("background","#ffffff");
                        $(this).parent().css("background","	#C0C0C0")
                        var roleName = event.data.roleName;
                        $.ajax({
                            type: "get",
                            cache: false,
                            url: contextPath + "/rest/wk-teamwork/latest/power/getMenuPermitByRoleName/" + roleName,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data) {
                                showMenuPower(roleName, data[1], data[2], data[3]);
                            }
                        });
                    }

                    function showMenuPower(nowRole, data1, data2, data3) {
                        var menu = "";
                        var datum = data2;
                        var datum3 = data3;
                        var length = datum.length;
                        var gsryld = AJS.I18n.getText('workorg.property.menu.gsryld');
                        var bmryfb = AJS.I18n.getText('workorg.property.menu.bmryfb');
                        var zzjggl = AJS.I18n.getText('workorg.property.menu.zzjggl');
                        var permissionMgr = AJS.I18n.getText('workorg.property.menu.permissionMgr');
                        var tdryfb = AJS.I18n.getText('workorg.property.menu.tdryfb');
                        var tdrygz = AJS.I18n.getText('workorg.property.menu.tdrygz');
                        var bmrygz = AJS.I18n.getText('workorg.property.menu.bmrygz');
                        var adsjtb = AJS.I18n.getText('workorg.property.menu.adsjtb');
                        var qxkznew = AJS.I18n.getText('workorg.property.menu.qxkznew');
                        var setReport = AJS.I18n.getText('workorg.property.menu.setReport');
                        var showReport = AJS.I18n.getText('workorg.property.menu.showReport');
                        var lowerissues = AJS.I18n.getText('workorg.property.menu.lowerissues');
                        for (var a = 0; a < length; a++) {
                            menu += "<tr class='menuP'><td style='text-align: center;vertical-align: middle;cursor: pointer' class='showDetailP'>";
                            menu += "<input type='hidden' value='" + datum[a] + "' >";
                            menu += "<aui-label for=" + datum[a] + ">";
                            if (datum[a] == "gsryld") {
                                menu += gsryld;
                            } else if (datum[a] == "bmryfb") {
                                menu += bmryfb;
                            } else if (datum[a] == "zzjggl") {
                                menu += zzjggl;
                            } else if (datum[a] == "permissionMgr") {
                                menu += permissionMgr;
                            } else if (datum[a] == "tdryfb") {
                                menu += tdryfb;
                            }
                            else if (datum[a] == "tdrygz") {
                                menu += tdrygz;
                            } else if (datum[a] == "bmrygz") {
                                menu += bmrygz;
                            } else if (datum[a] == "adsjtb") {
                                menu += adsjtb;
                            }else if (datum[a] == "qxkznew") {
                                menu += qxkznew;
                            }else if (datum[a] == "setReport") {
                                menu += setReport;
                            }else if (datum[a] == "showReport") {
                                menu += showReport;
                            }else if (datum[a] == "lowerissues") {
                                menu += lowerissues;
                            }
                            menu += "</aui-label></td>";
                            menu += "<td style='text-align: center;vertical-align: middle'><aui-toggle name='" + nowRole + datum[a] + "' id=" + datum[a] + "_menu" + " label=" + datum[a] + "  ";
                            if (data1) {
                                menu += " checked";
                            } else {
                                menu += "checked  disabled";
                            }
                            menu += "></aui-toggle>";
                            menu += "</tr>"
                        }
                        var length2 = datum3.length;
                        for (var v = 0; v < length2; v++) {
                            menu += "<tr class='menuP'><td style='text-align: center;vertical-align: middle;cursor: pointer'>";
                            menu += "<aui-label for=" + datum3[v] + ">";
                            if (datum3[v] == "gsryld") {
                                menu += gsryld;
                            } else if (datum3[v] == "bmryfb") {
                                menu += bmryfb;
                            } else if (datum3[v] == "zzjggl") {
                                menu += zzjggl;
                            } else if (datum3[v] == "permissionMgr") {
                                menu += permissionMgr;
                            } else if (datum3[v] == "tdryfb") {
                                menu += tdryfb;
                            } else if (datum3[v] == "tdrygz") {
                                menu += tdrygz;
                            } else if (datum3[v] == "bmrygz") {
                                menu += bmrygz;
                            } else if (datum3[v] == "adsjtb") {
                                menu += adsjtb;
                            }else if (datum3[v] == "qxkznew") {
                                menu += qxkznew;
                            }else if (datum3[v] == "setReport") {
                                menu += setReport;
                            }else if (datum3[v] == "showReport") {
                                menu += showReport;
                            }else if (datum3[v] == "lowerissues") {
                                menu += lowerissues;
                            }
                            menu += "</aui-label></td>";
                            menu += "<td style='text-align: center;vertical-align: middle'><aui-toggle name='" + nowRole + datum3[v] + "' id=" + datum3[v] + "_menu" + " label=" + datum3[v] + "  ";
                            if (data1) {
                                menu += " ";
                            } else {
                                menu += "checked  disabled";
                            }
                            menu += "></aui-toggle></td>";
                            menu += "</tr>"
                        }
                        $('#' + editId).empty();
                        $('#' + editId).append(menu)
                        for (var a = 0; a < length; a++) {
                            var newVar = datum[a];
                            $("aui-toggle[name='" + nowRole + newVar + "']").bind("click", {
                                test: newVar,
                                rolesss: nowRole
                            }, changeMP)
                        }
                        for (var i = 0; i < length2; i++) {
                            var newVara = datum3[i];
                            $("aui-toggle[name='" + nowRole + newVara + "']").bind('click', {
                                test: newVara,
                                rolesss: nowRole
                            }, changeMP)
                        }
                        bindShowDetailP(nowRole);
                    }

                    function showDetailP(event) {
                        var savechange =AJS.I18n.getText("workorg.property.permissionMgr.save")
                        var closeDetail =AJS.I18n.getText("workorg.property.permissionMgr.close")
                        var ownpermissions =AJS.I18n.getText("workorg.property.permissionMgr.ownpermissions")
                        var allpermissions =AJS.I18n.getText("workorg.property.permissionMgr.allpermissions")
                        var subpermissions =AJS.I18n.getText("workorg.property.permissionMgr.subpermissions")
                        var map = {};
                        map["edit"] = AJS.I18n.getText("workorg.property.permissionMgr.edit");
                        map["del"] = AJS.I18n.getText("workorg.property.permissionMgr.delall");
                        map["creorg"] = AJS.I18n.getText("workorg.property.permissionMgr.creorg");
                        map["credep"] = AJS.I18n.getText("workorg.property.permissionMgr.credep");
                        map["creemp"] = AJS.I18n.getText("workorg.property.permissionMgr.creemp");
                        map["upload"] = AJS.I18n.getText("workorg.property.permissionMgr.upload");
                        map["creteam"]  = AJS.I18n.getText("workorg.property.permissionMgr.creteam");
                        map["detail"]  = AJS.I18n.getText("workorg.property.permissionMgr.detail");
                        map["detail1"]  = AJS.I18n.getText("workorg.property.permissionMgr.detail1");
                        map["addReport"]  = AJS.I18n.getText("workorg.property.permissionMgr.addReport");
                        map["saveReport"]  = AJS.I18n.getText("workorg.property.permissionMgr.saveReport");
                        map["updateReport"]  = AJS.I18n.getText("workorg.property.permissionMgr.updateReport");
                        map["delReport"]  = AJS.I18n.getText("workorg.property.permissionMgr.delReport");
                        map["query"]  = AJS.I18n.getText("workorg.property.permissionMgr.query");
                        map["edited"]  = AJS.I18n.getText("workorg.property.permissionMgr.edited");
                        map["pause"] = AJS.I18n.getText("workorg.property.permissionMgr.pause");
                        map["sync"] = AJS.I18n.getText("workorg.property.permissionMgr.sync");
                        map["viewed"]  = AJS.I18n.getText("workorg.property.permissionMgr.viewed");
                        map["Tdetail"]  = AJS.I18n.getText("workorg.property.permissionMgr.Tdetail");
                        map["TdetailZ"]  = AJS.I18n.getText("workorg.property.permissionMgr.TdetailZ");
                        map["addRole"] = AJS.I18n.getText("workorg.property.permissionMgr.addRole");
                        map["delRole"]  = AJS.I18n.getText("workorg.property.permissionMgr.delRole");
                        map["changePower"]  = AJS.I18n.getText("workorg.property.permissionMgr.changePower");
                        map["showLowerIssues"]  = AJS.I18n.getText("workorg.property.permissionMgr.showLowerIssues");
                        map["shareDemo"]  = AJS.I18n.getText("workorg.property.permissionMgr.shareDemo");
                        map["cretegs"]  = AJS.I18n.getText("workorg.property.permissionMgr.creategs");
                        map["creategs"]  = AJS.I18n.getText("workorg.property.permissionMgr.creategs");
                        var role = event.data.role;
                        var menuName = $(this).find('input ').val();
                        if (!!$('.addDetailPower').length) {
                            // alert("有详细的权限")
                        } else {
                            // alert("没有详细的权限")
                            var domain1 = '';
                            var domain2 = '';
                            var domain3 = '';
                            var domain4 = '';

                            $.ajax({
                                type: "get",
                                async: false,
                                cache: false,
                                url: contextPath + "/rest/wk-teamwork/latest/power/getDetailP/" + role + "/" + menuName,
                                contentType: "application/json; charset=utf-8",
                                dataType: "json",
                                success: function (data) {
                                    domain1 = data.domain0;
                                    domain2 = data.domain1;
                                    domain3 = data.domain2;
                                    domain4 = data.hiddens;
                                }
                            })

                            var str = "";
                            str += "<tr class='addDetailPower'><td colspan='2'><div style='display:inline-table;width: 100%;height: 100%' >";
                            str += "<table class='aui'>";
                            str += "<tbody id='" + menuName + "5" + menuName + "'><tr>"+
                                "";
                            for (var k in domain1) {

                                str += "<td>" +map[k]+"<input type='hidden' value='"+domain4[k]+"'>"+ "</td>"
                            }
                            str += "</tr>";
                            // str += "<tr style='background-color: #D5D5D5'><td>"+ownpermissions+"</td>";
                            // for (var k in domain1) {
                            //     if (domain1[k]) {
                            //         str += "<td><input type='checkbox' checked></td>"
                            //     } else {
                            //         str += "<td><input type='checkbox' disabled='disabled'></td>";
                            //     }
                            // }
                            str += "</tr>";
                            // str += "<tr style='background-color: #D5D5D5' ><td>"+subpermissions+"</td>";
                            // for (var k in domain2) {
                            //     if (domain2[k]) {
                            //         str += "<td><input type='checkbox' checked></td>"
                            //     } else {
                            //         str += "<td><input type='checkbox' disabled='disabled'></td>";
                            //     }
                            // }
                            // str += "</tr>";
                            str += "<tr>";
                            for (var k in domain3) {
                                if (domain3[k]) {
                                    str += "<td><input type='checkbox' checked></td>"
                                } else {
                                    str += "<td><input type='checkbox'></td>";
                                }
                            }
                            str += "</tr>";
                            str += "<tr><td colspan='2'class='changeAndRemove' style='cursor: pointer;visibility: hidden'><button class='aui-button '>"+savechange+"</button> </td><td colspan='2'style='cursor: pointer' class='closeMPDetail'><button class='aui-button'>"+closeDetail+"</button></td></tr>"
                            str += "</tbody></table>";
                            str += "</div></td></tr>";
                            $(this).parent().after(str)
                            $('.changeAndRemove').on('click', {role: role, menuName: menuName}, changetAndRemove);
                            $('.closeMPDetail').on('click', {role: role, menuName: menuName}, closeMPDetail);
                            controlePower();
                        }
                    }
                    function closeMPDetail(event) {
                        $(this).parent().parent().parent().parent().parent().parent().remove();
                    }
                    function changetAndRemove(event) {
                        var role = event.data.role;
                        var menuName = event.data.menuName;
                        var str = "";
                        $("#" + menuName + "5" + menuName).each(function () {
                            $(this).find('tr').each(function () {
                                $(this).find('td').each(function () {
                                    var length2 = $(this).find('input[type=checkbox]').length;
                                    if (length2) {
                                        $(this).find('input').each(
                                            function () {
                                                if ($(this).is(':checked')) {
                                                    str += 1;
                                                    str += ";"
                                                } else {
                                                    str += 0;
                                                    str += ";"
                                                }
                                            }
                                        )
                                    } else {
                                        var text = $(this).find('input[type=hidden]').val();
                                        str += text;
                                        str += ";"
                                    }
                                });
                                str +="|";
                            });
                        });
                        //alert(role + "111111" + menuName + "__" + str);
                        $.ajax({
                            type: "post",
                            cache: false,
                            url: contextPath + "/rest/wk-teamwork/latest/power/saveChangeP",
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            data:role+','+menuName+','+str,
                            success: function (data) {
                            }
                        })
                        $(this).parent().parent().parent().parent().parent().parent().remove();
                    }

                    function changeMP(event) {
                        var rolesss = event.data.rolesss;
                        var test2 = event.data.test;
                        var attrs = $("aui-toggle[name='" + rolesss + test2 + "']").prop("checked");
                        var prop = $("aui-toggle[name='" + rolesss + test2 + "']").prop("disabled");
                        if (!prop) {
                            if (!attrs) {
                                addPower(rolesss, test2)
                                $(this).parent().parent().children("td").eq(0).attr("class", "showDetailP");
                                bindShowDetailP(rolesss);
                            } else {
                                delPower(rolesss, test2);
                                $(this).parent().parent().children("td").eq(0).unbind("click");
                            }
                        }
                    }

                    function delPower(orgRole, menuName) {
                        $.ajax({
                            type: "post",
                            cache: false,
                            url: contextPath + "/rest/wk-teamwork/latest/power/delMenuPower/" + orgRole + "/" + menuName,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data) {
                                //alert("del  success")
                            }
                        })
                    }

                    function addPower(orgRole, menuName) {
                        $.ajax({
                            type: "post",
                            cache: false,
                            url: contextPath + "/rest/wk-teamwork/latest/power/addMenuPower/" + orgRole + "/" + menuName,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data) {
                                //alert("add success")
                            }
                        })
                    }

                    function bindShowDetailP(nowRole) {
                        $('.showDetailP').on('click', {role: nowRole}, showDetailP)
                    }

                    function bindDelRole(org) {
                        $('button[name="delRole'+type+'"]').live('click', {type: org}, delRole)
                    }

                    function bindAddRole() {
                        $('button[name="addRole'+type+'"]').live('click', addNewRole)
                    }

                    function bindCreateRole(org) {
                        $('button[name="saveRole'+type+'"]').live('click', {type: org}, saveCreateRole)
                    }

                    function delRole(event) {
                        var delRole = $(this).parent().parent().children("td").get(0).innerHTML;
                        $.ajax({
                            type: "post",
                            cache: false,
                            url: contextPath + "/rest/wk-teamwork/latest/power/delRole/" + delRole + "/" + event.data.type,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                        })
                        $(this).parent().parent().remove();
                        $('#' + editId).empty();
                        $('button[name="addRole'+type+'"]').css('display', 'inline-block')
                    }

                    function saveCreateRole(event) {
                        var newWeight = $(this).parent().parent().children("td").get(1).innerHTML;
                        var rolename = $(this).parents("tr").find(".rolename").val();
                        var roleDes = $(this).parents("tr").find(".roleDes").val();
                        var type = event.data.type;
                        var roleId = "";
                        var ndesc = "";
                        var nname = "";
                        var norder = "";
                        $.ajax({
                            type: "post",
                            cache: false,
                            async: false,
                            url: contextPath + "/rest/wk-teamwork/latest/power/saveNewRole/" + rolename + "/" + roleDes + "/" + newWeight + "/" + type,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data) {
                                roleId = data.id;
                                ndesc = data.desc;
                                nname = data.name;
                                norder = data.order;
                            }
                        })
                        $(this).css('display', 'none')
                        $('button[name="addRole'+type+'"]').css('display', 'inline-block')
                        $(this).parents("tr").find(".roleDes").remove();
                        $(this).parents("tr").find(".rolename").remove();
                        $(this).parent().parent().children("td").eq(0).html(nname);
                        $(this).parent().parent().children("td").eq(1).html(norder);
                        if(org == 1){
                        $(this).parent().parent().children("td").eq(0).css("cursor", "pointer");
                        $(this).parent().parent().children("td").eq(0).css("id", roleId)
                        $(this).parent().parent().children("td").eq(0).bind("click", {roleName: nname}, loadMenuPermite)
                      //  $(this).parent().parent().children("td").eq(0).bind("dblclick", {roleName: nname}, loadMenuPermite)
                        }
                        $(this).parent().parent().children("td").eq(2).html(ndesc);
                        bindDelRole(type);
                        controlePower();
                    }

                    function addNewRole() {
                        var attribute_id = $(this).parent().parent().children("td").get(1).innerHTML;
                        $(this).css('display', 'none')
                        var str = "";
                        str += "<tr>";
                        str += "<td style='padding: 2px'><input class='rolename' type='text' maxlength='19' placeholder='"+AJS.I18n.getText("workorg.property.permissionMgr.role.addRoleName")+"' style='border: none;padding:7px ;'></td>"
                        str += "<td style='padding: 2px'>" + (Number(attribute_id) + Number(1)) + "</td>"
                        str += "<td style='padding: 2px'><input style='border: none;padding: 7px' maxlength='19' class='roleDes' type='text' placeholder='"+AJS.I18n.getText("workorg.property.permissionMgr.role.addRoleDesc")+"' ></td>";
                        str += "<td style='padding: 2px;text-align: center'><button name='delRole"+type+"' style='display: none' class='aui-icon aui-icon-small aui-iconfont-delete'>del</button>&nbsp;" +
                            "<button name='addRole"+type+"' class='aui-icon aui-icon-small aui-iconfont-add'style='display:none'>add</button>" +
                            "<button name='saveRole"+type+"' class='aui-icon aui-icon-small aui-iconfont-success' style='display: inline-block'>save</button></td>";
                        str += "</tr>";
                        $(this).parent().parent().after(str)
                    }

                    function isInArray(arr,value){
                        var index = $.inArray(value,arr);
                        if(index >= 0){
                            return true;
                        }
                        return false;
                    }
                    function controlePower() {
                        $.ajax({
                            type: "get",
                            cache: false,
                            async:false,
                            url: contextPath + "/rest/wk-teamwork/latest/power/controlPower",
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data) {
                                for (var k in data) {
                                    if (k == 'addRole') {
                                        if (data[k]) {
                                            $('button[name="addRole' + type + '"]').css('display', 'inline-block')
                                        }
                                    } else if (k == "delRole") {
                                        if (data[k]) {
                                            $('button[name="delRole' + type + '"]').css('display', 'inline-block')
                                        }
                                    }else if (k == "changePower"){
                                        if (data[k]) {
                                            $('.changeAndRemove').css('visibility', 'visible')
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            })
            function showMenu(type) {
                if (type == 1){
                $('#menuShow').show();
                $('#projectPermission').hide();
                }else {
                $('#menuShow').hide();
                $('#projectPermission').show();
                }
            }
        }

    })
}(AJS.$, AJS.contextPath()));