$(document).ready(() => {
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
    }

});

// login 시 입력되는 email 과 password 를 검증합니다.
// email 과 password 모두 유효할 경우 true, 그렇지 않을 경우 false 를 반환합니다.
function validateLoginForm(email, password) {
    const isValidEmail = validateEmail(email);
    const isValidPassword = validatePassword(password);
    return isValidEmail && isValidPassword;
}

// email 입력에 대한 html element 를 받아서 검증합니다.
// 검증 통과 시 true 를 반환하고, 미통과 시 false 를 반환합니다.
function validateEmail(email) {
    // RFC 5322 Format
    const regex = new RegExp("^((?:[A-Za-z0-9!#$%&'*+\\-\\/=?^_`{|}~]|(?<=^|\\.)\"|\"(?=$|\\.|@)|(?<=\".*)[ .](?=.*\")|(?<!\\.)\\.){1,64})(@)((?:[A-Za-z0-9.\\-])*(?:[A-Za-z0-9])\\.(?:[A-Za-z0-9]){2,})$");
    if (regex.test(email.val())) {
        email.removeClass('is-invalid');
        return true;
    } else {
        email.addClass('is-invalid');
        return false;
    }
}

// password 입력에 대한 html element 를 받아서 검증합니다.
// 검증 통과 시 true 를 반환하고, 미통과 시 false 를 반환합니다.
function validatePassword(password) {
    const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (regex.test(password.val())) {
        password.removeClass('is-invalid');
        return true;
    } else {
        password.addClass('is-invalid');
        return false;
    }
}

// password 에 입력한 비밀번호와 확인을 위해 입력한 비밀번호가 일치하는지 검증합니다.
// 검증 통과 시 true 를 반환하고, 미통과 시 false 를 반환합니다.
function validatePasswordCheck(password, passwordCheck) {
    if (password.val() === passwordCheck.val()) {
        passwordCheck.removeClass('is-invalid');
        return true;
    } else {
        passwordCheck.addClass('is-invalid');
        return false;
    }
}

// 닉네임 규정을 준수하는지 검증합니다.
// 검증 통과 시 true 를 반환하고, 미통과 시 false 를 반환합니다.
function validateNickname(nickname) {
    const regex = /^[가-힣a-zA-Z0-9]{3,8}$/;
    if (regex.test(nickname.val())) {
        nickname.removeClass('is-invalid');
        return true;
    } else {
        nickname.addClass('is-invalid');
        return false;
    }
}