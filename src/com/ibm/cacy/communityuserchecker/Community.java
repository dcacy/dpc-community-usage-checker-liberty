package com.ibm.cacy.communityuserchecker;

import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.json.xml.XMLToJSONTransformer;

public class Community {

	private static final boolean DEBUG = false;
	private static Logger logger = Logger.getLogger(MainServlet.class.getName());

	public JSONObject getAllCommunities(JSONObject properties) 
	{
		final String ALL_COMMUNITIES_URI = "/communities/service/atom/communities/all?ps=500";
		JSONObject result = new JSONObject();
		try {
			
			Executor executor = Executor.newInstance().auth(properties.get("CONNECTIONS_USERID").toString(), properties.get("CONNECTIONS_PASSWORD").toString());
    		URI serviceURI = new URI("https://" + properties.get("CONNECTIONS_HOST") + ALL_COMMUNITIES_URI).normalize();
    		InputStream communityXmlStream = executor.execute(Request.Get(serviceURI)
			    ).returnContent().asStream();
    		
    		String jsonString = XMLToJSONTransformer.transform(communityXmlStream);
    		JSONObject communityJson = JSONObject.parse(jsonString);
    		
    		JSONArray entries = (JSONArray)((JSONObject)(communityJson.get("feed"))).get("entry");
    		debug("size of entries is " + entries.size());
    		@SuppressWarnings("unchecked")
			Iterator<JSONObject> it = entries.iterator();
    		JSONArray communityInfo = new JSONArray();
    		while ( it.hasNext() ) {
    			JSONObject entry = it.next();
    			JSONObject info = new JSONObject();
    			info.put("title", ((JSONObject)(entry.get("title"))).get("content"));
    			info.put("id",  ((entry.get("communityUuid"))));
    			info.put("updated", ((entry.get("updated"))));
    			info.put("owner", ((JSONObject)(entry.get("author"))).get("name"));
    			info.put("created", ((entry.get("published"))));
    			info.put("membercount", ((entry.get("membercount"))));
    			communityInfo.add(info);
    		}
    		result.put("info", communityInfo);
		}
		catch(Exception e) {
			e.printStackTrace();
			result.put("error", e.getMessage());
		}
		
		return result;
	}
	
