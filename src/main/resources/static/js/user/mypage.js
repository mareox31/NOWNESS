$(document).ready(function() {

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))

    const nicknameModal = $('#modal');

    nicknameModal.on('show.bs.modal', function(event) {

        const button = $(event.relatedTarget);
        const value = button.attr('data-bs-whatever');
        const modalTitle = nicknameModal.find('.modal-title');
        const modalBodyInput = nicknameModal.find('.modal-body input');

        const isPasswordChange = value === 'password-change';

        const nicknameForm = nicknameModal.find('#nicknameForm');
        const passwordForm = nicknameModal.find('#passwordForm');

        if (isPasswordChange) {
            nicknameForm.hide();
            passwordForm.show();
        } else {
            nicknameForm.show();
            passwordForm.hide();
        }

        modalTitle.text(isPasswordChange ? '비밀번호 변경하기' : '닉네임 변경하기');
        modalBodyInput.val(isPasswordChange ? '' : value);
    });

    const verifyMailSendBtn = $('#btnVerifyMailSend');

    // 인증 메일을 재발송한 후 여러번 클릭하는 것을 막기 위해 숨깁니다.
    verifyMailSendBtn.click(() => {
        verifyMailSendBtn.hide();
        $('#btnEmailSending').show();
        resendVerifyMail();
    });

});

const path = '/api/v1/user/';

function resendVerifyMail() {
    const resource = 'unverified-email';
    const method = 'POST';
    const data = {
        email: $('#email').text(),
    };

    requestAjax(path, resource, method, data);
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
            if (resource === 'unverified-email' && status === 'success') {
                $('#btnEmailSending').hide();
                alert('인증 메일이 재발송되었습니다.');
            }
        },
        error: function (xhr, status, error) {
            alert('오류가 발생하였습니다. 나중에 다시 시도해주세요.');
            $('#btnVerifyMailSend').show();
        }
    });
}