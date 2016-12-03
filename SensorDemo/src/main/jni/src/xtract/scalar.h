/*
 * Copyright (C) 2012 Jamie Bullock
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 */

/* scalar.c: defines functions that extract a feature as a single value from an input vector */

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <math.h>
#include <limits.h>
#include <stdint.h>

#ifndef DBL_MAX
#include <float.h> /* on Linux DBL_MAX is in float.h */
#endif


enum xtract_return_codes_ {
    XTRACT_SUCCESS,
    XTRACT_MALLOC_FAILED,
    XTRACT_BAD_ARGV,
    XTRACT_BAD_VECTOR_SIZE,
    XTRACT_BAD_STATE,
    XTRACT_DENORMAL_FOUND,
    XTRACT_NO_RESULT, /* This usually occurs when the correct calculation cannot take place because required data is missing or would result in a NaN or infinity/-infinity. Under these curcumstances 0.f is usually given by *result */
    XTRACT_FEATURE_NOT_IMPLEMENTED,
    XTRACT_ARGUMENT_ERROR
};

#define XTRACT_BARK_BANDS 26

#define XTRACT_SQ(a) ((a) * (a))
#define XTRACT_MAX(a, b) ((a) > (b) ? (a) : (b))


int xtract_mean(const float *data, const int N, const void *argv, float *result)
{


    int n = N;

    *result = 0.0;

    while(n--)
        *result += data[n];

    *result /= N;

    return XTRACT_SUCCESS;
}

int xtract_variance(const float *data, const int N, const void *argv, float *result)
{

    int n = N;

    *result = 0.0;

    while(n--)
        *result += pow(data[n] - *(float *)argv, 2);

    *result = *result / (N - 1);

    return XTRACT_SUCCESS;
}

int xtract_average_deviation(const float *data, const int N, const void *argv, float *result)
{

    int n = N;

    *result = 0.0;

    while(n--)
        *result += fabs(data[n] - *(float *)argv);

    *result /= N;

    return XTRACT_SUCCESS;
}

int xtract_standard_deviation(const float *data, const int N, const void *argv, float *result)
{

    *result = sqrt(*(float *)argv);

    return XTRACT_SUCCESS;
}

int xtract_skewness(const float *data, const int N, const void *argv,  float *result)
{

    int n = N;

    float temp = 0.0;

    *result = 0.0;

    while(n--)
    {
        temp = (data[n] - ((float *)argv)[0]) / ((float *)argv)[1];
        *result += pow(temp, 3);
    }

    *result /= N;


    return XTRACT_SUCCESS;
}

int xtract_kurtosis(const float *data, const int N, const void *argv,  float *result)
{

    int n = N;

    float temp = 0.0;

    *result = 0.0;

    while(n--)
    {
        temp = (data[n] - ((float *)argv)[0]) / ((float *)argv)[1];
        *result += pow(temp, 4);
    }

    *result /= N;
    *result -= 3.0;

    return XTRACT_SUCCESS;
}


int xtract_spectral_centroid(const float *data, const int N, const void *argv,  float *result)
{

    int n = (N >> 1);

    const float *freqs, *amps;
    float FA = 0.0, A = 0.0;

    amps = data;
    freqs = data + n;

    while(n--)
    {
        FA += freqs[n] * amps[n];
        A += amps[n];
    }

    if(A == 0.0)
        *result = 0.0;
    else
        *result = FA / A;

    return XTRACT_SUCCESS;
}

int xtract_spectral_mean(const float *data, const int N, const void *argv, float *result)
{

    return xtract_spectral_centroid(data, N, argv, result);

}

int xtract_spectral_variance(const float *data, const int N, const void *argv, float *result)
{

    int m;
    float A = 0.0;
    const float *freqs, *amps;

    m = N >> 1;

    amps = data;
    freqs = data + m;

    *result = 0.0;

    while(m--)
    {
        A += amps[m];
        *result += pow(freqs[m] - ((float *)argv)[0], 2) * amps[m];
    }

    *result = *result / A;

    return XTRACT_SUCCESS;
}

int xtract_spectral_skewness(const float *data, const int N, const void *argv,  float *result)
{

    int m;
    const float *freqs, *amps;

    m = N >> 1;

    amps = data;
    freqs = data + m;

    *result = 0.0;

    while(m--)
        *result += pow(freqs[m] - ((float *)argv)[0], 3) * amps[m];

    *result /= pow(((float *)argv)[1], 3);

    return XTRACT_SUCCESS;
}

