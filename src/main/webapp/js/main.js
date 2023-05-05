let ws;

let roomID;
function newRoom(){
    // calling the ChatServlet to retrieve a new room ID
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet";
    fetch(callURL, {
        method: 'GET',
        headers: {
            'Accept': 'text/plain',
        },
    })
        .then(response => response.text())
        .then(response => {
            enterRoom(response);
        });
}

function enterRoom(code){
    // set the code for the room
    roomID = code.substring(0,5);
    // refresh the list of rooms
    refresh();
    if (ws != null) {
        ws.close();
    }
    // create the web socket
    ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/" + code);

    // parse messages received from the server and update the UI accordingly
    ws.onmessage = function (event) {
        console.log(event.data);
        // parsing the server's message as json
        let message = JSON.parse(event.data);

        // handle message

        document.getElementById("log").value += "[" + timestamp() + "] " + message.msg + "\n";

        }
    document.getElementById("section-inner").innerHTML = "<h2>You are chatting in room: " + code + "</h2>";


}

const input = document.getElementById('chat-input');
input.addEventListener('keydown', function(event) {
    if (event.key === 'Enter') {
        event.preventDefault();
        sendJSON();
    }
});

function sendJSON() {
    let input = document.getElementById("chat-input");

    let request = {"room": roomID, "type": "chat", "msg": input.value};
    requestJSON = JSON.stringify(request);
    console.log(requestJSON);
    ws.send(requestJSON);
    input.value = "";
}

function refresh(){
    // calling the RoomServlet to retrieve list of rooms for all clients
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/room-servlet";
    fetch(callURL, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
        },
    })
        .then(response => response.json())
        .then(response => {
            console.log(response);
            let sidebar = document.getElementById('sidebar');
            let nav = sidebar.querySelector('nav');
            let ul = nav.querySelector('ul');
            let h2 = ul.querySelector('h2');
            for(let i = 0; i < response.rooms.length; i++)
            {
                let roomID = response.rooms[i];
                let existingButton = ul.querySelector(`button[data-roomid="${roomID}"]`);
                if (!existingButton) {
                    let button = document.createElement('button');
                    button.textContent = roomID;
                    button.style.display = 'block';
                    button.setAttribute('data-roomid', roomID);
                    button.classList.add('buttons');
                    button.onclick = function() {
                        enterRoom(roomID);
                    };
                    ul.insertBefore(button, h2.nextSibling);
                }
            }
        });
}


function timestamp() {
    var d = new Date(), minutes = d.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    return d.getHours() + ':' + minutes;
}
// light-mode, dark-mode toggle
const modeToggleBtn = document.getElementById('mode-toggle');

function toggleMode() {
    const body = document.body;
    const sidebar = document.querySelector('.sidebar');
    body.classList.toggle('light-mode');
    sidebar.classList.toggle('light-mode');
    if (body.classList.contains('light-mode')) {
        modeToggleBtn.textContent = 'Toggle Dark Mode';
    } else {
        modeToggleBtn.textContent = 'Toggle Light Mode';
    }
    var darkStylesheet = document.getElementById("theme-style");
    var lightStylesheet = document.getElementById("theme-style-light");
    var themeToggle = document.querySelector(".theme-toggle");

    if (darkStylesheet.disabled) {
        darkStylesheet.disabled = false;
        lightStylesheet.disabled = true;
        themeToggle.textContent = "Toggle Dark Mode";
    } else {
        darkStylesheet.disabled = true;
        lightStylesheet.disabled = false;
        themeToggle.textContent = "Toggle Light Mode";
    }
}

modeToggleBtn.addEventListener('click', toggleMode);


