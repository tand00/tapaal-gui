package pipe.gui;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import pipe.dataLayer.Template;
import pipe.gui.graphicElements.Transition;

import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedTAPNNetworkTrace;
import dk.aau.cs.verification.VerifyTAPN.TraceType;

public class AnimationHistoryComponent extends JList {
	private static final long serialVersionUID = -4284885450021683552L;
	private TraceType lastShown = TraceType.NOT_EG;

	public AnimationHistoryComponent() {
		super();
		setModel(new DefaultListModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reset();
		
		for (MouseListener listener : getMouseListeners()) {
			removeMouseListener(listener);
		}
		
		for (MouseMotionListener listener : getMouseMotionListeners()) {
			removeMouseMotionListener(listener);
		}
		for (KeyListener listener : getKeyListeners()) {
			removeKeyListener(listener);
		}
	}

	public void addHistoryItem(String transitionName) {
		if(lastShown == TraceType.NOT_EG){
			getListModel().addElement(transitionName);
			setSelectedIndex(getListModel().size() - 1);
		} else {
			getListModel().add(getListModel().size()-1, transitionName);
			setSelectedIndex(getListModel().size() - 2);
		}
		
		updateAccordingToDeadlock();
	}

	public void addHistoryItemDontChange(String transitionName) {
		if(lastShown == TraceType.NOT_EG){
			getListModel().addElement(transitionName);
		} else {
			getListModel().add(getListModel().size()-1, transitionName);
		}
		updateAccordingToDeadlock();
	}

	public void clearStepsForward() {
		DefaultListModel listModel = getListModel();
		int lastIndex = listModel.size() - 1;

		if (listModel.size() > 1 && getSelectedIndex() < lastIndex) {
			listModel.removeRange(getSelectedIndex() + 1, lastIndex);
		}
		lastShown = TraceType.NOT_EG;
		updateAccordingToDeadlock();
	}

	public void stepForward() {
		if (isStepForwardAllowed()) {
			int nextIndex = getSelectedIndex() + 1;
			setSelectedIndex(nextIndex);
		}
	}

	public void stepBackwards() {
		if (isStepBackAllowed()) {
			int indexToMoveTo = getSelectedIndex() - 1;
			setSelectedIndex(indexToMoveTo);
		}
	}

	public boolean isStepForwardAllowed() {
		if(lastShown != TraceType.EG_DEADLOCK){
			return getSelectedIndex() < getListModel().size() - 1;
		} else {
			return getSelectedIndex() < getListModel().size() - 2;
		}
	}

	public boolean isStepBackAllowed() {
		return getSelectedIndex() > 0;
	}

	public String getCurrentItem() {
		return getElement(getSelectedIndex());
	}

	public String getElement(int i) {
		return (String) getListModel().get(i);
	}

	DefaultListModel getListModel() {
		return (DefaultListModel) getModel();
	}

	@Override
	public void setSelectedIndex(int index) {
		super.setSelectedIndex(index);
		ensureIndexIsVisible(index);
	}

	public void reset() {
		getListModel().clear();
		getListModel().addElement("Initial Marking");
		setSelectedIndex(0);
		lastShown = TraceType.NOT_EG;
		updateAccordingToDeadlock();
	}
	
	static final private String deadlockString = "<html><i><font color=red>" + "Deadlock" + "</i></font></html>";
	static final private String delayForeverString = "<html><i><font color=red>" + "Delay forever" + "</i></font></html>";
	static final private String gotoString = "<html><i><font color=red>" + "Goto *" + "</i></font></html>";
	
	public void setLastShown(TraceType tracetype){
		if(lastShown != TraceType.NOT_EG){
				getListModel().remove(getListModel().size()-1);
		}
		lastShown = tracetype;
		
		switch (tracetype) {
		case EG_DEADLOCK:
			getListModel().addElement(deadlockString);
			break;
		case EG_DELAY_FOREVER:
			getListModel().addElement(delayForeverString);
			break;
		case EG_LOOP:
			getListModel().addElement(gotoString);
		case NOT_EG:
			break;
		}
	}
	
	private void updateAccordingToDeadlock() {
		if(CreateGui.getTab().getSelectedIndex() == -1 || lastShown == TraceType.EG_DELAY_FOREVER){
			return;
		}
		for (Template t : CreateGui.getCurrentTab().activeTemplates()){
			for(Transition trans : t.guiModel().getTransitions()){
				if(trans.isEnabled(true) || trans.isBlueTransition(true)){
					return;
				}
			}
		}
		
		setLastShown(TraceType.EG_DEADLOCK);
	}
}
