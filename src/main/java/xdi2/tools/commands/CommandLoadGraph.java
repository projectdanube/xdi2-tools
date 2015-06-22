package xdi2.tools.commands;

import java.io.IOException;

import xdi2.core.Graph;
import xdi2.core.io.XDIReader;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("load-graph")
@CommandUsage("request-path [path-to-applicationContext.xml]")
@CommandArgs(min=1,max=2)
public class CommandLoadGraph extends AbstractGraphCommand<CommandLoadGraph.MyState> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String requestPath = commandArgs[0];
		String applicationContextPath = commandArgs.length > 1 ? commandArgs[1] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandGraph(applicationContextPath, requestPath, new MyState());
	}

	@Override
	protected void callbackGraph(String messagingTargetPath, Graph graph, MyState state) throws Xdi2MessagingException, IOException {

		XDIReader reader = XDIReaderRegistry.getAuto();

		try {

			graph.clear();
			reader.read(graph, System.in);
		} catch (Exception ex) {

			System.err.println("Problem while loading graph " + messagingTargetPath);
			ex.printStackTrace(System.err);
		}
	}

	public class MyState {

	}
}