int xtract_spectral_kurtosis(const float *data, const int N, const void *argv,  float *result)
{

    int m;
    const float *freqs, *amps;

    m = N >> 1;

    amps = data;
    freqs = data + m;

    *result = 0.0;

    while(m--)
        *result += pow(freqs[m] - ((float *)argv)[0], 4) * amps[m];

    *result /= pow(((float *)argv)[1], 4);
    *result -= 3.0;

    return XTRACT_SUCCESS;
}

int xtract_irregularity_j(const float *data, const int N, const void *argv, float *result)
{

    int n = N - 1;

    float num = 0.0, den = 0.0;

    while(n--)
    {
        num += pow(data[n] - data[n+1], 2);
        den += pow(data[n], 2);
    }

    *result = (float)(num / den);

    return XTRACT_SUCCESS;
}

int xtract_spread(const float *data, const int N, const void *argv, float *result)
{

    return xtract_spectral_variance(data, N, argv, result);
}

int xtract_zcr(const float *data, const int N, const void *argv, float *result)
{

    int n = N;

    for(n = 1; n < N; n++)
        if(data[n] * data[n-1] < 0) (*result)++;

    *result /= (float)N;

    return XTRACT_SUCCESS;
}

int xtract_loudness(const float *data, const int N, const void *argv, float *result)
{

    int n = N, rv;

    *result = 0.0;

    if(n > XTRACT_BARK_BANDS)
    {
        n = XTRACT_BARK_BANDS;
        rv = XTRACT_BAD_VECTOR_SIZE;
    }
    else
        rv = XTRACT_SUCCESS;

    while(n--)
        *result += pow(data[n], 0.23);

    return rv;
}

#define INDEX 0
int xtract_is_denormal(float const d)
{
    int l = ((int *)&d)[INDEX];
    return (l&0x7ff00000) == 0 && d!=0; //Check for 0 may not be necessary
}


int xtract_flatness(const float *data, const int N, const void *argv, float *result)
{

    int n, count, denormal_found;

    float num, den, temp;

    num = 1.0;
    den = temp = 0.0;

    denormal_found = 0;
    count = 0;

    for(n = 0; n < N; n++)
    {
        if((temp = data[n]) != 0.0)
        {
            if (xtract_is_denormal(num))
            {
                denormal_found = 1;
                break;
            }
            num *= temp;
            den += temp;
            count++;
        }
    }

    if(!count)
    {
        *result = 0.0;
        return XTRACT_NO_RESULT;
    }

    num = pow(num, 1.0 / (float)N);
    den /= (float)N;


    *result = (float) (num / den);

    if(denormal_found)
        return XTRACT_DENORMAL_FOUND;
    else
        return XTRACT_SUCCESS;

}

int xtract_rms_amplitude(const float *data, const int N, const void *argv, float *result)
{

    int n = N;

    *result = 0.0;

    while(n--) *result += XTRACT_SQ(data[n]);

    *result = sqrt(*result / (float)N);

    return XTRACT_SUCCESS;
}

int xtract_highest_value(const float *data, const int N, const void *argv, float *result)
{

    int n = N;

    *result = data[--n];

    while(n--)
        *result = XTRACT_MAX(*result, data[n]);

    return XTRACT_SUCCESS;
}

int xtract_crest(const float *data, const int N, const void *argv, float *result)
{

    float max, mean;

    max = mean = 0.0;

    max = *(float *)argv;
    mean = *((float *)argv+1);

    *result = max / mean;

    return XTRACT_SUCCESS;

}

int xtract_power(const float *data, const int N, const void *argv, float *result)
{

    return XTRACT_FEATURE_NOT_IMPLEMENTED;

}

int xtract_sharpness(const float *data, const int N, const void *argv, float *result)
{

    int n = N, rv;
    float sl, g; /* sl = specific loudness */
    float temp;

    sl = g = 0.0;
    temp = 0.0;

    if(n > XTRACT_BARK_BANDS)
        rv = XTRACT_BAD_VECTOR_SIZE;
    else
        rv = XTRACT_SUCCESS;


    while(n--)
    {
        sl = pow(data[n], 0.23);
        g = (n < 15 ? 1.0 : 0.066 * exp(0.171 * n));
        temp += n * g * sl;
    }

    temp = 0.11 * temp / (float)N;
    *result = (float)temp;

    return rv;

}
