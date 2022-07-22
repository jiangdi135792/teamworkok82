!(function ($, contextPath) {
    $(function () {

        AJS.org = {
            message: {
                chooseReport: AJS.I18n.getText('workorg.property.report.chooseReport'),
                dateInfo: AJS.I18n.getText('workorg.property.report.dateInfo'),
                chartLoadFaile: AJS.I18n.getText('workorg.property.report.chartLoadFaile')
            },
            showInfo: function (options) {
                options = options || {};
                var myFlag = AJS.flag({
                    type: options.type || 'info',
                    title: options.title
                });
                setTimeout(function () {
                    myFlag.close()
                }, 1000);
            },
            showError: function (mes) {
                this.showInfo({type: 'error', title: mes || AJS.org.message.savefaile});
            }
        }

        var global_reportKey;//定义全局的报表key
        var global_reportInfos;//定义全局的所有报表issue信息
        var showdata;//定义全局的报表列参数
        var global_jira_user_key;

        init();
        /**
         * 初始化页面信息
         */
        function init(){
            getSetReport();//获取此用户的报表设置
            getProName();//获取项目名称
            getOrgTree();//获取组织机构树
            getTeamTree();//获取团队树
            initDateBox();//初始化日历控件
            // $("#orgRadio").attr("checked","checked");//默认点击机构radio
            RadioClick();//机构和团队单选框点击事件
        }

        /**
         * 获取此用户的报表设置
         */
        function getSetReport() {
            //controlePower();
            $("#chart_main").hide();
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/1/setreport/report/show",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    if (data.length == 0) {
                        $("#report_showSet").append("<h2>"+AJS.I18n.getText('workorg.property.report.addReportFirst')+"</h2>");
                    } else {
                        global_reportInfos = data;
                        for (x in data) {
                            var but = "<a href=\"#\" class=\"easyui-linkbutton\" style=\"width:150px;\" onclick=\"show_reportInfo('" + data[x].reportKey + "','" +data[x].reportName + "','" +data[x].jira_user_key + "')\">" + data[x].reportName + "</a><br><br>";
                            $("#report_showSet").append(but);
                        }
                        $.parser.parse("#report_showSet");//重新渲染局部div
                    }
                },
                error: function (data) {

                }
            });
        }

        /**
         * 获取项目名称
         */
        function getProName() {
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/showReport/proNames",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data){
                    initSelect2('proname',data);
                },
                error: function (data) {
                }
            });
        }

        /**
         * 获取组织机构树
         */
        function getOrgTree() {
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/showReport/orgTree",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data){
                    $("#organization").combotree('loadData',data);
                    $("#clearTree").hide();
                    $('#organization').combotree({
                        onSelect:function(node) {
                            $("#clearTree").show();
                        }
                    })
                },
                error: function (data) {

                }
            });
        }

        /**
         * 获取团队树
         */
        function getTeamTree() {
            var proname = $('#proname').val();//选择的项目
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/showReport/teamTree",
                contentType: "application/json; charset=utf-8",
                data:{proname:proname},
                dataType: "json",
                success: function (data){
                    $("#team").combotree('loadData',data);
                    $('#team').combotree({
                        panelWidth:"400px",
                        lines:true,
                        multiple:true, //这个选项设置多选功能
                        cascadeCheck:true,
                        onSelect:function(node) {
                            $("#clearTree").show();
                        },
                        onLoadSuccess:function(node,data){
                            $('#team').combotree('setValues', data);//默认选中所有根节点
                            // $('#team').combotree({cascadeCheck:$(this).is(':checked')})
                        }
                    })
                },
                error: function (data) {

                }
            });
        }

        /**
         * 清除组织机构选项
         */
        clearTree = function(){
            $("#organization").combotree('clear');
            $("#clearTree").hide();
        }

        /**
         * 初始化select2多选下拉框
         */
        function initSelect2(id,data){
            AJS.$("#"+id).auiSelect2({
                data: data,
                width:200,
                height:20,
                // placeholder:'请选择',//默认文字提示
                language: "zh-CN",
                tags: true,//允许手动添加
                allowClear: true,//允许清空
                escapeMarkup: function (markup) { return markup; }, // 自定义格式化防止xss注入
                minimumInputLength: 0,
                formatResult: function formatRepo(repo){return repo.text;}, // 函数用来渲染结果
                formatSelection: function formatRepoSelection(repo){return repo.text;} // 函数用于呈现当前的选择
            });
        }


        /**
         * 获取报表信息
         * @param group_one 第一分组
         * @param group_two 第二分组
         */
        show_reportInfo = function (reportKey,reportName,jira_user_key) {
            global_jira_user_key = jira_user_key;
            $('#reportInfo').panel({title: reportName});
            $('#initialtable').treegrid('loadData',{total:0,rows:[]});//清空表格数据
            $("#chart_main").html("");//清空图表面板
            global_reportKey = reportKey;
            showdata = getReportColumn(reportKey,jira_user_key);
            //如果不指定宽度，easyui会调用自己的autoSizeColumn方法来自适应宽度，速度回非常慢
            var titlearr = "{'titles':[";
            for (x in showdata) {
                titlearr = titlearr + "{'field':'" + showdata[x].columnName + "','title':'" + showdata[x].columnName + "','width':'" + showdata[x].columnWidth + "'},";
            }
            titlearr = titlearr.substring(0, titlearr.length - 1) + "]}";
            var titlearrobj = eval("(" + titlearr + ")");
            document.getElementById('report_show_page').style.height =""+ 540+"px";
            document.getElementById('report_show_page1').style.height =""+ 540+"px";
            document.getElementById('report_show_page2').style.height =""+ 540+"px";
            $('#initialtable').treegrid({
                lines: true,
                scrollbarSize: 0,
                loadMsg: '数据加载中,请稍候...',
                nowrap: true,//当字符串超过单元格时不换行
                idField: 'id',            //定义关键字段来标识树节点。也就是数据的id
                treeField: 'name',        //定义树形显示字段
                formatter: function (value, row, index) {
                    return '<div style="width=250px;word-break:break-all;word-wrap:break-word;white-space:pre-wrap;">' + value + '</div>';
                },
                columns: [titlearrobj.titles]
            });

        }

        var globel_columns;
        /**
         * 获取此报表对应的列
         */
        getReportColumn = function (reportKey,jira_user_key) {
            var columns;
            var type = "show";
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/setreport/reportColumn",
                contentType: "application/json; charset=utf-8",
                data: {
                    reportKey: reportKey,
                    type: type,
                    jira_user_key:jira_user_key
                },
                dataType: "json",
                async: false,
                success: function (data) {
                    columns = data;
                    globel_columns = data;
                },
                error: function (data) {

                }
            });
            return columns;
        }

        var dateFlag = false;
        /**
         * 初始化日历控件
         */
        function initDateBox(){
            //判断开始时间与结束时间
            $.extend($.fn.validatebox.defaults.rules,{
                equaldDate: {
                    validator: function(value, param) {
                        var d1 = $(param[0]).datetimebox('getValue'); //获取开始时间
                        if(value < d1){
                            dateFlag = true;
                        } else {
                            dateFlag = false;
                        }
                        return value >= d1; //有效范围为大于开始时间的日期
                    },
                    message: AJS.I18n.getText('workorg.property.report.dateInfo')
                }
            });
            //日历控件添加清空按钮
            var buttons = $.extend([], $.fn.datebox.defaults.buttons);
            buttons.splice(1, 0,{
                text: '清空',//按钮文本
                handler: function (target) {
                    $("#endTime").datebox('setValue', "");//根据ID清空
                    $("#endTime").datebox('hidePanel', "");
                }
            });
            $('#endTime').datebox({
                prompt:AJS.I18n.getText('workorg.property.report.chooseEndTime'),
                buttons:buttons,
                validType:'equaldDate[\'#startTime\']'
            });

            //日历控件添加清空按钮
            var buttons1 = $.extend([], $.fn.datebox.defaults.buttons);
            buttons1.splice(1, 0,{
                text: '清空',//按钮文本
                handler: function (target) {
                    $("#startTime").datebox('setValue', "");//根据ID清空
                    $("#startTime").datebox('hidePanel', "");
                }
            });
            $('#startTime').datebox({
                prompt:AJS.I18n.getText('workorg.property.report.chooseStartTime'),
                buttons:buttons1
            });
        }


        var result;
        var temp;
        /**
         * 根据条件查询报表结果
         */
        doSearch = function () {
            if (global_reportKey == null) {
                // alert("请先选择报表");
                AJS.org.showInfo({title: AJS.org.message.chooseReport});
                return;
            }
            if(dateFlag){
                // alert("结束日期不能早于开始日期!");
                AJS.org.showInfo({title: AJS.org.message.dateInfo});
                return;
            }
            //获取查询条件
            var startTime = $("#startTime").val();
            var endTime = $("#endTime").val();
            var proname = $('#proname').val();//选择的项目
            var tree = $('#organization').combotree('tree');//整个组织机构树的对象
            var orgdata = tree.tree('getSelected');//选择的组织或部门对象
            var radioType = "orgRadio";//机构或团队的单选框类型
            if($("#teamRadio:checked").val() != null){
                radioType = "teamRadio";
            }
            var teamids = $("#team").val();//选择的团队id，不选默认为""
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/latest/showReport/all",
                contentType: "application/json; charset=utf-8",
                data: {
                    startTime: startTime,
                    endTime: endTime,
                    reportKey: global_reportKey,
                    proname:proname,
                    orgid:(orgdata == null?0:orgdata.id),
                    orgname:(orgdata == null?"":orgdata.text),
                    orgtype:(orgdata == null?"":orgdata.type),
                    radioType:radioType,
                    teamids:teamids,
                    jira_user_key:global_jira_user_key
                },
                dataType: "json",
                success: function (data) {
                    if(data.lists == undefined){
                        $('#initialtable').treegrid('loadData', data);
                        return;
                    }
                    result = data;
                    var total = data.lists.length;
                    var list = data.lists;
                    showChart(list);
                    var a1 = 0;
                    var a2 = 0;
                    var a3 = 0;
                    var a4 = 0;
                    for(i in globel_columns){
                        if(globel_columns[i].columnName == "workingHours"){
                            a1 = globel_columns[i].statistics;
                        }
                        if(globel_columns[i].columnName == "Time Spent"){
                            a2 = globel_columns[i].statistics;
                        }
                        if(globel_columns[i].columnName == "estimate"){
                            a3 = globel_columns[i].statistics;
                        }
                        if(globel_columns[i].columnName == "originalEstimate"){
                            a4 = globel_columns[i].statistics;
                        }
                    }
                    for (x in list) {
                        list[x].workingHours = formatHour(list[x].workingHours,a1,list[x].issuekey);
                        list[x]['Time Spent'] = formatHour(list[x]['Time Spent'],a2,list[x].issuekey);
                        list[x]['estimate'] = formatHour(list[x]['estimate'],a3,list[x].issuekey);
                        list[x]['originalEstimate'] = formatHour(list[x]['originalEstimate'],a4,list[x].issuekey);

                        // list[x].workingHours = ((list[x].workingHours==undefined||list[x].workingHours=="")?"0h":(list[x].workingHours + "h"));
                        // list[x]['Time Spent'] = ((list[x]['Time Spent']==undefined||list[x]['Time Spent']=="")?"0h":(list[x]['Time Spent'] + "h"));
                        // list[x]['estimate'] = ((list[x].estimate==undefined||list[x].estimate=="")?"0h":(list[x].estimate + "h"));
                        // list[x]['originalEstimate'] = ((list[x].originalEstimate==undefined||list[x].originalEstimate=="")?"0h":(list[x].originalEstimate + "h"));
                    }
                    var str = "{'total':" + total + ",'rows':" + JSON.stringify(list) + "}";
                    var obj = eval('(' + str + ')');
                    $('#initialtable').treegrid('loadData', obj);
                    if (list != null) {
                        flag = true;
                    }
                    $("#detailbut").show();


                },
                error: function (data) {

                }
            });

        }


        function formatHour(data,type,issuekey){
            if(issuekey == undefined){
                if(data == undefined){
                    return "0h";
                }
                if(type == "0"){
                    return "";
                } else if(type == "2"){
                    return data;
                } else {
                    if(data == "0" || data == "0.0"){
                        return "0h";
                    }
                    return data+"h";
                }
            } else {
                if(data == "0" || data == "0.0" || data == undefined || data == ""){
                    return "0h";
                }
                return data+"h";
            }

        }

        /**
         * 将传入的字符串转化为int，并保留小数点后1位，并在后面加上"h"
         * @param data
         */
        function changeToInt(data) {
            if (data != null) {
                if (!isNaN(parseInt(data))) {
                    var result = parseInt(data) / 3600;
                    if (result == 0) {
                        return "0";
                    }
                    // return "" + result.toFixed(1) + "h";
                    return "" + result.toFixed(1);
                }
            }
            return "0";
        }

        function controlePower() {
            $.ajax({
                type: "get",
                cache: false,
                async: false,
                url: contextPath + "/rest/wk-teamwork/latest/showReport/controlPower",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data) {
                    for (var k in data) {
                        if (k == 'query') {
                            if (data[k]) {
                                $('#tb').show();
                            }
                        } else {
                            $('#tb').hide();
                        }
                    }
                }
            })
        }

        var flag = true;
        /**
         * 展示或隐藏issue详情
         */
        detailShow = function () {
            var expand = AJS.I18n.getText('workorg.property.report.expand');
            var collapse = AJS.I18n.getText('workorg.property.report.collapse');
            if (flag) {
                $("#detailbut").html(expand);
                var total = result.lists.length;
                var list = result.lists;
                var list_hide = [];
                for (x in list) {
                    if (list[x].issuekey == null) {
                        list_hide.push(list[x]);
                    }
                }

                var total_hide = list_hide.length;
                var str = "{'total':" + total_hide + ",'rows':" + JSON.stringify(list_hide) + "}";
                var obj = eval('(' + str + ')');
                $('#initialtable').treegrid('loadData', obj);
            } else {
                $("#detailbut").html(collapse);
                var total = result.lists.length;
                var list = result.lists;
                var str = "{'total':" + total + ",'rows':" + JSON.stringify(list) + "}";
                var obj = eval('(' + str + ')');
                $('#initialtable').treegrid('loadData', obj);
            }
            flag = !flag;

        }

        var width_size = window.screen.width; //获取屏幕宽度
        /**
         * 点击查询时，展示图表
         * @param data 查询到的issue数据
         */
        function showChart(result) {
            $("#chart_main").hide();
            var data;//报表对应的模板数据，例如模板名称、开始行、结束行等
            for(x in global_reportInfos){
                if(global_reportInfos[x].reportKey == global_reportKey){
                    data = global_reportInfos[x];
                }
            }
            if(data == null){
                // alert("图表加载失败");
                AJS.org.showInfo({title: AJS.org.message.chartLoadFaile});
                return;
            }
            //判断图表是否展示
            if(data.model_show == 8){
                return;
            }
            var modelName = data.modelName;//模板名称
            var modeltype = "";//模板类型,折线图还是柱状图
            if(data.modelType == 0){
                modeltype = "line";
            } else {
                modeltype = "bar";
            }
            var startLine = data.startLine;//开始行
            var endLine = data.endLine;//结束行
            var names = [];//图表对应的列
            for(x in showdata){
                if(showdata[x].columnName != "name"){
                    names.push(showdata[x].columnName);
                }
            }
            var xalis = [];//x轴的数组值
            var data_arr = [];//查询到的数据,只包括issue的数据
            var map = new Map();//issue的key与summary的键值对，用于鼠标放在图表上是展示对应的summary
            for(x in result){
                if(result[x].issuekey != null){
                    data_arr.push(result[x]);
                    var key = result[x].issuekey;
                    map[key] = result[x].name;
                }
            }

            //当查询出的数据为空时
            if(data_arr.length == 0){
                $("#chart_main").hide();
                return;
            }

            var cus_datas = [];//用于生成图的数组
            for(z in data_arr){
                var temp = [];
                if(endLine == 0){
                    if(z >= startLine-1){
                        xalis.push(data_arr[z].issuekey);
                        for(i in names){
                            temp.push(data_arr[z][names[i]]);
                        }
                        cus_datas.push(temp);
                        temp = [];
                    }
                } else {
                    if(z >= startLine-1 && z <= endLine-1){
                        xalis.push(data_arr[z].issuekey);
                        for(i in names){
                            temp.push(data_arr[z][names[i]]);
                        }
                        cus_datas.push(temp);
                        temp = [];
                    }
                }
            }

            var arr_new=[];
            for(i=0;i<cus_datas[0].length;i++){
                arr_new[i]=[];
            }
            for(i=0;i<cus_datas.length;i++){
                for(j=0;j<cus_datas[i].length;j++){
                    arr_new[j][i]=cus_datas[i][j];
                }
            }

            var datas = [];
            for(i in names){
                datas.push({name:names[i],type:modeltype,data:arr_new[i]});
            }

            var arr1 = [];
            var arr2 = [];
            var arr3 = [];

            for(x in datas){
                if(datas[x].name == "workingHours" || datas[x].name == "Time Spent" || datas[x].name == "estimate" || datas[x].name == "originalEstimate"){
                    arr1.push(datas[x]);

                } else if(datas[x].name == "Votes" || datas[x].name == "Watchers"){
                    arr2.push(datas[x]);

                } else if(datas[x].name == "number"){
                    arr3.push(datas[x]);

                }
            }
            var chartNum=0;
            if(arr1.length != 0){chartNum=chartNum+1;}
            if(arr2.length != 0){chartNum=chartNum+1;}
            if(arr3.length != 0){chartNum=chartNum+1;}
            document.getElementById('report_show_page').style.height =""+ (500+280*chartNum)+"px";
            document.getElementById('report_show_page1').style.height =""+ (500+280*chartNum)+"px";
            document.getElementById('report_show_page2').style.height =""+ (500+280*chartNum)+"px";

            var divArr = [];
            if(arr1.length != 0){
                var div = document.createElement("DIV");//创建模板div
                div.setAttribute("id","myChart"+x);
                div.style.height = "280px";
                div.style.width = "1150px";
                div.style.cssFloat  = "left";
                var myChart = echarts.init(div);
                var option = {
                    title : {
                        text: modelName +"( timesheet )"
                    },
                    tooltip : {
                        trigger: 'axis'
                    },
                    legend: {
                        data:names
                    },
                    toolbox: {
                        show : true,
                        feature : {
                            mark : {show: true},
                            dataView : {show: true, readOnly: false},
                            magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
                            restore : {show: true},
                            saveAsImage : {show: true}
                        }
                    },
                    calculable : true,
                    boundaryGap : flag,
                    series:arr1,
                    xAxis : [{
                        type : 'category',
                        boundaryGap : false,
                        data : xalis,
                        axisLabel:{
                            rotate:45, //刻度旋转45度角
                        },
                        axisPointer: {
                            label: {
                                formatter: function (params) {
                                    return params.value + map[params.value] ;
                                }
                            }
                        }
                    }],
                    yAxis : [{
                        type : 'value',
                        axisLabel:{formatter:'{value} h'}
                    }]
                };
                myChart.setOption(option);
                divArr.push(div);
            }
            if(arr2.length != 0){
                var div = document.createElement("DIV");//创建模板div
                div.setAttribute("id","myChart"+x);
                div.style.height = "280px";
                div.style.width = "1150px";
                div.style.cssFloat  = "left";
                var myChart = echarts.init(div);
                var option = {
                    title : {
                        text: modelName + "( count person )"
                    },
                    tooltip : {
                        trigger: 'axis'
                    },
                    legend: {
                        data:names
                    },
                    toolbox: {
                        show : true,
                        feature : {
                            mark : {show: true},
                            dataView : {show: true, readOnly: false},
                            magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
                            restore : {show: true},
                            saveAsImage : {show: true}
                        }
                    },
                    calculable : true,
                    boundaryGap : flag,
                    series:arr2,
                    xAxis : [{
                        type : 'category',
                        boundaryGap : false,
                        data : xalis,
                        axisLabel:{
                            rotate:45, //刻度旋转45度角
                        },
                        axisPointer: {
                            label: {
                                formatter: function (params) {
                                    return params.value + map[params.value] ;
                                }
                            }
                        }
                    }],
                    yAxis : [{
                        type : 'value',
                        axisLabel:{formatter:'{value} per'}
                    }]
                };
                myChart.setOption(option);
                divArr.push(div);
            }
            if(arr3.length != 0){
                var div = document.createElement("DIV");//创建模板div
                div.setAttribute("id","myChart"+x);
                div.style.height = "280px";
                div.style.width = "1150px";
                div.style.cssFloat  = "left";
                var myChart = echarts.init(div);
                var option = {
                    title : {
                        text: modelName + "( number )"
                    },
                    tooltip : {
                        trigger: 'axis'
                    },
                    legend: {
                        data:names
                    },
                    toolbox: {
                        show : true,
                        feature : {
                            mark : {show: true},
                            dataView : {show: true, readOnly: false},
                            magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
                            restore : {show: true},
                            saveAsImage : {show: true}
                        }
                    },
                    calculable : true,
                    boundaryGap : flag,
                    series:arr3,
                    xAxis : [{
                        type : 'category',
                        boundaryGap : false,
                        data : xalis,
                        axisLabel:{
                            rotate:45, //刻度旋转45度角
                        },
                        axisPointer: {
                            label: {
                                formatter: function (params) {
                                    return params.value + map[params.value] ;
                                }
                            }
                        }
                    }],
                    yAxis : [{
                        type : 'value'
                    }]
                };
                myChart.setOption(option);
                divArr.push(div);
            }

            // 使用刚指定的配置项和数据显示图表。
            $("#chart_main").html("");//清空图表面板
            for(x in divArr){
                $("#chart_main").append(divArr[x]);
            }
            // $("#chart_main").html(div);
            if(divArr.length != 0){
                $("#chart_main").show();
            }

        }

        /**
         * 机构和团队单选框点击事件,项目多选框点击事件
         * @constructor
         */
        function RadioClick(){
            $("#organization_div").show();
            $("#team_div").hide();
            // $("#orgRadio").click(function(){
            //     $("#organization_div").show();
            //     $("#team_div").hide();
            // });
            // $("#teamRadio").click(function(){
            //     $("#organization_div").hide();
            //     $("#team_div").show();
            //     getTeamTree();
            // });
            $("#proname").click(function(){
                getTeamTree();
            });
        }

        function createChartDiv(){

        }

    });

}(AJS.$, AJS.contextPath()));