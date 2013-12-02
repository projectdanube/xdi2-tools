package xdi2.tools.commands;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import xdi2.core.Graph;
import xdi2.core.io.MimeType;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("backup-graphs")
@CommandUsage("filename [mime-type] [path-to-applicationContext.xml]")
@CommandArgs(min=1,max=3)
public class CommandBackupGraphs extends AbstractGraphsCommand<CommandBackupGraphs.MyState> implements Command {

	public static final String DEFAULT_APPLICATIONCONTEXTPATH = "applicationContext.xml";

	@Override
	public void execute(String[] commandArgs) throws Exception {

		String filename = commandArgs[0];
		String mimeType = commandArgs.length > 1 ? commandArgs[1] : null;
		String applicationContextPath = commandArgs.length > 2 ? commandArgs[2] : null;

		if (applicationContextPath == null) applicationContextPath = DEFAULT_APPLICATIONCONTEXTPATH;

		ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(filename));

		this.commandGraphs(applicationContextPath, new MyState(mimeType, zipOutputStream));

		zipOutputStream.close();
	}

	@Override
	protected void callbackGraph(String messagingTargetPath, Graph graph, MyState state) throws Xdi2MessagingException, IOException {

		String zipEntryName = messagingTargetPath + ".xdi";
		if (zipEntryName.startsWith("/")) zipEntryName = zipEntryName.substring(1);

		ZipEntry zipEntry = new ZipEntry(zipEntryName);
		state.zipOutputStream.putNextEntry(zipEntry);

		System.out.println("Backing up graph " + messagingTargetPath + ".");

		XDIWriter writer = state.mimeType == null ? XDIWriterRegistry.getDefault() : XDIWriterRegistry.forMimeType(new MimeType(state.mimeType));

		writer.write(graph, state.zipOutputStream);

		state.zipOutputStream.closeEntry();
	}

	public class MyState {

		private String mimeType;
		private ZipOutputStream zipOutputStream;

		public MyState(String mimeType, ZipOutputStream zipOutputStream) {

			this.mimeType = mimeType;
			this.zipOutputStream = zipOutputStream;
		}
	}
}
