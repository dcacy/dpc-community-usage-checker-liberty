package com.ibm.cacy.communityuserchecker;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * Servlet implementation class Checker
 */
@WebServlet("/api")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final boolean DEBUG = false;
	private static Logger logger = Logger.getLogger(MainServlet.class.getName());

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		debug("in doGet");
		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
		JSONObject properties = new JSONObject();
		if ( VCAP_SERVICES != null ) {
			debug("found VCAP_SERVICES: " + VCAP_SERVICES);
			properties = JSONObject.parse(VCAP_SERVICES);
			debug("properties are " + properties.toString());
		} else {
			Properties props = new Properties();
			props.load(getServletContext().getResourceAsStream("/WEB-INF/connections.properties"));
			Enumeration<Object> e = props.keys();
			while (e.hasMoreElements()) {
				String key = (String)e.nextElement();
				debug("prop key is " + key + " and value is " + props.getProperty(key));
				properties.put(key, props.getProperty(key));
			}
		}
		String action = request.getParameter("action");
		debug("action is [" + action + "]");
		if ( action != null ) {
			if ( action.equals("getAllCommunities")) {
				JSONObject result = new Community().getAllCommunities(properties);
				response.setContentType(MediaType.APPLICATION_JSON);
				response.getWriter().print(result);
			} else if ( action.equals("getCommunityDetails")) {
				String id = request.getParameter("id");
				if ( id != null && !id.equals("")) {
				JSONObject files = new Community().getCommunityFiles(properties, id);
				JSONObject activity = new Community().getCommunityActivity(properties, id);
				JSONObject members = new Community().getCommunityMembers(properties, id);
				JSONObject subcommunities = new Community().getSubcommunities(properties, id);
				JSONArray result = new JSONArray();
				result.add(files);
				result.add(activity);
				result.add(members);
				result.add(subcommunities);
				response.setContentType(MediaType.APPLICATION_JSON);
				response.getWriter().print(result);
				} else {
					JSONObject error = new JSONObject();
					error.put("error", "no Community ID provided");
					response.setContentType(MediaType.APPLICATION_JSON);
					response.getWriter().print(error);
				}
			} else {
				response.setContentType(MediaType.TEXT_PLAIN);
				response.getWriter().print("No action provided");
			}
		} else {
			response.setContentType(MediaType.TEXT_PLAIN);
			response.getWriter().print("No action provided");
		}
	}

	
	private void debug(Object o) {
		if (DEBUG) {
			logger.info(o.toString());
		}
			
			
	}
}
