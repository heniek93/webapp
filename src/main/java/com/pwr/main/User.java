package com.pwr.main;

import com.pwr.db.DB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class User {

    private Integer id;
    private String card;
    private String name;
    private String email;
    private String password;
    private Boolean isAdmin;
    private String signInBy;


    public User(String card, String name,
                String email, String password,
                Boolean isAdmin, String singInBy) throws SQLException {

        id = null;
        this.card = card;
        this.name = name;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.signInBy = singInBy;
        add();
    }
    
    public User(String id) {
    	this(new Integer(id));
    }

    public User(Integer id) {
        this.id = id;
    }
    
    public User(String email, String password) throws AccessDeniedException, SQLException {
		PreparedStatement stmt = DB.conn.prepareStatement("Select id,card,name,email,isAdmin,signInBy From users Where email = ? And password = ?");
    	stmt.setString(1,email);
    	stmt.setString(2,User.getHash(password));
    	ResultSet rs = stmt.executeQuery();
    	if (rs.next()) {
    		this.id = rs.getInt("id");
    		this.card = rs.getString("card");
    		this.name = rs.getString("name");
    		this.email = rs.getString("email");
    		this.isAdmin = rs.getBoolean("isAdmin");
    		this.signInBy= rs.getString("signInBy");
    	} else {
    		throw new AccessDeniedException();
    	}
    }

    public Integer getID() {
    	return id;
    }

    private void add() throws SQLException {

        if (id != null)
            throw new RuntimeException("Inserting empty object");

        PreparedStatement pstmt = DB.conn.prepareStatement("INSERT INTO users (" +
                " card, name, email, password, isAdmin, signInBy )" +
                " VALUES (?, ?, ?, ?, ?, ?)");

        pstmt.setString(1, card);
        pstmt.setString(2, name);
        pstmt.setString(3, email);
        pstmt.setString(4, getHash(password));
        pstmt.setBoolean(5, isAdmin);
        pstmt.setString(6, signInBy);

        pstmt.executeUpdate();
    }


    public void remove() throws SQLException {
        if (id == null)
            throw new RuntimeException("Working at unspecified object");

        PreparedStatement pstmt = DB.conn.prepareStatement(
                "DELETE FROM users WHERE id = ?");
        pstmt.setInt(1, id);
        pstmt.executeUpdate();

    }

    public void signCard(String newCard) throws SQLException {
        if (id == null)
            throw new RuntimeException("Working at unspecified object");

        PreparedStatement pstmt = DB.conn.prepareStatement(
                "UPDATE users SET card = ? WHERE id = ?");
        pstmt.setString(1, newCard);
        pstmt.setInt(2, id);

        pstmt.executeUpdate();
    }

    public void changePassword(String newPassword) throws SQLException {
        if (id == null)
            throw new RuntimeException("Working at unspecified object");

        PreparedStatement pstmt = DB.conn.prepareStatement(
                "UPDATE users SET password = ? WHERE id = ?");
        pstmt.setString(1, newPassword);
        pstmt.setInt(2, id);

        pstmt.executeUpdate();
    }

    public void changeAdministrativePermissions(String permission) throws SQLException {
        if (id == null)
            throw new RuntimeException("Working at unspecified object");

        PreparedStatement pstmt = DB.conn.prepareStatement(
                "UPDATE users SET isAdmin = ? WHERE id = ?");
        pstmt.setString(1, permission);
        pstmt.setInt(2, id);

        pstmt.executeUpdate();
    }

    public void setLoggingPermissions(String signIn) throws SQLException {
        if (id == null)
            throw new RuntimeException("Working at unspecified object");

        PreparedStatement pstmt = DB.conn.prepareStatement(
                "UPDATE users SET signInBy = ? WHERE id = ?");
        pstmt.setString(1, signIn);
        pstmt.setInt(2, id);

        pstmt.executeUpdate();
    }
    
    public JSONObject getGeneralInfo() throws SQLException, NotFoundException {
    	JSONObject data = new JSONObject();
    	PreparedStatement stmt = DB.conn.prepareStatement("Select id,name,email,isAdmin,signInBy,card,(Select If(action = 'in',zone,NULL) From events Where user = users.id Order By ts Desc, id Desc Limit 1) zone From users Where id = ?");
    	stmt.setInt(1,id);
    	ResultSet rs = stmt.executeQuery();
    	if (!rs.next()) {
    		throw new NotFoundException("User");
    	}
    	data.put("id",rs.getInt("id"));
    	data.put("name",rs.getString("name"));
    	data.put("email",rs.getString("email"));
    	data.put("isAdmin",rs.getBoolean("name"));
    	data.put("signInBy",rs.getString("signInBy"));
    	data.put("card",rs.getString("card"));
    	data.put("zone",rs.getInt("zone"));
    	return data;
    }
    
    public Zone getZone() throws SQLException {
    	PreparedStatement stmt = DB.conn.prepareStatement("Select zone,action From events Where user = ? Order By ts Desc, id Desc Limit 1");
    	stmt.setInt(1,id);
    	ResultSet rs = stmt.executeQuery();
    	if (!rs.next()) {
    		return null;
    	}
    	if (rs.getString("action").equals("in")) {
    		return new Zone(rs.getInt("zone"));
    	} else {
    		return null;
    	}
    }
    
    public void getIn(Integer zoneID) throws SQLException, NotFoundException {
    	boolean transactionBeganEarlier = false;
    	try {
    		transactionBeganEarlier = !DB.conn.getAutoCommit();
    		DB.conn.setAutoCommit(false);
    		
	    	Zone zone = new Zone(zoneID);
	    	Zone currentZone = getZone();
	    	if (currentZone != null) {
				/* change zones */
				try {
					getOut(true);
					try {
						java.lang.Thread.sleep(0,100);
					} catch (InterruptedException e) {}
				} catch(ImpossibleException e) {}
	    	}
			new Event(id,zone.getID(),"in","www");
			DB.conn.commit();
    	} catch(SQLException e) {
    		e.printStackTrace();
    		DB.conn.rollback();
    	} finally {
    		if (!transactionBeganEarlier) {
    			DB.conn.setAutoCommit(true);    			
    		}
    	}
    }
    
    public void getOut(boolean returnToParent) throws SQLException, ImpossibleException, NotFoundException {
    	Zone zone = getZone();
    	if (zone == null) {
    		throw new ImpossibleException();
    	}
    	new Event(id,zone.getID(),"out","www");
    }
    
    public JSONArray getLastEvents() throws SQLException {
    	EventReport report = new EventReport(new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000)),new Date());
    	report.setLimit(10);
    	return report.get(id);
    }
    
    public JSONArray getList() throws SQLException {
    	JSONArray data = new JSONArray();
    	PreparedStatement stmt = DB.conn.prepareStatement("Select id,name,email From users Order By name Asc");
    	ResultSet rs = stmt.executeQuery();
    	while (rs.next()) {
    		JSONObject row = new JSONObject();
    		row.put("id",rs.getInt("id"));
    		row.put("name",rs.getString("name"));
    		row.put("email",rs.getString("email"));
    		if (id == rs.getInt("id")) {
    			row.put("signedIn",true);
    		}
    		data.put(row);
    	}
    	return data;
    }
    
    public static String getHash(String string) {
    	MessageDigest md = null;
    	try {
    		md = MessageDigest.getInstance("SHA-256");    		
    	} catch(NoSuchAlgorithmException e) {
    		
    	}
        
        md.update(string.getBytes());
 
        byte byteData[] = md.digest();
 
        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
 
        return sb.toString();
    }

}