$(function () {
    setTimeout(function () {
        getInfo("sub1");
        getInfo("sub2");
        getInfo("sub3");

        AJS.tabs.setup()
    }, 200)
})

function getInfo(datas) {
    $("#" + datas).find("a").on("click", {datas: datas}, getInfoDetail)
}

function getInfoDetail(event) {
    var datasss = event.data.datas;
    var contextpath = AJS.contextPath();
    var valss = $(this).find("input:hidden").val();
    var n1 = valss.indexOf("_")
    var n2 = valss.lastIndexOf("m")
    var s2 = valss.substring((Number(n1) + 1), Number(n2 + 1));
    dw.ajax({
        type: "get",
        url: contextpath + "/rest/wk-teamwork/latest/issue/" + valss,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (data) {
            for (var k in data) {
                if (k == "supName") {
                    var idk = "supervisor";
                    $("." + k + datasss + s2 + data["id"]).html(data[k] + '<input hidden value="emp_subm' + data[idk] + '">');
                } else {
                    $("." + k + datasss + s2 + data["id"]).html(data[k]);
                }
            }
        }
    })
}
function dw() {
}
dw.ajax = function (s) {
    jQuery.ajax(s);
}
function doClick(o) {
//当前被中的对象设置css
    o.className = "active";
    var j;
    var id;
    var e;
//遍历所有的页签，没有被选中的设置其没有被选中的css
    for (var i = 1; i <= 7; i++) { //i<7 多少个栏目就填多大值
        id = "nav" + i;
        j = document.getElementById(id);
        e = document.getElementById("sub" + i);
        if (id != o.id) {
            j.className = "active11";
            e.style.display = "none";
        } else {
            e.style.display = "block";
        }
    }
}

function SetCwinHeight(obj) {

    var iframeid = obj;//document.getElementById("iframeid"); //iframe id
    if (document.getElementById) {
        if (iframeid && !window.opera) {
            if (iframeid.contentDocument && iframeid.contentDocument.body.offsetHeight) {
                if (iframeid.contentDocument.body.offsetHeight + 125 > iframeid.height)
                    iframeid.height = iframeid.contentDocument.body.offsetHeight + 125;
            } else if (iframeid.Document && iframeid.Document.body.scrollHeight) {
                if (iframeid.Document.body.scrollHeight + 125 > iframeid.height)
                    iframeid.height = iframeid.Document.body.scrollHeight + 125;
            }

        }
    }
}

