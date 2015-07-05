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

import xdi2.transport.registry.impl.uri.UriMessagingTargetRegistry;

public class CommandUtil {

	private static Map<String, UriMessagingTargetRegistry> uriMessagingTargetRegistries = new HashMap<String, UriMessagingTargetRegistry> ();

	public static UriMessagingTargetRegistry getUriMessagingTargetRegistry(String applicationContextPath) throws IOException {

		if (uriMessagingTargetRegistries.containsKey(applicationContextPath)) return uriMessagingTargetRegistries.get(applicationContextPath);

		File applicationContextFile = new File(applicationContextPath);
		if (! applicationContextFile.exists()) throw new FileNotFoundException(applicationContextPath + " not found");

		Resource applicationContextResource = new FileSystemResource(applicationContextFile);
		ApplicationContext applicationContext = makeApplicationContext(applicationContextResource);

		UriMessagingTargetRegistry uriMessagingTargetRegistry = (UriMessagingTargetRegistry) applicationContext.getBean("UriMessagingTargetRegistry");
		uriMessagingTargetRegistries.put(applicationContextPath, uriMessagingTargetRegistry);

		return uriMessagingTargetRegistry;
	}

	private static ApplicationContext makeApplicationContext(Resource... resources) {

		GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
		applicationContext.load(resources);
		applicationContext.refresh();

		return applicationContext;
	}
}
