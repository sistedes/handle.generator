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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Generator {
	
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

	private static Map<String, String> vars = new HashMap<String, String>();

	private static final Options options = new Options();
	private static final Properties commands = new Properties();

	static {
		configureOptions(options);
		configureCommands(commands);
	}
	
	public static void main(String[] args) {
		try {
			final CommandLineParser parser = new DefaultParser();
			CommandLine commandLine = null;
			boolean useGuid = false;
			boolean addDelete = false;
			InputStream input = System.in;
			PrintWriter output = new PrintWriter(System.out);
			
			try {
				commandLine = parser.parse(options, args);

				useGuid = commandLine.hasOption(USE_GUID);
				addDelete = commandLine.hasOption(ADD_DELETE);
				input = System.in;
				output = new PrintWriter(System.out);

				vars.put(Variables.prefix.toString(), commandLine.getOptionValue(PREFIX));
				
				if (commandLine.hasOption(INPUT)) {
					input = new FileInputStream(new File(commandLine.getOptionValue(INPUT)));
				}
				
				if (commandLine.hasOption(OUTPUT)) {
					output = new PrintWriter(new File(commandLine.getOptionValue(OUTPUT)));
				}
			} catch (ParseException e) {
				printError(e.getLocalizedMessage());
				printHelp();
			}
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(input);
			
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			
			NodeList list = (NodeList) xpath.evaluate(
					"//channel/item[link and guid and postmeta[meta_key/text()='handle']]",
					doc,
					XPathConstants.NODESET);
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				vars.put(Variables.url.toString(), 
						useGuid ? 
								xpath.evaluate("guid/text()", node) : 
								xpath.evaluate("link/text()", node));
				vars.put(Variables.handle.toString(),
						xpath.evaluate("postmeta[meta_key/text()='handle']/meta_value/text()", node));
				
				if (addDelete) {
					output.println(StrSubstitutor.replace(commands.get("command.delete"), vars));
				}
				output.println(StrSubstitutor.replace(commands.get("command.create"), vars));
				output.println(StrSubstitutor.replace(commands.get("command.admin"), vars));
				output.println(StrSubstitutor.replace(commands.get("command.url"), vars));
				output.println();
				output.flush();
			}
			
		} catch (Exception e) {
			printError(e.getLocalizedMessage());
			System.exit(ReturnCodes.ERROR.getReturnCode());
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
		Option prefixOpt = Option
				.builder(PREFIX)
				.longOpt(PREFIX_LONG)
				.argName("prefix")
				.desc("Handle server's prefix (mandatory)")
				.numberOfArgs(1)
				.required()
				.build();
		
		Option inputOpt = Option
				.builder(INPUT)
				.longOpt(INPUT_LONG)
				.argName("input file")
				.desc("The input file (optional, stdin will be used if no input file is specified)")
				.numberOfArgs(1)
				.build();

		Option outputOpt = Option
				.builder(OUTPUT)
				.longOpt(OUTPUT_LONG)
				.argName("output file")
				.desc("The output file (optional, stdout will be used if no input file is specified)")
				.numberOfArgs(1)
				.build();

		Option guidOpt = Option
				.builder(USE_GUID)
				.longOpt(USE_GUID_LONG)
				.desc("Use the guid tag instead of the link")
				.build();

		Option deleteOpt = Option
				.builder(ADD_DELETE)
				.longOpt(ADD_DELETE_LONG)
				.desc("Add delete statements before the creation")
				.build();
		
		options.addOption(prefixOpt);
		options.addOption(inputOpt);
		options.addOption(outputOpt);
		options.addOption(guidOpt);
		options.addOption(deleteOpt);
	}

	/**
	 * Loads the properties file containing the command strings
	 * 
	 * @param commands
	 *            The {@link Properties} containing the commands
	 */
	private static void configureCommands(Properties commands) {
		try {
			commands.load(Generator.class.getResourceAsStream("commands.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
