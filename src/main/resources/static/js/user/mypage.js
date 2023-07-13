$(document).ready(function() {

    const nicknameModal = document.getElementById('nicknameModal');
    nicknameModal.addEventListener('show.bs.modal', event => {

        const button = event.relatedTarget;// 닉네임 변경 버튼
        const nickname = button.getAttribute('data-bs-whatever');
        const modalTitle = nicknameModal.querySelector('.modal-title');
        const modalBodyInput = nicknameModal.querySelector('.modal-body input');

        modalTitle.textContent = `닉네임 변경하기`;
        modalBodyInput.value = nickname;
    });

});