const idTest = /^[a-zA-z](?=.*[0-9]{1,16}).{7,16}/
const pwTest = /(?=.*\d{1,30})(?=.*[~`!@#$%\^&*()-+=]{1,30})(?=.*[a-zA-Z]{1,30}).{8,30}$/;
const numTest = /^\d{3}-\d{3,4}-\d{4}$/;
const account = {
    init: function () {
        const func = this;
        $('#btn-signup').on('click', function () {
            func.save();
        });

        $('#btn-idCheck').on('click', function () {
            func.idCheck();
        });

        $('#btn-postcode').on('click', function () {
            func.postcode();
        });

        $('#btn-userModify').on('click', function () {
            func.userModify();
        });

        $('#btn-passwordModify').on('click', function () {
            func.passwordModify();
        });

        $('#btn-findId').on('click', function () {
            func.findId();
        });

        $('#btn-leave').on('click', function () {
            func.leave();
        });

        $('#btn-email').on('click', function () {
            func.email();
        });

        $('#btn-emailCheck').on('click', function () {
            func.emailCheck();
        });

        $('#btn-sendCodeById').on('click', function () {
            func.sendCodeById();
        });

        $('#btn-pwEmailCheck').on('click', function () {
            func.pwEmailCheck();
        });

        $('#id').change(function () {
            $('#idCheck').val("f");
        });

        $('#email').change(function () {
            $('#mailCheck').val("f");
        });
    },

    save: function () {
        const data = {
            name: $('#name').val(),
            id: $('#id').val(),
            password: $('#password').val(),
            phone: $('#phone').val(),
            address: $('#address').val(),
            detailAddress: $('#detailAddress').val(),
            email: $('#email').val() + "@" + $('#server').val()
        };
        if ($('#idCheck').val() != "t"){
            alert("아이디 중복 확인을 해주세요");
            return;
        }
        if (data.password == "") {
            alert("비밀번호를 입력해주세요");
            return;
        }
        if(!pwTest.test(data.password)) {
            alert("비밀번호는 영문, 숫자, 특수문자를 1회이상 사용한 8자리 이상이여야합니다");
            return;
        }
        if (data.password != $('#password2').val()) {
            alert("비밀번호가 일치하지 않습니다");
            return;
        }
        if (data.name == "") {
            alert("이름을 입력해주세요");
            return;
        }
//        if ($('#mailCheck').val() != "t"){
//            console.log($('#mailCheck').val())
//            alert("이메일 인증을 해주세요");
//            return;
//        }
        if (data.address == "") {
            alert("주소를 입력해주세요");
            return;
        }
        if (data.detailAddress == "") {
            alert("상세주소를 입력해주세요");
            return;
        }
        if (data.phone == "") {
            alert("휴대폰번호를 입력해주세요");
            return;
        }
        if(!numTest.test(data.phone)) {
            alert("휴대폰번호 형식이 잘못되었습니다");
            return;
        }

        $.ajax({
            type: 'POST',
            url: "/signup",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data),
        }).done(function () {
            alert('회원가입 되었습니다');
            window.location.href = "/";
        }).fail(function (error) {
            alert('가입 실패');
        });
    },

    idCheck: function () {
        const data = {
            id: $('#id').val()
        };
        if ($('#id').val().length == 0) {
            var con = "<p>ID를 입력해주세요</p>";
            $('#msg').empty();
            $('#msg').append(con);
            return;
        }
        if(!idTest.test(data.id)) {
            alert("아이디는 영어로 시작해 숫자를 혼합한 8~16자리여야합니다");
            return;
        }
        $.ajax({
            url: "/idCheck",
            type: "Post",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data),
            success: function(result) {
                if ( result.data != true) {
                    $('#idCheck').val("f");
                    var con = "<p style=\"color:red;\">사용할 수 없는 ID입니다</p>";
                    $('#msg').empty();
                    $('#msg').append(con);
                    alert("이미 가입된 ID입니다");
                } else {
                    $('#idCheck').val("t");
                    var con = "<p style=\"font-size: 1em;\">사용 가능합니다</p>";
                    $('#msg').empty();
                    $('#msg').append(con);
                    alert("가입하실수 있는 ID입니다");
                }
            },
            error: function(error) {
                console.error(error);
                $('#idCheck').val("f");
            }
        });
    },

    postcode: function () {
        new daum.Postcode({
            oncomplete: function(data) {
                // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.

                // 각 주소의 노출 규칙에 따라 주소를 조합한다.
                // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
                let addr = ''; // 주소 변수
                let extraAddr = ''; // 참고항목 변수

                //사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
                if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
                    addr = data.roadAddress;
                } else { // 사용자가 지번 주소를 선택했을 경우(J)
                    addr = data.jibunAddress;
                }

                // 사용자가 선택한 주소가 도로명 타입일때 참고항목을 조합한다.
                if(data.userSelectedType === 'R'){
                    // 법정동명이 있을 경우 추가한다. (법정리는 제외)
                    // 법정동의 경우 마지막 문자가 "동/로/가"로 끝난다.
                    if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                        extraAddr += data.bname;
                    }
                    // 건물명이 있고, 공동주택일 경우 추가한다.
                    if(data.buildingName !== '' && data.apartment === 'Y'){
                        extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                    }
                    // 표시할 참고항목이 있을 경우, 괄호까지 추가한 최종 문자열을 만든다.
                    if(extraAddr !== ''){
                        extraAddr = ' (' + extraAddr + ')';
                    }
                    // 조합된 참고항목을 해당 필드에 넣는다.
                    document.getElementById("extraAddress").value = extraAddr;

                } else {
                    document.getElementById("extraAddress").value = '';
                }

                // 우편번호와 주소 정보를 해당 필드에 넣는다.
                document.getElementById('postcode').value = data.zonecode;
                document.getElementById("address").value = addr;
                // 커서를 상세주소 필드로 이동한다.
                document.getElementById("detailAddress").focus();
            }
        }).open();
    },

    userModify: function () {
        if ($('#name').val() == "") {
            alert("이름을 입력해주세요");
            return;
        }
        if ($('#address').val() == "") {
            alert("개인정보를 변경하기위해서는 주소를 입력하여야 합니다");
            return;
        }
        if ($('#detailAddress').val() == "") {
            alert("상세주소를 입력해주세요");
            return;
        }
        if ($('#phone').val() == "") {
            alert("개인정보를 변경하기위해서는 전화번호를 입력하여야 합니다");
            return;
        }
        if(!numTest.test($('#phone').val())) {
            alert("휴대폰번호 형식이 잘못되었습니다");
            return;
        }
        const data = {
            id: $('#id').val(),
            name: $('#name').val(),
            phone: $('#phone').val(),
            address: $('#address').val(),
            detailAddress: $('#detailAddress').val()
        };
        $.ajax({
            type: 'POST',
            url: "/userModify",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data)
        }).done(function () {
            alert('수정되었습니다');
            window.location.href = "/myPage";
        }).fail(function (error) {
            alert('다시 시도해주세요');
        });
    },

    passwordModify: function () {
        const data = {
            id: $('#idModify').val(),
            password: $('#passwordModify').val()
        };
        if (data.password === "") {
            alert("비밀번호를 입력해주세요");
            return;
        }
        if(!pwTest.test(data.password)) {
            alert("비밀번호는 영문, 숫자, 특수문자를 1회이상 사용한 8자리 이상이여야합니다");
            return;
        }
        if (data.password !== $('#passwordModify2').val()) {
            alert("비밀번호가 일치하지 않습니다");
            return;
        }
        $.ajax({
            type: 'POST',
            url: "/passwordModifyApi",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data),
            success: function() {
                alert("변경되었습니다");
                 window.location.href="/myPage";
            },
            error: function(error) {
                console.error(error);
            }
        });
    },

    findId: function () {
        const data = {
            email: $('#email').val()+"@"+$('#server').val()
        }
        $.ajax({
            type: 'POST',
            url: "/findIdByEmail",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data)
        }).done(function (result) {
            if (result === "null") {
                alert('가입 정보가 없습니다');
            } else {
                alert("해당 이메일로 가입된 아이디는 "+result+" 입니다");
            }
        }).fail(function (error) {
            console.error(JSON.stringify(error));
        });
    },

    leave: function () {
        const check = confirm("정말 탈퇴하시겠습니까? 개인정보는 복구되지 않습니다");
        if (check == true) {
            const id = $('#id').val();
            $.ajax({
                type: 'get',
                url: "/leave/"+id,
                contentType: 'application/json; charset=UTF-8'
            }).done(function () {
                alert("정상적으로 탈퇴되었습니다");
                window.location.href = "/logout";
            }).fail(function (error) {
                console.error(JSON.stringify(error));
            });
        }
    },

    emailCheck: function () {
        const data = {
            inputCode: $('#code').val(),
        }
        if ($('#code').val() == 0) {

            alert("인증 코드를 입력해주세요")
            return;
        }
        $.ajax({
            type: "post",
            url: "/emailCheck",
            dataType: "json",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data),
            success : function (result) {
                if (result == true) {
                    alert("인증완료");
                    $('#mailCheck').val("t");
                    return;
                }
                alert("인증번호 오류");
            }
        });
    },

    pwEmailCheck: function () {
        const data = {
            inputCode: $('#code').val(),
        }
        if ($('#code').val() == 0) {
            alert("인증 코드를 입력해주세요")
            return;
        }
        $.ajax({
            type: "post",
            url: "/emailCheck",
            dataType: "json",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data),
            success : function (result) {
                if (result === true) {
                    alert("인증완료! 비밀번호를 변경해주세요");
                    $('#attach').append("<div class=\"form-floating input-group mb-3\">\n" +
                        "<input type=\"hidden\" id=\"idModify\" value=\"" + $('#toFindId').val() + "\">" +
                        "<input type=\"password\" class=\"form-control password\" id=\"passwordModify\" placeholder=\"비밀번호\">\n" +
                        "<label for=\"password\">비밀번호</label>\n" +
                        "</div>\n" +
                        "<div class=\"form-floating input-group mb-3\">\n" +
                        "<input type=\"password\" class=\"form-control password\" id=\"passwordModify2\" placeholder=\"비밀번호 확인\">\n" +
                        "<label for=\"password2\">비밀번호 확인</label>\n" +
                        "</div>\n");
                    $('#button-attach').append("<button type=\"button\" class=\"btn btn-primary\" id=\"btn-passwordModify\">비밀번호 변경</button>");
                    account.init();
                } else {
                    alert("인증번호 오류");
                }
            }
        });
    },

    email: function () {
        const data = {
            userEmail: $('#email').val() + "@" + $('#server').val(),
            find: "false"
        }
        if ($('#email').val() == "") {
            alert("이메일을 입력하세요")
            return;
        }
        if ($('#server').val() == "") {
            alert("이메일을 입력하세요")
            return;
        }
        alert("잠시만 기다려주세요");
        $.ajax({
            type: "post",
            url: "/emailSend",
            dataType: "json",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data),
            success: function(result) {
                if (result == true) {
                    alert("메일전송 완료");
                    return;
                } else {
                    alert("이미 가입된 이메일입니다");
                    return;
                }
            },
            error: function(error) {
                alert("메일 전송 실패");
                console.error(error);
            }
        });
    },

    sendCodeById: function () {
        const id = $('#toFindId').val();
        if (id == null) {
            alert("아이디를 입력해주세요");
            return;
        }
        const data = {
            id: id
        }
        $.ajax({
            url: "/findEmailById",
            type: "Post",
            contentType: 'application/json; charset=UTF-8',
            data: JSON.stringify(data),
            success: function(result) {
                alert("잠시만 기다려주세요");
                if (result === "null") {
                    alert("존재하지 않는 아이디입니다");
                } else {
                    const data2 = {
                        userEmail: result,
                        find: "find"
                    };
                    $.ajax({
                        url: "/emailSend",
                        type: "post",
                        contentType: 'application/json; charset=UTF-8',
                        data: JSON.stringify(data2)
                    }).done(function () {
                        alert("가입시 입력한 이메일로 메일이 발송되었습니다");
                    }).fail(function (error) {
                        alert("전송 실패");
                        console.log(error);
                    })
                }
            },
            error: function(error) {
                console.log(error);
            }
        });
    }

};

account.init();

function readImage(input) {
    if(input.files && input.files[0]) {
        const reader = new FileReader()
        reader.onload = e => {
            const previewImage = document.getElementById("preview-image")
            previewImage.src = e.target.result
        }
        reader.readAsDataURL(input.files[0])
    }
}

function naver() {
    $('#server').val("naver.com");
}
function google() {
    $('#server').val("gmail.com");
}
function hanmail() {
    $('#server').val("hanmail.com");
}