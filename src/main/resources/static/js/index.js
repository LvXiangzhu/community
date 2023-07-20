$(function(){
	$("#publishBtn").click(publish); //点击publishBtn后，会调用publish函数
});

function publish() {
	$("#publishModal").modal("hide"); //隐藏发布框

	// 发送AJAX请求之前,将CSRF令牌设置到请求的消息头中.
   // var token = $("meta[name='_csrf']").attr("content");
   // var header = $("meta[name='_csrf_header']").attr("content");
   // $(document).ajaxSend(function(e, xhr, options){
   //     xhr.setRequestHeader(header, token);
   // });

	//获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		context_path + "/discuss/add", //请求路径
		{"title":title, "content":content}, //传给controller的参数
		function(data) { //controller的返回值 data
			data = $.parseJSON(data);
			//在提示框中显示返回的消息
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//过2s后隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//如果添加成功，刷新页面
				if(data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	)


}