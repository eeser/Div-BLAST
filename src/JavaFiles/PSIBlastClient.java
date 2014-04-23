/* $Id: PSIBlastClient.java 2624 2013-04-23 13:25:39Z hpm $
 * ======================================================================
 * 
 * Copyright 2010-2013 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * JDispatcher PSI-BLAST (SOAP) web service Java client using Axis 1.x.
 * ----------------------------------------------------------------------
 * Tested with:
 *   Sun Java 1.5.0_17 with Apache Axis 1.4 on CentOS 5.2.
 * ====================================================================== */
package JavaFiles;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import uk.ac.ebi.webservices.axis1.stubs.psiblast.*;

/** <p>JDispatcher PSI-BLAST (SOAP) web service Java client using Apache Axis 
 * 1.x.</p>
 * 
 * <p>See:</p>
 * <ul>
 * <li><a href="http://www.ebi.ac.uk/Tools/webservices/services/sss/psiblast_soap">http://www.ebi.ac.uk/Tools/webservices/services/sss/psiblast_soap</a></li>
 * <li><a href="http://www.ebi.ac.uk/Tools/webservices/tutorials/06_programming/java">http://www.ebi.ac.uk/Tools/webservices/tutorials/06_programming/java</a></li>
 * <li><a href="http://ws.apache.org/axis/">http://ws.apache.org/axis/</a></li>
 * </ul>
 */
public class PSIBlastClient extends PsiAbstractWsToolClient {
	/** Proxy object for web service. */
	private JDispatcherService_PortType srvProxy = null;
	/** Client version/revision for use in user-agent string. */
	private String revision = "$Revision: 2624 $";
	/** Tool specific usage for help. */
	private static final String usageMsg = "NCBI PSI-BLAST\n"
		+ "==============\n"
		+ "\n"
		+ "Rapid sequence database search programs utilizing the PSI-BLAST algorithm.\n"
		+"\n"
		+"[Required]\n"
		+ "\n"
		+"  -D, --database      : str  : database(s) to search, space separated. See\n"
		+"                               --paramDetail database\n"
		+"  seqFile             : file : query sequence (\"-\" for STDIN, @filename for\n"
		+"                               identifier list file)\n"
		+ "\n"
		+"[Optional]\n"
		+ "\n"
		+"  -M, --matrix        : str  : scoring matrix, see --paramDetail matrix\n"
		+"  -e, --expthr        : real : 0<E<= 1000. Statistical significance threshold \n"
		+"                               for reporting database sequence matches.\n"
		+"  -h, --psithr        : real : E-value limit for inclusion in PSSM\n"
		+"  -F, --filter        :      : filter the query sequence for low complexity\n" 
		+"                               regions, see --paramDetail filter\n"
		+"  -m, --alignView     : int  : pairwise alignment format,\n" 
		+"                               see --paramDetail align\n"
		+"  -v, --scores        : int  : number of scores to be reported\n"
		+"  -b, --alignments    : int  : number of alignments to report\n"
		+"  -G, --gapopen       : int  : Gap open penalty\n"
		+"  -E, --gapext        : int  : Gap extension penalty\n"
		+"  -X, --dropoff       : int  : Drop-off\n"
		+"  -Z, --finaldropoff  : int  : Final dropoff score\n"
		+"      --seqrange      : str  : region within input to use as query\n"
		+"      --previousjobid : str  : Job Id for last iteration\n"
		+"      --selectedHits  : file : Selected hits from last iteration for building\n" 
		+"                               search profile (PSSM)\n"
		+"  -R, --cpfile        : file : PSI-BLAST checkpoint from last iteration\n";
	/** Additional usage information for iterations */
	private static final String iterationMsg = "Iterations:\n"
		+ "\n"
		+ "  To generate and refine the profile (PSSM) used to perform the search after\n" 
		+ "  the first iteration the set of hits to be included in the generation of the\n"
		+ "  PSSM needs to be specified for each iteration. This can be either obtained\n"
		+ "  from the previous iteration using the job identifier of the iteration, or\n"
		+ "  be explicit specification of a file containing the list of identifiers.\n"
		+ "\n"
		+ "  Iteration 1:\n"
		+ "  java -jar <jarFile>  --email <email> --database <db> <seqFile> [options...]\n"
		+ "\n"
		+ "  Iteration 2:\n"
		+ "  java -jar <jarFile> --email <email> --previousjobid <jobId> \\ \n"
		+ "    [--selectedHits <selFile>] [options...]\n"
		+ "\n"
		+ "  Iteration 3+:\n"
		+ "  java -jar <jarFile> --email <email> --previousjobid <jobId> \\ \n"
		+ "    [--selectedHits <selFile>] [--cpfile <checkpoint>] [options...]\n";
	/** Documentation and help */
	private static final String docAndHelpMsg = "Further information:\n"
		+ "\n"
		+ "  http://www.ebi.ac.uk/Tools/webservices/services/sss/psiblast_soap\n"
		+ "  http://www.ebi.ac.uk/Tools/webservices/tutorials/java\n"
		+ "\n"
		+ "Support/Feedback:\n"
		+ "\n"
		+ "  http://www.ebi.ac.uk/support/\n";