	public JSONObject getCommunityMembers(JSONObject properties, String id) {
		debug("in getCommunityMembers with id " + id);
		JSONObject result = new JSONObject();
		final String COMM_MEMBERS_URI = "/communities/service/atom/community/members?ps=1000&communityUuid=";
		
		try {
			Executor executor = Executor.newInstance().auth(properties.get("CONNECTIONS_USERID").toString(), properties.get("CONNECTIONS_PASSWORD").toString());
			URI serviceURI = new URI("https://" + properties.get("CONNECTIONS_HOST") + COMM_MEMBERS_URI + id).normalize();
			InputStream membersXmlStream = executor.execute(Request.Get(serviceURI)
			    ).returnContent().asStream();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlDoc = builder.parse(membersXmlStream);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList entries = (NodeList) xPath.compile("/feed/entry").evaluate(xmlDoc, XPathConstants.NODESET);
			JSONArray members = new JSONArray();
			for ( int i = 0; i < entries.getLength(); i++ ) {
				Node entry = entries.item(i);
				Node contributor = (Node) xPath.compile("contributor").evaluate(entry, XPathConstants.NODE);
				NodeList children = contributor.getChildNodes();
				JSONObject member = new JSONObject();
				for (int j = 0; j < children.getLength(); j++ ){
					if ( children.item(j).getNodeName().equals("email")) {
						member.put("email", children.item(j).getTextContent());
					} else if ( children.item(j).getNodeName().equals("name")) {
						member.put("name", children.item(j).getTextContent());
					} else if ( children.item(j).getNodeName().equals("snx:userState")) {
						member.put("state", children.item(j).getTextContent());
					}
				}
				debug("member is " + member.toString());
				members.add(member);
				result.put("type",  "members");
				result.put("data",  members);
	
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public JSONObject getCommunityFiles(JSONObject properties, String id) {
		debug("in getCommunityFiles with id " + id);
		JSONObject result = new JSONObject();
		final String COMM_FILES_URI = "/files/basic/api/communitycollection/"
				+ id
				+ "/feed?sC=document&pageSize=500&sortBy=title&type=communityFiles";
		
		try {
			Executor executor = Executor.newInstance().auth(properties.get("CONNECTIONS_USERID").toString(), properties.get("CONNECTIONS_PASSWORD").toString());
			URI serviceURI = new URI("https://" + properties.get("CONNECTIONS_HOST") + COMM_FILES_URI).normalize();
			InputStream filesXmlStream = executor.execute(Request.Get(serviceURI)
			    ).returnContent().asStream();
			
			debug("stream type is " + filesXmlStream.getClass());
			debug("result is " + filesXmlStream);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlDoc = builder.parse(filesXmlStream);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList entries = (NodeList) xPath.compile("/feed/entry").evaluate(xmlDoc, XPathConstants.NODESET);
			JSONArray files = new JSONArray();
			for ( int i = 0; i < entries.getLength(); i++ ) {
				Node entry = entries.item(i);
				String fileLength = (String) xPath.compile("link/@length").evaluate(entry, XPathConstants.STRING);
				Node title = (Node)xPath.compile("title").evaluate(entry, XPathConstants.NODE);
				debug("title is " + title.getTextContent());
				JSONObject file = new JSONObject();
				file.put("title", title.getTextContent());
				file.put("size",  fileLength);
				files.add(file);
			}
			result.put("type", "files");
			result.put("data",  files);

		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public JSONObject getSubcommunities(JSONObject properties, String id) {
		debug("in getSubcommunities with id " + id);
		JSONObject result = new JSONObject();
		final String SUBCOMMUNITIES_URI = "/communities/service/atom/community/subcommunities?communityUuid="
				+ id;
		
		try {
			Executor executor = Executor.newInstance().auth(properties.get("CONNECTIONS_USERID").toString(), properties.get("CONNECTIONS_PASSWORD").toString());
			URI serviceURI = new URI("https://" + properties.get("CONNECTIONS_HOST") + SUBCOMMUNITIES_URI).normalize();
			InputStream subcommunitiesXmlStream = 
				executor.execute(Request.Get(serviceURI)).returnContent().asStream();
			
//			debug("stream type is " + subcommunitiesXmlStream.getClass());
//			debug("result is " + subcommunitiesXmlStream);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlDoc = builder.parse(subcommunitiesXmlStream);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList entries = (NodeList) xPath.compile("/feed/entry").evaluate(xmlDoc, XPathConstants.NODESET);
			JSONArray subcommunities = new JSONArray();
			for ( int i = 0; i < entries.getLength(); i++ ) {
				Node entry = entries.item(i);
//				String fileLength = (String) xPath.compile("link/@length").evaluate(entry, XPathConstants.STRING);
				Node title = (Node)xPath.compile("title").evaluate(entry, XPathConstants.NODE);
				debug("title is " + title.getTextContent());
				JSONObject subcommunity = new JSONObject();
				subcommunity.put("title", title.getTextContent());
//				file.put("size",  fileLength);
				subcommunities.add(subcommunity);
			}
			result.put("type", "subcommunities");
			result.put("data",  subcommunities);

		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

public JSONObject getCommunityActivity(JSONObject properties, String id) {
		debug("in getCommunityActivity with id " + id);
		JSONObject result = new JSONObject();
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, -30);
		debug("one month ago is " + cal.getTime());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		debug("ISO IS " + df.format(cal.getTime()));
		String oneMonthAgo = df.format(cal.getTime());
		final String COMM_ACTIVITY_URI = "/connections/opensocial/basic/rest/activitystreams/urn:lsid:lconn.ibm.com:communities.community:"
			+ id 
			+ "/@all/@all?rollup=true&shortStrings=true&format=json&updatedSince" + oneMonthAgo;

		try {
			Executor executor = Executor.newInstance().auth(properties.get("CONNECTIONS_USERID").toString(), properties.get("CONNECTIONS_PASSWORD").toString());
			debug("url is " + "https://" + properties.get("CONNECTIONS_HOST") + COMM_ACTIVITY_URI);
			URI serviceURI = new URI("https://" + properties.get("CONNECTIONS_HOST") + COMM_ACTIVITY_URI).normalize();
			String activitiesString = executor.execute(Request.Get(serviceURI)
			    ).returnContent().asString();

			JSONObject json = JSONObject.parse(activitiesString);
			JSONArray activitiesList = (JSONArray)json.get("list");
			@SuppressWarnings("rawtypes")
			Iterator it = activitiesList.iterator();
			JSONArray activities = new JSONArray();
			while ( it.hasNext() ) {
				JSONObject activity = new JSONObject();
				JSONObject item = (JSONObject)it.next();
				String title = (String)((JSONObject)item.get("connections")).get("plainTitle");
				String author = (String)((JSONObject)item.get("actor")).get("displayName");
				String published = (String)item.get("published");
				activity.put("title", title);
				activity.put("author", author);
				activity.put("publishedDate", published);
				activities.add(activity);
			}
			result.put("type", "activity");
			result.put("data", activities);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void debug(Object o) {
		if (DEBUG) {
			logger.info(o.toString());
		}
	}
}
