$(document).ready(function() {

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))

    const modal = $('#modal');
    const passwordInput = $('#passwordInput');
    const nicknameInput = $('#nicknameInput');

    modal.on('show.bs.modal', function(event) {

        const button = $(event.relatedTarget);
        disableRequestButton();
        passwordInput.removeClass('is-invalid');
        passwordInput.removeClass('is-valid');
        nicknameInput.removeClass('is-invalid');
        nicknameInput.removeClass('is-valid');
        const value = button.attr('data-bs-whatever');
        const modalTitle = modal.find('.modal-title');

        const nicknameForm = $('#nicknameForm');
        const passwordForm = $('#passwordForm');

        const isPasswordChange = value === 'password-change';

        if (isPasswordChange) {
            nicknameForm.hide();
            passwordForm.show();
            passwordInput.val('');
        } else {
            nicknameForm.show();
            passwordForm.hide();
            nicknameInput.val(value);
        }

        modalTitle.text(isPasswordChange ? '비밀번호 변경하기' : '닉네임 변경하기');
    });

    const verifyMailSendBtn = $('#btnVerifyMailSend');

    // 인증 메일을 재발송한 후 여러번 클릭하는 것을 막기 위해 버튼을 숨깁니다.
    verifyMailSendBtn.click(() => {
        verifyMailSendBtn.hide();
        $('#btnEmailSendingMessage').show();
        resendVerifyMail();
    });

    const changeNicknameBtn = $('#btnChangeNickname');

    changeNicknameBtn.click(() => {
        passwordInput.val('');
    });

    const changePasswordBtn = $('#btnChangePassword');

    changePasswordBtn.click(() => {
        nicknameInput.val('');
    });

    const changeRequestBtn = $('#btnChangeRequest');

    changeRequestBtn.click(() => {

        changeRequestBtn.hide();
        $('#btnChangingInfoMessage').show();
        const newPassword = passwordInput.val();
        const newNickname = nicknameInput.val();
        if (newPassword.length >= 8) {
            console.log(newPassword.length);
            changePassword(newPassword);
        } else if (newNickname.length >= 3 && newNickname.length <= 8) {
            changeNickname(newNickname);
        }
    });

    nicknameInput.keyup(() => {
        if (validateNickname(nicknameInput)) checkDuplicateNickname(nicknameInput.val());
    });

    passwordInput.keyup(() => {
        validatePassword(passwordInput);
    });

});

// 닉네임 입력에 대한 html element 를 받아서 검증합니다.
function validateNickname(nicknameInput) {

    if (nicknameInput.val() === $('#nickname').text()) {
        nicknameInput.addClass('is-invalid');
        nicknameInput.removeClass('is-valid');
        disableRequestButton();
        $('#nicknameValidationFeedback').text('기존 닉네임과 동일합니다.');
        return false;
    }

    const regex = /^[가-힣a-zA-Z0-9]{3,8}$/;
    if (regex.test(nicknameInput.val())) {
        nicknameInput.addClass('is-valid');
        nicknameInput.removeClass('is-invalid');
        return true;
    } else {
        nicknameInput.addClass('is-invalid');
        nicknameInput.removeClass('is-valid');
        disableRequestButton();
        $('#nicknameValidationFeedback').text('한글, 영어(대/소문자), 숫자로만 입력할 수 있으며 3~8글자만 입력 가능합니다.');
        return false;
    }
}

// 비밀번호 입력에 대한 html element 를 받아서 검증합니다.
function validatePassword(password) {
    const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (regex.test(password.val())) {
        password.addClass('is-valid');
        password.removeClass('is-invalid');
        enableRequestButton();
    } else {
        password.addClass('is-invalid');
        password.removeClass('is-valid');
        disableRequestButton();
    }
}

function enableRequestButton() {
    $('#btnChangeRequest').prop('disabled', false);
}

function disableRequestButton() {
    $('#btnChangeRequest').prop('disabled', true);
}

const path = '/api/v1/user/';
const emailResourceName = 'unverified-email';
const nicknameResourceName = 'nickname';
const passwordResourceName = 'password';

function resendVerifyMail() {
    const method = 'POST';
    const data = {
        email: $('#email').text(),
    };

    requestAjax(emailResourceName, method, JSON.stringify(data));
}

// 닉네임 입력 값을 받아서 중복 여부를 검사합니다.
function checkDuplicateNickname(newNickname) {
    const method = 'GET';
    const data = {
        newNickname: newNickname,
    };

    requestAjax(nicknameResourceName, method, data);
}

function changeNickname(newNickname) {
    const method = 'PATCH';
    const data = {
        email: $('#email').text(),
        newNickname: newNickname,
    };

    requestAjax(nicknameResourceName, method, JSON.stringify(data));
}

function changePassword(newPassword) {
    const method = 'PATCH';
    const data = {
        email: $('#email').text(),
        newPassword: newPassword,
    };

    requestAjax(passwordResourceName, method, JSON.stringify(data));
}

function requestAjax(resource, method, data) {
    $.ajax({
        url: path + resource,
        type: method,
        dataType: 'json',
        data: data,
        contentType: 'application/json',
        headers: {
            'X-CSRF-TOKEN': $('input[name="_csrf"]').val(),
        },
        success: function (xhr, status, response) {
            if (resource === emailResourceName && status === 'success') {
                $('#btnEmailSendingMessage').hide();
                alert('인증 메일이 재발송되었습니다.');
            } else if (resource === nicknameResourceName && status === 'success') {
                const nicknameInput = $('#nicknameInput');
                if (response['responseJSON'] === true) {
                    nicknameInput.addClass('is-invalid');
                    nicknameInput.removeClass('is-valid');
                    disableRequestButton();
                    $('#nicknameValidationFeedback').text('중복된 닉네임 입니다.');
                } else {
                    nicknameInput.addClass('is-valid');
                    nicknameInput.removeClass('is-invalid');
                    enableRequestButton();
                }
            } else if (resource === nicknameResourceName && status === 'nocontent') {
                $('#nickname').text(JSON.parse(this.data).newNickname);
                $('#btnChangingInfoMessage').hide();
                $('#btnChangeRequest').show();
                alert('닉네임이 변경되었습니다.');
                disableRequestButton()
            } else if (resource === passwordResourceName && status === 'nocontent') {
                $('#btnChangingInfoMessage').hide();
                $('#btnChangeRequest').show();
                alert('비밀번호가 변경되었습니다.');
                disableRequestButton()
            }
        },
        error: function (xhr, status, error) {
            alert('오류가 발생하였습니다. 나중에 다시 시도해주세요.');
            if (resource === emailResourceName) {
                $('#btnVerifyMailSend').show();
            } else {
                $('#btnChangingInfoMessage').hide();
                $('#btnChangeRequest').show();
            }
        }
    });
}