	/** Default constructor.
	 */
	public PSIBlastClient() {
		// Set the HTTP user agent string for (java.net) requests.
		this.setUserAgent();
	}
	
	/** <p>Get a user-agent string for this client.</p>
	 * 
	 * <p><b>Note</b>: this affects all java.net based requests, but not the 
	 * Axis requests. The user-agent used by Axis is set from the 
	 * /org/apache/axis/i18n/resource.properties file included in the Axis 
	 * JAR.</p>
	 * 
	 * @return Client user-agent string.
	 */
	protected String getClientUserAgentString() {
		printDebugMessage("getClientUserAgent", "Begin", 11);
		String clientVersion = this.revision.substring(11, this.revision.length() - 2);
		String clientUserAgent = "EBI-Sample-Client/" + clientVersion 
			+ " (" + this.getClass().getName() + "; " 
			+ System.getProperty("os.name") + ")";
		printDebugMessage("getClientUserAgent", "End", 11);
		return clientUserAgent;
	}

	/** Print usage message. */
	private static void printUsage() {
		System.out.println(usageMsg);
		printGenericOptsUsage();
		System.out.println(iterationMsg);
		System.out.println(docAndHelpMsg);
	}
	
	/** Ensure that a service proxy is available to call the web service.
	 * 
	 * @throws ServiceException
	 */
	protected void srvProxyConnect() throws ServiceException {
		printDebugMessage("srvProxyConnect", "Begin", 11);
		if(this.srvProxy == null) {
			JDispatcherService_Service service =  new JDispatcherService_ServiceLocator();
			if(this.getServiceEndPoint() != null) {
				try {
					this.srvProxy = service.getJDispatcherServiceHttpPort(new java.net.URL(this.getServiceEndPoint()));
				}
				catch(java.net.MalformedURLException ex) {
					System.err.println(ex.getMessage());
					System.err.println("Warning: problem with specified endpoint URL. Default endpoint used.");
					this.srvProxy = service.getJDispatcherServiceHttpPort();					
				}
			}
			else {
				this.srvProxy = service.getJDispatcherServiceHttpPort();
			}
		}
		printDebugMessage("srvProxyConnect", "End", 11);
	}

	/** Get the web service proxy so it can be called directly.
	 * 
	 * @return The web service proxy.
	 * @throws javax.xml.rpc.ServiceException
	 */
	public JDispatcherService_PortType getSrvProxy() throws javax.xml.rpc.ServiceException {
		printDebugMessage("getSrvProxy", "", 1);
		this.srvProxyConnect(); // Ensure the service proxy exists
		return this.srvProxy;
	}
	
