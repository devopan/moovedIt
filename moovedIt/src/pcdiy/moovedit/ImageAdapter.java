package pcdiy.moovedit;

import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
//import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private File mediaStore = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/moovedit");
    private File[] images = mediaStore.listFiles();
    
    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return images.length;
    }
    
    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(6, 6, 6, 6);
        } else {
            imageView = (ImageView) convertView;
        }
        Uri img_uri = Uri.fromFile(images[position]);
        imageView.setImageURI(img_uri);
        return imageView;
    }
}