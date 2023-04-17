$(function(){
	$("#sendBtn").click(send_letter); //点击发送按钮时调用send_letter函数
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide"); //隐藏弹出框

	//代码逻辑
	var toName = $("#recipient-name").val(); //这是letter.html里弹出框中对应的姓名输入框id，取框里的值
	var content = $("#message-text").val();
	$.post(
		context_path + "/message/letter/send",
		{"toName":toName, "content":content},
		function(data) {
			data = $.parseJSON(data); //把data转换为Json格式
			if(data.code == 0) { //data.code为0表示发送成功
				$("#hintBody").text("发送成功！"); //hintBody是提示框id，里面写发送成功
			}else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show"); //显示提示框
			setTimeout(function(){ //2s之后提示框消失
				$("#hintModal").modal("hide");
				location.reload(); //刷新当前页面
			}, 2000);
		}
	)


}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}