function getData(){
    var el = $("#employee-table");
    var employeeId = el.attr('data-employee-id');

    $.ajax({
        type: "get",
        url: "/jira/rest/wk-teamwork/1/strEmployee/"+employeeId,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (data) {
            var formView = $('form.form1');
            for(var item in data){
//                alert(item + "   " + data[item]);
                formView.find('.' + item).text(data[item]);
            }
        },
        error: function (data) {
            alert("faile");
        }
    });
}
window.onload=getData;
