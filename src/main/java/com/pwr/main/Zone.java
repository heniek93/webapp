package com.pwr.main;

import com.pwr.db.DB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Bartosz Ufnal on 26.04.15.
 */
public class Zone {

    private Integer id;
    private String name;
    private Integer parent;

    public Zone(Integer id) {
        this.id = id;
        name = null;
        parent = null;
    }
    
    public Zone(String id) {
    	this(new Integer(id));
    }
    
    public Zone(String name, Integer parent) throws InvalidException,ExistsException,SQLException {
        id = null;
        this.name = name;
        this.parent = null;

        if (parent != null && parent > 0) {
        	this.parent = parent;
        }

        add();
    }

    private void parseName() throws InvalidException,ExistsException,SQLException {
    	name = name.replaceAll("\\<.*?\\>", "").trim();
    	if (name.length() < 3) {
    		throw new InvalidException("ZoneName");
    	}
    	if (exists()) {
    		throw new ExistsException("Zone");
    	}
    }
    
    public void add() throws SQLException, InvalidException, ExistsException {
    	
    	parseName();
    	
        PreparedStatement stmt = DB.conn.prepareStatement(
                "INSERT INTO zones (name, parent) VALUES (?, ?)");
        stmt.setString(1, name);
        if (parent == null) {
        	stmt.setNull(2,java.sql.Types.INTEGER);
        } else {
        	stmt.setInt(2, parent);        	
        }
        

        stmt.executeUpdate();
    }

    public boolean exists() throws SQLException {
    	PreparedStatement stmt = DB.conn.prepareStatement("Select id From zones Where name = ? And id != ?");
    	stmt.setString(1,name);
    	if (id == null) {
    		stmt.setInt(2,0);
    	} else {
    		stmt.setInt(2,id);    		
    	}
    	ResultSet rs = stmt.executeQuery();
    	return rs.next();
    }
    
    public void remove() throws SQLException {
        if (id == null) {
            throw new RuntimeException("Working at unspecified object");
        }

        boolean transactionAlready = !DB.conn.getAutoCommit();
        
        try {
	        ArrayList<Integer> children = getChildren();
	        DB.conn.setAutoCommit(false);
	    	for (Integer childID : children) {
	    		Zone zone = new Zone(childID);
	    		zone.remove();
	    	}
	    	
	        if (hasEvents()) {
	        	PreparedStatement stmt = DB.conn.prepareStatement("Update zones Set removed = 1 Where id = ?");
	        	stmt.setInt(1, id);
	            stmt.executeUpdate();
	        } else {
	        	PreparedStatement stmt = DB.conn.prepareStatement("Delete From zones Where id = ?");
	            stmt.setInt(1, id);
	            stmt.executeUpdate();
	        }
        } catch(SQLException e) {
        	DB.conn.rollback();
        	e.printStackTrace();
        } finally {
        	if (!transactionAlready) {
        		DB.conn.setAutoCommit(true);
        	}
        }
        
    }
    
    private ArrayList<Integer> getChildren() throws SQLException {
    	ArrayList<Integer> arr = new ArrayList<Integer>();
    	PreparedStatement stmt = DB.conn.prepareStatement("Select id From zones Where parent = ? And removed = 0");
    	stmt.setInt(1, id);
    	ResultSet rs = stmt.executeQuery();
    	while (rs.next()) {
    		arr.add(rs.getInt("id"));
    	}
    	return arr;
    }
    
    private boolean hasEvents() throws SQLException {
    	PreparedStatement stmt = DB.conn.prepareStatement("Select Count(*) count From events Where zone = ?");
    	stmt.setInt(1,id);
    	ResultSet rs = stmt.executeQuery();
    	rs.next();
    	return rs.getInt("count") > 0;
    }

    public void rename(String name) throws SQLException,InvalidException,ExistsException {
    	this.name = name;
    	parseName();
        
    	if (id == null)
            throw new RuntimeException("Working at unspecified object");
        
        PreparedStatement pstmt = DB.conn.prepareStatement("Update zones Set name = ? Where id = ?");
        pstmt.setString(1, name);
        pstmt.setInt(2, id);
        pstmt.executeUpdate();
    }
    
    public void fetch() throws SQLException,NotFoundException {
    	PreparedStatement stmt = DB.conn.prepareStatement("Select name,parent From zones Where id = ?");
    	stmt.setInt(1,id);
    	ResultSet rs = stmt.executeQuery();
    	if (!rs.next()) {
    		throw new NotFoundException("Zone");
    	}
    	parent = rs.getInt("parent");
    	name = rs.getString("name");
    }
    
    public Integer getID() {
    	return id;
    }
    
    public Integer getParent() throws SQLException,NotFoundException {
    	if (parent == null) {
    		fetch();
    	}
    	return parent;
    }
    
    public String getName() throws SQLException,NotFoundException {
    	if (name == null) {
    		fetch();
    	}
    	return name;
    }

    static public JSONArray getList() throws SQLException {
    	JSONArray data = new JSONArray();
    	PreparedStatement stmt = DB.conn.prepareStatement("Select id,parent,name From zones Where removed = 0 Order By parent Is Not Null,name");
    	ResultSet rs = stmt.executeQuery();
    	while (rs.next()) {
    		JSONObject row = new JSONObject();
    		row.put("id",rs.getInt("id"));
    		row.put("name",rs.getString("name"));
    		row.put("parent",rs.getInt("parent"));
    		data.put(row);
    	}
    	return data;
    }
    
}