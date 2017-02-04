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

import xdi2.transport.registry.impl.uri.UriMessagingContainerRegistry;

public class CommandUtil {

	private static Map<String, UriMessagingContainerRegistry> uriMessagingContainerRegistries = new HashMap<String, UriMessagingContainerRegistry> ();

	public static UriMessagingContainerRegistry getUriMessagingContainerRegistry(String applicationContextPath) throws IOException {

		if (uriMessagingContainerRegistries.containsKey(applicationContextPath)) return uriMessagingContainerRegistries.get(applicationContextPath);

		File applicationContextFile = new File(applicationContextPath);
		if (! applicationContextFile.exists()) throw new FileNotFoundException(applicationContextPath + " not found");

		Resource applicationContextResource = new FileSystemResource(applicationContextFile);
		ApplicationContext applicationContext = makeApplicationContext(applicationContextResource);

		UriMessagingContainerRegistry uriMessagingContainerRegistry = (UriMessagingContainerRegistry) applicationContext.getBean("UriMessagingContainerRegistry");
		uriMessagingContainerRegistries.put(applicationContextPath, uriMessagingContainerRegistry);

		return uriMessagingContainerRegistry;
	}

	private static ApplicationContext makeApplicationContext(Resource... resources) {

		GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
		applicationContext.load(resources);
		applicationContext.refresh();

		return applicationContext;
	}
}
