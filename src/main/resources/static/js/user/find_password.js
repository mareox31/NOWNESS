$(document).ready(() => {
    const email = $('#floatingEmail');
    email.keyup(() => {
        validate(email);
        if (!email.hasClass('is-invalid')) requestToServer('duplicate', email);
    });

    email.on("keydown", function(event) {
        if (event.key === "Enter" && $(this).is(':focus') && $(this).hasClass('is-valid')) {
            requestPasswordResetEmail(email);
        }
    });

    $('#btnSubmit').click(() => {
        requestPasswordResetEmail(email);
    });
});

// email 입력에 대한 html element 를 받아서 검증합니다.
// 검증 통과 시 true 를 반환하고, 미통과 시 false 를 반환합니다.
function validate(email) {
    // RFC 5322 Format
    const regex = new RegExp("^((?:[A-Za-z0-9!#$%&'*+\\-\\/=?^_`{|}~]|(?<=^|\\.)\"|\"(?=$|\\.|@)|(?<=\".*)[ .](?=.*\")|(?<!\\.)\\.){1,64})(@)((?:[A-Za-z0-9.\\-])*(?:[A-Za-z0-9])\\.(?:[A-Za-z0-9]){2,})$");
    if (regex.test(email.val())) {
        email.removeClass('is-invalid');
    } else {
        email.addClass('is-invalid');
        email.removeClass('is-valid');
        disableSubmitButton();
        $('#emailValidationFeedback').text('허용되는 이메일 양식이 아닙니다. 이메일을 확인해주세요.');
    }
}

// 버튼을 비활성화(disabled)하는 함수
function disableSubmitButton() {
    $('#btnSubmit').prop('disabled', true);
}

// 버튼을 활성화(enable)하는 함수
function enableSubmitButton() {
    $('#btnSubmit').prop('disabled', false);
}

function requestPasswordResetEmail(email) {
    disableSubmitButton();
    requestToServer('password', email);
    alert("비밀번호 재설정을 위한 이메일을 보내드렸습니다. 이메일을 확인해주세요.");
    // window.location.href = '/';
}

function requestToServer(resource, email) {
    const data = {
        email: email.val(),
    }
    ajaxRequest(resource, data);
}

function ajaxRequest(resource, data) {
    $.ajax({
        url: '/api/v1/user/' + resource,
        type: 'POST',
        dataType: 'json',
        data: JSON.stringify(data),
        contentType: 'application/json',
        headers: {
            'X-CSRF-TOKEN': $('input[name="_csrf"]').val(),
        },
        success: function(response) {
            const email = $('#floatingEmail');
            if(response === true) {
                enableSubmitButton();
                email.addClass('is-valid');
            } else {
                email.addClass('is-invalid');
                email.removeClass('is-valid');
                $('#emailValidationFeedback').text('가입되지 않은 이메일입니다.');
                disableSubmitButton();
            }
        },
        error: function(xhr, status, error) {
            alert("오류가 발생하였습니다. 나중에 다시 시도해주세요.");
        }
    });
}