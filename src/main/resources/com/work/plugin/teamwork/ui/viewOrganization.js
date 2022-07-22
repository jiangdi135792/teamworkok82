function getData(){
    var el = $("#organization-table");
    var  organizationId= el.attr('data-organization-id');

    $.ajax({
        type: "get",
        url: "/jira/rest/wk-teamwork/1/orgstr/"+organizationId,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (data) {
            var formView = $('form.form1');
           var dic = {
                  type : {0:'公司',1:'子公司',2:'分支机构',9:'其他'},
                 status : {0:'有效',1:'无效'},
                  character : {'0':'一般子公司','1':'独立法人'}
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