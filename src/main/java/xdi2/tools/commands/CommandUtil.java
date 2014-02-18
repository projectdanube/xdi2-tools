package xdi2.tools.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import xdi2.transport.impl.http.registry.HttpMessagingTargetRegistry;

public class CommandUtil {

	private static Map<String, HttpMessagingTargetRegistry> httpMessagingTargetRegistries = new HashMap<String, HttpMessagingTargetRegistry> ();

	public static HttpMessagingTargetRegistry getHttpMessagingTargetRegistry(String applicationContextPath) throws IOException {

		if (httpMessagingTargetRegistries.containsKey(applicationContextPath)) return httpMessagingTargetRegistries.get(applicationContextPath);

		File applicationContextFile = new File(applicationContextPath);
		if (! applicationContextFile.exists()) throw new FileNotFoundException(applicationContextPath + " not found");

		Resource applicationContextResource = new FileSystemResource(applicationContextFile);
		ApplicationContext applicationContext = makeApplicationContext(applicationContextResource);

		HttpMessagingTargetRegistry httpMessagingTargetRegistry = (HttpMessagingTargetRegistry) applicationContext.getBean("HttpMessagingTargetRegistry");
		httpMessagingTargetRegistries.put(applicationContextPath, httpMessagingTargetRegistry);

		return httpMessagingTargetRegistry;
	}

	private static ApplicationContext makeApplicationContext(Resource... resources) {

		GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
		applicationContext.load(resources);
		applicationContext.refresh();

		return applicationContext;
	}
}
