package com.example.webchatserver;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class represents a web socket server, a new connection is created and it receives a roomID as a parameter
 * **/
@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {

    // contains a static List of ChatRoom used to control the existing rooms and their users
//    static HashMap<String, ChatRoom> rooms = new HashMap<>(); // indexed by string and get Chatroom
//    static HashMap<ChatRoom, String> users = new HashMap<>();
    static HashMap<String,ChatRoom> rooms = new HashMap<>();
    // you may add other attributes as you see fit



    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {
        //TODO : check if user is in another room. if they are remove them and then add them to the other room
        // if not then just add them to the room
        String userId = session.getId();

        for (Map.Entry<String, ChatRoom> room : rooms.entrySet()) {

            if (room.getValue().inRoom(userId)) {
                room.getValue().removeUser(userId);
                for (Session peer : session.getOpenSessions()){ //broadcast this person left the server
                    if(rooms.get(roomID).inRoom(peer.getId())) { // broadcast only to those in the same room
                        peer.getBasicRemote().sendText("{\"type\": \"chat\", \"msg\":\"(Server): "
                                + room.getValue().getUsers().get(userId) + " left the chat room.\"}");

                    }
                }
            }

        }

        if (!rooms.containsKey(roomID)) { // created new room
            ChatRoom chatRoom = new ChatRoom(roomID,session.getId());
            rooms.put(roomID,chatRoom); // put the room in a hashmap
        } else {
            rooms.get(roomID).setUserName(session.getId(), ""); // add user to the existing room but set name to "" because user has not entered a name
        }

        session.getBasicRemote().sendText("{\"room\": \"" + roomID + "\", \"type\": \"chat\", \"msg\":\"(Server): Welcome to the chat room (" + roomID + "). Please type your username to begin.\"}");
//        accessing the roomID parameter
        System.out.println(rooms.toString());
    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        //get user id through session
        String userId = session.getId();

        //
        for (Map.Entry<String, ChatRoom> room : rooms.entrySet()) {

            if (room.getValue().inRoom(userId)) {
                String roomID = room.getValue().getCode();
                String username = rooms.get(roomID).getUsers().get(userId);
                room.getValue().removeUser(userId);

                    for (Session peer : session.getOpenSessions()){ //broadcast this person left the server
                        if(rooms.get(roomID).inRoom(peer.getId())) { // broadcast only to those in the same room
                            peer.getBasicRemote().sendText("{\"type\": \"chat\", \"msg\":\"(Server): "
                                    + username + " left the chat room.\"}");

                        }
                    }
            }

        }

    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
        //String roomId = "";
        //        example getting unique userID that sent this message
        String userId = session.getId();

//        for (Map.Entry<String, ChatRoom> room: rooms.entrySet()) { // finding which room does user belong to
//            if (room.getValue().inRoom(userId)) {
//                roomId = room.getValue().getCode();
//            }
//        }

        JSONObject jsonMsg = new JSONObject(comm);
        // {"room": "123ABC", "type": "chat", "msg": "hi"}
        String roomID = jsonMsg.get("room").toString();
        String type = jsonMsg.get("type").toString();
        String message = jsonMsg.get("msg").toString();

        if (rooms.get(roomID).getUsers().get(userId).equals("")) { // their first message
            // TO DO: VALIDATE STRING IS NOT EMPTY
//            boolean flag = true;
//            while (flag) {
//                if (!(message.trim().equals("") || message == null)) {
//                    flag = false;
//                } else {
//                    session.getBasicRemote().sendText("{\"room\": \""+ roomID +"\",\"type\": \"chat\", \"msg\":\"(Server): Please enter a valid username! (empty strings not allowed)\"}");
//                }
//            }

            rooms.get(roomID).setUserName(userId, message); // set username to first message
            // acknowledge user
            session.getBasicRemote().sendText("{\"room\": \""+ roomID +"\",\"type\": \"chat\", \"msg\":\"(Server): Welcome, " + message + "!\"}");

            // let other users know that user joined the channel
            for (Session peer: session.getOpenSessions()) {
                // check if peers are in that room and make sure current peer is not the same as self (the person who sent the message)
                if (rooms.get(roomID).inRoom(peer.getId()) && !(peer.getId().equals(userId))) {
                    peer.getBasicRemote().sendText("{\"room\": \""+ roomID +"\",\"type\": \"chat\", \"msg\":\"(Server): " + message + " joined the chat room.\"}");
                    System.out.println("Joined the chatroom");

                }
            }
        } else { // not their first message
            String username = rooms.get(roomID).getUsers().get(userId);
            System.out.println(username); // printing who sent

            // broadcasting message to everyone else in the room
            System.out.print(rooms.toString());
            for (Session peer : session.getOpenSessions()) {
                // checking if peer is in the same room
                if (rooms.get(roomID).inRoom(peer.getId())) {
                    peer.getBasicRemote().sendText("{\"room\": \""+ roomID +"\",\"type\": \"chat\", \"msg\":\"("+username+"): " + message + "\"}");
                    System.out.println("broadcasting to other users");
                }
            }
        }


       // session.getBasicRemote().sendText("Welcome to chat room: " + roomID);
//        if (type.equals("create")) {
//            ChatRoom chatRoom = new ChatRoom(room,session.getId());
//            rooms.put(room,chatRoom);
//        }
//        Example conversion of json messages from the client
        //        JSONObject jsonmsg = new JSONObject(comm);
//        String val1 = (String) jsonmsg.get("attribute1");
//        String val2 = (String) jsonmsg.get("attribute2");

        // handle the messages


    }


}