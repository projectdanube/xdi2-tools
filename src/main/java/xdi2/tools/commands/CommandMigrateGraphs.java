package xdi2.tools.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import xdi2.core.Graph;
import xdi2.core.features.nodetypes.XdiLocalRoot;
import xdi2.core.features.nodetypes.XdiPeerRoot;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.util.CopyUtil;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.server.exceptions.Xdi2ServerException;
import xdi2.server.factory.MessagingTargetFactory;
import xdi2.server.factory.impl.RegistryGraphMessagingTargetFactory;
import xdi2.server.registry.HttpEndpointRegistry;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("migrate-graphs")
@CommandUsage("[path-to-input-applicationContext.xml] [path-to-output-applicationContext.xml")
@CommandArgs(min=2,max=2)
public class CommandMigrateGraphs implements Command {

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String inputApplicationContextPath;
		String outputApplicationContextPath;

		if (commandArgs.length == 2) {

			inputApplicationContextPath = commandArgs[0];
			outputApplicationContextPath = commandArgs[1];
		} else {

			return;
		}

		File inputApplicationContextFile = new File(inputApplicationContextPath);
		File outputApplicationContextFile = new File(outputApplicationContextPath);
		if (! inputApplicationContextFile.exists()) throw new FileNotFoundException(inputApplicationContextPath + " not found");
		if (! outputApplicationContextFile.exists()) throw new FileNotFoundException(outputApplicationContextPath + " not found");

		Resource inputApplicationContextResource = new FileSystemResource(inputApplicationContextFile);
		Resource outputApplicationContextResource = new FileSystemResource(outputApplicationContextFile);

		ApplicationContext inputApplicationContext = makeApplicationContext(inputApplicationContextResource);
		ApplicationContext outputApplicationContext = makeApplicationContext(outputApplicationContextResource);

		HttpEndpointRegistry inputHttpEndpointRegistry = (HttpEndpointRegistry) inputApplicationContext.getBean("HttpEndpointRegistry");
		HttpEndpointRegistry outputHttpEndpointRegistry = (HttpEndpointRegistry) outputApplicationContext.getBean("HttpEndpointRegistry");
		if (inputHttpEndpointRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpEndpointRegistry' not found in " + inputApplicationContextPath);
		if (outputHttpEndpointRegistry == null) throw new NoSuchBeanDefinitionException("Required bean 'HttpEndpointRegistry' not found in " + outputApplicationContextPath);

		migrateMessagingTargets(inputHttpEndpointRegistry, outputHttpEndpointRegistry);
		migrateMessagingTargetFactorys(inputHttpEndpointRegistry, outputHttpEndpointRegistry);
	}

	private static void migrateMessagingTargets(HttpEndpointRegistry inputHttpEndpointRegistry, HttpEndpointRegistry outputHttpEndpointRegistry) {

		for (String messagingTargetPath : inputHttpEndpointRegistry.getMessagingTargetPaths()) {

			MessagingTarget inputMessagingTarget = inputHttpEndpointRegistry.getMessagingTarget(messagingTargetPath);
			MessagingTarget outputMessagingTarget = outputHttpEndpointRegistry.getMessagingTarget(messagingTargetPath);

			migrateMessagingTarget(messagingTargetPath, inputMessagingTarget, outputMessagingTarget);
		}
	}

	private static void migrateMessagingTargetFactorys(HttpEndpointRegistry inputHttpEndpointRegistry, HttpEndpointRegistry outputHttpEndpointRegistry) throws Xdi2ServerException, Xdi2MessagingException {

		for (String messagingTargetFactoryPath : inputHttpEndpointRegistry.getMessagingTargetFactoryPaths()) {

			MessagingTargetFactory inputMessagingTargetFactory = inputHttpEndpointRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);
			MessagingTargetFactory outputMessagingTargetFactory = outputHttpEndpointRegistry.getMessagingTargetFactory(messagingTargetFactoryPath);

			if (inputMessagingTargetFactory == null || ! (inputMessagingTargetFactory instanceof RegistryGraphMessagingTargetFactory)) continue;
			if (outputMessagingTargetFactory == null || ! (outputMessagingTargetFactory instanceof RegistryGraphMessagingTargetFactory)) continue;

			if (! (((RegistryGraphMessagingTargetFactory) inputMessagingTargetFactory).getPrototypeMessagingTarget() instanceof GraphMessagingTarget)) continue;
			if (! (((RegistryGraphMessagingTargetFactory) outputMessagingTargetFactory).getPrototypeMessagingTarget() instanceof GraphMessagingTarget)) continue;

			Graph inputRegistryGraph = ((RegistryGraphMessagingTargetFactory) inputMessagingTargetFactory).getRegistryGraph();

			Iterator<XdiPeerRoot> inputPeerRoots = XdiLocalRoot.findLocalRoot(inputRegistryGraph).getPeerRoots();

			while (inputPeerRoots.hasNext()) {

				XdiPeerRoot inputPeerRoot = inputPeerRoots.next();

				String requestPath = messagingTargetFactoryPath + "/" + inputPeerRoot.getXriOfPeerRoot().toString();
				String messagingTargetPath = messagingTargetFactoryPath + "/" + inputPeerRoot.getXriOfPeerRoot().toString();

				MessagingTarget inputMessagingTarget = inputMessagingTargetFactory.mountMessagingTarget(inputHttpEndpointRegistry, messagingTargetFactoryPath, requestPath);
				MessagingTarget outputMessagingTarget = outputMessagingTargetFactory.mountMessagingTarget(outputHttpEndpointRegistry, messagingTargetFactoryPath, requestPath);

				migrateMessagingTarget(messagingTargetPath, inputMessagingTarget, outputMessagingTarget);
			}

			System.out.println("At path " + messagingTargetFactoryPath + " migrated from " + inputMessagingTargetFactory.getClass().getSimpleName() + " to " + outputMessagingTargetFactory.getClass().getSimpleName());
		}
	}

	private static void migrateMessagingTarget(String messagingTargetPath, MessagingTarget inputMessagingTarget, MessagingTarget outputMessagingTarget) {

		if (inputMessagingTarget == null || ! (inputMessagingTarget instanceof GraphMessagingTarget)) return;
		if (outputMessagingTarget == null || ! (outputMessagingTarget instanceof GraphMessagingTarget)) return;

		Graph inputGraph = ((GraphMessagingTarget) inputMessagingTarget).getGraph();
		Graph outputGraph = ((GraphMessagingTarget) outputMessagingTarget).getGraph();
		Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();

		CopyUtil.copyGraph(inputGraph, tempGraph, null);
		inputGraph.close();

		outputGraph.clear();
		CopyUtil.copyGraph(tempGraph, outputGraph, null);
		outputGraph.close();

		System.out.println("At path " + messagingTargetPath + " copied " + inputGraph.getRootContextNode().getAllStatementCount() + " statements from " + inputGraph.getClass().getSimpleName() + " to " + outputGraph.getRootContextNode().getAllStatementCount() + " statements in " + outputGraph.getClass().getSimpleName());
	}

	private static ApplicationContext makeApplicationContext(Resource... resources) {

		GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();
		applicationContext.load(resources);
		applicationContext.refresh();

		return applicationContext;
	}
}
