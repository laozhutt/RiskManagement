#include <jni.h>
#include <string.h>
#include <cstdlib>
#include "../src/log.h"
#include "../src/predict.h"
#include "com_sensordemo_JniUtils.h"

#include  "get_func.h"

namespace example {

void print_XYZ(float (*func)(float v[], int size), float X[], float Y[],
		float Z[], int size, int &cnt, float res[]) {
	//	printf("%.2lf ", func(X, size));
	//	printf("%.2lf ", func(Y, size));
	//	printf("%.2lf ", func(Z, size));
	res[cnt] = func(X, size);
	cnt++;
	res[cnt] = func(Y, size);
	cnt++;
	res[cnt] = func(Z, size);
	cnt++;
}

void output(int size, float *X, float *Y, float *Z, float *wX, float *wY,
		float *wZ, float *gX, float *gY, float *gZ, float res[]) {
	int cnt = 0;
	print_XYZ(get_mean, X, Y, Z, size, cnt, res);
	print_XYZ(get_variance, X, Y, Z, size, cnt, res);
	print_XYZ(get_standard_deviation, X, Y, Z, size, cnt, res);
	print_XYZ(get_average_deviation, X, Y, Z, size, cnt, res);
	print_XYZ(get_skewness, X, Y, Z, size, cnt, res);
	print_XYZ(get_kurtosis, X, Y, Z, size, cnt, res);
	print_XYZ(get_zcr, X, Y, Z, size, cnt, res);
	print_XYZ(get_rms_amplitude, X, Y, Z, size, cnt, res);
	print_XYZ(get_lowest_value, X, Y, Z, size, cnt, res);
	print_XYZ(get_highest_value, X, Y, Z, size, cnt, res);

	//printf("%.2f ", get_result(X, Y, Z, size));
	res[cnt] = get_result(X, Y, Z, size);
	cnt++;

	print_XYZ(get_mean, wX, wY, wZ, size, cnt, res);
	print_XYZ(get_variance, wX, wY, wZ, size, cnt, res);
	print_XYZ(get_standard_deviation, wX, wY, wZ, size, cnt, res);
	print_XYZ(get_average_deviation, wX, wY, wZ, size, cnt, res);
	print_XYZ(get_skewness, wX, wY, wZ, size, cnt, res);
	print_XYZ(get_kurtosis, wX, wY, wZ, size, cnt, res);
	print_XYZ(get_zcr, wX, wY, wZ, size, cnt, res);
	print_XYZ(get_rms_amplitude, wX, wY, wZ, size, cnt, res);
	print_XYZ(get_lowest_value, wX, wY, wZ, size, cnt, res);
	print_XYZ(get_highest_value, wX, wY, wZ, size, cnt, res);

	//printf("%.2lf ", get_result(wX, wY, wZ, size));
	res[cnt] = get_result(wX, wY, wZ, size);

}

static jfloatArray get_matrix(JNIEnv *env, jobject obj, jint size,
		jfloatArray ax, jfloatArray ay, jfloatArray az, jfloatArray wx,
		jfloatArray wy, jfloatArray wz, jfloatArray gx, jfloatArray gy,
		jfloatArray gz) {
	float *fax = env->GetFloatArrayElements(ax, NULL);
//	LOGE("%.2f",fax[0]);
//	LOGE("%.2f",fax[size]);
	float *fay = env->GetFloatArrayElements(ay, NULL);
	float *faz = env->GetFloatArrayElements(az, NULL);
	float *fwx = env->GetFloatArrayElements(ax, NULL);
	float *fwy = env->GetFloatArrayElements(ay, NULL);
	float *fwz = env->GetFloatArrayElements(az, NULL);
	float *fgx = env->GetFloatArrayElements(ax, NULL);
	float *fgy = env->GetFloatArrayElements(ay, NULL);
	float *fgz = env->GetFloatArrayElements(az, NULL);
	float res[62];
	output(size, fax, fay, faz, fwx, fwy, fwz, fgx, fgy, fgz, res);

	jfloatArray ret = env->NewFloatArray(62);
	env->SetFloatArrayRegion(ret, 0, 62, res);

	env->ReleaseFloatArrayElements(ax, fax, 0);
	env->ReleaseFloatArrayElements(ay, fay, 0);
	env->ReleaseFloatArrayElements(az, faz, 0);
	env->ReleaseFloatArrayElements(gx, fgx, 0);
	env->ReleaseFloatArrayElements(gy, fgy, 0);
	env->ReleaseFloatArrayElements(gz, fgz, 0);
	env->ReleaseFloatArrayElements(wx, fwx, 0);
	env->ReleaseFloatArrayElements(wy, fwy, 0);
	env->ReleaseFloatArrayElements(wz, fwz, 0);
	return ret;
}

static jint doClassification(JNIEnv *env, jobject obj, jobjectArray valuesArr,
		jobjectArray indicesArr, jint isProb, jstring modelFiles,
		jintArray labelsArr, jdoubleArray probsArr) {

	jboolean isCopy;
	const char *modelFile = env->GetStringUTFChars(modelFiles, &isCopy);
	int *labels = env->GetIntArrayElements(labelsArr, NULL);
	double *probs = env->GetDoubleArrayElements(probsArr, NULL);

	int rowNum = env->GetArrayLength(valuesArr);
	jfloatArray dim = (jfloatArray) env->GetObjectArrayElement(valuesArr, 0);
	int colNum = env->GetArrayLength(dim);
	float **values = (float **) calloc(rowNum, sizeof(float *));
	int **indices = (int **) calloc(rowNum, sizeof(int *));
	for (int i = 0; i < rowNum; i++) {
		jfloatArray vrows = (jfloatArray) env->GetObjectArrayElement(valuesArr,
				i);
		jintArray irows = (jintArray) env->GetObjectArrayElement(indicesArr, i);
		jfloat *velement = env->GetFloatArrayElements(vrows, NULL);
		jint *ielement = env->GetIntArrayElements(irows, NULL);
		values[i] = (float *) calloc(colNum, sizeof(float));
		indices[i] = (int *) calloc(colNum, sizeof(int));
		for (int j = 0; j < colNum; j++) {
			values[i][j] = velement[j];
			indices[i][j] = ielement[j];
		}

		env->ReleaseFloatArrayElements(vrows, velement, JNI_ABORT);
		env->ReleaseIntArrayElements(irows, ielement, JNI_ABORT);
	}


	int r = predict(values, indices, rowNum, colNum, isProb, modelFile, labels,
			probs);


	for (int i = 0; i < rowNum; i++) {
		free(values[i]);
		free(indices[i]);
	}
	env->ReleaseIntArrayElements(labelsArr, labels, 0);
	env->ReleaseDoubleArrayElements(probsArr, probs, 0);
	env->ReleaseStringUTFChars(modelFiles, modelFile);
	return r;
}

static JNINativeMethod sMethods[] = {
/* name, signature, funcPtr */
{ "doClassificationNative", "([[F[[IILjava/lang/String;[I[D)I",
		(void*) doClassification }, { "getMatrixNative",
		"(I[F[F[F[F[F[F[F[F[F)[F", (void*) get_matrix },

};

static int jniRegisterNativeMethods(JNIEnv *env, const char *className,
		JNINativeMethod* Methods, int numMethods) {
	jclass clazz = env->FindClass(className);
	if (clazz == NULL) {
		LOGE("Native registration unable to find class '%s'", className);
		return JNI_FALSE;
	}

	if (env->RegisterNatives(clazz, Methods, numMethods) < 0) {
		LOGE("RegisterNatives failed for '%s'", className);
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

int register_Signal(JNIEnv *env) {
	return jniRegisterNativeMethods(env, "com/sensordemo/JniUtils",
			sMethods, sizeof(sMethods) / sizeof(sMethods[0]));
}

}
