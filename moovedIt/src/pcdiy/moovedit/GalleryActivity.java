package pcdiy.moovedit;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;
//import android.widget.Toast;

public class GalleryActivity extends ActionBarActivity implements Imagedel.ImagedelListener {
	private File mediaStore = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/moovedit");
    private File[] images = mediaStore.listFiles();
    //private boolean deleteImgBool;
    private ImageAdapter imgAdapter = new ImageAdapter(this);
    private boolean individualSel = false;
    private boolean allSel = false;
    private GridView gridview;
    private int img_counter;
    private ArrayList<Integer> marked; 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_gallery);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new PlaceholderFragment()).commit();
        }
	}
	
	public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
    	super.onResume();

        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(imgAdapter);
        marked = new ArrayList<Integer>(gridview.getCount());
	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	
	        	if (marked.contains(position))  {
	        		img_counter--;
	        		marked.remove(position);
	        	}
	        	else if (individualSel) {
	        		img_counter++;
	    			marked.add(position, position);
	        	}
	        	else {
	        		Uri img_uri = Uri.fromFile(images[position]);
		        	Intent intent = new Intent();
		        	intent.setAction(Intent.ACTION_VIEW);
		        	intent.setDataAndType(img_uri, "image/*");
					startActivity(intent);
	        	}
	        }
	    });
	    
	    gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
	    	@Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
	    		Imagedel newFragment = new Imagedel();
	    		newFragment.setPosition(position);
	            newFragment.show(getSupportFragmentManager(), "imagedel");
	            
                return true;
	        }
	    });
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallery, menu);
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	switch (item.getItemId()) {
    	case R.id.action_select_all:
            selectAll();
            return true;
    	case R.id.action_select_check:
            selectIndividual();
            return true;
        case R.id.action_select_delete:
            deleteSelected();
            return true;
        case R.id.action_refresh:
        	finish();
    	    startActivity(getIntent());
        	return true;
        case R.id.action_select_properties:
        	openProperties();
        default:
    	}
    	return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
            return rootView;
        }
    }
    
    /** Called when the user clicks the Send button */
    public void selectAll() {
    	img_counter = 0;
    	//final GridView gridview = (GridView) findViewById(R.id.gridview);
    	if (!allSel) {
    		for (int i = 0; i < gridview.getCount(); i++) { 
    			img_counter++;
    			marked.add(i);
        	}
        	allSel = true;
        	Toast.makeText(getBaseContext() , "Selected " + img_counter + " images", Toast.LENGTH_SHORT).show();
    	}
    	else {
    		for (int i = 0; i < gridview.getCount(); i++) { 
    			img_counter = 0;
    			marked.clear();
        	}
    		allSel = false;	
    		individualSel = false;
    		Toast.makeText(getBaseContext() , "Selection cleared", Toast.LENGTH_SHORT).show();
    	}
    	
    	
    }
    
    public void selectIndividual() {
    	if (individualSel) {
    		individualSel = false;
    		img_counter = 0;
			marked.clear();
    		allSel = false;
    		Toast.makeText(getBaseContext() , "Individual selection disabled", Toast.LENGTH_SHORT).show();
    	}
    	else {
    		individualSel = true;
        	Toast.makeText(getBaseContext() , "Individual selection enabled", Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void deleteSelected() {
    	img_counter = 0;
    	//final GridView gridview = (GridView) findViewById(R.id.gridview);
    	for (int i = 0; i < marked.size(); i++) { 
    		if (marked.contains(i)) {
    			images[marked.get(i)].delete();
    			img_counter++;
    		}
    	}
    	imgAdapter.notifyDataSetChanged();
	    startActivity(getIntent());
	    if (img_counter > 0) {
	    	Toast.makeText(getBaseContext() , "Images deleted: " + img_counter, Toast.LENGTH_LONG).show();
	    	img_counter = 0;
	    }
	    else
	    	Toast.makeText(getBaseContext() , "No images seleted", Toast.LENGTH_LONG).show();
    }
    
    
    
    
    public void openProperties() {
    	//final GridView gridview = (GridView) findViewById(R.id.gridview);
    	DialogFragment newFragment = new Properties();
        newFragment.show(getSupportFragmentManager(), "properties");
    }

	public void onDialogPositiveClick(Imagedel dialog, int position) {
		images[dialog.getPosition()].delete();
	    imgAdapter.notifyDataSetChanged();
	    Toast.makeText(getBaseContext() , "Image deleted", Toast.LENGTH_SHORT).show();
	    finish();
	    startActivity(getIntent());
	}

	public void onDialogNegativeClick(Imagedel dialog, int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDialogNeutralClick(Imagedel dialog, int position) {
		// TODO Auto-generated method stub
		
	}
}
