import static java.lang.System.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static java.lang.Math.pow;



import java.io.*;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sun.org.apache.xml.internal.serialize.Printer;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;


public class MainRunner {

	public static void main(String[] args) throws IllegalArgumentException, Exception {
		//long tStart = (long) (System.currentTimeMillis()/1000.0);
		//long tEnd=tStart;
		    double duration=441000;
		   
		  @SuppressWarnings("resource")
			DataInputStream stream = new DataInputStream(new FileInputStream("MusicDataBaseSwapPadMaxAmpMaxFreq.bin"));   	    	
	        ArrayList<BigDecimal> record=new ArrayList<BigDecimal>();
	       Correlation corr=new Correlation();
	        PrintWriter writer = new PrintWriter(new FileOutputStream("TripetsData.txt"));
			LineOpen a= new LineOpen();	
		    double[] input = a.getArray();	  
		    for(int i=0;i<input.length;i++)
		    {
		    	record.add(new BigDecimal(input[i]));
		    }
		    
		   
	       //	@SuppressWarnings("resource")
		 	/*Scanner stream2 = new Scanner(new File("test_piece.txt"));  
             
             //boolean running=true;
             
             while(stream2.hasNext()){
            
	        	 record.add(new BigDecimal(Float.parseFloat(stream2.nextLine())));
	        	 //out.println(new BigDecimal(stream2.nextLine()));	
	        
             }
            //out.println(record.size());*/
		
	        int sampleRate=44100;
	        int bot=0;
        	int top=sampleRate*10;
        	
        	 ArrayList <Double> position=new ArrayList<Double>();
		     ArrayList <Double> triplets=new ArrayList<Double>();
		        
        	//while((top+sampleRate)<record.size()){
        		
        	//position=new ArrayList<Double>();
        	//triplets=new ArrayList<Double>();
        		
        	    BigDecimal [] windowFrame=new BigDecimal[top-bot];
        	    
        	    
        	    
        		for(int i=0;i<(top-bot);i++)
        		{
        			windowFrame[i]=record.get(i);
        			
        			
        		}
        	
        		
	        double nextPow = (int) pow(2, ceil(log(windowFrame.length)/log(2)));//pad with 00000000
	        //out.println("Next Power :"+nextPow);
	        duration=nextPow/44100.0;
        	
        	if (windowFrame.length!=nextPow)
	            {
	        	 BigDecimal[] temp = windowFrame;
	        	 windowFrame=new BigDecimal[(int)nextPow];
	        	 for(int i=0;i<nextPow;i++)
	        	 {
	        		 windowFrame[i]=new BigDecimal(0);
	        	 }
	        	 
	        	 for(int i=0; i<temp.length;i++)
	        	 {
	        		 windowFrame[i]=temp[i];
	        		
	        		
	        	 }
	            } 
        	
       
	        	Complex[] cinput = new Complex[(int)nextPow]; //set all as complex with 0 imaginary
		        for (int i = 0; i < windowFrame.length; i++){
		        	
		        	//out.println(windowFrame[i].doubleValue());
		            cinput[i] = new Complex(windowFrame[i].doubleValue(), 0.0);
		        }
		 
		        cinput=FFT.fft(cinput); //compute fft
		        
		        //out.println("cinput Length: "+cinput.length);
		       
		        
		  

		        int len=(int)(duration*(5000-20)+1);
		      //  out.println("len :"+len);
		        
		        double rf1=Math.round(duration*20);
		        
		        double df=(double)(1/duration);
		    
		        double [] d=new double[len];

		        for (int y=0;y<d.length;y++)
		        {
		        	d[y]=cinput[(int) Math.round(y+rf1-1)].abs();
		        	//writer.println(d[y]);
		        	
		        }
		      
		        
		       
		        
		        
		        
		  
		    
		       int peakcount=0;
		       int troughcount=0;
		        
		    
		    
		    	
		        for(int i=1;i<d.length-1;i++)
		        {
		      
		        	double slope=d[i]-d[i-1];
		    
		        	double slope2=d[i+1]-d[i];
		        	
		        	boolean productNeg=(slope2*slope)<0;
		        	
		        	boolean sumNeg=(slope2-slope)<0;
		        	
		   
	
		        	if(productNeg)
		        	{
		            	if(sumNeg){
		            		
		            		for(int x=i-1;x>=1;x--)
		            		{
		            			slope=d[x]-d[x-1];
		            			slope2=d[x+1]-d[x];
		            			
		            			sumNeg=(slope2-slope)<0;
		            			productNeg=(slope2*slope)<0;
		            			
		            			if(!sumNeg&&productNeg)
		            			{
		            				triplets.add(d[x]);

		    		        		position.add( ((x-1)*df)+rf1*df);
		    		        		
		    		        		troughcount++;
		    		        		break;
		            			}
		            		}
		        		
		            	if(troughcount!=0){
		        		triplets.add(d[i]);

		        		position.add( ((i-1)*df)+rf1*df);
		        		peakcount++;
		        		
		        		for(int x=i+1;x<d.length-1;x++)
	            		 {
	            			slope=d[x]-d[x-1];
	            			slope2=d[x+1]-d[x];
	            			
	            			sumNeg=(slope2-slope)<0;
	            			productNeg=(slope2*slope)<0;
	            			
	            			if(!sumNeg&&productNeg)
	            			{
	            				triplets.add(d[x]);

	    		        		position.add( ((x-1)*df)+rf1*df);
	    		        		
	    		        		troughcount++;
	    		        		break;
	            			}
	            		 }
		            	}		        		
		              }
		            	
		        	}
		        	
		        }
		        
		      
		        double max=Double.MIN_VALUE;
			    double mark=0;
			       
			       for(int i=0;i<triplets.size();i++)
			        {
			        	if(triplets.get(i)>max)
			        	{
			        		max=triplets.get(i);
			        		mark=position.get(i);
			        	}
			        }
		     
		     //  out.println("Peakcount and Troughcount: "+peakcount+" "+troughcount);
		    out.println("triplets.size() and position.size(): "+triplets.size()+" "+position.size());
		   out.println("Maximum Amp and Frequency: "+max+" "+mark );
		       
		        for(int i=1;i<triplets.size()-1;i+=3)
		        {
		        	if(triplets.get(i)<max*.3)
		        	{
		        		triplets.set(i,-1.0);
		        		position.set(i,(double) -1);
		        		
		        		triplets.set(i+1,-1.0);
		        		position.set(i+1,(double) -1);
		        		
		        		triplets.set(i-1,-1.0);
		        		position.set(i-1,(double) -1);
		        	}
		        	
		        }
		      
		        for(int i=triplets.size()-1;i>=0;i--)
		        {
		        	if(triplets.get(i)==(double)-1||position.get(i)==(double)-1)
		        	{
		        		triplets.remove(i);
		        		position.remove(i);
		        	}
		        }
		        
		      
        		//triplets.remove(triplets.size()-1);
        		//position.remove(position.size()-1);
        		
        		
		        
		        out.println("triplets.size() and position.size(): "+triplets.size()+" "+position.size());
		        
		        for(int i=0;i<triplets.size();i++)
		        {
		        
		        	writer.println(triplets.get(i)+" "+position.get(i));
		        }
		        
		        
		       // bot=bot+sampleRate;
	        	//top=top+sampleRate;
	        	
		        
        	//}
	       // writer.close();
	        
	        //out.println((double)triplets.size()/3);
		        
	        out.println("ok");
	        
	    double [][] tripletsArrayRecord=new double[2][triplets.size()];
	  
	     for(int i=0;i<2;i++)
	     {
	    	 for(int b=0;b<tripletsArrayRecord[i].length;b++)
	    	 {
	    		 if(i==0)
	    		 {
	    			 tripletsArrayRecord[i][b]=triplets.get(b);
	    		 }
	    		 if(i==1)
	    		 {
	    			 tripletsArrayRecord[i][b]=position.get(b);
	    		 }
	    	 }
	     }
	    
	     
	     
	     /* @SuppressWarnings("resource")
		  Scanner kb=new Scanner(new File("TripletData.txt"));
	
	      
	      int i=0;
	      
	      while(kb.hasNext()){
	    	  String line=kb.nextLine();
	 
	    	  
	    	  if(line.equals("position"))
	    	  {
	   
	    		  i++;
	    		  
	    	
	    	  }
	    	  
	    	  else{
	    		  
	    	    double[] frame=new double[1];
	    	    
	    			 frame[0]=Double.parseDouble(line);
	    			
	    		  tripletsArrayRecord[i]=a.concat(tripletsArrayRecord[i],frame);
	    	  
	    	  }
	      }*/
	      
	      
	     
	    
	    
	      
	        //while(running){
	         
	             int Nmusic_pieces=stream.readInt();
	             ///out.println(Nmusic_pieces);
	             
	             int music_index=stream.readInt();
	           //  out.println(music_index);

	             int Ntime_windows=stream.readInt();
	           //  out.println(Ntime_windows);

	             float window_duration=stream.readFloat();
	           //  out.println(window_duration);

	             float time_overlap=stream.readFloat();
	          //   out.println(time_overlap);

	             int Ntriplets=-1;
	             
	             float maxFreq=-1;
	             float maxAmp=-1;
	             
	             float freq=-1;
	             float amp=-1;
	             float phase=-1;
	          
	             
	            float correlation=-1;
	         
	             Correlation calc=new Correlation();
	             
	             float [] [] check=new float[0][0];
	             
	             for(int t=0;t<Ntime_windows;t++)
	             {
	            	 Ntriplets=stream.readInt();
	            	 
	            	//out.println("NumTriplets: "+Ntriplets);
	            	 
	            	 maxFreq=stream.readFloat();
	            	 maxAmp=stream.readFloat();
	            	 
	            	 //if(t==80)
	            		 //out.println(Ntriplets);
	            	 
	            	
	            		out.println(maxFreq+" "+mark+" <= frequencies ");
	            		// out.println(maxAmp);
	            	 
	            	 
	            	 
	            			 
	            			
	    	            	 check=new float [2] [Ntriplets*3];
	    	            	 
	    	            	 for(int b=1;b<=Ntriplets;b++)
	    	            	 {
	                		 for(int c=0;c<3;c++)
	    	            		 {
	    	            			 
	    	            		         freq=stream.readFloat();
	    	            				 amp=stream.readFloat();
	    	            				 phase=stream.readFloat();
	    	            				// out.println((b-1)*3+c);
	    	            				

	    		    	            	 if(Math.abs(maxFreq-mark)<df||maxFreq==mark)
	    		    	            	 {
	    		    	            	 
	    	            			 check[0][(b-1)*3+c]=amp;
	    	            			 check[1][(b-1)*3+c]=freq;
	    		    	            	 }
	    	            			 //if((b-1)*3+c>=182&&(b-1)*3+c<=185&&t==80)
	    	            			 //out.println((b-1)*3+c+"    "+freq+" "+amp);
	    	            			 
	    	            		 }
	
	    	            	 }
	    	            	// out.println(Arrays.toString(check[1]));
	    	            	 
	    	            	 
	    	            	
	    	            	 if(Math.abs(maxFreq-mark)<2*df||maxFreq==mark)
	    	            	 {
	    	            	 
	    	            			
	    	            			 correlation=(calc.calcCorrelation(tripletsArrayRecord, check));
	    	            			 if(correlation>.7){
	    	            				 //tEnd = (long)(System.currentTimeMillis()/1000);
	    	            				 //long tDelta=tEnd=tStart;
	    	            				 out.println("\n"+correlation+" correlation at "+ (t+1) +" frame with maxFreq = "+maxFreq+" while mark = "+mark);
	    	            				 //out.println("\n"+correlation+" correlation at "+ (t+1) +" frame with maxFreq = "+maxFreq+" while mark = "+mark+", using "+tDelta+" time.");
	    	            				 break;
	    	            			 
	    	            		    
	    	            			}
	    	            	 }
	 
	             }
	 
	        // }*/
	     
	       
	            
	        
	      
	        
	       
	     
	    
	        
	        
	        
	      
	     
	     
	 
	  
	         

	        
	    
 }
}