/*
 * AddPetriNetObjectEdit.java
 */

package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;
import pipe.gui.DrawingSurfaceImpl;

/**
 *
 * @author corveau
 */
public class AddPetriNetObjectEdit 
        extends UndoableEdit {
   
   PetriNetObject pnObject;
   DataLayer model;
   DrawingSurfaceImpl view;
   
   
   /** Creates a new instance of placeWeightEdit */
   public AddPetriNetObjectEdit(PetriNetObject _pnObject, 
                                DrawingSurfaceImpl _view, DataLayer _model) {
      pnObject = _pnObject;
      view = _view;
      model = _model;
   }

   
   /** */
   @Override
public void undo() {
      pnObject.delete();
   }

   
   /** */
   @Override
public void redo() {
      pnObject.undelete(model, view);
   }
   
   
   @Override
public String toString(){
      return super.toString() + " \"" + pnObject.getName() + "\"";
   }
   
}
