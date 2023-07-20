$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        context_path + "/like", // 访问路径
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId}, // 传给controller的参数
        function(data) { //controller返回来的值
            //把data字符串转变成json格式
            data = $.parseJSON(data);
            if(data.code == 0) {
                //改变赞数
                $(btn).children("i").text(data.likeCount);
                //改变点赞状态
                $(btn).children("b").text(
                    data.likeStatus == 1 ? '已赞' : '赞'
                );
            }else { //点赞不成功的时候以后统一处理
                alert(data.msg);
            }

        }
    )
}

// 置顶
function setTop() {
    $.post(
        context_path + "/discuss/top",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                //置顶后，设置置顶按钮不可用
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    vv = $("#postId").val()
    $.post(
        context_path + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            data = JSON.parse(data);
            if(data.code == 0) {
                //加精后，设置加精按钮不可用
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        context_path + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                //删除后，重定向到index页面
                location.href = context_path + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}