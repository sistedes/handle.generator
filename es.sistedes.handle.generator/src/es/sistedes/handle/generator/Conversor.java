package es.sistedes.handle.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A {@link Conversor} class that transforms Wordpress XML dump into a Handle
 * batch text file.
 * 
 * The lifecycle of the {@link InputStream} and {@link OutputStream} used within
 * this class must be controlled by the caller. This class, does not perform any
 * close action in such streams. By default, this class uses <code>stdin</code>
 * and <code>stdout</code> as input and output streams.
 * 
 * @author agomez
 *
 */
public class Conversor {
	
	private static final Properties commands = new Properties();

	static {
		configureCommands(commands);
	}
	
	private String prefix;
	private InputStream input;
	private OutputStream output;
	private Map<ConversorOptions, Object> options;
	
	public Conversor(String prefix) {
		this.prefix = prefix;
		this.input = System.in;
		this.output = System.out;
		this.options = new HashMap<ConversorOptions, Object>();
	}
	
	/**
	 * Changes the {@link InputStream} used by this {@link Conversor}
	 * 
	 * @param input
	 */
	public void changeInput(InputStream input) {
		this.input = input;
	}
	
	/**
	 * Changes the  {@link OutputStream} used by this {@link Conversor}
	 * @param output
	 */
	public void changeOutput(OutputStream output) {
		this.output = output;
	}
	
	/**
	 * Changes the {@link ConversorOptions}
	 * @param options
	 */
	public void changeOptions(Map<ConversorOptions, Object> options) {
		this.options = options;
	}
	
	/**
	 * Sets a new {@link ConversorOptions}
	 * @param key
	 * @param value
	 */
	public void putOption(ConversorOptions key, Object value) {
		this.options.put(key, value);
	}
	
	/**
	 * Removes the given {@link ConversorOptions}
	 * 
	 * @param key
	 */
	public void removeOption(ConversorOptions key) {
		this.options.remove(key);
	}
	
	/**
	 * Converts the XML data available in the <code>input</code>
	 * {@link InputStream} and dumps the result in the <code>output</code>
	 * {@link OutputStream}
	 * 
	 * @throws ConversorException
	 *             If any error occurs, check
	 *             {@link ConversorException#getCause()} to figure out the exact
	 *             cause
	 */
	public synchronized void generate()	throws ConversorException {
		
		PrintWriter outputWriter = new PrintWriter(output);
		
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(input);
			
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			
			NodeList list = (NodeList) xpath.evaluate(
					"//channel/item[link and guid and postmeta[meta_key/text()='handle']]",
					doc,
					XPathConstants.NODESET);
	
			Boolean useGuid = useGuid();
			Boolean addDelete = addDelete();
	
			Map<String, String> vars = new HashMap<String, String>();
			vars.put(Variables.prefix.toString(), prefix);
	
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				vars.put(Variables.url.toString(), 
						useGuid ? 
								xpath.evaluate("guid/text()", node) : 
								xpath.evaluate("link/text()", node));
				vars.put(Variables.handle.toString(),
						xpath.evaluate("postmeta[meta_key/text()='handle']/meta_value/text()", node));
				
				if (addDelete) {
					outputWriter.println(StrSubstitutor.replace(commands.get("command.delete"), vars));
				}
				outputWriter.println(StrSubstitutor.replace(commands.get("command.create"), vars));
				outputWriter.println(StrSubstitutor.replace(commands.get("command.admin"), vars));
				outputWriter.println(StrSubstitutor.replace(commands.get("command.url"), vars));
				outputWriter.println();
			}
		} catch (Exception e) {
			throw new ConversorException(e);
		} finally {
			outputWriter.flush();
		}
	}

	/**
	 * Checks if {@link ConversorOptions#ADD_DELETE} option is set
	 * 
	 * @return
	 */
	private Boolean addDelete() {
		return (Boolean) (options.get(ConversorOptions.ADD_DELETE) != null ? options.get(ConversorOptions.ADD_DELETE) : false);
	}

	/**
	 * Checks if {@link ConversorOptions#USE_GUID} option is set
	 * 
	 * @return
	 */
	private Boolean useGuid() {
		return (Boolean) (options.get(ConversorOptions.USE_GUID) != null ? options.get(ConversorOptions.USE_GUID) : false);
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
}
