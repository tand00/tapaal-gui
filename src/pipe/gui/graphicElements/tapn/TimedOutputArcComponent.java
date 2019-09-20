package pipe.gui.graphicElements.tapn;

import java.awt.Container;
import java.util.Hashtable;

import javax.swing.BoxLayout;

import pipe.gui.CreateGui;

import pipe.gui.Grid;
import pipe.gui.Zoomer;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.ArcPath;
import pipe.gui.graphicElements.PlaceTransitionObject;
import pipe.gui.handler.ArcHandler;
import pipe.gui.undo.ArcTimeIntervalEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.GuardDialogue;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.Weight;

public class TimedOutputArcComponent extends Arc {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5588142404135607382L;

	private dk.aau.cs.model.tapn.TimedOutputArc outputArc;

	public TimedOutputArcComponent(PlaceTransitionObject sourceInput, PlaceTransitionObject targetInput, int weightInput, String idInput) {
		super(sourceInput, targetInput, weightInput, idInput);

		//XXX: se note in funcation
		addMouseHandler();
	}

	/** @deprecated */
	@Deprecated
	public TimedOutputArcComponent(double startPositionXInput,
			double startPositionYInput, double endPositionXInput,
			double endPositionYInput, PlaceTransitionObject sourceInput,
			PlaceTransitionObject targetInput, int weightInput, String idInput,
			boolean taggedInput) {
		this(sourceInput, targetInput, weightInput, idInput);
	}

	/**
	 * Create Petri-Net Arc object
	 */
	public TimedOutputArcComponent(PlaceTransitionObject newSource) {
		super(newSource);

		//XXX: se note in funcation
		addMouseHandler();
	}

	public TimedOutputArcComponent(TimedOutputArcComponent arc) {

		myPath = new ArcPath(this);
		for (int i = 0; i <= arc.myPath.getEndIndex(); i++) {
			myPath.addPoint(arc.myPath.getRealPoint(i).getX(), arc.myPath.getRealPoint(i).getY(), arc.myPath.getPointType(i));
		}
		myPath.createPath();
		this.updateBounds();
		id = arc.id;
		this.setSource(arc.getSource());
		this.setTarget(arc.getTarget());
		this.setNameOffsetX(arc.getNameOffsetXObject());
		this.setNameOffsetY(arc.getNameOffsetYObject());
		this.getNameLabel().setPosition(
				Grid.getModifiedX((int) (arc.getNameLabel().getXPosition() + Zoomer.getZoomedValue(getNameOffsetX(), getZoom()))),
				Grid.getModifiedY((int) (arc.getNameLabel().getYPosition() + Zoomer.getZoomedValue(getNameOffsetY(), getZoom()))));

		//XXX: se note in funcation
		addMouseHandler();
	}

	private void addMouseHandler() {
		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		mouseHandler = new ArcHandler(this);
	}


	public Command setGuardAndWeight(TimeInterval guard, Weight weight) {

		Weight oldWeight = getWeight();
		setWeight(weight);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		repaint();

		return new ArcTimeIntervalEdit(this, guard, guard, oldWeight, weight);
	}

	public void updateLabel(boolean displayConstantNames) {
		getNameLabel().setText("");
		getNameLabel().setText(getWeight().toString(displayConstantNames)+" " + getNameLabel().getText());
		setLabelPosition();
	}
	
	public void showTimeIntervalEditor() {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Arc", true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		// 2 Add Place editor
		contentPane.add(new GuardDialogue(guiDialog.getRootPane(), this));

		guiDialog.setResizable(false);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

	}

	public dk.aau.cs.model.tapn.TimedOutputArc underlyingArc() {
		return outputArc;
	}

	public void setUnderlyingArc(dk.aau.cs.model.tapn.TimedOutputArc outputArc) {
		this.outputArc = outputArc;
	}

	public TimedOutputArcComponent copy(TimedArcPetriNet tapn, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedOutputArcComponent newCopyArc = new TimedOutputArcComponent(this);
		newCopyArc.setSource(oldToNewMapping.get(this.getSource()));
		newCopyArc.setTarget(oldToNewMapping.get(this.getTarget()));
		newCopyArc.setUnderlyingArc(tapn.getOutputArcFromTransitionAndPlace(tapn.getTransitionByName(outputArc.source().name()), tapn.getPlaceByName(outputArc.destination().name())));
		
		return newCopyArc;
	}

	@Override
	public void setWeight(Weight weight) {
		outputArc.setWeight(weight);
	}

	@Override
	public Weight getWeight() {
		return outputArc.getWeight();
	}

	public TimeInterval getGuard() {
		// TODO Auto-generated method stub
		return null;
	}

}
