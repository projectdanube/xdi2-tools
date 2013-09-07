package xdi2.tools.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import xdi2.server.registry.HttpEndpointRegistry;

public class CommandUtil {

	public static HttpEndpointRegistry getHttpEndpointRegistry(String applicationContextPath) throws IOException {

		File applicationContextFile = new File(applicationContextPath);
		if (! applicationContextFile.exists()) throw new FileNotFoundException(applicationContextPath + " not found");

		Resource applicationContextResource = new FileSystemResource(applicationContextFile);
		ApplicationContext applicationContext = makeApplicationContext(applicationContextResource);

		return (HttpEndpointRegistry) applicationContext.getBean("HttpEndpointRegistry");
	}

	private static ApplicationContext makeApplicationContext(Resource... resources) {

		GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
		applicationContext.load(resources);
		applicationContext.refresh();

		return applicationContext;
	}
}
