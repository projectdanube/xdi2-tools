package xdi2.tools;

import java.io.IOException;
import java.util.Arrays;

import xdi2.core.plugins.PluginsLoader;
import xdi2.server.exceptions.Xdi2ServerException;
import xdi2.tools.annotations.CommandArgs;
import xdi2.tools.annotations.CommandName;
import xdi2.tools.annotations.CommandUsage;
import xdi2.tools.commands.Command;
import xdi2.tools.commands.CommandGenerateDigestSecretToken;
import xdi2.tools.commands.CommandListClassLoaders;
import xdi2.tools.commands.CommandListGraphs;
import xdi2.tools.commands.CommandListPlugins;
import xdi2.tools.commands.CommandMigrateGraphs;

public class XDI2Tools {

	private final static Command[] commands = new Command[] {
		new CommandListPlugins(),
		new CommandListClassLoaders(),
		new CommandListGraphs(),
		new CommandMigrateGraphs(),
		new CommandGenerateDigestSecretToken()
	};

	public static void main(String... args) throws Exception {

		// check arguments

		if (args.length < 1) {

			printUsage(null);
			return;
		}

		// find command

		String commandName = args[0];
		Command command = findCommand(commandName);

		if (command == null) {

			printUsage(null);
			return;
		}

		// load plugins

		try {

			PluginsLoader.loadPlugins();
		} catch (IOException ex) {

			throw new Xdi2ServerException("Cannot load plugins: " + ex.getMessage(), ex);
		}

		// execute command

		String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

		if (commandArgs.length < commandMinArgs(command) || commandArgs.length > commandMaxArgs(command)) {

			printUsage(command);
			return;
		}

		command.execute(commandArgs);
	}

	private static Command findCommand(String commandName) {

		for (Command command : commands) {

			if (commandName.equals(commandName(command))) return command;
		}

		return null;
	}

	private static void printUsage(Command command) {

		System.out.println("Usage: java -jar xdi2-tools-XXX.one-jar.jar");

		if (command == null) {

			for (Command c : commands) {

				System.out.println("   " + commandName(c) + " " + commandUsage(c));
			}
		} else {

			System.out.println("   " + commandName(command) + " " + commandUsage(command));
		}
	}

	private static String commandName(Command command) {

		return command.getClass().getAnnotation(CommandName.class).value();
	}

	private static String commandUsage(Command command) {

		return command.getClass().getAnnotation(CommandUsage.class).value();
	}

	private static int commandMinArgs(Command command) {

		return command.getClass().getAnnotation(CommandArgs.class).min();
	}

	private static int commandMaxArgs(Command command) {

		return command.getClass().getAnnotation(CommandArgs.class).max();
	}
}
