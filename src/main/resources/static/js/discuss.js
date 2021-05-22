$(function(){
   $("#topBtn").click(setTop);
   $("#wonderfulBtn").click(setWonderful);
   $("#deleteBtn").click(setDelete);
   $("#section").change(setSubject);
   $(".collect-btn").click(collect);
});

function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId, "entityUserId":entityUserId, "postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
            } else{
                alert(data.msg);
            }
        }
    );
}

//置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    )
}

//加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    )
}

//删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    )
}


//分区
function setSubject() {

    var section = $("#section").val();
    var id = $("#postId").val();
    $.post(
        CONTEXT_PATH + "/discuss/section",
        {"id":id,"section":section},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                window.location.reload();
            } else {
                alert(data.msg);
            }
        }
    )
}


function collect() {
    var btn = this;
    if($(btn).hasClass("btn-info")) {
        // 关注TA
        $.post(
            CONTEXT_PATH + "/follow",
            {"entityType":1,"entityId":$(btn).prev().val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
        // $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
    } else {
        // 取消关注
        $.post(
            CONTEXT_PATH + "/unfollow",
            {"entityType":1,"entityId":$(btn).prev().val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );

        // $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
    }
}

