package com.ibm.cacy.communityuserchecker;

public class Activity {

//	private String verb;
	private String title;
	private String published;
	private OpenSocial openSocial;
	private Connections connections;
	private Actor actor;
	
	
	private class OpenSocial{}
	private class Connections{
		private String containerName;
		private String plainTitle;
		
		public String getContainerName() {
			return containerName;
		}
		public void setContainerName(String name){
			containerName = name;
		}
		public String getPlainTitle() {
			return plainTitle;
		}
		public void setPlainTitle(String title) {
			plainTitle = title;
		}
	}
	
	private class Actor{
		private String displayName;
		public String getDisplayName() {
			return displayName;
		}
		public void setDisplayName(String name) {
			displayName = name;
		}
	}
}


