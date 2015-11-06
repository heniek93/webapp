package com.pwr.main;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

abstract public class Report {
	protected Date from,to;
	
	protected int limit = 1000;
	
	public Report(Date date) {
		set(date,date);
	}
	
	public Report(Date from, Date to) {
		set(from,to);
	}
	
	public Report(String from, String to) throws InvalidException {
		set(toJavaDate(from),toJavaDate(to));
	}
	
	public Report(String date) throws InvalidException {
		set(toJavaDate(date),toJavaDate(date));
	}
	
	public void setLimit(Integer limit) {
		if (limit == null) {
			limit = 1000;
		} else if (limit < 0) {
			throw new InvalidParameterException();
		}
		this.limit = limit;
	}
	
	public void set(Date from, Date to) {
		this.from = from;
		this.to = to;
	}
	
	static String toSQLDateTime(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}
    
    static Date toJavaDate(String date) throws InvalidException {
    	try {
    		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);    		
    	} catch(ParseException e) {
    		throw new InvalidException(e.getMessage());
    	}
		
	}
    
    static String toTime(int totalSeconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;

        return String.format("%02d:%02d:%02d",hours,minutes,seconds);
    }
    
    public static String toSQLDate(Date date) {
    	return new SimpleDateFormat("yyyy-MM-dd").format(date);    	
    }
	
};