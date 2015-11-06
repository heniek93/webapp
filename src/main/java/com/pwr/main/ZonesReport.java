package com.pwr.main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pwr.db.DB;

public class ZonesReport extends Report {
	
	public ZonesReport(String date) throws InvalidException {
		super(date);
	}
	
	public ZonesReport(Date date) {
		super(date);
	}
	
	public ZonesReport(Date from, Date to) {
		super(from,to);
	}
	
	public ZonesReport(String from, String to) throws InvalidException {
		super(from,to);
	}
	
	public JSONArray get() throws SQLException {
		JSONArray data = new JSONArray();
		PreparedStatement stmt = DB.conn.prepareStatement(""+
"				Select z.id,z.name zoneName,z.parent,u.name userName,u.email From (Select"+
"						("+
"							Select"+
"								If (action='in',zone,NULL)"+
"							From"+
"								events"+
"							Where"+
"								user = users.id"+
"							And"+
"								ts <= ?"+
"							Order By"+
"								ts Desc"+
"							Limit 1"+
"					    ) as currentZone,"+
"					    id,"+
"					    name,"+
"					    email"+
"					From"+
"						users) u"+
"					Right Join"+
"						zones z"+
"					On"+
"						z.id = u.currentZone"+
"					/*Group By z.id*/"+
"					Order By parent Is Not Null,z.name,u.name"
				);
		stmt.setString(1,toSQLDateTime(from));
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			JSONObject row = new JSONObject();
			row.put("id",rs.getInt("id"));
			row.put("zoneName",rs.getString("zoneName"));
			row.put("parent",rs.getInt("parent"));
			row.put("userName",rs.getString("userName"));
			row.put("email",rs.getString("email"));
			data.put(row);
		}
		return data;
	}
}
