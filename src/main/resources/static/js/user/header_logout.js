$(document).ready(function() {

    $('#btnLogout').click(() => {
        requestAjax('logout', 'POST');
    });

    function requestAjax(resource, method) {
        $.ajax({
            url: '/api/v1/user/' + resource,
            type: method,
            dataType: 'json',
            contentType: 'application/json',
            headers: {
                'X-CSRF-TOKEN': $('input[name="_csrf"]').val(),
            },
            success: function (xhr, status, response) {
                console.log(response);
                if (status === 'nocontent') {
                    alert('로그아웃 되었습니다.');
                    location.reload();
                }
            },
            error: function (xhr, status, error) {
                alert('오류가 발생하였습니다. 나중에 다시 시도해주세요.');
            }
        });
    }

});