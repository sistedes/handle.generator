/*******************************************************************************
* Copyright (c) 2016 Sistedes
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Abel Gómez - initial API and implementation
*******************************************************************************/

package es.sistedes.handle.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

public class CliLauncher {

	private static final Logger LOGGER = Logger.getLogger(CliLauncher.class.getName());
	
	private static final String PREFIX = "p";
	private static final String PREFIX_LONG = "prefix";
	private static final String INPUT = "i";
	private static final String INPUT_LONG = "input";
	private static final String OUTPUT = "o";
	private static final String OUTPUT_LONG = "output";
	private static final String USE_GUID = "g";
	private static final String USE_GUID_LONG = "guid";
	private static final String ADD_DELETE = "d";
	private static final String ADD_DELETE_LONG = "add-delete";

	private static final Options options = new Options();

	static {
		configureOptions(options);
	}

	public static void main(String[] args) {
		try {
			run(args);
		} catch (Throwable t) {
			if (t instanceof RuntimeException || t instanceof Error) {
				// Log unexpected unchecked exception
				LOGGER.log(Level.SEVERE, t.toString(), t);
			}
			System.exit(ReturnCodes.ERROR.getReturnCode());
		}
	}

	/**
	 * Runs the {@link CliLauncher}
	 * 
	 * @param args
	 * @throws Exception
	 */
	private static void run(String[] args) throws Exception {
		try {
			CommandLine commandLine = null;

			try {
				CommandLineParser parser = new DefaultParser();
				commandLine = parser.parse(options, args);
			} catch (ParseException e) {
				printError(e.getLocalizedMessage());
				printHelp();
				throw e;
			}

			Conversor conversor = new Conversor(commandLine.getOptionValue(PREFIX));

			FileInputStream input = null;
			if (commandLine.hasOption(INPUT)) {
				input = new FileInputStream(new File(commandLine.getOptionValue(INPUT)));
				conversor.changeInput(input);
			}

			FileOutputStream output = null;
			if (commandLine.hasOption(OUTPUT)) {
				output = new FileOutputStream(new File(commandLine.getOptionValue(OUTPUT)));
				conversor.changeOutput(output);
			}

			conversor.putOption(ConversorOptions.USE_GUID, commandLine.hasOption(USE_GUID));
			conversor.putOption(ConversorOptions.ADD_DELETE, commandLine.hasOption(ADD_DELETE));

			try {
				conversor.generate();
			} finally {
				IOUtils.closeQuietly(input);
				IOUtils.closeQuietly(output);
			}
		} catch (ConversionException e) {
			printError(e.getLocalizedMessage());
			throw e;
		}
	}

	/**
	 * Prints the help about the command-line options
	 */
	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(new OptionComarator<Option>());
		formatter.printHelp("java -jar <this-file.jar>", options, true);
	}

	/**
	 * Prints the <code>message</code> {@link String} in the standard error
	 * 
	 * @param message
	 *            The message
	 */
	private static void printError(String message) {
		System.err.println(message);
	}

	/**
	 * Configures the command-line {@link Options}
	 * 
	 * @param options
	 *            The {@link Options}
	 */

	private static void configureOptions(Options options) {
		Option prefixOpt = Option.builder(PREFIX).longOpt(PREFIX_LONG).argName("prefix")
				.desc("Handle server's prefix (mandatory)").numberOfArgs(1).required().build();

		Option inputOpt = Option.builder(INPUT).longOpt(INPUT_LONG).argName("input file")
				.desc("The input file (optional, stdin will be used if no input file is specified)").numberOfArgs(1)
				.build();

		Option outputOpt = Option.builder(OUTPUT).longOpt(OUTPUT_LONG).argName("output file")
				.desc("The output file (optional, stdout will be used if no input file is specified)").numberOfArgs(1)
				.build();

		Option guidOpt = Option.builder(USE_GUID).longOpt(USE_GUID_LONG).desc("Use the guid tag instead of the link")
				.build();

		Option deleteOpt = Option.builder(ADD_DELETE).longOpt(ADD_DELETE_LONG)
				.desc("Add delete statements before the creation").build();

		options.addOption(prefixOpt);
		options.addOption(inputOpt);
		options.addOption(outputOpt);
		options.addOption(guidOpt);
		options.addOption(deleteOpt);
	}

	/**
	 * Comparator to always give the command line options in the same order
	 * 
	 * @author agomez
	 *
	 * @param <T>
	 */
	private static class OptionComarator<T extends Option> implements Comparator<T> {
		private static final String OPTS_ORDER = "piogdq";

		@Override
		public int compare(T o1, T o2) {
			return OPTS_ORDER.indexOf(o1.getOpt()) - OPTS_ORDER.indexOf(o2.getOpt());
		}
	}

}
