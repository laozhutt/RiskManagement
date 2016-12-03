LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := signal
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS := \
	-llog \
	-lz \
	-lm \

LOCAL_SRC_FILES := \
	G:\riskapp\RiskManagement-fix\SensorDemo\src\main\jni\jni\com_sensordemo_JniUtils.cpp \
	G:\riskapp\RiskManagement-fix\SensorDemo\src\main\jni\jni\onload.cpp \
	G:\riskapp\RiskManagement-fix\SensorDemo\src\main\jni\src\predict.cpp \
	G:\riskapp\RiskManagement-fix\SensorDemo\src\main\jni\src\svm\svm-predict.cpp \
	G:\riskapp\RiskManagement-fix\SensorDemo\src\main\jni\src\svm\svm.cpp \

LOCAL_C_INCLUDES += G:\riskapp\RiskManagement-fix\SensorDemo\src\main\jni
LOCAL_C_INCLUDES += G:\riskapp\RiskManagement-fix\SensorDemo\src\debug\jni

include $(BUILD_SHARED_LIBRARY)
