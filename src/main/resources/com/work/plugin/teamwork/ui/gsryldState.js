
(function ($, contextPath) {

    var treeTR;
    var JsonOBJ,arrColumn;
    var col;
    var cc; //预请求一下
    $(function () {
        alert("aaaa")
    });

   // $.get(contextPath + "/rest/wk-teamwork/latest/orgstr/getDetailOrgTree");
    $(window).load(function () {
        alert("bbbb")
        getGsryldData()
    })



// window.onload = getGsryldData;


function getGsryldData(){
    controlePower();
    $('#YWaitDialog').show();
    $.ajax({
        type: "get",
        url: AJS.contextPath() + "/rest/wk-teamwork/1/gsryldState/",
        contentType: "application/json; charset=utf-8",
        dataType: "text",
        success: function (data) {
            $('#YWaitDialog').hide();
            arrColumn = new Array();
            arrColumn[0] = "name";
            arrColumn[1] = "entNum";
            arrColumn[2] = "dimNum";
            arrColumn[3] = "dimRate";
            arrColumn[4] = "entTotalNum";
            arrColumn[5] = "dimTotalNum";
            arrColumn[6] = "dimTotalRate";
            //创建TH
            treeTR = "<tr>";
            for(var t=0;t<arrColumn.length;t++){
                if(arrColumn[t] == "entNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.entNum') + "</th>";
                 } else if(arrColumn[t] == "dimNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.dimNum') + "</th>";
                 } else if(arrColumn[t] == "dimRate"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.dimRate') + "(%)</th>";
                 } else if(arrColumn[t] == "entTotalNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.entTotalNum') + "</th>";
                 } else if(arrColumn[t] == "dimTotalNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.dimTotalNum') + "</th>";
                 } else if(arrColumn[t] == "dimTotalRate"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.dimTotalRate') + "(%)</th>";
                 } else if(arrColumn[t] == "name"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.name') + "</th>";
                 }
            }
            treeTR+="</tr>";
            if(data == ""){
                $("#example-advanced").append(treeTR);
                return;
            }
            var obj = eval('(' + data + ')');
            JsonOBJ = obj.root.children;
           //创建底层节点
            for(var i=0;i< JsonOBJ.length;i++){
                treeTR += "<tr data-tt-id='"+i+"'>" ;
                for(var x=0;x<arrColumn.length;x++){
                    col = arrColumn[x];
                    if(x == 0){
                        treeTR +="<td style=\"text-align:left;\">" +JsonOBJ[i].parameter[col]+ "</td>";
                    } else {
                        treeTR +="<td style=\"text-align:center;\">" +JsonOBJ[i].parameter[col]+ "</td>";
                    }

                }
                treeTR += "</tr>";
                cc= i;
                tree(JsonOBJ[i].children,cc);
            }
            //example-advanced 为table的ID
            $("#example-advanced").append(treeTR);
            $("#example-advanced").treetable({
                indent: 20,
                expandable: true,//true 默认全部隐藏 false 全部展开
                isPadding:true,
                clickableNodeNames:true
             });


            $("table[class=treetable]").each(function () {
                var _this = $(this);
                //设置偶数行和奇数行颜色
                _this.find("tr:even").css("background-color", "#f8f8f8");
                _this.find("tr:odd").css("background-color", "#f0f0f0");

                //鼠标移动隔行变色hover用法\
                _this.find("tr:not(:first)").hover(function () {
                    $(this).attr("bColor", $(this).css("background-color")).css("background-color", "#E0E0E0").css("cursor", "pointer");
                }, function () {
                    $(this).css("background-color", $(this).attr("bColor"));
                });

            });

        },
        error: function (data) {
            $('#YWaitDialog').hide();
            arrColumn = new Array();
            arrColumn[0] = "name";
            arrColumn[1] = "entNum";
            arrColumn[2] = "dimNum";
            arrColumn[3] = "dimRate";
            arrColumn[4] = "entTotalNum";
            arrColumn[5] = "dimTotalNum";
            arrColumn[6] = "dimTotalRate";
            //创建TH
            treeTR = "<tr>";
            for(var t=0;t<arrColumn.length;t++){
                if(arrColumn[t] == "entNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.entNum') + "</th>";
                 } else if(arrColumn[t] == "dimNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.dimNum') + "</th>";
                 } else if(arrColumn[t] == "dimRate"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.dimRate') + "(%)</th>";
                 } else if(arrColumn[t] == "entTotalNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.entTotalNum') + "</th>";
                 } else if(arrColumn[t] == "dimTotalNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.dimTotalNum') + "</th>";
                 } else if(arrColumn[t] == "dimTotalRate"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.dimTotalRate') + "(%)</th>";
                 } else if(arrColumn[t] == "name"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.name') + "</th>";
                 }
            }
            treeTR+="</tr>";
            $("#example-advanced").append(treeTR);
        }
    });
}



//加载树的子节点
function tree(w,bb){
    for(var j=0;j< w.length;j++){
        treeTR += "<tr data-tt-id='"+bb+'-'+j+"' data-tt-parent-id='"+bb+"'>" ;
        for(var m=0;m<arrColumn.length;m++){
            col=arrColumn[m];
            if(m == 0){
                treeTR += "<td style=\"text-align:left;\">" +w[j].parameter[col]+ "</td>";
            } else {
                treeTR += "<td style=\"text-align:center;\">" +w[j].parameter[col]+ "</td>";
            }
        }
        treeTR += "</tr>";
        if(jQuery.isArray(w[j].children)){
            cc=bb+"-"+j;
       //递归遍历所有的子节点
            tree(w[j].children,cc);
        }
    }
}
function controlePower() {
    $.ajax({
        type: "get",
        cache: false,
        async:false,
        url: contextPath + "/rest/wk-teamwork/1/gsryldState/controlPower",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success:function (data) {
            for (var k in data) {
                //TODO 没有进行控制
                /*if (data[k]) {
                    $('.'+k).css("display", "inline");
                }*/
            }
        }
    })
}
})(AJS.$, AJS.contextPath());