!(function ($, contextPath) {
    $(function () {

        AJS.org = {
            message: {
                getDataFaile:AJS.I18n.getText('workorg.property.report.getDataFaile'),
                writeReportName:AJS.I18n.getText('workorg.property.report.writeReportName'),
                updateSuccess:AJS.I18n.getText('workorg.property.report.updateSuccess'),
                updateFaile:AJS.I18n.getText('workorg.property.report.updateFaile'),
                deleteSuccess:AJS.I18n.getText('workorg.property.report.deleteSuccess'),
                deleteFaile:AJS.I18n.getText('workorg.property.report.deleteFaile'),
                getColumnFaile:AJS.I18n.getText('workorg.property.report.getColumnFaile'),
                writeReportKey:AJS.I18n.getText('workorg.property.report.writeReportKey'),
                reportKeyIsExist:AJS.I18n.getText('workorg.property.report.reportKeyIsExist'),
                modelnamenull:AJS.I18n.getText('workorg.property.report.modelnamenull'),
                modeltypenull:AJS.I18n.getText('workorg.property.report.modeltypenull'),
                savefaile:AJS.I18n.getText('workorg.property.report.savefaile')
            },
            showInfo: function (options) {
                options = options || {};
                var myFlag = AJS.flag({
                    type: options.type || 'info',
                    title: options.title
                });
                setTimeout(function () {
                    myFlag.close()
                }, 2000);
            },
            showError: function (mes) {
                this.showInfo({type: 'error', title: mes || AJS.org.message.savefaile});
            }
        }

        init();//报表初始化信息
        function init(){
            controlePower();//初始化报表权限
            getSetReport();//获取此用户的报表设置
            getInitColumn();//获取所有需要展示的列名
        }

        var orgRoleData;
        /**
         * 获取全部组织角色
         */
        function getOrgRole(){
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/1/setreport/orgrole",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                async: false,
                success: function (data){
                    var  map={}
                    map["System Administrator"]=AJS.I18n.getText('workorg.property.permissionMgr.role.sa');
                    map["It Operation"]=AJS.I18n.getText('workorg.property.permissionMgr.role.op');
                    map["Top Executives"]=AJS.I18n.getText('workorg.property.permissionMgr.role.sm');
                    map["Department Manager"]=AJS.I18n.getText('workorg.property.permissionMgr.role.dm');
                    map["Project Managers"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pm');
                    map["General Staff"]=AJS.I18n.getText('workorg.property.permissionMgr.role.gs');
                    map["Product Manager"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pdm');
                    map["Project Manager"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pl');
                    map["Test supervisor"]=AJS.I18n.getText('workorg.property.permissionMgr.role.ts');
                    map["Developer"]=AJS.I18n.getText('workorg.property.permissionMgr.role.dev');
                    map["Product Tester"]=AJS.I18n.getText('workorg.property.permissionMgr.role.pt');
                    for(x in data.results){
                        data.results[x].text = map[data.results[x].text];
                    }
                    orgRoleData = data;
                    initSelect2('orgRole',data);
                },
                error: function (data) {
                }
            });
        }

        var employeeData;
        /**
         * 获取除自己的全部雇员
         */
        function getEmployee(){
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/1/setreport/employee",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                async: false,
                success: function (data){
                    employeeData = data;
                    initSelect2('employee',data);
                },
                error: function (data) {
                }
            });
        }

        var global_jira_user_key = AJS.params.loggedInUser;
        /**
         * 获取此用户的报表设置
         */
        function getSetReport() {
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/1/setreport/report/set",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data){
                    $("#report_west_add").nextAll().remove();
                    if(data.length != 0){
                        for(var i=0;i<data.length;i++){
                            var html = "<br><br><a href=\"#\" class=\"easyui-linkbutton\" style=\"width:150px;\"" +
                                "onclick=\"showReportSet(\'"+data[i].reportKey+"\',\'"+data[i].reportName+"\',\'"+data[i].group_one+"\',\'"+data[i].group_two+
                                "\',\'"+data[i].modelName+"\',\'"+data[i].modelType+"\',\'"+data[i].model_show+"\',\'"+data[i].startLine+"\',\'"+data[i].endLine+
                                "\')\">"+data[i].reportName+"</a>";
                            // global_jira_user_key = data[i].jira_user_key;
                            $(html).appendTo($("#report_west"));
                            $.parser.parse("#report_west");//重新渲染局部div
                        }

                    }

                },
                error: function (data) {

                }
            });

        }

        /**
         * 点击报表按钮展示报表设置详细信息
         * @param reportKey 报表key值
         * @param reportName 报表名称
         * @param group_one 第一分组
         * @param group_two 第二分组
         * @param modelName 模板名称
         * @param modelType 模板类型
         * @param model_show 模板是否启用
         * @param startLine 开始行
         * @param endLine 结束行
         */
        showReportSet = function(reportKey,reportName,group_one,group_two,modelName,modelType,model_show,startLine,endLine){
            if(isHaveForm()){
                $("#setReport").empty();
            }

            var orgRole = [];
            var employee = [];
            //获取报表对应的分享数据
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/1/setreport/sharereport",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                async: false,
                data: {
                    reportKey: reportKey
                },
                success: function (data) {
                    getOrgRole();
                    getEmployee();
                    if(data.length != 0){
                        for(x in data){
                            if(data[x].type == 0){
                                var result = orgRoleData.results;
                                for(y in result){
                                    if(result[y].id == data[x].emp_org_id){
                                        orgRole.push(result[y]);
                                    }
                                }
                            } else {
                                var result = employeeData.results;
                                for(y in result){
                                    if(result[y].id == data[x].emp_org_id){
                                        employee.push(result[y]);
                                    }
                                }
                            }
                        }
                    }
                },
                error: function () {

                }
            });

            //获取报表对应的列展示及其详细列属性配置
            var type = "set";
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/1/setreport/reportColumn",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                data:{
                    reportKey:reportKey,
                    type:type,
                    jira_user_key:global_jira_user_key
                },
                success: function (data){
                    if(data.length == 0){
                        // alert("数据获取失败");
                        AJS.org.showInfo({title: AJS.org.message.getDataFaile});
                        return;
                    }
                    if(group_two=="999"){
                        group_two="";
                    }
                    var html = work.report.Template.showReportSet({reportKey:reportKey,reportName:reportName});
                    $(html).appendTo($("#setReport"));
                    $.parser.parse("#setReport");//重新渲染局部div
                    myChart = echarts.init(document.getElementById('main'));
                    controlePower();
                    var columns = [];
                    for(x in data){
                        if(data[x].columnName != "name"){
                            columns.push(data[x].columnName);
                        }
                    }
                    initCombobox('showColumn',initcolumns);
                    $("#showColumn").combobox('setValue',columns);

                    initDatagrid();
                    $('#configureColumn').datagrid('loadData', data);

                    //增加第一分组与第二分组的下拉框二级联动
                    $('#groupone').combobox({
                        onSelect: function (data) {
                            if (data != null) {
                                $('#grouptwo').combobox({
                                    disabled: false,
                                    url: contextPath + "/rest/wk-teamwork/1/setreport/getGrouptwo/"+data.text,
                                    valueField: 'value',
                                    textField: 'text'
                                });
                            }
                        }
                    });
                    changeModelType();
                    $("#groupone").combobox("setValue",group_one);
                    $("#grouptwo").combobox("setValue",group_two);
                    $("#modelName").textbox('setValue',modelName);
                    $("#model_show").combobox('setValue',model_show);
                    $("#modelType").combobox('setValue',modelType);
                    $("#startLine").textbox('setValue',startLine);
                    $("#endLine").textbox('setValue',endLine);

                    initSelect2('orgRole',orgRoleData);
                    initSelect2('employee',employeeData);
                    $("#orgRole").select2("data", orgRole);
                    $("#employee").select2("data", employee);

                    var names = [];
                    var datas = [];
                    var nowColumns = $("#showColumn").combobox('getValues');//获取选择前的列
                    switch(modelType){
                        case "0" :
                            for(var i=0;i<nowColumns.length;i++){
                                names.push(nowColumns[i]);
                                datas.push({name:nowColumns[i],type:'line',data:randomFn()});
                            }
                            break;
                        case "1" :
                            for(var i=0;i<nowColumns.length;i++){
                                names.push(nowColumns[i]);
                                datas.push({name:nowColumns[i],type:'bar',data:randomFn()});
                            }
                            break;
                    }
                    reloadEaharts(names,datas);

                    //修改按钮点击事件
                    $("#updateReport_but").click(function(){
                        if(editIndex != undefined){
                            endEditing();
                        }

                        var reportName = $("#reportName").val();
                        if(reportName == null || reportName == ""){
                            // alert("请填写报表名称");
                            AJS.org.showInfo({title: AJS.org.message.writeReportName});
                            return;
                        }
                        var reportKey = $("#reportKey").val()
                        var group_one = $("#groupone").combobox('getValue');
                        var group_two = $("#grouptwo").combobox('getValue');
                        if(group_two == null || group_two == ""){
                            group_two = "999";
                        }
                        var columnDatas = $('#configureColumn').datagrid('getData');
                        var str = JSON.stringify(columnDatas.rows);

                        var modelName = $("#modelName").val();//图表名称
                        var modelType = $("#modelType").val();//图表类型
                        var model_show = $("#model_show").val();//图表是否展示
                        var startLine = $("#startLine").val();//开始行
                        var endLine = $("#endLine").val();//结束行
                        if(startLine == null || startLine == ""){
                            startLine = 0;
                        }
                        if(endLine == null || endLine == ""){
                            endLine = 0;
                        }
                        if(modelName == ""){
                            AJS.org.showInfo({title: AJS.org.message.modelnamenull});
                            return;
                        }
                        if(modelType == ""){
                            AJS.org.showInfo({title: AJS.org.message.modeltypenull});
                            return;
                        }

                        var orgRole = ($("#orgRole").val()==""?"null":$("#orgRole").val());
                        var employee = ($("#employee").val()==""?"null":$("#employee").val());
                        $.ajax({
                            type: "post",
                            url: contextPath + "/rest/wk-teamwork/1/setreport/updateReport/"+reportName+"/"+
                                 reportKey+"/"+group_one+"/"+group_two+"/"+modelName+"/"+modelType+"/"+model_show+"/"+
                                 startLine+"/"+endLine+"/"+orgRole+"/"+employee,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            data:str,
                            success: function (data){
                                getSetReport();
                                // alert("修改成功");
                                AJS.org.showInfo({title: AJS.org.message.updateSuccess});
                            },
                            error: function (data) {
                                // alert("修改失败");
                                AJS.org.showInfo({title: AJS.org.message.updateFaile});
                            }
                        });

                    });

                    //删除按钮点击事件
                    $("#deleteReport_but").click(function(){
                        $.ajax({
                            type: "post",
                            url: contextPath + "/rest/wk-teamwork/1/setreport/deleteReport/"+reportKey,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (data){
                                // alert("删除成功");
                                AJS.org.showInfo({title: AJS.org.message.deleteSuccess});
                                getSetReport();
                                $("#setReport").empty();
                            },
                            error: function (data) {
                                // alert("删除失败");
                                AJS.org.showInfo({title: AJS.org.message.deleteFaile});
                            }
                        });

                    });

                },
                error: function (data) {

                }
            });

        }

        /**
         * 新增报表，展示报表设置界面
         */
        addBut = function(){
            if(isHaveForm()){
                $("#setReport").empty();
            }
            var html = work.report.Template.setReport();
            $(html).appendTo($("#setReport"));
            $.parser.parse("#setReport");//重新渲染局部div
            controlePower();

            //增加第一分组与第二分组的下拉框二级联动
            $('#groupone').combobox({
                onSelect: function (data) {
                    if (data != null) {
                        $('#grouptwo').combobox({
                            disabled: false,
                            url: contextPath + "/rest/wk-teamwork/1/setreport/getGrouptwo/"+data.text,
                            valueField: 'value',
                            textField: 'text'
                        });
                    }
                }
            });

            if(initcolumns == null){
                // alert("获取列失败！");
                AJS.org.showInfo({title: AJS.org.message.getColumnFaile});
            } else {
                initCombobox('showColumn',initcolumns);//初始化下拉框
                initDatagrid();//初始化列配置表格
                var str ="[{columnName:'name',columnWidth:'200',statistics:'0',sequence:'1'}]";
                str  = eval("(" + str + ")");
                $('#configureColumn').datagrid('loadData', str);

                myChart = echarts.init(document.getElementById('main'));
                loadModel();
                changeModelType();
            }

            getOrgRole();//获取组织角色
            getEmployee();//获取雇员

            //保存按钮点击事件
            $("#saveReport_but").click(function(){
                if(editIndex != undefined){
                    endEditing();
                }

                var reportName = $("#reportName").val();//报表名称
                if(reportName == null || reportName == ""){
                    // alert("请填写报表名称");
                    AJS.org.showInfo({title: AJS.org.message.writeReportName});
                    return;
                }
                var reportKey = $("#reportName").val()+"_"+global_jira_user_key;;//报表key
                if(reportKey == null || reportKey == ""){
                    // alert("请填写报表key");
                    AJS.org.showInfo({title: AJS.org.message.writeReportKey});
                    return;
                } else {
                    var flag = true;//判断报表key是否存在
                    $.ajax({
                        type: "get",
                        url: contextPath + "/rest/wk-teamwork/1/setreport/reportKeyIsExist/"+reportKey,
                        contentType: "application/json; charset=utf-8",
                        dataType: "json",
                        async: false,
                        success: function (data){
                            flag = data;
                        },
                        error: function (data) {

                        }
                    });
                    if(flag){
                        // alert("报表key已存在");
                        AJS.org.showInfo({title: AJS.org.message.reportKeyIsExist});
                        return;
                    }
                }
                var group_one = $("#groupone").combobox('getValue');//第一分组
                var group_two = $("#grouptwo").combobox('getValue');//第二分组
                if(group_two == null || group_two == ""){
                    group_two = "999";
                }
                var columnDatas = $('#configureColumn').datagrid('getData');//列表格数据
                var str = JSON.stringify(columnDatas.rows);//列表格数据序列化
                var modelName = $("#modelName").val();//图表名称
                var modelType = $("#modelType").val();//图表类型
                var model_show = $("#model_show").val();//图表是否展示
                var startLine = $("#startLine").val();//开始行
                var endLine = $("#endLine").val();//结束行
                if(startLine == null || startLine == ""){
                    startLine = 0;
                }
                if(endLine == null || endLine == ""){
                    endLine = 0;
                }
                if(modelName == ""){
                    AJS.org.showInfo({title: AJS.org.message.modelnamenull});
                    return;
                }
                if(modelType == ""){
                    AJS.org.showInfo({title: AJS.org.message.modeltypenull});
                    return;
                }
                var orgRole = ($("#orgRole").val()==""?"null":$("#orgRole").val());
                var employee = ($("#employee").val()==""?"null":$("#employee").val());

                $.ajax({
                    type: "post",
                    url: contextPath + "/rest/wk-teamwork/1/setreport/saveReport/"+
                         reportName+"/"+reportKey+"/"+group_one+"/"+group_two+"/"+modelName+"/"+
                         modelType+"/"+model_show+"/"+startLine+"/"+endLine+"/"+orgRole+"/"+employee,
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    data:str,
                    success: function (data){
                        var html = "<br><br><a href=\"#\" class=\"easyui-linkbutton\" style=\"width:150px;\"" +
                            "onclick=\"showReportSet(\'"+reportKey+"\',\'"+reportName+"\',\'"+group_one+"\',\'"+group_two+
                                            "\',\'"+modelName+"\',\'"+modelType+"\',\'"+model_show+"\',\'"+startLine+"\',\'"+endLine+
                                            "\')\">"+reportName+"</a>";
                        $(html).appendTo($("#report_west"));
                        $.parser.parse("#report_west");//重新渲染局部div
                    },
                    error: function (data) {
                        // alert("保存失败");
                        AJS.org.showInfo({title: AJS.org.message.savefaile});
                        return;
                    }
                });

            });

        }

        var initcolumns;
        /**
         * 获取所有需要展示的列名
         */
        function getInitColumn() {
            $.ajax({
                type: "get",
                url: contextPath + "/rest/wk-teamwork/1/setreport/initcolumn",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data){
                    initcolumns = data;
                },
                error: function (data) {

                }
            });
        }

        /**
         * 初始化列的多选下拉框
         */
        function initCombobox(id,data){
            //加载下拉框复选框
            $("#"+id).combobox({
                data:data,
                panelHeight:200,//设置为固定高度，combobox出现竖直滚动条
                valueField:'comboboxValue',
                textField:'comboboxValue',
                multiple:true,
                formatter: function (row) { //formatter方法就是实现了在每个下拉选项前面增加checkbox框的方法
                    var opts = $(this).combobox('options');
                    return '<input type="checkbox" class="combobox-checkbox">' + row[opts.textField]
                },
                onLoadSuccess: function () {  //下拉框数据加载成功调用
                    var opts = $(this).combobox('options');
                    var target = this;
                    var values = $(target).combobox('getValues');//获取选中的值的values
                    $.map(values, function (value) {
                        var el = opts.finder.getEl(target, value);
                        el.find('input.combobox-checkbox')._propAttr('checked', true);
                    })
                },
                onSelect: function (row) { //选中一个选项时调用
                    var opts = $(this).combobox('options');
                    //获取选中的值的values
                    $("#"+id).val($(this).combobox('getValues'));

                    //设置选中值所对应的复选框为选中状态
                    var el = opts.finder.getEl(this, row[opts.valueField]);
                    el.find('input.combobox-checkbox')._propAttr('checked', true);

                    var nowColumns = $(this).combobox('getValues');//获取选择前的列
                    var addColumn = row[opts.valueField];//获取此次选择的列
                    append(addColumn);

                    var names = [];
                    var datas = [];
                    switch($("#modelType").combobox('getValue')){
                        case "0" :
                            for(var i=0;i<nowColumns.length;i++){
                                names.push(nowColumns[i]);
                                datas.push({name:nowColumns[i],type:'line',data:randomFn()});
                            }
                            names.push(""+addColumn);
                            datas.push({name:addColumn,type:'line',data:randomFn()});
                            break;
                        case "1" :
                            for(var i=0;i<nowColumns.length;i++){
                                names.push(nowColumns[i]);
                                datas.push({name:nowColumns[i],type:'bar',data:randomFn()});
                            }
                            names.push(""+addColumn);
                            datas.push({name:addColumn,type:'bar',data:randomFn()});
                    }
                    reloadEaharts(names,datas);


                },
                onUnselect: function (row) {//不选中一个选项时调用
                    var opts = $(this).combobox('options');
                    //获取选中的值的values
                    $("#"+id).val($(this).combobox('getValues'));
                    var el = opts.finder.getEl(this, row[opts.valueField]);
                    el.find('input.combobox-checkbox')._propAttr('checked', false);

                    var nowColumns = $(this).combobox('getValues');//获取选择前的列
                    var deleteColumn = row[opts.valueField];//获取此次取消的列
                    removeit(deleteColumn);

                    var names = [];
                    var datas = [];
                    var addColumn = row[opts.valueField];
                    switch($("#modelType").combobox('getValue')){
                        case "0" :
                            for(var i=0;i<nowColumns.length;i++){
                                if(nowColumns[i] != addColumn){
                                    names.push(nowColumns[i]);
                                    datas.push({name:nowColumns[i],type:'line',data:randomFn()});
                                }
                            }
                            break;
                        case "1" :
                            for(var i=0;i<nowColumns.length;i++){
                                if(nowColumns[i] != addColumn){
                                    names.push(nowColumns[i]);
                                    datas.push({name:nowColumns[i],type:'bar',data:randomFn()});
                                }
                            }
                            break;
                    }
                    reloadEaharts(names,datas);
                }
            });
        }


        /**
         * 新增一行
         * @param addColumn 增加行的列名
         */
        function append(addColumn){
            if (endEditing()){
                var datas = $('#configureColumn').datagrid("getRows");
                var maxCount = 1;
                if(datas != null && datas != undefined && datas.length != 0){
                    maxCount = datas[0].sequence;
                }
                for(x in datas){
                    if(datas[x].sequence >= maxCount){
                        maxCount = parseInt(datas[x].sequence) + 1;
                    }
                }
                $('#configureColumn').datagrid('appendRow',{columnName:addColumn,columnWidth:'100',statistics:'0',sequence:maxCount});
            }
        }

        /**
         * 删除一行
         * @param deleteColumn 删除行的列名
         */
        function removeit(deleteColumn){
            var dataList = $('#configureColumn').datagrid("getData").rows; //取得原始数据列表
            for(x in dataList){
                if(dataList[x].columnName == deleteColumn){
                    editIndex = $('#configureColumn').datagrid('getRowIndex',dataList[x]);//获取删除行的索引
                    continue;
                }
            }
            $('#configureColumn').datagrid('cancelEdit', editIndex).datagrid('deleteRow', editIndex);
            editIndex = undefined;
        }

        /**
         * 判断报表设置是否已包含soy模板文件的form
         * @returns true-包含 false-不包含
         */
        function isHaveForm(){
            if($("#setReport form").length == 0){
                return false;
            } else {
                return true;
            }
        }

        /**
         * 初始化列属性配置表格
         */
        function initDatagrid(){
            $('#configureColumn').datagrid({
                rownumbers:true,
                singleSelect:true,
                width:600,
                columns: [[
                    {field:'columnName',title:AJS.I18n.getText('workorg.property.report.columnName'),width:200,align:'center'},
                    {field:'showDetail',title:AJS.I18n.getText('workorg.property.report.showDetail'),width:100,align:'center'},
                    {field:'columnWidth',title:AJS.I18n.getText('workorg.property.report.columnWidth'),width:100,align:'center',editor:'numberbox'},
                    {field:'statistics',title:AJS.I18n.getText('workorg.property.report.statistics'),width:100,align:'center',
                        formatter:function(value,row){
                            if(value == 0){
                                return AJS.I18n.getText('workorg.property.report.none');
                            } else if(value == 1){
                                return AJS.I18n.getText('workorg.property.report.sum');
                            } else if(value == 2){
                                return AJS.I18n.getText('workorg.property.report.count');
                            }
                            return "";
                        },
                        editor:{
                            type:'combobox',
                            options:{
                                valueField:'statistics',
                                textField:'statisticsname',
                                data:[
                                    {statistics:'0',statisticsname:AJS.I18n.getText('workorg.property.report.none')},
                                    {statistics:'1',statisticsname:AJS.I18n.getText('workorg.property.report.sum')},
                                    {statistics:'2',statisticsname:AJS.I18n.getText('workorg.property.report.count')}
                                ],
                                required:true
                            }
                        }
                    },
                    {field:'sequence',title:AJS.I18n.getText('workorg.property.report.sequence'),width:100,align:'center',editor:'numberbox'}
                ]],
                // onClickRow:onClickRow
                onClickRow:function(index){
                    if (editIndex != index){
                        if (endEditing()){
                            $('#configureColumn').datagrid('selectRow', index).datagrid('beginEdit', index);
                            editIndex = index;
                        } else {
                            $('#configureColumn').datagrid('selectRow', editIndex);
                        }
                    }
                }
            });
            // $('#configureColumn').parent().mouseout(function (){
            //     if(editIndex != undefined) {
            //         $('#configureColumn').datagrid('endEdit',editIndex);//当前行编辑事件取消
            //     }
            // });
        }

        var editIndex = undefined;
        function endEditing(){
            //如果为undefined的话，为真，说明可以编辑
            if(editIndex == undefined) {
                return true;
            }
            if($('#configureColumn').datagrid('validateRow',editIndex)) {
                $('#configureColumn').datagrid('endEdit',editIndex);//当前行编辑事件取消
                editIndex = undefined;
                return true;//重置编辑行索引对象，返回真，允许编辑
            }else{
                return false;
            }//否则，为假，返回假，不允许编辑
        }

        function onClickRow(index) {
            if (editIndex != index) {
                if (endEditing()) {
                    $('#configureColumn').datagrid('selectRow', index)
                        .datagrid('beginEdit', index);
                    var editors = $('#configureColumn').datagrid('getEditors', index);
                    var sfgzEditor = editors[0];
                    sfgzEditor.target.bind('blur', function () {
                        $('#configureColumn').datagrid('selectRow', index).datagrid('endEdit', index);
                        editIndex = undefined;
                    });
                    editIndex = index;
                } else {
                    $('#configureColumn').datagrid('selectRow', editIndex);
                }
            }
        }

        function initCombogrid(){
            $('#configureColumn').combogrid({
                striped: true,
                multiple: true,
                panelWidth: 600,
                panelHeight:300,
                rownumbers: true,
                columns: [[
                    {field:'columnName',title:AJS.I18n.getText('workorg.property.report.columnName'),minwidth:200,align:'center'},
                    {field:'showDetail',title:AJS.I18n.getText('workorg.property.report.showDetail'),width:100,align:'center'},
                    {field:'columnWidth',title:AJS.I18n.getText('workorg.property.report.columnWidth'),maxwidth:50,align:'center',editor: { type: 'text', options: { required: true } }},
                    {field:'statistics',title:AJS.I18n.getText('workorg.property.report.statistics'),maxwidth:50,align:'center'},
                    {field:'sequence',title:AJS.I18n.getText('workorg.property.report.sequence'),maxwidth:50,align:'center'}
                ]],
                onClickCell:function(rowIndex, field, value){
                    if(field == "jqlvalue"){
                        $("#jql_str").textbox('setValue',value);
                    }
                }
            });
            var str ="[{columnName:'name',columnWidth:'200',statistics:'0',sequence:'1'}]";
            str  = eval("(" + str + ")");
            $('#configureColumn').combogrid('grid').datagrid('loadData', str);
            $('#configureColumn').combogrid("setValue","配置列属性");
        }

        function controlePower() {
            $.ajax({
                type: "get",
                cache: false,
                async:false,
                url: contextPath + "/rest/wk-teamwork/1/setreport/controlPower",
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success:function (data) {
                    for (var k in data) {
                        console.info("-------------------------")
                        if (data[k]) {
                            if (k == "addReport") {
                                $('#report_west_add').css("display", "inline-block");
                            }
                            if (k == "saveReport") {
                                $('#saveReport_but').css("display", "inline-block");
                            }
                            if (k == "delReport") {
                                $('#deleteReport_but').css("display", "inline-block");
                            }
                            if (k == "updateReport") {
                                $('#updateReport_but').css("display", "inline-block");
                            }else {
                                $('#'+k).css("display", "inline-block");
                            }
                        }
                    }
                }
            })
        }

        /**
         * 初始化select2多选下拉框
         */
        function initSelect2(id,data){
            AJS.$("#"+id).auiSelect2({
                data: data,
                width:250,
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

        // --------------------以下为图表相关设置--------------------------

        // 基于准备好的dom，初始化echarts实例
        var myChart;''
        /**
         * 加载图表初始模型
         */
        function loadModel(){
            option = {
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    data:[]
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                toolbox: {
                    feature: {
                        saveAsImage: {}
                    }
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: ['issue1','issue2','issue3','issue4','issue5','issue6','issue7']
                },
                yAxis: {
                    type: 'value'
                },
                series: []
            };
            // 使用刚指定的配置项和数据显示图表。

            myChart.setOption(option);
        }

        /**
         * 图表创建时使用随机数模拟数据
         * @returns {Array}
         */
        function randomFn(){
            var Arr = [];
            while(Arr.length < 7){
                Arr.push(Math.floor(Math.random()*3000));
            }
            return Arr;
        }

        /**
         * 点击图表类型下拉框或列下拉框时，重新加载图表
         */
        function reloadEaharts(names,datas){

            if(myChart != null){
                myChart.clear();
            }

            loadModel();
            myChart.setOption({
                legend: {
                    data:names
                },
                series:datas
            });
        }

        /**
         * 图形类别下拉框选择事件
         */
        function changeModelType() {
            $("#modelType").combobox({
                onSelect: function (row) {
                    var opts = $(this).combobox('options');
                    var names = [];
                    var datas = [];
                    var nowColumns = $("#showColumn").combobox('getValues');
                    switch(row.value){
                        case "0" :
                            for(var i=0;i<nowColumns.length;i++){
                                names.push(nowColumns[i]);
                                datas.push({name:nowColumns[i],type:'line',data:randomFn()});
                            }
                            break;
                        case "1" :
                            for(var i=0;i<nowColumns.length;i++){
                                names.push(nowColumns[i]);
                                datas.push({name:nowColumns[i],type:'bar',data:randomFn()});
                            }
                            break;
                    }
                    reloadEaharts(names,datas);
                }
            });
        }

    });
}(AJS.$, AJS.contextPath()));