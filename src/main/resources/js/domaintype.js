(function ($, contextPath) {
    $(function () {
    $.ajax({
        type: "GET",
        cache:false,
        url: contextPath + "/rest/wk-teamwork/latest/info/kindNum",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (Data) {
            var html = "";
            var dicectoryIds = new Array();

            for (var key in Data) {
                var directoryId = Data[key]['id'];
                var name = Data[key]['name'];
                var ip = Data[key]['ip'];
                var exit = Data[key]['exit'];
                var time = Data[key]['time'];
                var substring = Data[key]['substring'];
                var directoryType = Data[key]['directoryType'];
                dicectoryIds.push(directoryId);
                var changeName=null;
                if (exit){
                    changeName=AJS.I18n.getText("workorg.property.sync.pause")
                }else {
                    changeName=AJS.I18n.getText("workorg.property.sync.start")
                }
                html += "<tr>"
                html += "<td style='text-align: center'>" + key + "</td>" + "<td style='text-align: center'>" + name + "</td>"+"<td style='text-align: center'>" + directoryType + "</td>" + "<td style='text-align: center'>" + ip + "</td>" +"<td style='text-align: center'><input id='"+directoryId+9+"' size='3' value='"+substring+"'readonly style='border: none'></td>"+ "<td style='text-align: center'><input id='"+directoryId+"' value='" + time+ "' size='5'>"+AJS.I18n.getText("workorg.property.sync.unit")+"</td>"+ "<td style='text-align: center'><button class='aui-button' style='display: none' id='id" + directoryId + "' value='" + directoryId + "'>"+AJS.I18n.getText("workorg.property.sync.change")+"</button><button style='display: none' class='aui-button' id='id"+directoryId+"pause"+"'>"+changeName+"</button><button style='display: none' class='aui-button' id='id" + directoryId +"sync"+ "'>"+AJS.I18n.getText("workorg.property.sync.sync")+"</button></td>"
                html += "</tr>";
            }
            $("#tbody").append(html);
            controlePower();
            getStatus();
            for (num in dicectoryIds) {
                var dicectoryId = dicectoryIds[num];
                (function (dicectoryId) {
                    var flag=0;
                    $("#id" + dicectoryId).click(function () {
                        var newtime = $("#" + dicectoryId).val();
                        $.ajax({
                            type: "GET",
                            async: false,
                            cache: false,
                            url: contextPath + "/rest/wk-teamwork/latest/info/changeTimer/" + dicectoryId + "/" + newtime,
                            contentType: "application/json; charset=utf-8",
                            dataType: "json",
                            success: function (Data) {
                                $("#"+directoryId+9).val(Data['delayTime']);
                                $("#id" + dicectoryId + "pause").text(AJS.I18n.getText("workorg.property.sync.pause"));
                                alert(AJS.I18n.getText("workorg.property.sync.setsuccess"))
                            }
                        })
                    }),
                        $("#id" + dicectoryId + "sync").click(function () {
                            $.ajax({
                                type: "POST",
                                async: false,
                                cache: false,
                                url: contextPath + "/rest/wk-teamwork/latest/info/sync/" + dicectoryId,
                                contentType: "application/json; charset=utf-8",
                                dataType: "json",
                                success: function () {
                                    alert(AJS.I18n.getText("workorg.property.sync.syncover"))
                                }
                            })
                        }),
                        $("#id" + dicectoryId + "pause").click(function () {
                            //alert($("#id" + dicectoryId + "pause").text()+"1111111111"); ---TEST
                            if(flag==0){
                                $.ajax({
                                    type: "POST",
                                    async: false,
                                    cache: false,
                                    url: contextPath + "/rest/wk-teamwork/latest/info/pause/" + dicectoryId,
                                    contentType: "application/json; charset=utf-8",
                                    dataType: "json",
                                    success: function () {
                                        alert(AJS.I18n.getText("workorg.property.sync.syncpaused"))
                                    }
                                })
                                $("#id" + dicectoryId + "pause").text(AJS.I18n.getText("workorg.property.sync.start"));
                                flag=1;
                            }else {
                                $.ajax({
                                    type: "POST",
                                    async: false,
                                    cache: false,
                                    url: contextPath + "/rest/wk-teamwork/latest/info/start/" + dicectoryId,
                                    contentType: "application/json; charset=utf-8",
                                    dataType: "json",
                                    success: function (Da) {
                                        $("#"+directoryId+9).val(Da['delayTime']);
                                        alert(AJS.I18n.getText("workorg.property.sync.syncstart"))
                                    }
                                })
                                $("#id" + dicectoryId + "pause").text(AJS.I18n.getText("workorg.property.sync.pause"));
                                flag=0;
                            }
                        })
                })(dicectoryId);
            }
        }
    })
    function pauseSync () {
        alert("pauseSync")
    }
    function syncBySelf() {
        alert("syncByself")
    }
    setInterval(getStatus,5*1000)
    function getStatus() {
        $.ajax({
            type: "POST",
            async: true,
            cache: false,
            url: contextPath + "/rest/wk-teamwork/latest/info/getStatus/",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (ActiveDate) {
                for (var num in ActiveDate){
                    var activeDate1 = ActiveDate['1'][num];
                    var activeDate2 = ActiveDate['2'][num];
                    $("#id" + activeDate1).prop('disabled', true);
                    $("#id" + activeDate1 + "pause").prop('disabled', true);
                    $("#id" + activeDate1 + "sync").prop('disabled', true);
                    $("#id" + activeDate2).prop('disabled', false);
                    $("#id" + activeDate2+ "pause").prop('disabled', false);
                    $("#id" + activeDate2 + "sync").prop('disabled', false);
                }
               // alert("获取状态"+ActiveDate["noActiveID"])
            }
        })
    }
    })
    function controlePower() {
        $.ajax({
            type: "get",
            cache: false,
            async:false,
            url: contextPath + "/rest/wk-teamwork/latest/info/controlPower",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success:function (data) {
                var ids=new Array();
                $.ajax({
                    type: "get",
                    cache: false,
                    async:false,
                    url: contextPath + "/rest/wk-teamwork/latest/info/getDirectoryId",
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    success:function (data) {
                        ids= data;
                    },
                    error:function (data) {

                    }
                })
                for (var i =0;i<ids.length;i++){
                    if (ids[i] != 1){
                        for (var k in data) {
                            if (k == 'sync'){
                            if (data[k]) {
                                $('#id'+ids[i]+'sync').css("display", "inline");
                            }
                            }
                            if (k == 'pause'){
                                if (data[k]) {
                                    $('#id'+ids[i]+'pause').css("display", "inline");
                                }
                            }
                            if (k == 'edited'){
                                if (data[k]) {
                                    $('#id'+ids[i]).css("display", "inline");
                                }
                            }
                        }
                    }
                }
            }
        })
    }
})(AJS.$,AJS.contextPath());