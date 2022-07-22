(function($){
    //将数据json序列化
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

    //将json数据依次放到form中
    $.fn.setForm = function(jsonValue) {
        var obj=this;
        $.each(jsonValue, function (name, ival) {
            var $oinput = obj.find("input:[name=" + name + "]");
            if ($oinput.attr("type")== "radio" || $oinput.attr("type")== "checkbox"){
                 $oinput.each(function(){
                     if(Object.prototype.toString.apply(ival) == '[object Array]'){//是复选框，并且是数组
                          for(var i=0;i<ival.length;i++){
                              if($(this).val()==ival[i])
                                 $(this).attr("checked", "checked");
                          }
                     }else{
                         if($(this).val()==ival)
                            $(this).attr("checked", "checked");
                     }
                 });
            }else if($oinput.attr("type")== "textarea"){//多行文本框
                obj.find("[name="+name+"]").html(ival);
            }else{
                 obj.find("[name="+name+"]").val(ival);
            }
       });
    };

})(jQuery);
window.onload=function(){
                $.ajax({
                    type: "get",
                    url: "/jira/rest/api/2/user/search?username=%27%27",
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    success: function (data) {
                        var html="";
                        for(var i=0;i<data.length;i++){
                         html+= "<option value="+data[i].key+">"+data[i].name+"</option>";
                        }
                           $(".form1 #jiraUserKey").html(html);
                           $(".form2 #jiraUserKey").html(html);


                    }
})
}
function check(a){
    if(a.value==''){
//        a.placeholder='此项不可为空';
        return false;
    }
}


!(function($, contextPath) {

//    //根据在职和离职的下拉列表控制离职日期的div是否显示
//    function choose(sel){
//            if(sel.value == "1"){
//                document.getElementById('outjob').style.display='none';
//            } else if(sel.value == "2"){
//                document.getElementById('outjob').style.display='block';
//            }
//        };


    $(function() {

        //初始化时间控件
//        var now = new Date() ;
//        var nowYear = now.getFullYear() ; //年
//        var nowMonth = now.getMonth()+1<10?"0"+(now.getMonth()+1):now.getMonth() ; //月
//        var nowDay = now.getDate()<10?"0"+now.getDate():now.getDate() ; //日期
//        var nowDate = nowYear+"-"+nowMonth+"-"+nowDay ;
//        $("#entryTime").val(nowDate) ;//设置入职时间默认为今天
//        $("#leaveTime").val(nowDate) ;//设置离职时间默认为今天

        AJS.$(document).ready(function() {
            AJS.$('#entryTime').datePicker({
                'overrideBrowserDefault': true,
                'languageCode':zh-CN
            });
        });


        //创建雇员按钮点击事件
        $('#savebut').click(function(e){
            var data = $('form.form1').serializeJson();
            var str = JSON.stringify(data);
            $.ajax({
                type: "post",
                url: "/jira/rest/wk-teamwork/1/strEmployee",
                contentType: "application/json; charset=utf-8",
                data: str,
                dataType: "json",
                success: function (data) {
                    $('form.form1').hide();
                    $('form.form2').show();
                    $('form.form2').setForm(data);//将ajax获得的json数据设值到form表单中
                    //弹窗
                    var myFlag = AJS.flag({
                        type: 'info',
                        title: '创建成功'
                    });
                    //2秒后关闭弹窗
                    setTimeout(function () {
                        myFlag.close()
                    },2000);
                },
                error: function (data) {
                    alert("创建失败！");
                },

            });

        });

        //修改雇员按钮点击事件
        $('#updbut').click(function(){
            var data = $('form.form2').serializeJson();
            var str = JSON.stringify(data);
            $.ajax({
                type: "put",
                url: "/jira/rest/wk-teamwork/1/strEmployee",
                contentType: "application/json; charset=utf-8",
                data: str,
                dataType: "json",
                success: function (data) {
                    $('form.form2').setForm(data);//将ajax获得的json数据设值到form表单中
                    //弹窗
                    var myFlag = AJS.flag({
                        type: 'info',
                        title: '修改成功'
                    });
                    //2秒后关闭弹窗
                    setTimeout(function () {
                        myFlag.close()
                    },2000);
                },
                error: function (data) {
                    alert("修改失败！");
                },

            });
        });



    });
}(AJS.$, AJS.contextPath()));

function createCalendar() {
    if (Calendar._UNSUPPORTED === true) {
        alert("The JIRA Calendar does not currently support your language.");
        return
    }
    var dateEl = params.inputField || params.displayArea;
    var dateFmt = params.inputField ? params.ifFormat : params.daFormat;
    var mustCreate = false;
    var cal = window.calendar;
    if (cal) {
        cal.hide()
    }
    if (dateEl) {
        if (dateEl.value || dateEl.innerHTML) {
            params.date = Date.parseDate(dateEl.value || dateEl.innerHTML, dateFmt)
        }
    }
    if (!(cal && params.cache)) {
        window.calendar = cal = new Calendar(params.firstDay,params.date,params.todayDate,params.onSelect || onSelect,params.onClose || function(cal) {
            cal.hide()
        }
        );
        cal.showsTime = params.showsTime;
        cal.time24 = (params.timeFormat == "24");
        cal.weekNumbers = params.weekNumbers;
        Date.useISO8601WeekNumbers = params.useISO8601WeekNumbers;
        if (params.useISO8601WeekNumbers) {
            cal.firstDayOfWeek = 1
        }
        mustCreate = true
    } else {
        if (params.date) {
            cal.setDate(params.date)
        }
        cal.hide()
    }
    if (params.multiple) {
        cal.multiple = {};
        for (var i = params.multiple.length; --i >= 0; ) {
            var d = params.multiple[i];
            var ds = d.print("%Y%m%d");
            cal.multiple[ds] = d
        }
    }
    cal.showsOtherMonths = params.showOthers;
    cal.yearStep = params.step;
    cal.setRange(params.range[0], params.range[1]);
    cal.params = params;
    cal.setDateStatusHandler(params.dateStatusFunc);
    cal.getDateText = params.dateText;
    cal.setDateFormat(dateFmt);
    if (mustCreate) {
        cal.create()
    }
    cal.refresh();
    if (!params.position) {
        cal.showAtElement(params.button || params.displayArea || params.inputField, params.align)
    } else {
        cal.showAt(params.position[0], params.position[1])
    }
    return false
}
