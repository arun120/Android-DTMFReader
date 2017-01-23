package com.b2b.home.axisserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import audio.AudioFileException;
import dtmfdecoder.DTMFDecoderException;
import dtmfdecoder.DTMFUtil;

public class DTMFRecorder extends Service {

    private static final int RECORDER_SAMPLERATE = 44100;
    private static String callNumber;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    File f1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Axis/call.pcm"); // The location of your PCM file

    public DTMFRecorder() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        callNumber=intent.getExtras().get("number").toString();
        return super.onStartCommand(intent, flags, startId);
    }

    private void postRecording(){

        File f2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Axis/call.wav"); // The location where you want your WAV file
        if(f2.exists())
            f2.delete();
        try {
            f2.createNewFile();

            rawToWave(f1, f2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("File ","Converted");


        DTMFUtil dtmf= null;
        String sequence="";
        try {
            dtmf = new DTMFUtil(f2);
            dtmf.decode();
           sequence = dtmf.getDecoded()[0];
            Log.i("Input Sequence from "+callNumber,sequence);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Transaction t=Decoder.decode(sequence);
        t.setNumber(callNumber);
        String ackNumber=sequence.split("\\*")[3];
        new Pay().execute(t);
        this.stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Service","DTMF Record");

        File f=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Axis/");
        f.mkdir();

       if(f1.exists())
            f1.delete();

        startRecording();

        //stopRecording();
       // Log.i("Recording","Stopped");



/*
         AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);
        MediaRecorder recorder=new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(1);
        recorder.setAudioEncodingBitRate(8000);

        File f=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Axis/");
        try {

            f.mkdir();
            f=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Axis/call.mp3");
            if(f.exists())
                f.delete();
            Log.i("Location",Environment.getExternalStorageDirectory().getAbsolutePath()+"/Axis/call.mp3");
            f.createNewFile();

            Log.i("Recording","File Created");
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Axis/call.mp3");

        try {
            recorder.prepare();
            recorder.start();
            Log.i("Recording","Started");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        recorder.stop();
        recorder.reset();
        recorder.release();

        Log.i("Recording","Stopped");
    audioManager.setSpeakerphoneOn(false);*/
    }
    private void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_DOWNLINK,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {

                writeAudioDataToFile();

            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Axis/call.pcm";
        short sData[] = new short[BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, BufferElements2Rec);
            System.out.println("Short wirting to file" + sData.toString());
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte bData[] = short2byte(sData);
                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(!HookStateReceiver.CallState)
                stopRecording();
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    postRecording();

    }


    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

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
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 44100); // sample rate
            writeInt(output, RECORDER_SAMPLERATE * 2); // byte rate
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

            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }  catch (IOException e){
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
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



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return null;
    }
}
