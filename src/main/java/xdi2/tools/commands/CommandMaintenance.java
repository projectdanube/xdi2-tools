package xdi2.tools.commands;

import java.io.IOException;

import xdi2.core.Graph;
import xdi2.core.util.GraphUtil;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("maintenance")
@CommandUsage("[path-to-applicationContext.xml]")
@CommandArgs(min=1,max=3)
public class CommandMaintenance extends AbstractGraphsCommand<Object> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String applicationContextPath = commandArgs.length > 0 ? commandArgs[0] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandGraphs(applicationContextPath, null);
	}

	@Override
	protected void callbackGraph(String messagingTargetPath, Graph graph, Object state) throws Xdi2MessagingException, IOException {

		System.out.println("Maintainence for graph " + GraphUtil.getOwnerXri(graph));
	}
}
