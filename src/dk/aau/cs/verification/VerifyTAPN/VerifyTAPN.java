package dk.aau.cs.verification.VerifyTAPN;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.FileFinder;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.TCTLAFNode;
import dk.aau.cs.TCTL.TCTLAGNode;
import dk.aau.cs.TCTL.TCTLEFNode;
import dk.aau.cs.TCTL.TCTLEGNode;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.util.UnsupportedQueryException;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.ProcessRunner;
import dk.aau.cs.verification.QueryResult;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;

public class VerifyTAPN implements ModelChecker {
	private static final String NEED_TO_LOCATE_VERIFYTAPN_MSG = "TAPAAL needs to know the location of the file verifytapn.\n\n"
		+ "Verifytapn is a part of the TAPAAL distribution and it is\n"
		+ "normally located in tapaal/verifytapn.";
	
	private static String verifytapnpath = "";
	
	private FileFinder fileFinder;
	private Messenger messenger;

	private ProcessRunner runner;
	
	public VerifyTAPN(FileFinder fileFinder, Messenger messenger) {
		this.fileFinder = fileFinder;
		this.messenger = messenger;
	}
	
	public String getPath() {
		return verifytapnpath;
	}

	public String getVersion() { // atm. any version of VerifyTAPN will do
		return "";
	}

	public boolean isCorrectVersion() {
		if (isNotSetup()) {
			messenger.displayErrorMessage(
					"No verifyTAPN specified: The verification is cancelled",
					"Verification Error");
			return false;
		}
		
		return true;
	}

	public void kill() {
		if (runner != null) {
			runner.kill();
		}
	}

	public boolean setup() {
		if (isNotSetup()) {
			messenger.displayInfoMessage(NEED_TO_LOCATE_VERIFYTAPN_MSG, "Locate VerifyTAPN");

			try {
				File file = fileFinder.ShowFileBrowserDialog("VerifyTAPN", "");
				if(file != null)
					verifytapnpath = file.getAbsolutePath();

			} catch (Exception e) {
				messenger.displayErrorMessage("There were errors performing the requested action:\n" + e, "Error");
			}

		}

		return !isNotSetup();
	}

	private boolean isNotSetup() {
		return verifytapnpath == null || verifytapnpath.equals("");
	}

	public VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, TAPNQuery query) throws Exception {	
		if(!supportsModel(model.value1()))
			throw new UnsupportedModelException("VerifyTAPN does not support the given model.");
		
		if(!supportsQuery(model.value1(), query))
			throw new UnsupportedQueryException("VerifyTAPN does not support the given query.");
		
		VerifyTAPNExporter exporter = new VerifyTAPNExporter();
		ExportedVerifyTAPNModel exportedModel = exporter.export(model.value1(), query);

		if (exportedModel == null) {
			messenger.displayErrorMessage("There was an error exporting the model");
		}

		return verify(options, model, exportedModel, query);
	}
	
	private VerificationResult<TimedArcPetriNetTrace> verify(VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query) {
		((VerifyTAPNOptions)options).setTokensInModel(model.value1().marking().size()); // TODO: get rid of me
		runner = new ProcessRunner(verifytapnpath, createArgumentString(exportedModel.modelFile(), exportedModel.queryFile(), options));
		runner.run();

		if (runner.error()) {
			return null;
		} else {
			String errorOutput = readOutput(runner.errorOutput());
			String standardOutput = readOutput(runner.standardOutput());

			QueryResult queryResult = parseQueryResult(standardOutput);

			if (queryResult == null) {
				return new VerificationResult<TimedArcPetriNetTrace>(errorOutput + System.getProperty("line.separator") + standardOutput);
			} else {
				TimedArcPetriNetTrace tapnTrace = parseTrace(errorOutput, options, model, exportedModel, query, queryResult);
				return new VerificationResult<TimedArcPetriNetTrace>(queryResult, tapnTrace, runner.getRunningTime()); 
			}
		}
	}
	
	private TimedArcPetriNetTrace parseTrace(String output, VerificationOptions options, Tuple<TimedArcPetriNet, NameMapping> model, ExportedVerifyTAPNModel exportedModel, TAPNQuery query, QueryResult queryResult) {
		if (((VerifyTAPNOptions) options).trace() == TraceOption.NONE) return null;
		
		VerifyTAPNTraceParser traceParser = new VerifyTAPNTraceParser(model.value1());
		TimedArcPetriNetTrace trace = traceParser.parseTrace(new BufferedReader(new StringReader(output)));
		
		if (trace == null) {
			if (((VerifyTAPNOptions) options).trace() != TraceOption.NONE) {
				if((query.getProperty() instanceof TCTLEFNode && !queryResult.isQuerySatisfied()) || (query.getProperty() instanceof TCTLAGNode && queryResult.isQuerySatisfied()))
					return null;
				else
					messenger.displayErrorMessage("VerifyTAPN could not generate the requested trace for the model. Try another trace option.");
			}
		} 
		return trace;
	}

	private String createArgumentString(String modelFile, String queryFile, VerificationOptions options) {
		StringBuffer buffer = new StringBuffer(options.toString());
		buffer.append(" ");
		buffer.append(modelFile);
		buffer.append(" ");
		buffer.append(queryFile);

		return buffer.toString();
	}
	
	private String readOutput(BufferedReader reader) {
		try {
			if (!reader.ready())
				return "";
		} catch (IOException e1) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
		}

		return buffer.toString();
	}
	
	private QueryResult parseQueryResult(String output) {
		VerifyTAPNOutputParser outputParser = new VerifyTAPNOutputParser();
		QueryResult queryResult = outputParser.parseOutput(output);
		return queryResult;
	}
	
	
	boolean supportsModel(TimedArcPetriNet model) {
		return true;
	}
	
	boolean supportsQuery(TimedArcPetriNet model, TAPNQuery query) {
		if(query.getProperty() instanceof TCTLEGNode || query.getProperty() instanceof TCTLAFNode) {
			return false;
		}
		return true;
	}

	
}
