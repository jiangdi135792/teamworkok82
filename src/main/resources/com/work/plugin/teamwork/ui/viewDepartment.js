
function getData(){
    var el = $("#department-table");
    var departmentId = el.attr('data-department-id');

    $.ajax({
            type: "get",
            url: "/jira/rest/wk-teamwork/1/department/"+departmentId,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (data) {
                var formView = $('form.form1');
                 var dic = {
                   type : {0:'#getTextEx(\"workorg.property.Department.",\"department")',1:'workorg.property.Department.team'},
                    status : {0:'workorg.property.Department.valid',1:'workorg.property.Department.unvalid'},
                                                                   }
                      for(var item in data){
                        var val;
                   if (!!dic[item]) {
                       val = dic[item][data[item]];

                         } else {
                    val = data[item];
                             }

                    formView.find('.' + item).text(val);
                             }
                        },
            error: function (data) {
                alert("faile");
            }
        });
}
window.onload=getData;