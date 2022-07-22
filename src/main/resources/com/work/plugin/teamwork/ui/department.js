(function($){
    $.fn.serializeJson=function(){
        var serializeObj={};
        var array=this.serializeArray();
        var str=this.serialize();
        $(array).each(function(){
            if(serializeObj[this.name]){
                if($.isArray(serializeObj[this.name])){
                    serializeObj[this.name].push(this.value);
                }else{
                    serializeObj[this.name]=[serializeObj[this.name],this.value];
                }
            }else{
                serializeObj[this.name]=this.value;
            }
        });
        return serializeObj;
    };
})(jQuery);
!(function($, RestfulTable, contextPath) {
 $(function() {
        function loadTree() {
            $.ajax({
                type: "get",
                url: contextPath+"/rest/wk-teamwork/latest/department",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    var zNodes = [];
                    $(data).each(function (index,item) {
                        zNodes.push({id:item.id,name:item.groupName,pId:item.parent,t:item.groupName,open:true});
                    })
                    var setting = {
                        data: {
                            key: {
                                title:"t"
                            },
                            simpleData: {
                                enable: true
                            }
                        },
                        callback:{
                            onClick:function (a,b,c) {
                                var zTree = $.fn.zTree.getZTreeObj("treeDemo");
                                var nodes = zTree.getSelectedNodes();
                                var id = nodes[0].id;
                                $.ajax({
                                    type: "get",
                                    url: contextPath +"/rest/wk-teamwork/latest/department/" + id,
                                    contentType: "application/json; charset=utf-8",
                                    dataType: "json",
                                    success: function (data) {
                                        $('form.form1').hide();
                                        var formView = $('form.form2');
                                        var dic = {
                                            type : {0:'部门',1:'团队'},
                                            status : {0:'有效',1:'无效'},
                                        }
                                        for(var item in data){
                                            var val;
                                            if (!!dic[item]) {
                                                val = dic[item][data[item]];
                                                val = dic[item][data[item]];
                                            } else {
                                                val = data[item];
                                            }

                                            formView.find('.' + item).text(val);
                                        }
                                        formView.find('.parent').text($('#'+c.parentTId+'_span').text()||'根节点');

                                        formView.show();
                                    }
                                });
                            }
                        }
                    };
                    $.fn.zTree.init($("#treeDemo"), setting, zNodes);
                }
            });
        }
        loadTree();
        $('#create').bind('click',function () {
            $('form.form2').hide();
            var formEidt = $('form.form1');
            formEidt.resetForm();
            formEidt.find('#id').val('');
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = zTree.getSelectedNodes();
            if (nodes.length == 1) {
                var id = nodes[0].id;
                formEidt.find('#parent').val(id).trigger("change");
            }

            formEidt.show();
        });
        $('#edit').bind('click',function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = zTree.getSelectedNodes();
            if (nodes.length == 0) {
                alert("请先选择一个节点");
            }
            var id = nodes[0].id;
            $.ajax({
                type: "get",
                url: contextPath +"/rest/wk-teamwork/latest/department/" + id,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    $('form.form2').hide();
                    var formEidt = $('form.form1');
                    for(var item in data){
                        formEidt.find('#' + item).val(data[item]);
                    }
                    formEidt.find('#parent').val(data.parent).trigger("change");
                    formEidt.show();
                }
            });
        })
        $('#delete').bind('click',function () {
            var zTree = $.fn.zTree.getZTreeObj("treeDemo");
            var nodes = zTree.getSelectedNodes();
            if (nodes.length == 0) {
                alert("请先选择一个节点");
            }
            var id = nodes[0].id;
            $.ajax({
                type: "delete",
                url: contextPath +"/rest/wk-teamwork/latest/department/" + id,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    loadTree();
                    var myFlag = AJS.flag({
                        type: 'info',
                        title: '删除成功'
                    });
                    setTimeout(function () {
                        myFlag.close()
                    },2000);
                }
            });
        })
        $.ajax({
            type: "get",
            url: contextPath + "/rest/wk-teamwork/latest/department",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success:function (data) {
                var result = [];
                $(data).each(function (idx,item) {
                    result.push({id:item.id,text:item.groupName})
                })
                $('#parent').select2({
                    data:result
                });
            }
        })

        $('#org-save-button').click(function(){
                  var data = $('form.aui').serializeJson();
                  var str = JSON.stringify(data);
                  var id = $('#id').val();
                  var type = "POST";
                  if (!!id) {
                      type = "PUT";
                  }

                  $.ajax({
                      type: type,
                      url: contextPath + "/rest/wk-teamwork/latest/department",
                      contentType: "application/json; charset=utf-8",
                      data: str,
                      dataType: "json",
                      complete : function (mes) {
                          if (mes.readyState != 4) {
                              return;
                          }
                          loadTree();
                          var myFlag = AJS.flag({
                              type: 'info',
                              title: '操作成功'
                          });
                          setTimeout(function () {
                              myFlag.close()
                          },1000);
                      }
                  });
              })
          });
    }(AJS.$, AJS.RestfulTable, AJS.contextPath()));