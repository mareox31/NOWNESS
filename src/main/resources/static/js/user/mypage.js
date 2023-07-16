$(document).ready(function() {

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))

    const modal = $('#modal');

    modal.on('show.bs.modal', function(event) {

        const button = $(event.relatedTarget);
        const value = button.attr('data-bs-whatever');
        const modalTitle = modal.find('.modal-title');

        const nicknameForm = $('#nicknameForm');
        const passwordForm = $('#passwordForm');

        const isPasswordChange = value === 'password-change';

        if (isPasswordChange) {
            nicknameForm.hide();
            passwordForm.show();
        } else {
            nicknameForm.show();
            passwordForm.hide();
        }

        modalTitle.text(isPasswordChange ? '비밀번호 변경하기' : '닉네임 변경하기');
        $('#nickname').val(value);
    });

    const verifyMailSendBtn = $('#btnVerifyMailSend');

    // 인증 메일을 재발송한 후 여러번 클릭하는 것을 막기 위해 버튼을 숨깁니다.
    verifyMailSendBtn.click(() => {
        verifyMailSendBtn.hide();
        $('#btnEmailSendingMessage').show();
        resendVerifyMail();
    });

    const changeRequestBtn = $('#btnChangeRequest');
    const passwordInput = $('#password');

    changeRequestBtn.click(() => {

        changeRequestBtn.hide();
        $('#btnChangingInfoMessage').show();
        const newPassword = passwordInput.val();
        if (newPassword.length >= 8) {
            changePassword(newPassword);
        }
    });

    passwordInput.keyup(() => {
        validatePassword(passwordInput);
    });

});

// password 입력에 대한 html element 를 받아서 검증합니다.
function validatePassword(password) {
    const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (regex.test(password.val())) {
        password.removeClass('is-invalid');
        password.addClass('is-valid');
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
const passwordResourceName = 'password';

function resendVerifyMail() {
    const method = 'POST';
    const data = {
        email: $('#email').text(),
    };

    requestAjax(path, emailResourceName, method, data);
}

function changePassword(newPassword) {
    const method = 'PATCH';
    const data = {
        email: $('#email').text(),
        newPassword: newPassword,
    };

    requestAjax(path, passwordResourceName, method, data);
}

function requestAjax(path, resource, method, data) {
    $.ajax({
        url: path + resource,
        type: method,
        dataType: 'json',
        data: JSON.stringify(data),
        contentType: 'application/json',
        headers: {
            'X-CSRF-TOKEN': $('input[name="_csrf"]').val(),
        },
        success: function (xhr, status, response) {
            if (resource === emailResourceName && status === 'success') {
                $('#btnEmailSendingMessage').hide();
                alert('인증 메일이 재발송되었습니다.');
            } else if (resource === passwordResourceName && status === 'nocontent') {
                $('#btnChangingInfoMessage').hide();
                $('#btnChangeRequest').show();
                alert('비밀번호가 변경되었습니다.');
            }
        },
        error: function (xhr, status, error) {
            alert('오류가 발생하였습니다. 나중에 다시 시도해주세요.');
            if (resource === emailResourceName && status === 'success') {
                $('#btnVerifyMailSend').show();
            } else if (resource === passwordResourceName && status === 'nocontent') {
                $('#btnChangingInfoMessage').hide();
                $('#btnChangeRequest').show();
            }
        }
    });
}