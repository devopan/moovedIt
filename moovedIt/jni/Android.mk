LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include K:\Android\OpenCV\sdk\native\jni\OpenCV.mk

LOCAL_MODULE    := moovedit
LOCAL_SRC_FILES := moovedit_jni.cpp
LOCAL_SHARED_LIBRARIES += opencv_library_-_2.4.9
LOCAL_SHARED_LIBRARIES += appcompat_v7
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
