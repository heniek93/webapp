package com.pwr.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Bartosz Ufnal on 06.05.15.
 */
public class Access {
    public static Connection CONNECTION;

    private String id = null;
    private String user = null;
    private String zone = null;

    public Access(String id) {
        this.id = id;
    }

    public Access(String user, String zone) {
        this.user = user;
        this.zone = zone;
    }

    public void set() throws SQLException {
        PreparedStatement pstmt = CONNECTION.prepareStatement(
                "INSERT INTO Access ( user, zone) VALUES (?, ?)");
        pstmt.setString(1, user);
        pstmt.setString(2, zone);

        pstmt.executeUpdate();
    }

    public void reclaim() throws SQLException {
        if (id == null)
            throw new RuntimeException("Working at unspecified object");

        PreparedStatement pstmt = CONNECTION.prepareStatement(
                "DELETE FROM Access WHERE id = ?");
        pstmt.setString(1, id);
        pstmt.executeUpdate();
    }

}