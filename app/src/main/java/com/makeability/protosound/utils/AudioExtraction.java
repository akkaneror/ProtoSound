package com.makeability.protosound.utils;

import java.io.File;
import java.util.Arrays;

public class AudioExtraction {


    private static final String TAG = "AudioExtraction";

    public static float[][] getMelSpectrogramDb(String filePath) throws Exception {
        WavFile readWavFile = WavFile.openWavFile(new File(filePath));
        final int BUF_SIZE = (int) readWavFile.getNumFrames();
        float[] wav = new float[BUF_SIZE];
        int framesRead;
        do {
            framesRead = readWavFile.readFrames(wav, BUF_SIZE);

        }
        while (framesRead != 0);

        readWavFile.close();

        if (wav.length < 44100) {
            wav = pad_reflect(wav, (int) Math.ceil((44100 - wav.length) / 2.0));
        } else {
            wav = Arrays.copyOfRange(wav, 0, 44100);
        }

        AudioFeatureExtraction mfccConvert = new AudioFeatureExtraction();
        mfccConvert.setSampleRate(44100);
        mfccConvert.setN_fft(2048);
        mfccConvert.setN_mels(128);
        mfccConvert.setHop_length(512);
        mfccConvert.setfMin(20);
        mfccConvert.setfMax(8300);
        float[][] spec = mfccConvert.melSpectrogramWithComplexValueProcessing(wav);
        spec = mfccConvert.powerToDb(spec);
        return spec;
    }

    static float[] pad_reflect(float[] array, int pad_width) {
        float[] ret = new float[array.length + pad_width * 2];

        if (array.length == 0) {
            throw new IllegalArgumentException("can't extend empty axis 0 using modes other than 'constant' or 'empty'");
        }

        //Exception if only one element exists
        if (array.length == 1) {
            Arrays.fill(ret, array[0]);
            return ret;
        }

        //Left_Pad
        int pos = 0;
        int dis = -1;
        for (int i = 0; i < pad_width; i++) {
            if (pos == array.length - 1 || pos == 0) {
                dis = -dis;
            }
            pos += dis;
            ret[pad_width - i - 1] = array[pos];
        }

        System.arraycopy(array, 0, ret, pad_width, array.length);

        //Right_Pad
        pos = array.length - 1;
        dis = 1;
        for (int i = 0; i < pad_width; i++) {
            if (pos == array.length - 1 || pos == 0) {
                dis = -dis;
            }
            pos += dis;
            ret[pad_width + array.length + i] = array[pos];
        }
        return ret;
    }


    public static float[][] specToImage(float[][] spec) {
        int n = spec.length;
        int m = spec[0].length;
        float avg = 0.0f;
        float variance = 0.0f;
        for (float[] floats : spec)
            for (int j = 0; j < m; j++)
                avg += floats[j];
        avg /= n * m;
        for (float[] floats : spec)
            for (int j = 0; j < m; j++)
                variance += Math.pow((floats[j] - avg), 2);
        variance /= n * m;
        float stddev = (float) Math.sqrt(variance);

        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                spec[i][j] = (float) ((spec[i][j] - avg) / (stddev + 1E-6));

        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        for (float[] floats : spec) {
            for (int j = 0; j < spec[0].length; j++) {
                if (floats[j] > max) {
                    max = floats[j];
                }
                if (floats[j] < min) {
                    min = floats[j];
                }
            }
        }

        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++) {
                spec[i][j] = 255f * (spec[i][j] - min) / (max - min);
            }

        return spec;
    }
}
