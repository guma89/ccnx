package org.ccnx.android.apps.ui.callback;

public class GuiUpdaterWrapper {
	private static GuiUpdaterWrapper instance;
	private static CallbackGuiUpdater guiUpdater;
	
	private GuiUpdaterWrapper() {
		// singleton (Uncle Bob: arrrr..)
	}
	
	public static GuiUpdaterWrapper getInstance() {
		if(instance == null) {
			instance = new GuiUpdaterWrapper();
		}
		return instance;
	}
	
	public static void createGuiUpdaterIfNotExists(String username, String namespace, String remotehost, String remotePort) {
		if(guiUpdater == null) {
			guiUpdater = new CallbackGuiUpdater(username, namespace, remotehost, remotePort);
		}
	}
	
	public static CallbackGuiUpdater getGuiUpdater() {
		return guiUpdater;
	}

}
