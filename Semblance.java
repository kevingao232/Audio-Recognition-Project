
public class Semblance {

	private double [] data;
	private double[] recording;
	
	public Semblance()
	{
		data = null;
		recording = null;
	}
	
	public Semblance(double[] r, double [] d)
	{
		data=d;
		recording=r;
		if(d.length==r.length)
		{
			System.out.println("SEMBLANCE ERROR");
		}
	}
	
	
	public double calcSemblance()
	{
		
		recording=data;
		
		
		double semb=0;
   	 	double num=0;
   	 	double denom=0;
   	 	double numtrack=0;
   	 	double denomtrack=0;
		
	   for (int a=0; a< data.length;a++)
		{
		  boolean exit=false;
		  num=0;
	      denom=0;
	      
			for(int b=0; b<recording.length;b++){
		
		      if(a+recording.length<=data.length){
		    
		       num+=Math.pow((data[a+b]+recording[b]), 2);
		    	
		       denom+=(Math.pow(data[a+b], 2)+Math.pow(recording[b], 2));
		   
		       }
		      
		      else{
		    	  System.out.println("No significant semblance found.");
		    	  exit=true;
		    	  System.out.println(numtrack+" "+denomtrack);
		    	  break;
		       }
	        }
			denom=denom*2;
					
				if(num>numtrack)
				{
					numtrack=num;
				}
				if(denom>denomtrack)
				{
					denomtrack=denom;
				}
			
			
			
			 
			 
			 if(num>=0&&denom>0){
	    		 
	    		 semb=num/denom;
	    		 
	    		 if(semb>.2) return semb;
	    		
	    		 
	    	  }
	    	 
	    	 else{
	    		 System.out.println(num+" "+denom);
	    		    return -1;
	    	 }
			
		}
	   
	   return -2;
	   
	}
	
	
}
