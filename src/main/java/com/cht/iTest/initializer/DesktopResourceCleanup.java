package com.cht.iTest.initializer;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.util.DesktopCleanup;

import com.cht.iTest.def.DesktopResource;
import com.cht.iTest.selenium.App;

public class DesktopResourceCleanup implements DesktopCleanup {

	public void cleanup(Desktop desktop) throws Exception {
		App app = (App) desktop.removeAttribute(DesktopResource.SeleniumApp.name());

		if (app != null) {
			System.out.println("Not Normal way to Close Test Process!!!");
			app.exit();
		}
	}

}