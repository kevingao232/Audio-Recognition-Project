import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import static java.lang.System.*;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.ArrayUtils;
import org.omg.CORBA.Environment;
import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import java.io.*;
import com.sun.javafx.collections.MappingChange.Map;
import com.sun.media.sound.WaveFileWriter;

import javax.sound.sampled.AudioFormat;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.Obuffer;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.Player;
import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static java.lang.Math.pow;

import java.io.*;
import sun.audio.*;
import sun.rmi.runtime.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



import java.io.*;
import java.util.Arrays;

import javax.sound.sampled.*;

public class LineOpen {
	
	private byte[] array;
	//private byte[] bytesArray=new byte[10];
	private MusicFile file=new MusicFile();
	private double[] array2;
	
	public LineOpen() throws IOException, LineUnavailableException, UnsupportedAudioFileException, DecoderException, BitstreamException
	{		
		
		final AudioFormat format = getFormat(); //Fill AudioFormat with the wanted settings
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
		line.open(format);
		line.start();
	
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();

		byte[] buffer=new byte[44100];
		out.println(line.getBufferSize());
		
		 
		   
		 boolean timer=true;
		//When the game ends:
		long tStart = (long) (System.currentTimeMillis()/1000.0);
		long tEnd=tStart;
		int count=0;
		int total=0;
        while (timer)
        {
        	
        	tEnd = (long)(System.currentTimeMillis()/1000);

        	if( tEnd - tStart>12)
        	{
			break;
        	}
        	
        	 count = line.read(buffer, 0, buffer.length);
        
        	 total+=count;
		        if (count > 0) {
		            out2.write(buffer, 0, count);
		        }
		        if(total>=882000)
		        	break;
		        
	        
        	}
        out2.close();
        array=out2.toByteArray();
   
      
        array2 = doubleMe(shortMe(array));
        
      out.println(array2.length);
	
       
	
        
	}
	
	
	
	
	
	public double[] getArray()
	{
		return array2;
	}

	public AudioFormat getFormat() {
		
	    float sampleRate = 44100;
	    int sampleSizeInBits = 16;
	    int channels = 1;          //mono
	    boolean signed = true;     //Indicates whether the data is signed or unsigned
	    boolean bigEndian = true;  //Indicates whether the audio data is stored in big-endian or little-endian order
	    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);   
	}
	public static double[] doubleMe(short[] pcms) {
	    double[] floaters = new double[pcms.length];
	    for (int i = 0; i < pcms.length; i++) {
	        floaters[i] = (double)pcms[i];
	    }
	    return floaters;
	}
	
	public static short[] shortMe(byte[] bytes) {
	    short[] out = new short[bytes.length / 2]; // will drop last byte if odd number
	    ByteBuffer bb = ByteBuffer.wrap(bytes);
	    for (int i = 0; i < out.length; i++) {
	        out[i] = bb.getShort();
	    }
	    return out;
	}
	
	public double [] concat (double[] a, double[] b) {
	    int aLen = a.length;
	    int bLen = b.length;

	    @SuppressWarnings("unchecked")
	    double[] c = (double[]) Array.newInstance(a.getClass().getComponentType(), aLen+bLen);
	    System.arraycopy(a, 0, c, 0, aLen);
	    System.arraycopy(b, 0, c, aLen, bLen);

	    return c;
	}
	/*private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
	{
	  SourceDataLine res = null;
	  DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	  res = (SourceDataLine) AudioSystem.getLine(info);
	  res.open(audioFormat);
	  return res;
	}
	/*public int[][] getUnscaledAmplitude(byte[] eightBitByteArray, int nbChannels)
	{
	    int[][] toReturn = new int[nbChannels][eightBitByteArray.length / (2 * nbChannels)];
	    int index = 0;

	    for (int audioByte = 0; audioByte < eightBitByteArray.length;)
	    {
	        for (int channel = 0; channel < nbChannels; channel++)
	        {
	            // Do the byte to sample conversion.
	            int low = (int) eightBitByteArray[audioByte];
	            audioByte++;
	            int high = (int) eightBitByteArray[audioByte];
	            audioByte++;
	            int sample = (high << 8) + (low & 0x00ff);

	            toReturn[channel][index] = sample;
	        }
	        index++;
	    }

	    return toReturn;
	}*/
	/*private void setArray(File filename) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
		
		
		AudioInputStream in= AudioSystem.getAudioInputStream(filename);
		AudioInputStream din = null;
		AudioFormat baseFormat = in.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
		                                            baseFormat.getSampleRate(),
		                                            16,
		                                            baseFormat.getChannels(),
		                                            baseFormat.getChannels() * 2,
		                                            baseFormat.getSampleRate(),
		                                            false);
		din = AudioSystem.getAudioInputStream(decodedFormat, in);
	ByteArrayOutputStream outStream=new ByteArrayOutputStream();
		WaveFileWriter writer = new WaveFileWriter();
		System.out.println(writer.write(in, AudioFileFormat.Type.WAVE, outStream));
        

	       din.read(bytesArray); 
	       for(int i=0;i<10;i++)
	       {
	    	   System.out.println(bytesArray[i]);
	       }

	                }
	
    public void captureAudio(File file) throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
    	
    	AudioInputStream in= AudioSystem.getAudioInputStream(file);
    	AudioInputStream din = null;
    	AudioFormat baseFormat = in.getFormat();
    	AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
    	                                            baseFormat.getSampleRate(),
    	                                            16,
    	                                            baseFormat.getChannels(),
    	                                            baseFormat.getChannels() * 2,
    	                                            baseFormat.getSampleRate(),
    	                                            false);
    	din = AudioSystem.getAudioInputStream(decodedFormat, in);
    	din.read(bytesArray);
    	
    	
        
        @SuppressWarnings("resource")
		PrintWriter writer = new PrintWriter("DataBase.txt");
        writer.println(file.getName());
        for (int i=0; i<bytesArray.length;i++)
        {
        	writer.print(bytesArray[i]+" ");
        	
        	if((i+1)%40==0)
        	{
        		writer.print("\n");
        	}
        }
    }

   
	public void harvest(File rootDirectory) throws UnsupportedAudioFileException, IOException, LineUnavailableException
	{
		String [] itemsInDirectory =  rootDirectory.list();
		
		for(String itemInDirectory : itemsInDirectory)
		{
			if(itemInDirectory.endsWith(".wav")){
			
				File mp3File = new File("/Users/Kevin/Documents/workspace/MP4 to Binary/DataFolder/"+itemInDirectory);
				System.out.println(mp3File.getName());
				captureAudio(mp3File);
			}
			else if (new File(itemInDirectory+".wav").isDirectory()){
				harvest(new File(itemInDirectory+".wav"));
			}
		}
	}*/


	
	
}   

	
	


