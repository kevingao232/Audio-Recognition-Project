
import java.io.IOException;


import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;

 
public class FFT {
	
	public FFT()
	{
		fillRange();
	}

    public static int bitReverse(int n, int bits) {
        int reversedN = n;
        int count = bits - 1;
 
        n >>= 1;
        while (n > 0) {
            reversedN = (reversedN << 1) | (n & 1);
            count--;
            n >>= 1;
        }
 
        return ((reversedN << count) & ((1 << bits) - 1));
    }
 
    public static Complex[] fft(Complex[] x) {
 
    	int n = x.length;

        // base case
        if (n == 1) {
            return new Complex[] { x[0] };
        }

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[n/2];
        for (int k = 0; k < n/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < n/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n/2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].add(wk.mult(r[k]));
            y[k + n/2] = q[k].sub(wk.mult(r[k]));
        }
        return y;
    }
    
 
	
    

        public static  int[] RANGE = new int[200] ;

        public void fillRange()
        {
        	for (int i=0;i<200;i++)
        	{
        		RANGE[i]=(i+1)*100;
        	}
        }
        
        //Find out in which range
        public static  int getIndex(int freq) {
        	
        	for (int i=0;i<200;i++)
        	{
        		RANGE[i]=(i+1)*100;
        	}
            int i = 0;
            
            while(freq>RANGE[i]) {
            	
            	i++;
            	
            	if(i+1>=RANGE.length)
            	{
            		//System.out.println("Frequency out of 20kHz Range, exiting method.");
            		return RANGE.length-1;
            	}
            	
            }
                return i;
            }
       
        
        
       /*System.out.println("Results:");
        
       for (Complex b : cinput) {
            System.out.println(b);
        } */
    }

 
class Complex {
    public final double re;
    public final double im;
 
    public Complex() {
        re=0;
        im=0;
    }
 
    public Complex(double r, double i) {
        re = r;
        im = i;
    }
    public double getReal()
    {
    	return re;
    }
    public double getImag()
    {
    	return im;
    }
  
    public Complex add(Complex b) {
        return new Complex(re + b.re,im + b.im);
    }
 
    public Complex sub(Complex b) {
        return new Complex(re - b.re, im - b.im);
    }
 
    public Complex mult(Complex b) {
        return new Complex(re * b.re - im * b.im,
                re * b.im + im * b.re);
    }
 
    @Override
    public String toString() {
        //return String.format("(%f,%f)", re, im);
    	return "( " + re +" , "+im+" )";
    }
    public double abs() {
        return Math.hypot(re, im);
    }
   
}