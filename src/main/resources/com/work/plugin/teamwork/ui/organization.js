!(function($, RestfulTable, contextPath) {

    $(function() {
        var $el = $("#org-main-table");
        var $memberTable = new RestfulTable({
            autoFocus: true,
            el: $el,
            allowCreate:false,
            resources: {
                all: contextPath + "/rest/wk-teamwork/latest/",
                self: contextPath + "/rest/wk-teamwork/latest/"
            },
//allowCreate:false,

           views:{
               row: RestfulTable.Row.extend({
                  renderOperations: function () {
                       var instance = this;
                       var modelJSON = this.model.toJSON()
                       var operations = manageWorkOrg.Template.mainOperationsList({id: modelJSON.id, roleCount: modelJSON.roleCount, memberCount: modelJSON.memberCount});
                        var operationsList = $(operations);
                       operationsList.click(function(e) {
                            if ($(e.target).hasClass("org-main-delete")) {
                               instance.destroy();
                                e.preventDefault();
                           }
                       });

                        return operationsList
                    }
                })
            },

            columns: [
                {
                    id: "name",
                    header: AJS.I18n.getText("workorg.property.MenuName"),
                    allowEdit: false,
                    allowCreate:false
                }
            ]
        });


    });
    $('#content form tbody:first').hide()


}(AJS.$, AJS.RestfulTable, AJS.contextPath()));