	/** Get list of tool parameter names.
	 * 
	 * @return String array containing list of parameter names
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	public String[] getParams() throws ServiceException, RemoteException {
		printDebugMessage("getParams", "Begin", 1);
		String[] retVal = null;
		this.srvProxyConnect(); // Ensure the service proxy exists
		retVal = this.srvProxy.getParameters();
		printDebugMessage("getParams", retVal.length + " params", 2);
		printDebugMessage("getParams", "End", 1);
		return retVal;
	}

	/** Get detailed information about the specified tool parameter.
	 * 
	 * @param paramName Tool parameter name
	 * @return Object describing tool parameter
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	public WsParameterDetails getParamDetail(String paramName) throws ServiceException, RemoteException {
		printDebugMessage("getParamDetail", paramName, 1);
		this.srvProxyConnect(); // Ensure the service proxy exists
		return this.srvProxy.getParameterDetails(paramName);
	}

	/** Print detailed information about a tool parameter.
	 * 
	 * @param paramName Name of the tool parameter to get information for.
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	protected void printParamDetail(String paramName) throws RemoteException, ServiceException {
		printDebugMessage("printParamDetail", "Begin", 1);
		WsParameterDetails paramDetail = getParamDetail(paramName);
		// Print object
		System.out.println(paramDetail.getName() + "\t" + paramDetail.getType());
		System.out.println(paramDetail.getDescription());
		WsParameterValue[] valueList = paramDetail.getValues();
		if(valueList!=null) {
			for(int i = 0; i < valueList.length; i++) {
				System.out.print(valueList[i].getValue());
				if(valueList[i].isDefaultValue()) {
					System.out.println("\tdefault");
				}
				else {
					System.out.println();
				}
				System.out.println("\t" + valueList[i].getLabel());
				WsProperty[] valuePropertiesList = valueList[i].getProperties();
				if(valuePropertiesList != null) {
					for(int j = 0; j < valuePropertiesList.length; j++) {
						System.out.println("\t" + valuePropertiesList[j].getKey() + "\t" + valuePropertiesList[j].getValue());
					}
				}
			}
		}
		printDebugMessage("printParamDetail", "End", 1);
	}

	/** Get the status of a submitted job given its job identifier.
	 * 
	 * @param jobid The job identifier.
	 * @return Job status as a string.
	 * @throws IOException
	 * @throws ServiceException
	 */
	public String checkStatus(String jobid) throws IOException, ServiceException {
		printDebugMessage("checkStatus", jobid, 1);
		this.srvProxyConnect(); // Ensure the service proxy exists
		return this.srvProxy.getStatus(jobid);
	}

	/** Get details of the available result types for a job.
	 * 
	 * @param jobId Job identifier to check for results types.
	 * @return Array of objects describing result types.
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	public WsResultType[] getResultTypes(String jobId) throws ServiceException, RemoteException {
		printDebugMessage("getResultTypes", "Begin", 1);
		printDebugMessage("getResultTypes", "jobId: " + jobId , 2);
		WsResultType[] retVal = null;
		this.srvProxyConnect(); // Ensure the service proxy exists
		retVal = this.srvProxy.getResultTypes(jobId);
		printDebugMessage("getResultTypes", retVal.length + " result types", 2);
		printDebugMessage("getResultTypes", "End", 1);
		return retVal;
	}
	
	/** Print details of the available result types for a job.
	 * 
	 * @param jobId Job identifier to check for result types.
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	protected void printResultTypes(String jobId) throws ServiceException, RemoteException {
		printDebugMessage("printResultTypes", "Begin", 1);
		WsResultType[] typeList = getResultTypes(jobId);
		for(int i = 0; i < typeList.length; i++) {
			System.out.print(
					typeList[i].getIdentifier() + "\n\t"
					+ typeList[i].getLabel() + "\n\t"
					+ typeList[i].getDescription() + "\n\t"
					+ typeList[i].getMediaType() + "\n\t"
					+ typeList[i].getFileSuffix() + "\n"
					);
		}
		printDebugMessage("printResultTypes", "End", 1);
	}
	
	/** Get the results for a job and save them to files.
	 * 
	 * @param jobid The job identifier.
	 * @param outfile The base name of the file to save the results to. If
	 * null the jobid will be used.
	 * @param outformat The name of the data format to save, e.g. toolraw 
	 * or toolxml. If null all available data formats will be saved.
	 * @return Array of filenames
	 * @throws IOException
	 * @throws javax.xml.rpc.ServiceException
	 */
	public String[] getResults(String jobid, String outfile, String outformat) throws IOException, javax.xml.rpc.ServiceException {
		printDebugMessage("getResults", "Begin", 1);
		printDebugMessage("getResults", "jobid: " + jobid + " outfile: " + outfile + " outformat: " + outformat, 2);
		String[] retVal = null;
		this.srvProxyConnect(); // Ensure the service proxy exists
		clientPoll(jobid); // Wait for job to finish
		// Set the base name for the output file.
		String basename = (outfile != null) ? outfile : jobid;
		// Get result types
		WsResultType[] resultTypes = getResultTypes(jobid);
		int retValN = 0;
		if(outformat == null) {
			retVal = new String[resultTypes.length];
		} else {
			retVal = new String[1];
		}
		for(int i = 0; i < resultTypes.length; i++) {
			printProgressMessage("File type: " + resultTypes[i].getIdentifier(), 2);
			// Get the results
			if(outformat == null || outformat.equals(resultTypes[i].getIdentifier())) {
				byte[] resultbytes = this.srvProxy.getResult(jobid, resultTypes[i].getIdentifier(), null);
				if(resultbytes == null) {
					System.err.println("Null result for " + resultTypes[i].getIdentifier() + "!");
				}
				else {
					printProgressMessage("Result bytes length: " + resultbytes.length, 2);
					// Write the results to a file
					String result = new String(resultbytes);
					if(basename.equals("-")) { // STDOUT
						if(resultTypes[i].getMediaType().startsWith("text")) { // String
							System.out.print(result);
						}
						else { // Binary
							System.out.print(resultbytes);
						}
					}
					else { // File
						String filename = basename + "." + resultTypes[i].getIdentifier() + "." + resultTypes[i].getFileSuffix();
						if(resultTypes[i].getMediaType().startsWith("text")) { // String
							writeFile(new File(filename), result);
						}
						else { // Binary
							writeFile(new File(filename), resultbytes);
						}
						retVal[retValN] = filename;
						retValN++;
					}
				}
			}
		}
		printDebugMessage("getResults", retVal.length + " file names", 2);
		printDebugMessage("getResults", "End", 1);
		return retVal;
	}

