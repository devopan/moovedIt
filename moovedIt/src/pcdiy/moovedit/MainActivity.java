package pcdiy.moovedit;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.core.Size;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends ActionBarActivity implements CvCameraViewListener2 {
    public final static String EXTRA_MESSAGE = "pcdiy.moovedit.MESSAGE";
    private static final String TAG = null;
    private UnifiedCamera mOpenCvCameraView;
    private ArrayList<Mat> frames = new ArrayList<Mat>(3);
    private ArrayList<Scalar> scalarSums = new ArrayList<Scalar>(3);
    private ArrayList<Double> framesSums = new ArrayList<Double>();
    private List<org.opencv.core.Size> mResolutionList;
    private List<Size> supported_resolution_size;
    private org.opencv.core.Size preview_resolution_size = new org.opencv.core.Size(200,200);
    private Mat bgsMat;
    private Mat bgsMat2;
    private static Mat frameIn;
    private static Mat frameInter;
    private static Mat input;
	private static Mat output;
	private static Mat aDiff;
	private static Mat aTemplate;
    private SharedPreferences sharedPrefs;
    private Preferences sharedPrefs1;
    private SharedPreferences.Editor editPrefs;
    private ListPreference list;
    private PreferenceScreen root;
    private Set<String> setStr;
	private int thresPref;
    private String algPref;
    private boolean locPref;
    private String resPref;
    private String effPref;
    private static boolean isCapture;
    private CompoundButton tgbtn;
    private ToggleButton begin_end;
    private int threshold = 0;
    private BackgroundSubtractorMOG bgSbmog;
    private BackgroundSubtractorMOG2 bgSbmog2;
    private int frame_counter;
    private int photo_counter;
    private int detection_sensitivity;
    private int detection_method;
    private boolean detect_loc;
    private int preview_resolution;
    private int preview_effects;
    private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    private Scalar contour_color = new Scalar (50,50,200);
    private File mediaStore = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/moovedit");
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                	System.loadLibrary("moovedit");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
        if (!mediaStore.exists()) 
            mediaStore.mkdir();
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null) {
            try {
                Thread.sleep(2000);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }   
    }

    @Override
    public void onResume()
    {
        super.onResume();
        
        // Listening to preference values
        thresPref = sharedPrefs.getInt(SettingsActivity.KEY_PREF_THRESHOLD, 0);
        algPref = sharedPrefs.getString(SettingsActivity.KEY_PREF_ALGORITHM, "0");
        locPref = sharedPrefs.getBoolean(SettingsActivity.KEY_PREF_LOCATE_OBJECT, false);
        resPref = sharedPrefs.getString(SettingsActivity.KEY_PREF_RESOLUTION, "0");
        effPref = sharedPrefs.getString(SettingsActivity.KEY_PREF_PREVIEW_EFFECT, "0");
        detection_method = Integer.parseInt(algPref);
        preview_effects = Integer.parseInt(effPref);
        
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        mOpenCvCameraView = (UnifiedCamera) findViewById(R.id.unified_surface_view);
        tgbtn = (CompoundButton) findViewById(R.id.preview_onoff);
        begin_end = (ToggleButton) findViewById(R.id.begin_end);
        final Button glbtn = (Button) findViewById(R.id.gallery);
        final Button andr_cmr = (Button) findViewById(R.id.launch_camera);
        
        if (tgbtn.isChecked()) {
            // start preview - create surface view
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.enableView();
            begin_end.setEnabled(true);
        }
        else {
            // end preview - create welcome message
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
            begin_end.setChecked(false);
            begin_end.setEnabled(false);
        }
        
        tgbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton tgbtn_v, boolean isChecked) {
                if (isChecked == true) {
                    // start preview - create surface view
                	mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                	mOpenCvCameraView.setCvCameraViewListener(MainActivity.this);
                    mOpenCvCameraView.enableView();
                    begin_end.setEnabled(true);
                }
                else {
                    // end preview - create welcome message
                    mOpenCvCameraView.disableView();
                    mOpenCvCameraView.setCvCameraViewListener( (CvCameraViewListener2) null);
                    mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
                    begin_end.setChecked(false);
                    begin_end.setEnabled(false);
                }
            }
        });
        
      begin_end.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton begin_end_v, boolean isChecked) {
                if (isChecked == true) {
                    // start main operation
                    mOpenCvCameraView.disableView();
                    mOpenCvCameraView.setCvCameraViewListener(MainActivity.this);
                    mOpenCvCameraView.enableView();
                    isCapture = true;
                }
                else {
                    // end main operation
                    try {
                        Thread.sleep(1000);
                        mOpenCvCameraView.disableView();
                        mOpenCvCameraView.enableView();
                        isCapture = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
          }
      });
      
      glbtn.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
              Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
              startActivity(intent);
          }
      });
      
      mOpenCvCameraView.setOnTouchListener(new View.OnTouchListener() {
	      @Override
	  	  public boolean onTouch(View v, MotionEvent event) {
	          Log.i(TAG,"onTouch event");
	          Imgproc.cvtColor(frameIn, output, Imgproc.COLOR_RGBA2BGR);
	          String date = new SimpleDateFormat("yyyy-MM-dd_HH:mm.ss.SSS").format(new Date());
	          Highgui.imwrite(mediaStore + "/photo_taken_at_" + date + "_" + ".jpeg", output);
	          Toast.makeText(getBaseContext() , Environment.getExternalStorageDirectory().getAbsolutePath() + "/moovedit" + "/taken_at_" + date + ".jpeg" , Toast.LENGTH_SHORT).show();
	          
	          return false;
	      }
      });
      andr_cmr.setOnClickListener(new View.OnClickListener() {
    	  public void onClick(View v) {
    		  Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	      if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
    	          startActivityForResult(takePictureIntent, 1);
    	      }
    	  }
      });
    }
    
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            try {
                Thread.sleep(2000);
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
        	openSettings();
            return true;
        case R.id.action_help:
            openHelp();
            return true;
        case R.id.action_about:
            openAbout();
            return true;
        default:
            return super.onOptionsItemSelected(item);
    }
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    /** Called when the user clicks one of the menu buttons */
    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    
    public void openHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }
    
    public void openAbout() {
        DialogFragment newFragment = new About();
        newFragment.show(getSupportFragmentManager(), "about");
    }
    
    public void onCameraViewStarted(int width, int height) {
    	photo_counter = 0;
        mResolutionList = mOpenCvCameraView.getResolutionList();
        preview_resolution = Integer.parseInt(resPref);
        if (preview_resolution != 0 ) {
        	switch (preview_resolution) {
        	case 1: preview_resolution_size.height = 144;
					preview_resolution_size.width = 176;
				break;
        	case 2: preview_resolution_size.height = 240;
        			preview_resolution_size.width = 320;
        		break;
        	case 3: preview_resolution_size.height = 288;
					preview_resolution_size.width = 352;
				break;
        	case 4: preview_resolution_size.height = 288;
					preview_resolution_size.width = 384;
				break;
        	case 5: preview_resolution_size.height = 320;
					preview_resolution_size.width = 480;
				break;
        	case 6: preview_resolution_size.height = 432;
					preview_resolution_size.width = 576;
				break;
        	case 7: preview_resolution_size.height = 480;
					preview_resolution_size.width = 720;
				break;
        	case 8: preview_resolution_size.height = 432;
					preview_resolution_size.width = 768;
				break;
        	case 9: preview_resolution_size.height = 480;
					preview_resolution_size.width = 864;
				break;
        	case 10: preview_resolution_size.height = 480;
					 preview_resolution_size.width = 640;
				break;
        	case 11: preview_resolution_size.height = 768;
					 preview_resolution_size.width = 1024;
				break;
        	case 12: preview_resolution_size.height = 720;
					 preview_resolution_size.width = 1280;
				break;
        	case 13: preview_resolution_size.height = 1200;
					 preview_resolution_size.width = 1600;
				break;
        	case 14: preview_resolution_size.height = 1080;
					 preview_resolution_size.width = 1920;
				break;
        	case 15: preview_resolution_size.height = 1536;
					 preview_resolution_size.width = 2048;
				break;
        	case 16: preview_resolution_size.height = 1944;
					 preview_resolution_size.width = 2592;
				break;
        	case 17: preview_resolution_size.height = 2304;
					 preview_resolution_size.width = 3072;
				break;
        	case 18: preview_resolution_size.height = 2448;
					 preview_resolution_size.width = 3264;
				break;
        	case 19: preview_resolution_size.height = 2736;
					 preview_resolution_size.width = 3648;
				break;
        	case 20: preview_resolution_size.height = 3000;
					 preview_resolution_size.width = 4000;
				break;
        	}
        }
        
    	if (preview_resolution_size != null && mResolutionList.contains(preview_resolution_size)) {
            try {
            	mOpenCvCameraView.setResolution(preview_resolution_size);
            	String caption = ("Resolution set to: " +(int)preview_resolution_size.width + " x " + (int)preview_resolution_size.height);
                Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
				Thread.sleep(100);

			} catch (InterruptedException e) {
				e.printStackTrace();
				Toast.makeText(this, "Chosen resolution not supported.", Toast.LENGTH_SHORT).show();
			}
        }
        else if (preview_resolution_size ==  null) {
        	Toast.makeText(this, "Chosen resolution not supported.", Toast.LENGTH_SHORT).show();
        }

    	threshold = width * height;
    	frame_counter = 0;
    	frameInter = Mat.zeros(height, width, CvType.CV_8UC1); 
    	frameIn = Mat.zeros(height, width, CvType.CV_8UC4); 
    	input = Mat.zeros(height, width, CvType.CV_8UC1); 
    	output = Mat.zeros(height, width, CvType.CV_8UC4); 
    	aDiff = Mat.zeros(height, width, CvType.CV_8UC1); 
    	aTemplate = Mat.zeros(height, width, CvType.CV_8UC1);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onCameraViewStopped() {
    	isCapture = false;
       frame_counter = 0;
       frameInter.release();
       frameIn.release();
       input.release();
       output.release();
       aTemplate.release();
       aDiff.release();
    }
    
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	if (isCapture) {
    		new Compare().execute(inputFrame);
    		frameIn = inputFrame.rgba();
    	}
    	else {
    		switch (preview_effects) {
    		case 1:
    			frameIn = inputFrame.rgba();
                break;
            case 2:
            	frameIn = Mat.zeros(inputFrame.gray().size(), CvType.CV_8UC1); 
            	frameIn = inputFrame.gray();
                break;
            case 3:
                Imgproc.Canny(inputFrame.gray(), frameInter, 80, 100);
                Imgproc.cvtColor(frameInter, frameIn, Imgproc.COLOR_GRAY2RGBA, 4);
                break;
            case 4:
            	Imgproc.Sobel(inputFrame.gray(), frameInter, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(frameInter, frameInter, 10, 0);
                Imgproc.cvtColor(frameInter, frameIn, Imgproc.COLOR_GRAY2BGRA, 4);
            	break;
            default:
            	break;
            }
    	}
    	if (photo_counter > 1000) {
    		final CompoundButton tgbtn = (CompoundButton) findViewById(R.id.preview_onoff);
            final ToggleButton begin_end = (ToggleButton) findViewById(R.id.begin_end);
    		mOpenCvCameraView.disableView();
            mOpenCvCameraView.setCvCameraViewListener( (CvCameraViewListener2) null);
            mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
            begin_end.setChecked(false);
            begin_end.setEnabled(false);
            tgbtn.setChecked(false);
            Toast.makeText(getBaseContext() , "Total amount of photos taken has exceeded the maximum limit allowed! The capture session has been closed for safety reasons. Please lower sensitivity or start a new capture session", Toast.LENGTH_LONG).show();
    	}
        return frameIn;
    }
    
    private class Compare extends AsyncTask<CvCameraViewFrame, Mat, String> {
    	double totalDif = 0;
    	double inSum = 0;
    	Scalar inScl = new Scalar(CvType.CV_8UC1);
    	Mat inMat = new Mat();
    	
        @Override
        protected String doInBackground(CvCameraViewFrame...params) {
        	frame_counter++;

        	switch (detection_method) {
            case 1:  
            	//---- method 1
            	if (frame_counter < 100) {
            		framesSums.add(Core.sumElems(params[0].gray()).val[0]);
            	}
                inSum = Core.sumElems(params[0].gray()).val[0];
                framesSums.add(inSum);
                totalDif = inSum - framesSums.get(framesSums.size() - 2);
                //---- method 1
                     break;
            case 2:  
            	//---- method 2
            	params[0].gray().setTo(inScl);
            	if (frame_counter < 100) {
            		scalarSums.add(inScl);
            	}
            	scalarSums.add(inScl);
                Core.absdiff(params[0].gray(), scalarSums.get(scalarSums.size() - 2), aDiff);
                Imgproc.threshold(aDiff, aDiff, 35, 255, Imgproc.THRESH_TOZERO);
                totalDif =  Core.countNonZero(aDiff);
                //---- method 2
                     break;
            case 3: 
            	//---- method 3
            	params[0].gray().setTo(inScl);
            	if (frame_counter < 100) {
            		scalarSums.add(inScl);
            	}
            	scalarSums.add(inScl);
            	Imgproc.matchTemplate(params[0].gray(), aTemplate.setTo(scalarSums.get(scalarSums.size() - 2)), aDiff, Imgproc.TM_SQDIFF );
                Imgproc.threshold(aDiff, aDiff, 35, 255, Imgproc.THRESH_TOZERO);
                totalDif =  Core.countNonZero(aDiff);
                //---- method 3
                     break;
            case 4:  
            	//---- method 4
            	params[0].gray().setTo(inScl);
            	if (frame_counter < 100) {
            		scalarSums.add(inScl);
            	}
            	scalarSums.add(inScl);
            	Core.compare(params[0].gray(), scalarSums.get(scalarSums.size() - 2), aDiff, Core.CMP_EQ);
            	Imgproc.threshold(aDiff, aDiff, 35, 255, Imgproc.THRESH_TOZERO);
            	totalDif =  Core.countNonZero(aDiff);
            	//---- method 4
                     break;
            case 5:  
            	//---- method 5
            	params[0].gray().setTo(inScl);
            	if (frame_counter < 100) {
            		scalarSums.add(inScl);
            	}
            	scalarSums.add(inScl);
            	Core.subtract(params[0].gray(), scalarSums.get(scalarSums.size() - 2), aDiff);
            	Imgproc.threshold(aDiff, aDiff, 35, 255, Imgproc.THRESH_TOZERO);
            	totalDif =  Core.countNonZero(aDiff);
            	//---- method 5
                     break;
            case 6:
            	// NATIVE CODE CALL
            	inMat = Mat.zeros(params[0].gray().size(), CvType.CV_8UC1);
            	if (frame_counter < 100) {
            		inMat = params[0].gray();
            		frames.add(inMat);
            	}
            	inMat = params[0].gray().clone();
            	frames.add(inMat);
            	totalDif = DetectMotion(params[0].gray().getNativeObjAddr(), (frames.get(frames.size() - 2)).getNativeObjAddr());
            	inMat.release();
            	// NATIVE CODE CALL
            		 break;
            case 7:
            	bgSbmog2.apply(params[0].gray(), bgsMat2, 0.5);
            	break;
            default: 
                     break;
        	}
        	
        	if (detection_method == 1) {
        		if (Math.abs(totalDif) >= 150*threshold/thresPref  && params[0].rgba() != null) {
        			String date = new SimpleDateFormat("HH:mm.ss.SSS").format(new Date());
                    Imgproc.cvtColor(params[0].rgba(), output, Imgproc.COLOR_RGBA2BGR);
                    if (locPref == true) {
                		Imgproc.findContours(params[0].gray(), contours, input, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                		Imgproc.drawContours(output, contours, -1, contour_color, 1);
                    }
                    Highgui.imwrite(mediaStore + "/" + date + "__" + totalDif + "_" + threshold + "_" + frame_counter + ".jpeg", output);
                    photo_counter++;

                    return date;
        		}              
        		else {
        			
        			return null;
        		}
        	}
        	else if (detection_method == 7) {
        		if (contours.size() > 0) {
        			String date = new SimpleDateFormat("HH:mm.ss.SSS").format(new Date());
                    Imgproc.cvtColor(params[0].rgba(), output, Imgproc.COLOR_RGBA2BGR);
                    Imgproc.findContours(params[0].gray(), contours, bgsMat2, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
            		Imgproc.drawContours(bgsMat2, contours, -1, contour_color, 1);
                    if (locPref == true) {
                		Imgproc.findContours(params[0].gray(), contours, bgsMat2, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                		Imgproc.drawContours(output, contours, -1, contour_color, 1);
                    }
                    Highgui.imwrite(mediaStore + "/" + date + "__" + totalDif + "_" + threshold + "_" + frame_counter + ".png", bgsMat2);
                    photo_counter++;

                    return date;
        		} else {
        			return null;
        		}
        	}
        	else if (detection_method == 6) {
        		if (Math.abs(totalDif) > 500000000*threshold/thresPref  && params[0].rgba() != null) {
                    String date = new SimpleDateFormat("HH:mm.ss.SSS").format(new Date());
                    Imgproc.cvtColor(params[0].rgba(), output, Imgproc.COLOR_RGBA2BGR);
                    if (locPref == true) {
                		Imgproc.findContours(params[0].gray(), contours, input, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                		Imgproc.drawContours(output, contours, -1, contour_color, 1);
                    }
                    Highgui.imwrite(mediaStore + "/" + date + "__" + totalDif + "_" + threshold + "_" + frame_counter + ".jpeg", output);
                    photo_counter++;

                    return date;
        		}
        		else {

        			return null;
        		}
        	}
        	else {
        		if (Math.abs(totalDif) > 450*threshold/thresPref  && params[0].rgba() != null) {
                    String date = new SimpleDateFormat("HH:mm.ss.SSS").format(new Date());
                    Imgproc.cvtColor(params[0].rgba(), output, Imgproc.COLOR_RGBA2BGR);
                    if (locPref == true) {
                		Imgproc.findContours(params[0].gray(), contours, input, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                		Imgproc.drawContours(output, contours, -1, contour_color, 1);
                    }
                    Highgui.imwrite(mediaStore + "/" + date + "__" + totalDif + "_" + threshold + "_" + frame_counter + ".jpeg", output);
                    photo_counter++;

                    return date;
        		}
        		else {

        			return null;
        		}
        	}
        }

        protected void onPostExecute(String date) {
            if (date != null) {
                Toast.makeText(getBaseContext() , date + "__" + frame_counter + "_" + ".jpeg" + " saved", Toast.LENGTH_SHORT).show();
            }
            else {
            }
        }
    }

    public native int DetectMotion(long addr1Gray1, long addr1Gray2);

}