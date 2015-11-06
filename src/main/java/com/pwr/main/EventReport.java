package com.pwr.main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pwr.db.DB;

public class EventReport extends Report {
	public EventReport(Date date) {
		super(date);
	}
	
	public EventReport(Date from, Date to) {
		super(from,to);
	}
	
	public EventReport(String from, String to) throws InvalidException {
		super(from,to);
	}
	
	public JSONArray get(int userID) throws SQLException {
		JSONArray data = new JSONArray();
    	PreparedStatement stmt = DB.conn.prepareStatement("Select e.id,date_format(e.ts, '%Y-%m-%d %H:%i:%s') ts,z.id zoneID,z.name zoneName,action From events e Join zones z On z.id = e.zone Where user = ? And e.ts Between ? And ? Order By ts Desc Limit ?");//And Timediff(Now(),e.ts) < '23:59'
    	stmt.setInt(1,userID);
    	stmt.setString(2,toSQLDateTime(from));
    	stmt.setString(3,toSQLDateTime(to));
    	stmt.setInt(4,limit);
    	ResultSet rs = stmt.executeQuery();
    	while (rs.next()) {
    		JSONObject row = new JSONObject();
    		row.put("id",rs.getInt("id"));
    		row.put("ts",rs.getString("ts"));
    		row.put("zoneID",rs.getString("zoneID"));
    		row.put("zoneName",rs.getString("zoneName"));
    		row.put("action",rs.getString("action"));
    		data.put(row);
    	}
    	return data;
	}
}
