package xdi2.tools.commands;

import java.util.Iterator;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import xdi2.core.Graph;
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
@CommandUsage("path-to-input-applicationContext.xml path-to-output-applicationContext.xml")
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

		HttpEndpointRegistry inputHttpEndpointRegistry = CommandUtil.getHttpEndpointRegistry(inputApplicationContextPath);
		HttpEndpointRegistry outputHttpEndpointRegistry = CommandUtil.getHttpEndpointRegistry(outputApplicationContextPath);
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

			migrateMessagingTargetFactory(messagingTargetFactoryPath, inputMessagingTargetFactory, outputMessagingTargetFactory, inputHttpEndpointRegistry, outputHttpEndpointRegistry);
		}
	}

	private static void migrateMessagingTarget(String messagingTargetPath, MessagingTarget inputMessagingTarget, MessagingTarget outputMessagingTarget) {

		if (inputMessagingTarget == null || ! (inputMessagingTarget instanceof GraphMessagingTarget)) return;
		if (outputMessagingTarget == null || ! (outputMessagingTarget instanceof GraphMessagingTarget)) return;

		Graph inputGraph = ((GraphMessagingTarget) inputMessagingTarget).getGraph();
		Graph outputGraph = ((GraphMessagingTarget) outputMessagingTarget).getGraph();
		Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();

		CopyUtil.copyGraph(inputGraph, tempGraph, null);

		outputGraph.clear();
		CopyUtil.copyGraph(tempGraph, outputGraph, null);

		System.out.println("At path " + messagingTargetPath + " copied " + inputGraph.getRootContextNode().getAllStatementCount() + " statements from " + inputGraph.getClass().getSimpleName() + " to " + outputGraph.getRootContextNode().getAllStatementCount() + " statements in " + outputGraph.getClass().getSimpleName());
	}

	private static void migrateMessagingTargetFactory(String messagingTargetFactoryPath, MessagingTargetFactory inputMessagingTargetFactory, MessagingTargetFactory outputMessagingTargetFactory, HttpEndpointRegistry inputHttpEndpointRegistry, HttpEndpointRegistry outputHttpEndpointRegistry) throws Xdi2ServerException, Xdi2MessagingException {

		if (inputMessagingTargetFactory == null || ! (inputMessagingTargetFactory instanceof RegistryGraphMessagingTargetFactory)) return;
		if (outputMessagingTargetFactory == null || ! (outputMessagingTargetFactory instanceof RegistryGraphMessagingTargetFactory)) return;

		if (! (((RegistryGraphMessagingTargetFactory) inputMessagingTargetFactory).getPrototypeMessagingTarget() instanceof GraphMessagingTarget)) return;
		if (! (((RegistryGraphMessagingTargetFactory) outputMessagingTargetFactory).getPrototypeMessagingTarget() instanceof GraphMessagingTarget)) return;

		Iterator<String> requestPaths = ((RegistryGraphMessagingTargetFactory) inputMessagingTargetFactory).getRequestPaths(messagingTargetFactoryPath);

		while (requestPaths.hasNext()) {

			String requestPath = requestPaths.next();
			String messagingTargetPath = requestPath;

			MessagingTarget inputMessagingTarget = inputMessagingTargetFactory.mountMessagingTarget(inputHttpEndpointRegistry, messagingTargetFactoryPath, requestPath);
			MessagingTarget outputMessagingTarget = outputMessagingTargetFactory.mountMessagingTarget(outputHttpEndpointRegistry, messagingTargetFactoryPath, requestPath);

			migrateMessagingTarget(messagingTargetPath, inputMessagingTarget, outputMessagingTarget);

			inputHttpEndpointRegistry.unmountMessagingTarget(inputMessagingTarget);
			outputHttpEndpointRegistry.unmountMessagingTarget(outputMessagingTarget);
		}

		System.out.println("At path " + messagingTargetFactoryPath + " migrated from " + inputMessagingTargetFactory.getClass().getSimpleName() + " to " + outputMessagingTargetFactory.getClass().getSimpleName());
	}
}
