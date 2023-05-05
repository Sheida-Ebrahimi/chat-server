package com.example.webchatserver;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

/**
 * This is a class that has services
 * In our case, using to store a list of rooms**/
@WebServlet(name = "roomServlet", value = "/room-servlet")
public class RoomServlet extends HttpServlet {
    private String message;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        // send the list of rooms
        Set<String> roomSet = ChatServlet.rooms;
        JSONArray array = new JSONArray();

        // creating a json
        for (String key : roomSet) {
            array.put(key);
        }
        JSONObject obj = new JSONObject();
        obj.put("rooms",array);

        // sending json back to client
        PrintWriter out = response.getWriter();
        out.print(obj);
    }
}
