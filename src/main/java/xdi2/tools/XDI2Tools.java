package xdi2.tools;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class XDI2Tools {

	public static void main(String... args) throws Exception {

		String applicationContextPath;

		if (args.length == 1) {

			applicationContextPath = args[0];
		} else if (args.length == 0) {

			applicationContextPath = "applicationContext.xml";
		} else {

			usage();
			return;
		}

		File applicationContextFile = new File(applicationContextPath);
		if (! applicationContextFile.exists()) throw new FileNotFoundException(applicationContextPath + " not found");

		Resource applicationContextResource = new FileSystemResource(applicationContextFile);

		makeApplicationContext(applicationContextResource);
	}

	private static void usage() {

		System.out.println("Usage: java -jar xdi2-tools-XXX.one-jar.jar <path-to-applicationContext.xml>");
	}

	private static ApplicationContext makeApplicationContext(Resource... resources) {

		GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
		applicationContext.load(resources);
		applicationContext.refresh();

		return applicationContext;
	}
}
