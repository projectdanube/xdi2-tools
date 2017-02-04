package xdi2.tools.commands;

import xdi2.messaging.container.factory.impl.uri.UriMessagingContainerFactory;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;
import xdi2.transport.registry.impl.uri.UriMessagingContainerRegistry;

@CommandName("list-messaging-container-factorys")
@CommandUsage("[path-to-applicationContext.xml]")
@CommandArgs(min=0,max=1)
public class CommandListMessagingContainerFactorys extends AbstractMessagingContainerFactorysCommand<Object> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String applicationContextPath = commandArgs.length > 0 ? commandArgs[0] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandMessagingContainerFactorys(applicationContextPath, null);
	}

	@Override
	protected void callbackMessagingContainerFactory(String messagingContainerFactoryPath, UriMessagingContainerFactory messagingContainerFactory, UriMessagingContainerRegistry uriMessagingContainerRegistry, Object state) {

		StringBuilder buffer = new StringBuilder();

		buffer.append(messagingContainerFactoryPath + " ==> " + messagingContainerFactory.getClass().getSimpleName());

		System.out.println(buffer.toString());
	}
}
