window.onload = getBmryfbData;

var treeTR;
var JsonOBJ,arrColumn;
var col;
var cc;
function getBmryfbData(){
    $('#YWaitDialog').show();
    $.ajax({
        type: "get",
        url: AJS.contextPath() + "/rest/wk-teamwork/1/bmryfbState/bmrygz",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (data) {
        $('#YWaitDialog').hide();
            arrColumn = new Array();
            arrColumn[0] = "name";
            arrColumn[1] = "proNum";
            arrColumn[2] = "undoNum";
            arrColumn[3] = "doingNum";
            arrColumn[4] = "doneNum";
            arrColumn[5] = "otherNum";
            arrColumn[6] = "details";

            //创建TH
            treeTR = "<tr>";
            for(var t=0;t<arrColumn.length;t++){
                if(arrColumn[t] == "name"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.name') + "</th>";
                } else if(arrColumn[t] == "proNum"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmryfb.proNum') + "</th>";
                } else if(arrColumn[t] == "undoNum"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmrygz.undoNum') + "</th>";
                } else if(arrColumn[t] == "doingNum"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmrygz.doingNum') + "</th>";
                } else if(arrColumn[t] == "doneNum"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmrygz.doneNum') + "</th>";
                } else if(arrColumn[t] == "otherNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmrygz.otherNum') + "</th>";
                } else if(arrColumn[t] == "details"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmryfb.details') + "</th>";
                }
            }
            treeTR+="</tr>";
           //创建底层节点
            for(var i=0;i< data.length;i++){
                treeTR += "<tr id='"+i+"' data-tt-id='"+i+"'>" ;
                    for(var x=0;x<arrColumn.length;x++){
                        col = arrColumn[x];
                        if(x == 0){
                            treeTR +="<td style=\"text-align:left;\">" + data[i].name + "</td>";
                        } else if(x == 6){
                            treeTR +="<td style=\"text-align:center;\"><input type='button' class='detail1' style='display: none' onclick='addRow(this,"+data[i].type+","+data[i].pid+")' id=\""+ i +"\" value='"+AJS.I18n.getText('workorg.property.bmryfb.tj_Details')+ "'/></td>";
                        } else {
                            treeTR +="<td style=\"text-align:center;\">" + data[i].bmryfb[col] + "</td>";
                        }
                    }
                treeTR += "</tr>";
                cc= i;
                tree(data[i].childrens,cc);

            }
            //example-advanced 为table的ID
            $("#example-advanced").append(treeTR);
            controlePower();
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

                //鼠标移动隔行变色hover用法
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
            arrColumn[1] = "proNum";
            arrColumn[2] = "undoNum";
            arrColumn[3] = "doingNum";
            arrColumn[4] = "doneNum";
            arrColumn[5] = "otherNum";
            arrColumn[6] = "details";
            //创建TH
            treeTR = "<tr>";
            for(var t=0;t<arrColumn.length;t++){
                if(arrColumn[t] == "name"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.gsryld.name') + "</th>";
                } else if(arrColumn[t] == "proNum"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmryfb.proNum') + "</th>";
                } else if(arrColumn[t] == "undoNum"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmrygz.undoNum') + "</th>";
                } else if(arrColumn[t] == "doingNum"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmrygz.doingNum') + "</th>";
                } else if(arrColumn[t] == "doneNum"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmrygz.doneNum') + "</th>";
                } else if(arrColumn[t] == "otherNum"){
                    treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmrygz.otherNum') + "</th>";
                } else if(arrColumn[t] == "details"){
                   treeTR = treeTR + "<th>" + AJS.I18n.getText('workorg.property.bmryfb.details') + "</th>";
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
        treeTR += "<tr id='"+bb+'-'+j+"' data-tt-id='"+bb+'-'+j+"' data-tt-parent-id='"+bb+"'>" ;
        for(var m=0;m<arrColumn.length;m++){
            col=arrColumn[m];
            if(m == 0){
                treeTR += "<td style=\"text-align:left;\">" +w[j].name+ "</td>";
            } else if(m == 6){
                treeTR +="<td style=\"text-align:center;\"><input type='button' class='detail1' style='display: none' onclick='addRow(this,"+w[j].type+","+w[j].pid+")' id=\""+ bb+'-'+j +"\" value='"+AJS.I18n.getText('workorg.property.bmryfb.mx_Details')+ "'/></td>";
            } else {
                treeTR += "<td style=\"text-align:center;\">" +w[j].bmryfb[col]+ "</td>";
            }
        }
        treeTR += "</tr>";
        if(jQuery.isArray(w[j].childrens)){
            cc=bb+"-"+j;
       //递归遍历所有的子节点
            tree(w[j].childrens,cc);
        }

    }
    controlePower();
}


var temp = "";
var count = 0;
function addRow(but,type,pid){
    var classname = 'hid'+but.id;
    if(temp == classname){
        if(count%2 == 0){
            showDetail(but,type,pid);
        } else {
            deletetr();
        }
        count = count + 1;
    } else {
        deletetr();
        showDetail(but,type,pid);
        temp = classname;
        count = 1;
    }
}

function showDetail(but,type,pid){
    $.ajax({
        type: "get",
        url: AJS.contextPath() + "/rest/wk-teamwork/1/bmryfbState/?a=bmrygz&b="+type+"&c="+pid,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (data) {
            var trstr ="";
            if(data.length == 0){
                trstr +="<tr class='del'><td colspan='6' style=\"text-align:center;color:red;\">" + AJS.I18n.getText('workorg.property.bmryfb.noDatas') + "</td></tr>";
            } else {
                for(var x=0;x<data.length;x++){
                    trstr += "<tr class='del' style=\"text-align:center;\"><td></td><td>"+data[x].proName+"</td><td>"+data[x].issName+"</td><td>"+data[x].repName+"</td><td>"+data[x].repName1+"</td><td></td></tr>";
                }
            }
            var addtr = $("#"+but.id);
            $(trstr).insertAfter(addtr);
        },
        error: function (data) {

        }
    });
};

//删除所有已展开的信息行
function deletetr(){
    $('#example-advanced tr.del').remove();
};
function controlePower() {
    $.ajax({
        type: "get",
        cache: false,
        async:false,
        url: contextPath + "/rest/wk-teamwork/latest/bmryfbState/getPower",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success:function (data) {
            for (var k in data) {
                if (data[k]) {
                    $('.'+k).css("display", "inline");
                }
            }
        }
    })
}