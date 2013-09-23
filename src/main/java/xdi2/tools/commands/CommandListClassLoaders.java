package xdi2.tools.commands;

import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;

@CommandName("list-classloaders")
@CommandUsage("")
@CommandArgs(min=0,max=0)
public class CommandListClassLoaders implements Command {

	@Override
	public void execute(String[] commandArgs) throws Exception {

		ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
		printClassLoader(systemClassLoader, "[SYSTEM] ");

		Thread currentThread = Thread.currentThread();

		ClassLoader contextClassLoader = currentThread.getContextClassLoader();
		printClassLoader(contextClassLoader, "[" + currentThread.getName() + "] ");
	}

	private static void printClassLoader(ClassLoader classLoader, String prefix) {

		String indent = "";

		while (classLoader != null) {

			System.out.println(indent + prefix + classLoader.getClass().getCanonicalName());

			classLoader = classLoader.getParent();
			indent += "  ";
		}
	}
}
