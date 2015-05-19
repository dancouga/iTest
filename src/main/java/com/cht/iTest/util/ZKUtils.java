package com.cht.iTest.util;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import com.cht.iTest.def.Status;

public class ZKUtils {

	public static ListModel<?> createListEnumModel(String className) {
		Class<?> enumType = null;

		try {
			enumType = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return new ListModelList<>(enumType.getEnumConstants());
	}

	public static String statusCss(Status status) {
		String color = "font-weight:bold;color:";

		switch (status) {
		case Ready:
			color += "blue;";
			break;
		case Fail:
			color += "red;";
			break;
		case Playing:
			color += "orange;";
			break;
		case Done:
			color += "green;";
			break;
		}

		return color;
	}

	public static ArgBuilder argBuilder() {
		return new ArgBuilder();
	}

	public static void msgYN(String msg, String title, MessageAction action) {
		Messagebox.show(msg, title != null ? title : "注意", Messagebox.YES | Messagebox.NO, Messagebox.EXCLAMATION, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if (Messagebox.ON_NO.equals(event.getName())) {
					event.stopPropagation();
					return;
				}

				action.doIfYes();
			}
		});
	}

	public static void vmRefresh(Object vm, String... propertyies) {
		for (String prop : propertyies) {
			BindUtils.postNotifyChange(null, null, vm, prop);
		}
	}

	public static void msgYN(String msg, MessageAction action) {
		msgYN(msg, null, action);
	}

	public interface MessageAction {

		void doIfYes() throws Exception;

	}

	public static class ArgBuilder {

		private Map<String, Object> param = new HashMap<String, Object>();

		private ArgBuilder() {

		}

		public ArgBuilder put(String key, Object value) {
			param.put(key, value);
			return this;
		}

		public Map<String, Object> build() {
			return param;
		}

	}

}
