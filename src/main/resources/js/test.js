(function ($, contextPath) {
    $(function () {
      $('#saveb').click(function (e) {
          $.ajax({
              type:"get",
              url: contextPath + "/rest/wk-teamwork/latest/role/createIssue",
              contentType: "application/json; charset=utf-8",
              success:function (data) {
              }
          })
      })
    })
})(AJS.$,AJS.contextPath());