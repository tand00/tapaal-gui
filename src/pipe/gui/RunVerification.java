/**
 * 
 */
package pipe.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pipe.gui.GuiFrame.GUIMode;
import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.verification.IconSelector;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.VerificationResult;

public class RunVerification extends RunVerificationBase {
	private IconSelector iconSelector;
	public RunVerification(ModelChecker modelChecker, IconSelector selector, Messenger messenger) {
		super(modelChecker, messenger);
		iconSelector = selector;
	}

	@Override
	protected void showResult(VerificationResult<TAPNNetworkTrace> result) {
		if (result != null && !result.error()) {
			JOptionPane.showMessageDialog(CreateGui.getApp(), 
					createMessagePanel(result),
					"Verification Result", JOptionPane.INFORMATION_MESSAGE, iconSelector.getIconFor(result.getQueryResult()));

			if (result.getTrace() != null) {
				// DataLayer model = CreateGui.getModel();
				// TraceTransformer interpreter = model.isUsingColors() ? new
				// ColoredTraceTransformer(model) : new TraceTransformer(model);
				// TAPNTrace trace =
				// interpreter.interpretTrace(result.getTrace());
				CreateGui.getApp().setGUIMode(GUIMode.animation);

				CreateGui.getAnimator().SetTrace(result.getTrace());

			}

		}else{
			
			//Check if the is something like 
			//verifyta: relocation_error:
			///usr/lib32/libnss_msdn4_minimal.so.2 symbol strlen, 
			//version GLIB_2.0 not defined in file libc.so.6 with
			//link time reference
			//is the error as this (often) means the possibility for a uppaal licence key error
			
			String extraInformation = "";
			
			if (result != null && (result.errorMessage().contains("relocation") || result.errorMessage().toLowerCase().contains("internet connection is required for activation"))){
				
				extraInformation = "We detected an error that often arises when UPPAAL is missing a valid Licence file.\n" +
						"Open the UPPAAL GUI while connected to the internet to correct this problem.";
				
			}
			
			String message = "An error occured during the verification." +
			System.getProperty("line.separator") + 	
			System.getProperty("line.separator");
			
			if (!extraInformation.equals("")){
				message += extraInformation +			
				System.getProperty("line.separator") + 	
				System.getProperty("line.separator");
			}
			
			message += "Model checker output:\n" + result.errorMessage();
			
			messenger.displayWrappedErrorMessage(message,"Error during verification");

		}
	}

	private String toHTML(String string){
		StringBuffer buffer = new StringBuffer("<html>");
		buffer.append(string.replace(System.getProperty("line.separator"), "<br/>"));
		buffer.append("</html>");
		return buffer.toString();
	}
	
	private JPanel createTransitionStatisticsPanel(final VerificationResult<TAPNNetworkTrace> result) {
		JPanel panel = new JPanel(new GridBagLayout());
		JPanel headLinePanel = new JPanel(new GridBagLayout());
		JPanel fullPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		//gbc.fill = gbc.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 1;
		//gbc.gridwidth = 2;
		gbc.insets = new Insets(15,0,15,15);
		gbc.anchor = GridBagConstraints.WEST;
		headLinePanel.add(new JLabel(toHTML("Number of times transitions were enabled during the search.\n")), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		//gbc.fill = gbc.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(15,15,15,10);
		gbc.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel(toHTML(extractNamesFromTransitionStatistics(result))), gbc);
		
		gbc = new GridBagConstraints();
		//gbc.fill = gbc.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new Insets(15,10,15,15);
		gbc.anchor = GridBagConstraints.EAST;		
		panel.add(new JLabel(toHTML(extractValuesFromTransitionStatistics(result))), gbc);
		
		gbc = new GridBagConstraints();
		//gbc.fill = gbc.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		//gbc.insets = new Insets(15,10,15,15);
		gbc.anchor = GridBagConstraints.WEST;
		fullPanel.add(headLinePanel,gbc);
		
		gbc = new GridBagConstraints();
		//gbc.fill = gbc.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		//gbc.insets = new Insets(15,10,15,15);
		gbc.anchor = GridBagConstraints.WEST;
		fullPanel.add(panel,gbc);
		
		return fullPanel;
	}
	
	private String extractNamesFromTransitionStatistics(final VerificationResult<TAPNNetworkTrace> result) {
		StringBuilder names = new StringBuilder();
		List<Tuple<String,Integer>> transistionStats = result.getTransitionStatistics();
		for (int i=0;i<transistionStats.size();i++) {
			names.append(transistionStats.get(i).value1());
			names.append(":");
			names.append("\n");
		}
		return names.toString();
	}
	
	private String extractValuesFromTransitionStatistics(final VerificationResult<TAPNNetworkTrace> result) {
		StringBuilder names = new StringBuilder();
		List<Tuple<String,Integer>> transistionStats = result.getTransitionStatistics();
		for (int i=0;i<transistionStats.size();i++) {
			names.append(transistionStats.get(i).value2().toString());
			names.append("\n");
		}
		return names.toString();
	}
	
	private JPanel createMessagePanel(final VerificationResult<TAPNNetworkTrace> result) {
		final JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0,0,15,0);
		gbc.anchor = GridBagConstraints.WEST;		
		panel.add(new JLabel(toHTML(result.getResultString())), gbc);
		
		if(modelChecker.supportsStats()){
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(0,0,15,0);
			gbc.anchor = GridBagConstraints.WEST;
			panel.add(new JLabel(toHTML(result.getStatsAsString())), gbc);
			
			JButton infoButton = new JButton("Explanation");
			infoButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					JOptionPane.showMessageDialog(panel, modelChecker.getStatsExplanation(), "Stats Explanation", JOptionPane.INFORMATION_MESSAGE);
				}
			});
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.insets = new Insets(0,10,15,0);
			gbc.anchor = GridBagConstraints.EAST;
			panel.add(infoButton, gbc);
			
			JButton transitionStatsButton = new JButton("Transition Statistics");
			transitionStatsButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					JOptionPane.showMessageDialog(panel,createTransitionStatisticsPanel(result) , "Transition Statistics", JOptionPane.INFORMATION_MESSAGE);
				}
			});
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.insets = new Insets(10,0,10,0);
			gbc.anchor = GridBagConstraints.WEST;
			panel.add(transitionStatsButton, gbc);
		}
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel(result.getVerificationTimeString()), gbc);
		
		return panel;
	}
	
	
}