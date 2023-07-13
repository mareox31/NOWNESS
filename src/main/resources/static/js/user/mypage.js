$(document).ready(function() {

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

});