	/** Submit a job to the service.
	 * 
	 * @param params Input parameters for the job.
	 * @param content Data to run the job on.
	 * @return The job identifier.
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	public String runApp(String email, String title, InputParameters params) throws RemoteException, ServiceException {
		printDebugMessage("runApp", "Begin", 1);
		printDebugMessage("runApp", "email: " + email + " title: " + title, 2);
		printDebugMessage("runApp", "params:\n" + objectFieldsToString(params), 2);
		String jobId = null;
		this.srvProxyConnect(); // Ensure the service proxy exists
		jobId = srvProxy.run(email, title, params);
		printDebugMessage("runApp", "jobId: " + jobId, 2);
		printDebugMessage("runApp", "End", 1);
		return jobId;
	}

	/** Populate input parameters structure from command-line options.
	 * 
	 * @param line Command line options
	 * @return input Input parameters structure for use with runApp().
	 * @throws IOException
	 */
	public InputParameters loadParams(CommandLine line) throws IOException {
		printDebugMessage("loadParams", "Begin", 1);
		InputParameters params = new InputParameters();
		// Tool specific options
		if (line.hasOption("D")) params.setDatabase(line.getOptionValue("D"));
		if (line.hasOption("M")) params.setMatrix(line.getOptionValue("M"));
		if (line.hasOption("e")) params.setExpthr(new Double(line.getOptionValue("e")));
		else params.setExpthr(new Double(10.0));
		if (line.hasOption("h")) params.setPsithr(new Double(line.getOptionValue("h")));
		else params.setPsithr(new Double(0.001));
		if (line.hasOption("F")) params.setFilter(line.getOptionValue("F"));
		if (line.hasOption("m")) params.setAlignView(new Integer(line.getOptionValue("m"))); 
		if (line.hasOption("v")) params.setScores(new Integer(line.getOptionValue("v")));
		else params.setScores(new Integer(500));
		if (line.hasOption("b")) params.setAlignments(new Integer(line.getOptionValue("b")));
		else params.setAlignments(new Integer(500));
		if (line.hasOption("X")) params.setDropoff(new Integer(line.getOptionValue("X"))); 
		if (line.hasOption("Z")) params.setFinaldropoff(new Integer(line.getOptionValue("Z"))); 
		if (line.hasOption("G")) params.setGapopen(new Integer(line.getOptionValue("G"))); 
		if (line.hasOption("E")) params.setGapext(new Integer(line.getOptionValue("E")));
		if (line.hasOption("seqrange")) params.setSeqrange(line.getOptionValue("seqrange"));
		if(line.hasOption("previousjobid")) params.setPreviousjobid(line.getOptionValue("previousjobid"));
		printDebugMessage("loadParams", "End", 1);
		return params;
	}

