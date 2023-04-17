

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