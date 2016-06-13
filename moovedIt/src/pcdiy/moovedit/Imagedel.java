package pcdiy.moovedit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class Imagedel extends DialogFragment {
	private int position;
	
	public int getPosition() {
        return position;
    }
	
	public void setPosition(int pos) {
        position = pos;
    }
	
	public interface ImagedelListener {
        public void onDialogPositiveClick(Imagedel dialog, int position);
        public void onDialogNegativeClick(Imagedel dialog, int position);
        public void onDialogNeutralClick(Imagedel dialog, int position);
    }
    
    // Use this instance of the interface to deliver action events
	ImagedelListener imgdelListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
        	imgdelListener = (ImagedelListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.img_del_title)
        	   .setMessage(R.string.img_del_txt)
               .setPositiveButton(R.string.img_del_delete, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int position) {
                       // DELETE ZE IMG!
                	   dialog.dismiss();
                	   imgdelListener.onDialogPositiveClick(Imagedel.this, position);
                   }
               })
               .setNegativeButton(R.string.img_del_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int position) {
                       // OOOPSS OH NOes
                	   dialog.cancel();
                	   imgdelListener.onDialogNegativeClick(Imagedel.this, position);
                   }
               })
               .setNeutralButton(R.string.img_del_share, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int position) {
                       // SHARE TO THE INTERNETZZ
                	   dialog.dismiss();
                	   imgdelListener.onDialogNeutralClick(Imagedel.this, position);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}