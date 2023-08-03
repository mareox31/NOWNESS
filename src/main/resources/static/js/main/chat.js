var stompClient = null;
var username = null;

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    stompClient.subscribe('/topic/publicChatRoom', onMessageReceived);
}

function onError(error) {
    console.log('서버에 접속할 수 없습니다. 새로고침 해주세요.');
}

function sendMessage(event) {
    var messageContent = document.querySelector('#message').value.trim();
    username = document.querySelector('#username').value.trim();

    if(username && messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: document.querySelector('#message').value
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        document.querySelector('#message').value = '';
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    messageElement.innerHTML = message.sender+ ': ' + message.content;

    document.querySelector('#messageArea').appendChild(messageElement);
}

document.querySelector('#messageForm').addEventListener('submit', sendMessage, true);

connect();
