!(function($, RestfulTable, contextPath) {

    $(function() {
        var $el = $("#org-role-table");

        var $role = $el.attr('data-org-role');
        var $memberTable = new RestfulTable({
            autoFocus: true,
            el: $el,
            resources: {
                all: contextPath + "/rest/wk-teamwork/latest/org/" + $role +"/member/2" ,
                self: contextPath +"/rest/wk-teamwork/latest/org/" + $role +"/member/2",
            },
            // allowCreate:false,
            views:{
                row: RestfulTable.Row.extend({
                    // renderOperations: function () {
                    //     var instance = this;
                    //     var operations = manageWorkOrg.Template.memberOperationsList();
                    //     var operationsList = $(operations);
                    //     operationsList.click(function(e) {
                    //         if ($(e.target).hasClass("org-member-delete")) {
                    //             instance.destroy();
                    //             e.preventDefault();
                    //         }
                    //     });
                    //     return operationsList
                    // }
                })
            },

            columns: [
                {
                    id: "userKey",
                    header: AJS.I18n.getText("workorg.property.Organization.role"),
                    allowEdit: false,
                    createView: RestfulTable.CustomCreateView.extend({
                        render: function(self) {
                         $.ajax({
                                                   type: "get",
                                                    url: contextPath+"/rest/api/2/groups/picker",
                                                    contentType: "application/json; charset=utf-8",
                                                   dataType: "json",
                                                   success: function (data) {
                                                        var html="";
                                                        data = data.groups;
                                                        for(var i=0;i<data.length;i++){
                                                         html+= "<option value="+data[i].name+">"+data[i].name+"</option>";
                                                       }
                                                          $memberTable.getCreateRow().$el.find('select').html(html);
                                                   }
                                })
                            return manageWorkOrg.Template.roleCreateView({fieldName: "userKey"});
                        }
                    })
                }
            ]
        });


//        JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$memberTable.getCreateRow().$el], "createRow-loaded");


    });
}(AJS.$, AJS.RestfulTable, AJS.contextPath()));