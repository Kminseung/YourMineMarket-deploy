window.resizeTo(720,550);
var widthSize = 720/2;
var heightSize = 550/2;
window.moveTo(window.screen.width/2-widthSize, window.screen.height/2-heightSize);
let ws;

function wsOpen(){
    ws = new WebSocket("ws://" + location.host + "/chatsocket/" + $('#roomId').val());
    wsEvt();
}

function wsEvt() {
    ws.onopen = function(data){
        //소켓이 열리면 동작
    }

    ws.onmessage = function(data) {
        //메시지를 받으면 동작
        var msg = data.data;

        var chatContainer = document.getElementById("chatBoxTheme");
        var chatContainerMessage = chatContainer.getElementsByClassName("chatting")[0];

        if(msg != null && msg.trim() != ''){
            var d = JSON.parse(msg);
            var sendTime = chatTime(d.fulTime);
            if(d.type == "getId"){
                var si = d.sessionId != null ? d.sessionId : "";
                if(si != ''){
                    $("#sessionId").val(si);
                }
            }else if(d.type == "message"){
                if(d.sessionId == $("#sessionId").val()){
                    $("#chatting").append("<div class='me'><div class='b'></div><div class='a'><p class='me'>" + d.msg + "</p></div><div class='time'>" + sendTime + "</div></div>");
                }else{
                    $("#chatting").append("<div class='others'><div class='box'><div class='profile_name'>" + d.userName + "</div><div class='a'></div><div class='b'><p class='others'>" + d.msg + "</p></div><div class='time'>" + sendTime + "</div></div></div>");
                }

            }else{
                console.warn("unknown type!")
            }
        }

        chatContainerMessage.scrollTop = chatContainerMessage.scrollHeight;
    }

    document.addEventListener("keypress", function(e){
        if(e.keyCode == 13){ //enter press
            send();
        }
    });
}
function send() {
    // Setting a sending time
    var fulTime = new Date();

    var data = {
        type: "message",
        roomId: $("#roomId").val(),
        sessionId : $("#sessionId").val(),
        userName : $("#userName").val(),
        msg : $("#message").val(),
        listener: $('#listener').val(),
        fulTime: fulTime
    }
    ws.send(JSON.stringify(data));
    var chatDB = {
        speaker: data.userName,
        listener: data.listener,
        content: data.msg,
        roomId: data.roomId,
        fulTime: data.fulTime
    }
    $.ajax({
        type: 'POST',
        url: "/chat/db",
        contentType: 'application/json; charset=UTF-8',
        data: JSON.stringify(chatDB)
    }).done(function () {
        console.log("dbIn");
    }).fail(function (error) {
        console.error(error);
    });
    $('#message').val("");
}
function textLoad() {
    var now = $('#status').val();
    if (now === "거래완료") {
        $('#statusDropdown').attr("disabled", true);
    }

    var roomId = $("#roomId").val();
    var userName = $("#userName").val();

    readCheck();
    $.ajax({
        url: "/chat/db/demand/"+roomId,
        type: "get",
        contentType: 'application/json; charset=UTF-8',
        success: function(result) {
            var obj = JSON.parse(result);
            obj.forEach(function (item) {
                if (item.speaker === userName) {
                    $("#chatting").append("<div class='me'><div class='b'></div><div class='a'><p class='me'>" + item.content + "</p></div><div class='time'>" + LoadChatTime(item.fulTime) + "</div></div>");
                } else {
                    $("#chatting").append("<div class='others'><div class='box'><div class='profile_name'>" + item.speaker + "</div><div class='a'></div><div class='b'><p class='others'>" + item.content + "</p></div><div class='time'>" + LoadChatTime(item.fulTime) + "</div></div><div>");
                }
            })
        },
        error: function(error) {
            console.error(error);
        }
    });
}

function setStandby() {
    var now = $('#status').val();
    if (now === "거래대기") {
    } else {
        var check = confirm("거래 상태를 \"거래 대기\"으로 변경하시겠습니까?");
        if (check) {
            $('#statusDropdown').val("거래 대기");
            $('#status').val("거래대기");
            putStatus("거래대기");
        }
    }
}

