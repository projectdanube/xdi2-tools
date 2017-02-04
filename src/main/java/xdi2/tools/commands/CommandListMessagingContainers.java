package xdi2.tools.commands;

import xdi2.messaging.container.MessagingContainer;
import xdi2.messaging.container.factory.impl.uri.UriMessagingContainerFactory;
import xdi2.messaging.container.impl.graph.GraphMessagingContainer;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;
import xdi2.transport.registry.impl.uri.UriMessagingContainerRegistry;

@CommandName("list-messaging-containers")
@CommandUsage("[path-to-applicationContext.xml]")
@CommandArgs(min=0,max=1)
public class CommandListMessagingContainers extends AbstractMessagingContainersCommand<Object> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String applicationContextPath = commandArgs.length > 0 ? commandArgs[0] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandMessagingContainers(applicationContextPath, null);
	}

	@Override
	protected void callbackMessagingContainer(String messagingContainerPath, MessagingContainer messagingContainer, UriMessagingContainerRegistry uriMessagingContainerRegistry, Object state) {

		StringBuilder buffer = new StringBuilder();

		buffer.append(messagingContainerPath + " --> " + messagingContainer.getClass().getSimpleName());

		if (messagingContainer instanceof GraphMessagingContainer) {

			buffer.append(" (" + ((GraphMessagingContainer) messagingContainer).getGraph().getClass().getSimpleName() + ")");
		}

		System.out.println(buffer.toString());
	}

	@Override
	protected void callbackMessagingContainerFactory(String messagingContainerFactoryPath, UriMessagingContainerFactory messagingContainerFactory, UriMessagingContainerRegistry uriMessagingContainerRegistry, Object state) throws Exception {

		StringBuilder buffer = new StringBuilder();

		buffer.append(messagingContainerFactoryPath + " ==> " + messagingContainerFactory.getClass().getSimpleName());

		System.out.println(buffer.toString());

		super.callbackMessagingContainerFactory(messagingContainerFactoryPath, messagingContainerFactory, uriMessagingContainerRegistry, state);
	}
}