	/**
	 * Submit a job using the command-line information to construct the input.
	 * 
	 * @param cli
	 *            Command-line parameters.
	 * @param inputSeq
	 *            Data input.
	 * @throws ServiceException
	 * @throws IOException
	 */
	public void submitJobFromCli(CommandLine cli, String inputSeq)
			throws ServiceException, IOException {
		// Create job submission parameters from command-line
		InputParameters params = this.loadParams(cli);
		params.setSequence(inputSeq);
		if(cli.hasOption("selectedHits")) params.setSelectedHits(this.loadData(cli.getOptionValue("selectedHits")));
		if(cli.hasOption("R")) params.setCpfile(this.loadData(cli.getOptionValue("R")));
		// Submit the job
		String email = null, title = null;
		if (cli.hasOption("email"))
			email = cli.getOptionValue("email");
		if (cli.hasOption("title"))
			title = cli.getOptionValue("title");
		String jobid = this.runApp(email, title, params);
		// For asynchronous mode
		if (cli.hasOption("async")) {
			System.out.println(jobid); // Output the job id.
			System.err
					.println("To get status: java -jar PsiBlast_Axis1.jar --status --jobid "
							+ jobid);
		} else {
			// In synchronous mode try to get the results
			this.printProgressMessage(jobid, 1);
			String[] resultFilenames = this
					.getResults(jobid, cli.getOptionValue("outfile"), cli
							.getOptionValue("outformat"));
			for (int i = 0; i < resultFilenames.length; i++) {
				if (resultFilenames[i] != null) {
					System.out.println("Wrote file: " + resultFilenames[i]);
				}
			}
		}
	}

