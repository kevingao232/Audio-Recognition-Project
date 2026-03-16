import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

public class MusicFile {

	
	private double duration;
	private String name;
	private byte[]FFTData;
	
	
	
	public MusicFile()
	{
		
		name=null;
		FFTData=null;
	}
	public MusicFile(String n, byte[]d) throws UnsupportedAudioFileException, IOException
	{
		name=n;
		FFTData=d;
		//duration=calcDuration(f);
	}
	public MusicFile(String n) throws UnsupportedAudioFileException, IOException
	{
		name=n;
		
		//duration=calcDuration(f);
	}
	
	public void setFFTData(byte [] f)
	{
		FFTData=f;
	}
	
	public double getDuration()
	{
		return duration;
	}
	public String getName()
	{
		return name;
	}
	public byte[] getFFTData()
	{
		return FFTData;
	}
	
	


public int calcDuration(File file) throws UnsupportedAudioFileException, IOException {
	
    
    
	int duration = 0;

	
	
	try {
	  AudioFile audioFile = AudioFileIO.read(file);
	  duration = audioFile.getAudioHeader().getTrackLength();
	  
	} catch (Exception e) {
	  e.printStackTrace();

	}
    
	return duration;
        
        
    } 

}

