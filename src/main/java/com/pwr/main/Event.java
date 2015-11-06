package com.pwr.main;

import java.sql.Connection;

import com.pwr.db.DB;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Event {
    public static Connection CONNECTION;
    
    private Integer id = null;
    private String date = null;
    private Integer user = null;  // user.id
    private Integer zone = null;  // zone.id
    private String action = null;  // enum: in/out
    private String signedInBy = null;  // enum: card/www

    public Event(Integer id) {
        this.id = id;
    }

    public Event(String date, Integer user, Integer zone,
                 String action, String signedInBy) throws SQLException {

        this.date = date;
        this.user = user;
        this.zone = zone;
        this.action = action;
        this.signedInBy = signedInBy;
        
        add();
        System.out.println("Added zone: "+this);
    }

    public Event(Integer user, Integer zone, String action, String signedInBy) throws SQLException {
    	
    	this.date = null;
        this.user = user;
        this.zone = zone;
        this.action = action;
        this.signedInBy = signedInBy;
        add();
    }

    private void add() throws SQLException {
        PreparedStatement stmt = DB.conn.prepareStatement(
                "Insert Into events (ts, user, zone, action, signedInBy)" +
                            "Values (If(? Is Null,Sysdate(6),?), ?, ?, ?, ?)");
        
        stmt.setString(1, date);
        stmt.setString(2, date);
        stmt.setInt(3, user);
        stmt.setInt(4, zone);
        stmt.setString(5, action);
        stmt.setString(6, signedInBy);

        stmt.executeUpdate();
    }

    public void remove() throws SQLException {
        if (id == null)
            throw new RuntimeException("Working at unspecified object");

        PreparedStatement pstmt = CONNECTION.prepareStatement(
                "DELETE FROM events WHERE id = ?");
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
    }

}