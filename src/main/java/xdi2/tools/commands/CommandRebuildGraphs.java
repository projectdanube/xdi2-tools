package xdi2.tools.commands;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import xdi2.core.Graph;
import xdi2.core.io.MimeType;
import xdi2.core.io.XDIReader;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.io.readers.AutoReader;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("rebuild-graphs")
@CommandUsage("[mime-type] [path-to-applicationContext.xml]")
@CommandArgs(min=0,max=2)
public class CommandRebuildGraphs extends AbstractGraphsCommand<CommandRebuildGraphs.MyState> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String mimeType = commandArgs.length > 0 ? commandArgs[0] : null;
		String applicationContextPath = commandArgs.length > 1 ? commandArgs[1] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		this.commandGraphs(applicationContextPath, new MyState(mimeType));
	}

	@Override
	protected void callbackGraph(String messagingTargetPath, Graph graph, MyState state) throws Xdi2MessagingException, IOException {

		System.out.println("Rebuilding graph " + messagingTargetPath + ".");

		XDIWriter writer = state.mimeType == null ? XDIWriterRegistry.getDefault() : XDIWriterRegistry.forMimeType(new MimeType(state.mimeType));
		XDIReader reader = new AutoReader(null);

		try {

			if (writer == null) throw new RuntimeException("Unknown MIME type " + state.mimeType);

			StringWriter stringWriter = new StringWriter();
			writer.write(graph, stringWriter);

			graph.clear();

			StringReader stringReader = new StringReader(stringWriter.toString());
			reader.read(graph, stringReader);
		} catch (Exception ex) {

			System.err.println("Problem while rebuilding graph " + messagingTargetPath);
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
