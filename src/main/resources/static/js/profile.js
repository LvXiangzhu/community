$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			context_path + "/follow",
			{"entityType":3, "entityId":$(btn).prev().val()}, // $(btn).prev().val()：取该按钮前一个节点（entityId）的值
			function(data) {
				data = $.parseJSON(data);
				if(data.code == 0) {
					window.location.reload(); // 刷新页面
				}else {
					alert(data.msg);
				}
			}
		)
		// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.post(
			context_path + "/unfollow",
			{"entityType":3, "entityId":$(btn).prev().val()}, // $(btn).prev().val()：取该按钮前一个节点（entityId）的值
			function(data) {
				data = $.parseJSON(data);
				if(data.code == 0) {
					window.location.reload();
				}else {
					alert(data.msg);
				}
			}
		)
		// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}