postModify = function postModify(att_zone) {

    const attZone = document.getElementById(att_zone);
    $.ajax({
        type: "get",
        url: "/post/files/"+$('#id').val(),
        success: function (list) {
            console.log("getList");
            list.forEach(function (item) {
                let img = document.createElement('img');
                img.setAttribute('style', 'width:100%;height:100%;z-index:none');
                img.src = item;
                attZone.appendChild(make(img, item));
            })
        }
    });

    let make = function make(img, item) {
        const div = document.createElement('div')
        div.setAttribute('style', 'display:inline-block;position:relative;'
            + 'width:150px;height:120px;margin:5px;border:1px solid #00f;z-index:1')

        const btn = document.createElement('input')
        btn.setAttribute('type', 'button')
        btn.setAttribute('value', 'x')
        btn.setAttribute('style', 'width:30px;height:30px;position:absolute;font-size:24px;'
            + 'right:0px;bottom:0px;z-index:999;background-color:rgba(255,255,255,0.1);color:#f00');
        let src = img;
        btn.onclick = function (ev) {
            const ele = ev.srcElement;
            const p = ele.parentNode;
            attZone.removeChild(p)
            $("#toDelFile").val(function(i, val) {
                return val + item + "&";
            });
        }

        div.appendChild(img)
        div.appendChild(btn)
        return div
    }
};
postModify('att_zone');