	/** Entry point for running as an application.
	 * 
	 * @param args list of command-line options
	 * @throws IOException 
	 * @throws ServiceException 
	 * @throws ParseException 
	 */
	public  void main(String[] args) throws ServiceException, IOException, ParseException {
		int exitVal = 0; // Exit value
		int argsLength = args.length; // Number of command-line arguments

		// Configure the command-line options
		Options options = new Options();
		// Common options for EBI clients
		addGenericOptions(options);
		options.addOption("multifasta", "multifasta", false,
				"Multiple fasta sequence input");
		// Application specific options
		options.addOption("D", "database", true, "Database to search");
		options.addOption("M", "matrix", true, "Scoring matrix");
		options.addOption("e", "expthr", true, "Expectation value threshold");
		options.addOption("h", "psithr", true, "PSI E-value threshold");
		options.addOption("F", "filter", true, "Low complexity sequence filter");
		options.addOption("m", "alignView", true, "Alignment format");
		options.addOption("v", "scores", true, "Maximum number of scores to display");
		options.addOption("b", "alignments", true, "Maximum number of alignments to display");
		options.addOption("X", "dropoff", true, "Drop-off score");
		options.addOption("Z", "finaldropoff", true, "Final drop-off score");
		options.addOption("G", "gapopen", true, "Gap creation penalty");
		options.addOption("E", "gapext", true, "Gap extension penalty");
		options.addOption("seqrange", "seqrange", true, "Region in query sequence to use for search");
		options.addOption("stype", "stype", true, "Sequence type");
		options.addOption("i", "sequence", true, "Query sequence");
		options.addOption("previousjobid", true, "Job identifier for previous iteration");
		options.addOption("selectedHits", true, "Selected hits for building PSSM");
		options.addOption("R", "cpfile", true, "PSI-BLAST checkpoint");

		CommandLineParser cliParser = new GnuParser(); // Create the command line parser    
		// Create an instance of the client
		PSIBlastClient client = new PSIBlastClient();
		
			// Parse the command-line
			CommandLine cli = cliParser.parse(options, args);
			// User asked for usage info
			if(argsLength == 0 || cli.hasOption("help")) {
				printUsage();
				//System.exit(0);
			}
			// Modify output level according to the quiet and verbose options
			if(cli.hasOption("quiet")) {
				client.outputLevel--;
			}
			if(cli.hasOption("verbose")) {
				client.outputLevel++;
			}
			// Set debug level
			if(cli.hasOption("debugLevel")) {
				client.setDebugLevel(Integer.parseInt(cli.getOptionValue("debugLevel")));
			}
			// Alternative service endpoint
			if(cli.hasOption("endpoint")) {
				client.setServiceEndPoint(cli.getOptionValue("endpoint"));
			}
			// Tool meta-data
			// List parameters
			if(cli.hasOption("params")) {
				client.printParams();
			}
			// Details of a parameter
			else if(cli.hasOption("paramDetail")) {
				client.printParamDetail(cli.getOptionValue("paramDetail"));
			}
			// Job related actions
			else if(cli.hasOption("jobid")) {
				String jobid = cli.getOptionValue("jobid");
				// Get results for job
				if(cli.hasOption("polljob")) {                
					String[] resultFilenames = client.getResults(jobid, cli.getOptionValue("outfile"), cli.getOptionValue("outformat"));
					boolean resultContainContent = false;
					for(int i = 0; i < resultFilenames.length; i++) {
						if(resultFilenames[i] != null) {
							System.out.println("Wrote file: " + resultFilenames[i]);
							resultContainContent = true;
						}
					}
					if (resultContainContent == false) {
						System.err.println("Error: requested result type " + cli.getOptionValue("outformat") + " not available!");
					}
				}
				// Get entry Ids from result
				else if(cli.hasOption("ids")) {
					client.getResults(jobid, "-", "ids");
				}
				// Get status of job
				else if(cli.hasOption("status")) {
					System.out.println(client.checkStatus(jobid));
				}
				// Get result types for job
				else if(cli.hasOption("resultTypes")) {
					client.printResultTypes(jobid);
				}
				// Unknown...
				else {
					System.err.println("Error: jobid specified without related action option");
					printUsage();
					exitVal = 2;
				}
			}
			// Submit a job
			else if (cli.hasOption("email")
					&& (cli.hasOption("i") || cli.getArgs().length > 0)) {
				// Input sequence, data file or entry identifier.
				String dataOption = (cli.hasOption("i")) ? cli.getOptionValue("i") : cli.getArgs()[0];
				// Multi-fasta sequence input.
				if (cli.hasOption("multifasta")) {
					client.printDebugMessage("main", "Mode: multifasta", 11);
					int numSeq = 0;
					client.setFastaInputFile(dataOption);
					// Loop over input sequences, submitting each one.
					String fastaSeq = null;
					fastaSeq = client.nextFastaSequence();
					client.printDebugMessage("main", "fastaSeq: " + fastaSeq,
							12);
					while (fastaSeq != null) {
						numSeq++;
						client.submitJobFromCli(cli, fastaSeq);
						fastaSeq = client.nextFastaSequence();
					}
					client.closeFastaFile();
					client.printProgressMessage("Processed " + numSeq
							+ " input sequences", 2);
				}
				// Entry identifier list.
				else if (dataOption.startsWith("@")) {
					client.printDebugMessage("main", "Mode: Id list", 11);
					int numId = 0;
					client.setIdentifierListFile(dataOption.substring(1));
					// Loop over input sequences, submitting each one.
					String id = null;
					id = client.nextIdentifier();
					while (id != null) {
						numId++;
						client.printProgressMessage("ID: " + id, 1);
						client.submitJobFromCli(cli, id);
						id = client.nextIdentifier();
					}
					client.closeIdentifierListFile();
					client.printProgressMessage("Processed " + numId
							+ " input identifiers", 2);
				}
				// Submit a job
				else {
					client.printDebugMessage("main", "Mode: sequence", 11);
					client.submitJobFromCli(cli, new String(client
							.loadData(dataOption)));
				}
			}
			// Unknown action
			else {
				System.err.println("Error: unknown combination of arguments. See --help.");
				exitVal = 2;
			}
		
	//	System.exit(exitVal);
	}
}
