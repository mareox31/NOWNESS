$(document).ready(() => {
    const password = $('#floatingPassword');
    const passwordCheck = $('#floatingPasswordCheck');
    password.keyup(() => {
        validatePassword(password);
        checkConditionsToEnableSubmitButton(password, passwordCheck);
    });
    passwordCheck.keyup(() => {
        validatePasswordCheck(password, passwordCheck);
        checkConditionsToEnableSubmitButton(password, passwordCheck);
    });

    passwordCheck.on("keydown", function(event) {
        if (event.key === "Enter" && $(this).is(':focus')
            && $(this).hasClass('is-valid') && password.hasClass('is-valid')) {
            submitChangedPassword(password);
        }
    });

    $('#btnSubmit').click(() => {
        submitChangedPassword(password);
    });
});

// password 입력에 대한 html element 를 받아서 검증합니다.
function validatePassword(password) {
    const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (regex.test(password.val())) {
        password.removeClass('is-invalid');
        password.addClass('is-valid');
    } else {
        password.addClass('is-invalid');
        password.removeClass('is-valid');
        disableSubmitButton();
    }
}

// password 에 입력한 비밀번호와 확인을 위해 입력한 비밀번호가 일치하는지 검증합니다.
function validatePasswordCheck(password, passwordCheck) {
    if (password.val() === passwordCheck.val()) {
        passwordCheck.removeClass('is-invalid');
        passwordCheck.addClass('is-valid');
    } else {
        passwordCheck.addClass('is-invalid');
        passwordCheck.removeClass('is-valid');
        disableSubmitButton();
    }
}

function checkConditionsToEnableSubmitButton(password, passwordCheck) {
    password.hasClass('is-valid') && passwordCheck.hasClass('is-valid') ? enableSubmitButton() : disableSubmitButton();
}

// submit 버튼을 비활성화(disabled)하는 함수
function disableSubmitButton() {
    $('#btnSubmit').prop('disabled', true);
}

// submit 버튼을 활성화(enable)하는 함수
function enableSubmitButton() {
    $('#btnSubmit').prop('disabled', false);
}

function submitChangedPassword(password) {
    const data = {
        code: new URLSearchParams(window.location.search).get('code'),
        password: password.val(),
    }
    ajaxRequest(data);
}

function ajaxRequest(data) {
    $.ajax({
        url: '/api/v1/user/password',
        type: 'PUT',
        dataType: 'json',
        data: JSON.stringify(data),
        contentType: 'application/json',
        headers: {
            'X-CSRF-TOKEN': $('input[name="_csrf"]').val(),
        },
        success: function (xhr, status, response) {
            if (status === 'nocontent') {
                alert("비밀번호 재설정이 완료되었습니다. 로그인 화면으로 이동합니다.")
                window.location.href = '/user/login';
            }
        },
        error: function (xhr, status, error) {
            alert("오류가 발생하였습니다. 나중에 다시 시도해주세요.");
        }
    });
}