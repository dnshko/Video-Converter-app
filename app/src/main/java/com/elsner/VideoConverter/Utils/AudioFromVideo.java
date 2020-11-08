package com.elsner.VideoConverter.Utils;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioFromVideo {

    private String audio, video;
    private MediaCodec amc;
    private MediaExtractor ame;
    private MediaFormat amf;
    private String amime;

    public AudioFromVideo(String srcVideo, String destAudio) {
        this.audio = destAudio;
        this.video = srcVideo;
        ame = new MediaExtractor();
        init();
    }

    public void init() {
        try {
            ame.setDataSource(video);
            amf = ame.getTrackFormat(1);
            ame.selectTrack(1);
            amime = amf.getString(MediaFormat.KEY_MIME);
            amc = MediaCodec.createDecoderByType(amime);
            amc.configure(amf, null, null, 0);
            amc.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        new AudioService(amc, ame, audio).start();
    }

    private File rawToWave(final File rawFile, final String filePath) throws IOException {

        File waveFile = new File(filePath);

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAV"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 16000); // sample rate
            writeInt(output, 64); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
            }
        }

        return waveFile;

    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    private class AudioService extends Thread {
        private MediaCodec amc;
        private MediaExtractor ame;
        private ByteBuffer[] aInputBuffers, aOutputBuffers;
        private String destFile;

        AudioService(MediaCodec amc, MediaExtractor ame, String destFile) {
            this.amc = amc;
            this.ame = ame;
            this.destFile = destFile;
            aInputBuffers = amc.getInputBuffers();
            aOutputBuffers = amc.getOutputBuffers();
        }

        public void run() {
            try {
                String strMp3 = Constants.GIF_FOLDER_PATH + String.format("/%s.pcm", "mymp3");
                File file = new File(strMp3);
                OutputStream os = new FileOutputStream(file);
                long count = 0;
                while (true) {
                    int inputIndex = amc.dequeueInputBuffer(0);
                    if (inputIndex == -1) {
                        continue;
                    }
                    int sampleSize = ame.readSampleData(aInputBuffers[inputIndex], 0);
                    if (sampleSize == -1) break;
                    long presentationTime = ame.getSampleTime();
                    int flag = ame.getSampleFlags();
                    ame.advance();
                    amc.queueInputBuffer(inputIndex, 0, sampleSize, presentationTime, flag);
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    int outputIndex = amc.dequeueOutputBuffer(info, 0);
                    if (outputIndex >= 0) {
                        byte[] data = new byte[info.size];
                        aOutputBuffers[outputIndex].get(data, 0, data.length);
                        aOutputBuffers[outputIndex].clear();
                        os.write(data);
                        count += data.length;
                        Log.e("write", "" + count);
                        amc.releaseOutputBuffer(outputIndex, false);
                    } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        aOutputBuffers = amc.getOutputBuffers();
                    } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    }
                }
                os.flush();
                os.close();

//                encode(rawToWave(new File(video),destFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}