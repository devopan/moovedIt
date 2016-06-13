package pcdiy.moovedit;

import java.util.List;
import org.opencv.android.NativeCameraView;
import android.content.Context;
import android.util.AttributeSet;

public class UnifiedCamera extends NativeCameraView {
    public UnifiedCamera(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<org.opencv.core.Size> getResolutionList() {
    	
        return mCamera.getSupportedPreviewSizes();
    }

    public void setResolution(org.opencv.core.Size preview_resolution_size) {
        disconnectCamera();
        mMaxHeight = (int) preview_resolution_size.height;
        mMaxWidth = (int) preview_resolution_size.width;
        connectCamera(getWidth(), getHeight());
    }
}
