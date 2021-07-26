const offcanvasElementList = [].slice.call(document.querySelectorAll('.offcanvas'))
const offcanvasList = offcanvasElementList.map(function (offcanvasEl) {
    return new bootstrap.Offcanvas(offcanvasEl)
})

const token = $("meta[name='_csrf']").attr("content");
const header = $("meta[name='_csrf_header']").attr("content");
$(function () {
    $(document).ajaxSend(function (e, xhr, option) {
        xhr.setRequestHeader(header, token);
    });
});



let search = function search() {
    window.location.href="/request/search/0/" + $('#keyword').val();
}
let getter = function getter() {
    const data = {
        kind: $('#kind').val(),
        value: $('#value').val(),
        cursor: $('#cursor').val(),
        id: $('#id').val()
    }
    $.ajax({
        url: "/request/list",
        type: "post",
        contentType: 'application/json; charset=UTF-8',
        data: JSON.stringify(data),
        success: function(result) {
            $("#cursor").val(function(i, val) {
                return val + 1;
            });
            let obj = JSON.parse(result);
            let i = 1;
            obj.forEach(function (item) {
                $("#board").append("<div class=\"col mb-5\">\n" +
                    "                        <div class=\"card h-100\">\n" +
                    "                            <a href=\"/posts/" + item.id + "\">\n" +
                    "                                <img src=\"" + item.thumbnail + "\" class=\"card-img-top\" alt=\"" + item.title + "\" width=\"200px\" height=\"200px\">\n" +
                    "                            </a>\n" +
                    "                            <div class=\"card-body p-4\">\n" +
                    "                                <div class=\"text-center\">\n" +
                    "                                    <h5 class=\"fw-bolder\">" + item.title + "</h5>\n" +
                    "                                    <p>" + item.price + "원</p>\n" +
                    "                                    <p>" + item.status + "</p>\n" +
                    "                                </div>\n" +
                    "                            </div>\n" +
                    "                        </div>\n" +
                    "                    </div>");
                i++;
                if (result.length === i) {
                    $('#id').val(item.id);
                }
            })
        },
        error: function(error) {
            console.error(error);
        }
    });
}

function alarm() {
    const alarm = $('#alarm').val();
    if (alarm !== null) {
        $.ajax({
            url: "/alarm/" + alarm,
            type: "get",
            contentType: 'application/json; charset=UTF-8',
            success: function (result) {
                if (result) {
                    toastr.remove();
                    toastr.options.onclick = function () {
                        document.getElementById("offcanvas").click();
                        toastr.remove();
                    }
                    toastr.info('읽지않은 메세지가 있습니다', {timeOut: 3000});
                }
            },
            error: function (error) {
                console.error(error);
            }
        });
    }
}

function getMessage() {
    const id = $('#alarm').val();
    $('#messageBox').empty();
    $.ajax({
        url: "/chat/db/list/"+id,
        type: "get",
        contentType: 'application/json; charset=UTF-8',
        success: function(result) {
            $("#cursor").val(function(i, val) {
                return val + 1;
            });
            let obj = JSON.parse(result);
            obj.forEach(function (item) {
                if (item.newChatCount === 0) {
                    $('#messageBox').append(
                        "           <div class=\"out-box\" onclick=\"closeOff(\'"+ item.url1 + "\'," + "\'"+ item.url2 + "\')\">\n" +
                        "                <img src=\"" + item.profile + "\">\n" +
                        "                <div class=\"content\">\n" +
                        "                    <span class=\"mes-title\">" + item.title + "</span>\n" +
                        "                    <span class=\"mes-time\">" + dateFormat(item.lastTime) + "</span>\n" +
                        "                    <div class=\"sub\">" + item.lastChat + "</div>\n" +
                        "                </div>\n" +
                        "            </div>" +
                        "          </a>"
                    );
                } else {
                    let count = item.newChatCount;
                    if (count > 99) {
                        count = 99;
                    }
                    $('#messageBox').append(
                        "              <div class=\"out-box new-chat\" onclick=\"closeOff(\'"+ item.url1 + "\'," + "\'"+ item.url2 + "\')\">\n" +
                        "                <img src=\"" + item.profile + "\">\n" +
                        "                <div class=\"content\">\n" +
                        "                    <span class=\"mes-title\">" + item.title + "</span>\n" +
                        "                    <span class=\"mes-time\">" + dateFormat(item.lastTime) + "</span>\n" +
                        "                    <div class=\"sub\">" + item.lastChat + "</div>\n" +
                        "                    <div class=\"mes-new\">" + count + "</div>" +
                        "                </div>\n" +
                        "            </div>" +
                        "          </a>"
                    );
                }
            })
        },
        error: function(error) {
            console.error(error);
        }
    });
}
alarm();
function closeOff(url1, url2) {
    document.getElementById("offcanvas").click();
    window.open("/chat/" + url1 + "/" + url2, "", "_blank");
}
function chatOpen(url1, url2) {
    window.open("/chat/" + url1 + "/" + url2, "", "_blank");
}
function logout() {
    const form = $('#logoutForm');
    form.submit();
}
function dateFormat(date) {
    const now = new Date();
    const time = new Date(date);
    if (now.getDate() === time.getDate()) {
        if ((now.getMonth()+1) === (time.getMonth()+1)) {
            if (now.getFullYear() === time.getFullYear()) {
                return chatTime(time);
            }
        }
    } else {
        let month = (time.getMonth()+1);
        let day = time.getDay();
        month = month >= 10 ? month : '0' + month;
        day = day >= 10 ? day : '0' + day;
        return month + '월 ' + day + '일';
    }
}
function chatTime(time) {
    const fulTime = new Date(time);

    const ampm = (fulTime.getHours()>12 ?  "PM" : "AM");
    const hour = (fulTime.getHours()>12 ? fulTime.getHours()-12 : fulTime.getHours());
    const min = (fulTime.getMinutes()>9 ? fulTime.getMinutes() : "0" + fulTime.getMinutes());
    return ampm + " " + hour + ":" + min;
}