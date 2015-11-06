package com.pwr.ajax;
import java.io.IOException;
import org.json.*;
import javax.servlet.http.HttpServletResponse;

public class AjaxReturn {
	private String status;
	Object data, misc;
	HttpServletResponse response;
	
	public AjaxReturn(HttpServletResponse response) {
		this.response = response;
	}
	
	public AjaxReturn(String status, Object data, Object misc) {
		set(status,data,misc);
	}
	
	public void set(String status, Object data, Object misc) {
		this.status = status;
		this.data = data;
		this.misc = misc;
	}
	
	public void send(String status, Object data) throws IOException {
		set(status,data,null);
		send();
	}
	
	public void send(String status) throws IOException {
		set(status,null,null);
		send();
	}
	
	public void send(String status, Object data, Object misc) throws IOException {
		set(status,data,misc);
		send();
	}
	
	public void send() throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("status",this.status);
		obj.put("data",this.data);
		obj.put("misc",this.misc);
		response.getWriter().print(obj.toString());
		//response.getWriter().print("fuck");
		//obj.writeJSONString(response.getWriter());
		//obj.toString();
	}
	
}
