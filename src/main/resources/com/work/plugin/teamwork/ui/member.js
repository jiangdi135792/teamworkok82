!(function($, RestfulTable, contextPath) {


    $(function() {
        var $el = $("#org-member-table");
        var $orgId = $el.attr('data-org-id');
        var $memberTable = new RestfulTable({
            autoFocus: true,
            el: $el,
            resources: {
                all: contextPath + "/rest/wk-teamwork/latest/org/" + $orgId +"/member/1",
                self: contextPath + "/rest/wk-teamwork/latest/org/" + $orgId+"/member/1",
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
                    header: AJS.I18n.getText("workorg.property.UserKey"),
                    allowEdit: false,
                    createView: RestfulTable.CustomCreateView.extend({
                        render: function(self) {
                            return manageWorkOrg.Template.memberCreateView({fieldName: "userKey"});
                        }
                    })
                }
            ]
        });

        JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$memberTable.getCreateRow().$el], "createRow-loaded");
    });
}(AJS.$, AJS.RestfulTable, AJS.contextPath()));