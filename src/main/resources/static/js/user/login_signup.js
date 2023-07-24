$(document).ready(() => {

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))

    const email = $('#floatingEmail');
    const password = $('#floatingPassword');

    email.keyup(() => {
        validateEmail(email);
    });
    password.keyup(() => {
        validatePassword(password);
    });

    if (window.location.pathname === '/user/signup') {
        const nickname = $('#floatingNickname');
        const passwordCheck = $('#floatingPasswordCheck');

        nickname.keyup(() => {
            validateNickname(nickname);
        });

        passwordCheck.keyup(() => {
            validatePasswordCheck(password, passwordCheck);
        });

        $('#floatingEmail, #floatingNickname').on('focusout', function(event) {
            const element = $(event.target);
            if (!element.hasClass('is-invalid'))
                duplicationCheck(element.attr('name'), element.val());
        });

        $('#signupForm').submit(function() {
            $('#btnSubmit').hide();
            $('#btnSignupProcessingMessage').show();
        });
    }

});

// email 입력에 대한 html element 를 받아서 검증합니다.
// 검증 통과 시 true 를 반환하고, 미통과 시 false 를 반환합니다.
function validateEmail(email) {
    // RFC 5322 Format
    const regex = new RegExp("^((?:[A-Za-z0-9!#$%&'*+\\-\\/=?^_`{|}~]|(?<=^|\\.)\"|\"(?=$|\\.|@)|(?<=\".*)[ .](?=.*\")|(?<!\\.)\\.){1,64})(@)((?:[A-Za-z0-9.\\-])*(?:[A-Za-z0-9])\\.(?:[A-Za-z0-9]){2,})$");
    if (regex.test(email.val())) {
        email.removeClass('is-invalid');
        if (isSignUpForm()) validateSingUpForm();
        else validateLoginForm();
        return true;
    } else {
        email.addClass('is-invalid');
        $('#emailValidationFeedback').text('허용되는 이메일 양식이 아닙니다. 이메일을 확인해주세요.');
        disableSubmitButton();
        return false;
    }
}

// 닉네임 규정을 준수하는지 검증합니다.
// 검증 통과 시 true 를 반환하고, 미통과 시 false 를 반환합니다.
function validateNickname(nickname) {
    const regex = /^[가-힣a-zA-Z0-9]{3,8}$/;
    if (regex.test(nickname.val())) {
        nickname.removeClass('is-invalid');
        validateSingUpForm();
        return true;
    } else {
        nickname.addClass('is-invalid');
        $('#nicknameValidationFeedback').text('한글, 영어(대/소문자), 숫자로만 입력할 수 있으며 3~8글자만 입력 가능합니다.');
        disableSubmitButton();
        return false;
    }
}

// password 입력에 대한 html element 를 받아서 검증합니다.
// 검증 통과 시 true 를 반환하고, 미통과 시 false 를 반환합니다.
function validatePassword(password) {
    const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (regex.test(password.val())) {
        password.removeClass('is-invalid');
        if (isSignUpForm()) validateSingUpForm();
        else validateLoginForm();
        return true;
    } else {
        password.addClass('is-invalid');
        disableSubmitButton();
        return false;
    }
}

// password 에 입력한 비밀번호와 확인을 위해 입력한 비밀번호가 일치하는지 검증합니다.
// 검증 통과 시 true 를 반환하고, 미통과 시 false 를 반환합니다.
function validatePasswordCheck(password, passwordCheck) {
    if (password.val() === passwordCheck.val()) {
        passwordCheck.removeClass('is-invalid');
        validateSingUpForm();
        return true;
    } else {
        passwordCheck.addClass('is-invalid');
        disableSubmitButton();
        return false;
    }
}

// 회원가입 양식에 작성된 값이 이상이 있으면 버튼을 비활성화하고, 이상이 없으면 활성화 합니다.
function validateSingUpForm() {
    const email = $('#floatingEmail');
    const password = $('#floatingPassword');
    const nickname = $('#floatingNickname');
    const passwordCheck = $('#floatingPasswordCheck');

    if (email.hasClass('is-invalid') || email.val().trim() === '' ||
        password.hasClass('is-invalid') || password.val().trim() === '' ||
        nickname.hasClass('is-invalid') || nickname.val().trim() === '' ||
        passwordCheck.hasClass('is-invalid') || passwordCheck.val().trim() === '') {
        disableSubmitButton()
    } else {
        enableSubmitButton();
    }
}

// 로그인 양식에 작성된 값이 이상이 있으면 버튼을 비활성화하고, 이상이 없으면 활성화 합니다.
function validateLoginForm() {
    const email = $('#floatingEmail');
    const password = $('#floatingPassword');

    if (email.hasClass('is-invalid') || email.val().trim() === '' ||
        password.hasClass('is-invalid') || password.val().trim() === '') {
        disableSubmitButton()
    } else {
        enableSubmitButton();
    }
}

// submit 버튼을 비활성화(disabled)하는 함수
function disableSubmitButton() {
    $('#btnSubmit').prop('disabled', true);
}

// submit 버튼을 활성화(enable)하는 함수
function enableSubmitButton() {
    $('#btnSubmit').prop('disabled', false);
}

function duplicationCheck(type, value) {
    const data = {
        [type]: value,
    }
    request('/api/v1/user/duplicate', 'POST', data, $('input[name="_csrf"]').val())
}

function request(url, method, data, csrfToken) {
    $.ajax({
        url: url,
        type: method,
        dataType: 'json',
        data: JSON.stringify(data),
        contentType: 'application/json',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        },
        success: function(response) {
            if(response === true) {
                disableSubmitButton();
                if (data.hasOwnProperty('email')) {
                    $('#floatingEmail').addClass('is-invalid');
                    $('#emailValidationFeedback').text('중복된 이메일 입니다.');
                } else {
                    $('#floatingNickname').addClass('is-invalid');
                    $('#nicknameValidationFeedback').text('중복된 닉네임 입니다.');
                }
            }
        },
        error: function(xhr, status, error) {
            alert('중복 검사 중 오류가 발생하였습니다. 나중에 다시 시도해주세요.');
        }
    });
}

function isSignUpForm() {
    return window.location.pathname === '/user/signup';
}