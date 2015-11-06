package com.pwr.ajax;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pwr.main.*;
import com.pwr.db.DB;

/**
 * Servlet implementation class Ajax
 */
public class Ajax extends HttpServlet {
	private static final long serialVersionUID = 15L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Ajax() throws ClassNotFoundException {
        super();
        DB.connect();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AjaxReturn ajxrtn = new AjaxReturn(response);
		int[] arr = new int[2];
		arr[0] = 1;
		
		ajxrtn.send("OK",arr,null);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AjaxReturn ajxrtn = new AjaxReturn(response);
		if (request.getParameter("module").equals("user")) {
			user(request,response);
		} else if (request.getParameter("module").equals("zone")) {
			zone(request,response);
		} else if (request.getParameter("module").equals("report")) {
			report(request,response);
		} else {
			ajxrtn.send("EoF",null,null);
		}
	}
	
	protected void zone(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AjaxReturn ajxrtn = new AjaxReturn(response);
		try {
			if (request.getParameter("getList") != null) {
				ajxrtn.send("OK",Zone.getList(),null);
			} else if (request.getParameter("add") != null) {
				try {
					Integer parent = null;
					if (request.getParameter("parent") != null && request.getParameter("parent").length() > 0) {
						parent = new Integer(request.getParameter("parent"));
					}
					new Zone(request.getParameter("name"),parent);
					ajxrtn.send("OK");
				} catch(ExistsException e) {
					ajxrtn.send("Exists",e.getMessage());
				} catch(InvalidException e) {
					ajxrtn.send("Invalid",e.getMessage());
				}
				
			} else if (request.getParameter("rename") != null) {
				try {
					Zone zone = new Zone(request.getParameter("id"));
					zone.rename(request.getParameter("name"));
					ajxrtn.send("OK");
				} catch(ExistsException e) {
					ajxrtn.send("Exists",e.getMessage());
				} catch(InvalidException e) {
					ajxrtn.send("Invalid",e.getMessage());
				}
			} else if (request.getParameter("remove") != null) {
				Zone zone = new Zone(request.getParameter("id"));
				zone.remove();
				ajxrtn.send("OK");
			} else {
				ajxrtn.send("EoF");
			}
		} catch(SQLException e) {
			e.printStackTrace();
			ajxrtn.send("Err", "SQL",null);
		}
	}

	protected void report(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AjaxReturn ajxrtn = new AjaxReturn(response);
		
		HttpSession session = request.getSession();
		User user = null;
		if (session != null && session.getAttribute("userID") != null) {
			user = new User((Integer) session.getAttribute("userID"));
		}
		
		if (user != null) {
			try {
				if (request.getParameter("getZonesReport") != null) {
					
					ZonesReport report = new ZonesReport(request.getParameter("datetime"));
					ajxrtn.send("OK",report.get());
					
				} else if (request.getParameter("getEventsTimeReport") != null) {
					JSONObject data = new JSONObject();
					int userID = new Integer(request.getParameter("userID")).intValue();
					EventReport events = new EventReport(request.getParameter("from"),request.getParameter("to")); 
					data.put("events",events.get(userID));
					
					TimeReport time = new TimeReport(request.getParameter("from"),request.getParameter("to"));
					data.put("time",time.get(userID));
					if (data.getJSONArray("events").length() > 0) {
						ajxrtn.send("OK",data);						
					} else {
						ajxrtn.send("NoRows");
					}
					
				} else {
					ajxrtn.send("EoF");
				}
			} catch(InvalidException e) {
				ajxrtn.send("Invalid","Date");
			} catch(SQLException e) {
				e.printStackTrace();
				ajxrtn.send("Err","SQL");
			}
		} else {
			ajxrtn.send("AccDen");
		}
	}
	
	protected void user(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AjaxReturn ajxrtn = new AjaxReturn(response);
		
		HttpSession session = request.getSession();
		User user = null;
		if (request.getParameter("signIn") != null) {
			try {
				session.setAttribute("userID", new User(request.getParameter("email"),request.getParameter("password")).getID());
				session.setMaxInactiveInterval(30*60);
				ajxrtn.send("OK");
			} catch(AccessDeniedException e) {
				ajxrtn.send("AccDen");
			} catch(SQLException e) {
				e.printStackTrace();
				ajxrtn.send("Err", "SQL");
			}
		} else if (request.getParameter("signOut") != null) {
			if (session != null) {
	            session.invalidate();
	        }
			ajxrtn.send("OK");
		} else if (session != null && session.getAttribute("userID") != null) {
			user = new User((Integer) session.getAttribute("userID"));
		} else {
			ajxrtn.send("AccDen");
		}
		
		if (user != null) {
			try {
				if (request.getParameter("getList") != null) {
					ajxrtn.send("OK",user.getList());
				} else if (request.getParameter("getPanel") != null) {
					JSONObject data = new JSONObject();
					data.put("generalInfo",user.getGeneralInfo());
					data.put("zones",Zone.getList());
					ajxrtn.send("OK",data,null);
				} else if (request.getParameter("getIn") != null) {
					user.getIn(new Integer(request.getParameter("zone")));
					ajxrtn.send("OK");
				} else if (request.getParameter("getOut") != null) {
					try {
						user.getOut(false);
						ajxrtn.send("OK");
					} catch(ImpossibleException e) {
						ajxrtn.send("Impossible",null,null);
					}
				} else if (request.getParameter("getLastEvents") != null) {
					JSONArray arr = user.getLastEvents();
					if (arr.length() > 0) {
						ajxrtn.send("OK",arr);						
					} else {
						ajxrtn.send("NoRows");
					}
					
				} else {
					ajxrtn.send("EoF",null,null);
				}
			} catch(SQLException e) {
				e.printStackTrace();
				ajxrtn.send("Err", "SQL",null);
			} catch(NotFoundException e) {
				e.printStackTrace();
				ajxrtn.send("NotFound",e.getMessage(),null);
			}
		}
	}
	
}
