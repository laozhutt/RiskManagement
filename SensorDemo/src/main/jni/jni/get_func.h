#include <cstdlib>
#include <cstring>
#include <cstdio>
#include <cmath>

#include "../src/xtract/scalar.h"

using namespace std;
#define ff(a,b) for (int i = a ; i < b ; i++)
#define vec_pb(v,x) v.push_back(x)
#define data_len 10
#define data_cross 5


float get_mean(float v[],int size){
	float res;
	xtract_mean(v,size,NULL,&res);
	return res;
}

float get_variance(float v[],int size){
	float res,mean;
	mean = get_mean(v,size);
	xtract_variance(v,size,&mean,&res);
	return res;
}


float get_average_deviation(float v[],int size){
	float mean,res;
	xtract_mean(v,size,NULL,&mean);
	xtract_average_deviation(v,size,&mean,&res);
	return res;
}

float get_standard_deviation(float v[],int size){
	float mean ,var , res;
	var = get_variance(v,size);
	xtract_standard_deviation(v,size,&var,&res);
	return res;
}


float get_result(float X[],float Y[], float Z[],int size){
	float sum = 0;
	ff(0 , size){
		sum += sqrt(X[i]*X[i] + Y[i]*Y[i] + Z[i]*Z[i]);
	}
	sum /= size;
	return sum;
}

float get_skewness(float v[],int size){
	float arg[2] , res;
	arg[0] = get_mean(v,size);
	arg[1] = get_standard_deviation(v,size);
	xtract_skewness(v,size,arg,&res);
	return res;
}

float get_kurtosis(float v[],int size){
	float res , arg[2];
	arg[0] = get_mean(v,size);
	arg[1] = get_standard_deviation(v,size);
	xtract_kurtosis(v,size,arg,&res);
	return res;
}

float get_spectral_mean(float v[],int size){
	float res;
	xtract_spectral_mean(v,size,NULL,&res);
	return res;
}


float get_spectral_variance(float v[],int size){
	float arg, res;
	arg = get_spectral_variance(v,size);
	xtract_spectral_variance(v,size,&arg,&res);
	return res;
}

float get_spectral_skewness(float v[],int size){
	float arg[2],res;
	arg[0] = get_spectral_mean(v,size);
	arg[1] = get_spectral_variance(v,size);
	xtract_spectral_skewness(v,size,&arg[0],&res);
	return res;
}


float get_spectral_kurtosis(float v[],int size){
	float arg[2], res;
	arg[0] = get_spectral_mean(v,size);
	arg[1] = get_spectral_variance(v,size);
	xtract_spectral_kurtosis(v,size,&arg[0],&res);
	return res;
}

float get_spectral_centroid(float v[],int size){
	float arg[2] , res;
	xtract_spectral_centroid(v,size,NULL,&res);
	return res;
}

float get_irregularity_j(float v[],int size){
	float arg[2], res;
	xtract_irregularity_j(v,size,NULL,&res);
	return res;
}

float get_spectral_standard_deviation(float v[],int size){
	float arg[2],res;
	arg[0] = get_spectral_variance(v,size);
	xtract_standard_deviation(v,size,&arg[0],&res);
	return res;
}

float get_spread(float v[],int size){
	float arg[2],res;
	arg[0] = get_spectral_centroid(v,size);
	xtract_spread(v,size,&arg[0],&res);
	return res;
}

float get_zcr(float v[],int size){
	float arg[2],res;
	xtract_zcr(v,size,NULL,&res);
	return res;
}

float get_loudness (float v[],int size){
	float arg[2],res;
	xtract_loudness(v,size,NULL,&res);
	return res;
}

float get_flatness(float v[],int size){
	float arg[2],res;
	xtract_flatness(v,size,NULL,&res);
	return res;
}

float get_rms_amplitude(float v[],int size){
	float res;
	xtract_rms_amplitude(v,size,NULL,&res);
	return res;
}


float get_lowest_value (float v[],int size){
	float res = v[0];
	for (int i = 1 ; i<=10 ; i++) res = (res<v[i])?res:v[i];
	return res;
}

float get_highest_value(float v[],int size){
	float res;
	xtract_highest_value(v,size,NULL,&res);
	return res;
}

float get_crest(float v[],int size){
	float res,arg[2];
	arg[0] = get_highest_value(v,size);
	arg[1] = get_mean(v,size);
	xtract_crest(v,size,NULL,&res);
	return res;
}

float get_power(float v[],int size){
	float res;
	xtract_power(v,size,NULL,&res);
	return res;
}

float get_sharpness(float v[],int size){
	float res;
	xtract_sharpness(v,size,NULL,&res);
	return res;
}
