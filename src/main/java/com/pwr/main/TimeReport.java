package com.pwr.main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.pwr.db.DB;

public class TimeReport extends Report {
	public TimeReport(Date date) {
		super(date);
	}
	
	public TimeReport(Date from, Date to) {
		super(from,to);
	}
	
	public TimeReport(String from, String to) throws InvalidException {
		super(from,to);
	}
	
	@Override
	public void set(Date from, Date to) {
		Date now = new Date();
		this.from = from;
		if (to.compareTo(now) > 0) {
			this.to = now;
		} else {
			this.to = to;			
		}
		
	}
	
	public String get(int userID) throws SQLException {
		boolean workingBefore = false;
		boolean workingAfter = false;
		PreparedStatement stmt = DB.conn.prepareStatement("Select action From events Where user = ? And ts < ? Order By ts Desc Limit 1");
		stmt.setInt(1,userID);
		stmt.setString(2,toSQLDateTime(from));
		ResultSet rs = stmt.executeQuery();
		if (rs.next() && rs.getString("action").equals("in")) {
			workingBefore = true;
		}
		stmt.close();
		
		stmt = DB.conn.prepareStatement("Select action From events Where user = ? And ts < ? Order By ts Desc Limit 1");
		stmt.setInt(1,userID);
		stmt.setString(2,toSQLDateTime(to));
		rs = stmt.executeQuery();
		if (rs.next() && rs.getString("action").equals("in")) {
			workingAfter = true;
		}
		stmt.close();
		
		
		
		
		
		stmt = DB.conn.prepareStatement(
				"	Select "+
				"Date_Format(first,'%Y-%m-%d %H:%i:%s'), "+
			    "Date_Format(last,'%Y-%m-%d %H:%i:%s'), "+
			    "worktime, "+
			    "unix_timestamp(?)-unix_timestamp(last)+worktime workingAfter, "+
			    "unix_timestamp(first)-unix_timestamp(?)+worktime workingBefore, "+
			    "unix_timestamp(?)-unix_timestamp(last)+unix_timestamp(first)-unix_timestamp(?)+worktime workingBeforeAfter "+
					"From (Select "+
					"	Min(start) first, "+
					"	Max(end) last, "+
					"	Sum(Timediff(end,start)) as worktime "+
					"From "+
					"	( "+
					"		Select "+
					"			ts start, "+
					"			( "+
					"				Select "+
					"					ts "+
					"				From "+
					"					events f "+
					"				Where "+
					"					f.ts > e.ts "+
					"				And "+
					"					f.ts <= ? "+
					"				And "+
					"					action = 'out' "+
					"				And "+
					"					user = e.user "+
					"				Order By "+
					"					f.ts Asc "+
					"				Limit 1 "+
					"			) as end "+
					"		From "+
					"			events e "+
					"		Where "+
					"			action = 'in' "+
					"		And "+
					"			ts Between ? And ? "+
					"		And "+
					"			user = ? "+
					"	) as sub) subsub"
				);
		stmt.setString(1,toSQLDateTime(to));
		stmt.setString(2,toSQLDateTime(from));
		stmt.setString(3,toSQLDateTime(to));
		stmt.setString(4,toSQLDateTime(from));
		stmt.setString(5,toSQLDateTime(to));
		stmt.setString(6,toSQLDateTime(from));
		stmt.setString(7,toSQLDateTime(to));
		stmt.setInt(8,userID);
		rs = stmt.executeQuery();
		if (!rs.next()) {
			return "00:00:00";
		} else {
			if (workingBefore && workingAfter) { 
				return Report.toTime(rs.getInt("workingBeforeAfter"));
			} else if (workingBefore) {
				return Report.toTime(rs.getInt("workingBefore"));
			} else if (workingAfter) {
				return Report.toTime(rs.getInt("workingAfter"));
			} else {
				return Report.toTime(rs.getInt("worktime"));
			}
		}
		
	}
}
