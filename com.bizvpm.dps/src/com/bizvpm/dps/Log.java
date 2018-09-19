package com.bizvpm.dps;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public class Log {
	
	static String PLUGIN_ID = null;

	static Bundle BUNDLE = null;

	public static void log(int severity, String message, Throwable throwable) {
		Platform.getLog(BUNDLE).log(
				new Status(severity, PLUGIN_ID, message, throwable));
	}

	public static void logInfo(String message) {
		log(Status.INFO, message, null);
	}

	public static void logWarning(String message) {
		log(Status.WARNING, message, null);
	}

	public static void logOK(String message) {
		log(Status.OK, message, null);
	}

	public static void logError(String message, Throwable throwable) {
		log(Status.ERROR, message, throwable);
	}

	public static void logError(Throwable throwable) {
		log(Status.ERROR, throwable.getMessage(), throwable);
	}

}
