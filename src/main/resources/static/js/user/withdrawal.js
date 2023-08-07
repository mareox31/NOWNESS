$(document).ready(() => {

    const withdrawalCheckbox = $('#checkWithdrawal');
    const withdrawalButton = $('#btnWithdrawal');

    withdrawalCheckbox.click(function() {
        const isChecked = withdrawalCheckbox.is(':checked');
        isChecked ? enableButton(withdrawalButton) : disableButton(withdrawalButton);
    });

    function enableButton(button) {
        button.prop('disabled', false);
    }

    function disableButton(button) {
        button.prop('disabled', true);
    }

    withdrawalButton.click(() => {
        if (confirm('확인 버튼을 누르면, 회원 탈퇴를 요청합니다.')) {
            requestAjax($('#userId').val(), 'DELETE');
        }
    });

    function requestAjax(resource, method) {
        $.ajax({
            url: '/api/v1/user/' + resource,
            type: method,
            headers: {
                'X-CSRF-TOKEN': $('input[name="_csrf"]').val(),
            },
            success: function (xhr, status, response) {
                if (status === 'nocontent') {
                    alert('회원탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.');
                    location.href = '/';
                }
            },
            error: function (xhr, status, error) {
                alert('오류가 발생하였습니다. 나중에 다시 시도해주세요.');
            }
        });
    }
});