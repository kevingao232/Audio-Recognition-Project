import java.util.ArrayList;

public class Correlation {

	public Correlation()
	{
	}
		
	public float calcCorrelation(double[][]record, float[][] data)
	{
		if(record[0].length!=record[1].length)
		{
			System.out.println("Mismatch between number of amplitudes and number of frequencies in record.");
			return -1;
		}
		
		//System.out.println("Record length: "+record[0].length);
		//System.out.println("Data length: "+data[0].length);
		
		
	    
		
	    double cov=0;
	 	double sigmax=0;
	 	double sigmay=0;
	    double sx = 0.0;
	 	double sy = 0.0;
	 	double sxx = 0.0;
	 	double syy = 0.0;
	 	double sxy = 0.0;
	 	int counter=0;
	 	float average;
	 	float total=0;
	 	ArrayList <Double> corrSet=new ArrayList<Double>();
	 	
	 	double corrtrack=0;
	 	int frameCount=0;
	    int n = 3;
        
	  
	    
	    double previousCorr=-1;
	   
	   
	    for(int x=0;x<data.length;x+=2)
	    
	    {
	    	
	    	counter=0;
	    	corrSet=new ArrayList<Double>();
	    	// System.out.println("ok");
	    	
	    	for(int y=0;(y+3)<data[x].length;y+=3)
	    	{
	    		//System.out.println("hi");
	    			corrtrack=0;
	    		    sx = 0.0;
			 	    sy = 0.0;
			 	    sxx = 0.0;
			 	    syy = 0.0;
			 	    sxy = 0.0;
			 	
			 	    
			 	   System.out.println(y+" "+record[1][counter]+ " " + record[1][counter+2]+" "+data[x+1][y]+" "+ data[x+1][y+2]);
	    		if(counter+2<record[0].length&&overlap(record[1][counter], record[1][counter+2], data[x+1][y], data[x+1][y+2])>.6)
	    		{

	    			//System.out.println(record[1][counter]+" " + record[1][counter+2]+" "+data[x+1][y]+" "+ data[x+1][y+1]);
	    		    //System.out.println(overlap(record[1][counter], record[1][counter+2], data[x+1][y], data[x+1][y+2]));
	    			
	    			for(int i=0;i<3;i++){
	    				
	    				
	    			
	    					
	    			double x2 = record[1][counter];
	    			
		            double y2 = data[x+1][i+y];
		            
		           // System.out.println(x2+" "+y2);
		  
		            sx += x2;
		            sy += y2;
		            sxx += x2 * x2;
		            syy += y2 * y2;
		            sxy += x2 * y2;
		            
		          
		            counter++;
		             

	    		    }
	    			
	    			 // covariation
	    			cov = Math.abs(sxy / n - sx * sy / n / n);
			       
		   	     	// standard error of x
		   	     	sigmax = Math.sqrt(Math.abs(sxx / n -  sx * sx / n / n));
		   	  
		   	     	// standard error of y
		   	     	sigmay = Math.sqrt(Math.abs(syy / n -  sy * sy / n / n));
		   	   
		   	       //System.out.println(cov+ " "+ sigmax+" "+sigmay);
		   	     	corrtrack+=cov/sigmax/sigmay;
		   	     	
		   	     	
		   	     	
	    	}
	    		if(corrtrack>.55){
		   	     	System.out.println(corrtrack+" <= good correlation.\n");
		   	     	corrSet.add(corrtrack);
		   	     	previousCorr=corrtrack;
		   	     	
		   	     	}

		   	     	else if(frameCount<5&&counter!=0){
		   	     		counter=counter-3;
		   	     		frameCount++;
		   	     		
		   	     	}
		   	     	else if(frameCount>=5)
		   	     	{
		   	     		frameCount=0;
		   	     	}
	    		
	    		
	      }
	    	
	    	
	    
	    	
	    	  for(int i=0;i<corrSet.size();i++)
	  	    {
	  	    	total+=corrSet.get(i);
	  	    }
	    	  
	    	  average=total/(corrSet.size());
	  	      if(average>.7&&corrSet.size()>(record.length/3)*.1){
	  	    	 
	  	       return average;
	  	     }
	  	    
	  	  }
	
	     
	  
	

	   // if(corrSet.size()>(int)record[0].length*.8){
    		
    		
    		
		
	    
    	/*else
    	{
    		System.out.println("wuhmp wuhmp, not verified lol");
    		return -12345;
    	}*/
    		
	
	    
	 
		  
	  
	  
	  
	  return -12345;
}
	  
	public double overlap(double start, double end, double data, double data2)
	{
		
		double a=0;
		double b=0;
		
		if(data>start||data==start)
		{
			a=data;
		}
		else if(start>data)
		{
			a=start;
		}
		
		if(data2<end||data2==end)
		{
			b=data2;
		}
		else if(end<data2)
		{
			b=end;
		}
		
		
			return ((double)(b-a))/(double)(data2-data);
		
		//return -1;
	}
	
}

