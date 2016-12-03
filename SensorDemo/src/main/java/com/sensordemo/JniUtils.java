package com.sensordemo;

import android.os.Environment;
import android.util.Log;

/**
 * Created by ztt on 2016/11/23.
 */
public class JniUtils {


    private static final String TAG = "Libsvm";

    public native int trainClassifierNative(String trainingFile,
                                             int kernelType, int cost, float gamma, int isProb, String modelFile);

    public native float[] getMatrixNative(int size, float[] ax, float[] ay,
                                           float[] az, float[] wx, float[] wy, float[] wz, float[] gx, float[] gy,
                                           float[] gz);

    public native int doClassificationNative(float values[][],
                                              int indices[][], int isProb, String modelFile, int labels[],
                                              double probs[]);

    private native int testCallocNative();

    static {
        System.loadLibrary("signal");
    }

    void test_native() {
        float[] ax = new float[10];
        for (int i = 0 ; i < 10 ; i++) ax[i] = i;
        float[]ret = getMatrixNative(ax.length, ax, ax,ax,ax,ax,ax,ax,ax,ax);
        for (int i = 0 ; i< ret.length;i++){
            Log.e("Return " + i, String.valueOf(ret[i]));
        }
    }

    private void train() {
        // Svm training
        int kernelType = 2; // Radial basis function
        int cost = 4; // Cost
        int isProb = 0;
        float gamma = 0.25f; // Gamma
        String trainingFileLoc = Environment.getExternalStorageDirectory()
                + "/Download/training_set";
        String modelFileLoc = Environment.getExternalStorageDirectory()
                + "/model";
        if (trainClassifierNative(trainingFileLoc, kernelType, cost, gamma,
                isProb, modelFileLoc) == -1) {
            Log.d(TAG, "training err");
//            finish();
        }
//        Toast.makeText(this, "Training is done", 2000).show();
    }



    /**
     * classify generate labels for features. Return: -1: Error 0: Correct
     */
    public int callSVM(float values[][], int indices[][], int groundTruth[],
                       int isProb, String modelFile, int labels[], double probs[]) {

        // SVM type
        final int C_SVC = 0;
        final int NU_SVC = 1;
        final int ONE_CLASS_SVM = 2;
        final int EPSILON_SVR = 3;
        final int NU_SVR = 4;

        // For accuracy calculation
        int correct = 0;
        int total = 0;
        float error = 0;
        float sump = 0, sumt = 0, sumpp = 0, sumtt = 0, sumpt = 0;
        float MSE, SCC, accuracy;

        int num = values.length;
        int svm_type = C_SVC;
        if (num != indices.length)
            return -1;
        // If isProb is true, you need to pass in a real double array for
        // probability array
        int r = doClassificationNative(values, indices, isProb, modelFile,
                labels, probs);

        // Calculate accuracy
        if (groundTruth != null) {
            if (groundTruth.length != indices.length) {
                return -1;
            }
            for (int i = 0; i < num; i++) {
                int predict_label = labels[i];
                int target_label = groundTruth[i];
                if (predict_label == target_label)
                    ++correct;
                error += (predict_label - target_label)
                        * (predict_label - target_label);
                sump += predict_label;
                sumt += target_label;
                sumpp += predict_label * predict_label;
                sumtt += target_label * target_label;
                sumpt += predict_label * target_label;
                ++total;
            }

            if (svm_type == NU_SVR || svm_type == EPSILON_SVR) {
                MSE = error / total; // Mean square error
                SCC = ((total * sumpt - sump * sumt) * (total * sumpt - sump
                        * sumt))
                        / ((total * sumpp - sump * sump) * (total * sumtt - sumt
                        * sumt)); // Squared correlation coefficient
            }
            accuracy = (float) correct / total * 100;
            Log.d(TAG, "Classification accuracy is " + accuracy);
        }

        return r;
    }

    public void classify() {
        // Svm classification
        float[] value = { -0.68f, 5.35f, 8.58f, 0.01f, 0.0f, 0.1f, 0.1f, 0.05f,
                0.31f, 0.07f, 0.04f, 0.22f, -0.82f, 1.0f, 0.43f, -0.16f,
                -0.05f, -0.14f, 0.4f, 0.4f, 0.4f, 0.69f, 5.35f, 8.59f, -0.91f,
                0.0f, 0.0f, -0.55f, 5.47f, 9.24f, 10.14f, 0.03f, 0.02f, -0.03f,
                0.0f, 0.0f, 0.0f, 0.07f, 0.02f, 0.02f, 0.06f, 0.02f, 0.02f,
                0.26f, 0.15f, 0.31f, -1.57f, -0.99f, -1.76f, 0.7f, 0.5f, 0.4f,
                0.07f, 0.03f, 0.03f, -0.07f, -0.02f, -0.05f, 0.12f, 0.06f,
                -0.0f, 0.07f };
        int[] index = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
                33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
                49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62 };

        float[][] values = new float[28][value.length];
        int[][] indices = new int[28][index.length];
        for (int i = 0; i < 28; i++) {
            values[i] = value;
            indices[i] = index;
        }

        int[] groundTruth = null;
        int[] labels = new int[28];
        labels[0] = -3;
        double[] probs = new double[28];
        int isProb = 0; // Not probability prediction
        String modelFileLoc = Environment.getExternalStorageDirectory()
                + "/train.scale.model";

        if (callSVM(values, indices, groundTruth, isProb, modelFileLoc, labels,
                probs) != 0) {
            Log.d(TAG, "Classification is incorrect");
        } else {
            String m = "";
            for (int l : labels)
                m += l + ", ";
//            Toast.makeText(this, "Classification is done, the result is " + m, 2000).show();
        }
    }
}
