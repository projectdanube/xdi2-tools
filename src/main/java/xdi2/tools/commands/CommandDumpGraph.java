package xdi2.tools.commands;

import java.io.IOException;

import xdi2.core.Graph;
import xdi2.core.io.MimeType;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.messaging.container.exceptions.Xdi2MessagingException;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("dump-graph")
@CommandUsage("request-path [mime-type] [path-to-applicationContext.xml]")
@CommandArgs(min=1,max=3)
public class CommandDumpGraph extends AbstractGraphCommand<CommandDumpGraph.MyState> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String requestPath = commandArgs[0];
		String mimeType = commandArgs.length > 1 ? commandArgs[1] : null;
		String applicationContextPath = commandArgs.length > 2 ? commandArgs[2] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandGraph(applicationContextPath, requestPath, new MyState(mimeType));
	}

	@Override
	protected void callbackGraph(String messagingContainerPath, Graph graph, MyState state) throws Xdi2MessagingException, IOException {

		XDIWriter writer = state.mimeType == null ? XDIWriterRegistry.getDefault() : XDIWriterRegistry.forMimeType(new MimeType(state.mimeType));

		try {

			if (writer == null) throw new RuntimeException("Unknown MIME type " + state.mimeType);

			writer.write(graph, System.out);
		} catch (Exception ex) {

			System.err.println("Problem while dumping graph " + messagingContainerPath);
			ex.printStackTrace(System.err);
		}
	}

	public class MyState {

		private String mimeType;

		public MyState(String mimeType) {

			this.mimeType = mimeType;
		}
	}
}