function setProgress() {
    var now = $('#status').val();
    if (now === "거래중") {
    } else {
        var check = confirm("거래 상태를 \"거래 중\"으로 변경하시겠습니까?");
        if (check) {
            $('#statusDropdown').val("거래 중");
            $('#status').val("거래중");
            putStatus("거래중");
        }
    }
}

function setDone() {
    var now = $('#status').val();
    if (now === "거래완료") {
    } else {
        var check = confirm("\"거래 완료\"상태로 변경하면 다시 거래 상태변경이 불가능합니다. 정말 바꾸시겠습니까?");
        if (check) {
            $('#statusDropdown').val("거래 완료");
            $('#status').val("거래완료");
            putStatus("거래완료");
            $('#statusDropdown').attr("disabled", true);
        }
    }
}

function putStatus(item) {
    var no = $('#postNo').val();
    var data = {
        seller: $('#userName').val(),
        roomId: $('#roomId').val()
    }
    $.ajax({
        type: 'put',
        url: "/post/status/"+no+"/"+item,
        contentType: 'application/json; charset=UTF-8',
        data: JSON.stringify(data)
    }).done(function () {
        console.log("done");
    }).fail(function (error) {
        console.error(error);
    });
}

function readCheck() {
    var data = {
        roomId: $('#roomId').val(),
        userName: $("#userName").val()
    }
    $.ajax({
        type: 'put',
        url: "/chat/db/check",
        contentType: 'application/json; charset=UTF-8',
        data: JSON.stringify(data)
    }).done(function () {
        $('#redDot').empty();
        console.log("read check");
    }).fail(function (error) {
        console.error(error);
    });
}

function chatOut() {
    var data = {
        identify: $('#roomId').val(),
        position: $('#position').val()
    }
    $.ajax({
        url: '/chatOut',
        type: 'put',
        contentType: 'application/json; charset=UTF-8',
        data: JSON.stringify(data)
    }).done(function () {
        window.close();
    }).fail(function (error) {
        console.error(error);
    });
}
function chatTime(time) {
    var now = new Date();
    var fulTime = new Date(time);

    if (now.getDate() === fulTime.getDate()) {
        if (now.getMonth() === fulTime.getMonth()) {
            if (now.getFullYear() === fulTime.getFullYear()) {
                var ampm = (fulTime.getHours()>12 ?  "PM" : "AM");
                var hour = (fulTime.getHours()>12 ? fulTime.getHours()-12 : fulTime.getHours());
                var min = (fulTime.getMinutes()>9 ? fulTime.getMinutes() : "0" + fulTime.getMinutes());
                return ampm + " " + hour + ":" + min;
            }
        }
    } else {
        return getDateFormat(fulTime);
    }
}

function LoadChatTime(time) {
    var now = new Date();
    if (now.getDate() === time.date.day) {
        if ((now.getMonth()+1) === time.date.month) {
            if (now.getFullYear() === time.date.year) {
                var korHour = time.time.hour + 9;
                if(korHour > 23) { korHour -= 24; }

                var ampm = (korHour>12 ?  "PM" : "AM");
                var hour = (korHour>12 ? korHour-12 : korHour);
                var min = (time.time.minute>9 ? time.time.minute : "0" + time.time.minute);
                return ampm + " " + hour + ":" + min;
            }
        }
    } else {
        var month = (1+ time.date.month);
        var day = time.date.day;
        month = month >= 10 ? month : '0' + month;
        day = day >= 10 ? day : '0' + day;
        return month + '월 ' + day + '일';
    }
}

function getDateFormat(time) {
    var month = (1+ time.getMonth());
    var day = time.getDate();
    month = month >= 10 ? month : '0' + month;
    day = day >= 10 ? day : '0' + day;
    return month + '월 ' + day + '일';
}

wsOpen();
textLoad();
