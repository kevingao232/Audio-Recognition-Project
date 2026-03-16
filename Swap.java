
public class Swap {
	
	
	public Swap()
	{
		
	}
	
	public  short swap (short value)
	  {
	    int b1 = value & 0xff;
	    int b2 = (value >> 8) & 0xff;

	    return (short) (b1 << 8 | b2 << 0);
	  }

	  

	  /**
	   * Byte swap a single int value.
	   * 
	   * @param value  Value to byte swap.
	   * @return       Byte swapped representation.
	   */
	  public  int swap (int value)
	  {
	    int b1 = (value >>  0) & 0xff;
	    int b2 = (value >>  8) & 0xff;
	    int b3 = (value >> 16) & 0xff;
	    int b4 = (value >> 24) & 0xff;

	    return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
	  }

	  

	  /**
	   * Byte swap a single long value.
	   * 
	   * @param value  Value to byte swap.
	   * @return       Byte swapped representation.
	   */
	  public  long swap (long value)
	  {
	    long b1 = (value >>  0) & 0xff;
	    long b2 = (value >>  8) & 0xff;
	    long b3 = (value >> 16) & 0xff;
	    long b4 = (value >> 24) & 0xff;
	    long b5 = (value >> 32) & 0xff;
	    long b6 = (value >> 40) & 0xff;
	    long b7 = (value >> 48) & 0xff;
	    long b8 = (value >> 56) & 0xff;

	    return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
	           b5 << 24 | b6 << 16 | b7 <<  8 | b8 <<  0;
	  }
	  

	  
	  /**
	   * Byte swap a single float value.
	   * 
	   * @param value  Value to byte swap.
	   * @return       Byte swapped representation.
	   */
	  public  float swap (float value)
	  {
	    int intValue = Float.floatToIntBits (value);
	    intValue = swap (intValue);
	    return Float.intBitsToFloat (intValue);
	  }

	  

	  /**
	   * Byte swap a single double value.
	   * 
	   * @param value  Value to byte swap.
	   * @return       Byte swapped representation.
	   */
	  public  double swap (double value)
	  {
	    long longValue = Double.doubleToLongBits (value);
	    longValue = swap (longValue);
	    return Double.longBitsToDouble (longValue);
	  }


	  
	  /**
	   * Byte swap an array of shorts. The result of the swapping
	   * is put back into the specified array.
	   *
	   * @param array  Array of values to swap
	   */
	  public  void swap (short[] array)
	  {
	    for (int i = 0; i < array.length; i++)
	      array[i] = swap (array[i]);
	  }

	  
	  
	  /**
	   * Byte swap an array of ints. The result of the swapping
	   * is put back into the specified array.
	   * 
	   * @param array  Array of values to swap
	   */
	  public  void swap (int[] array)
	  {
	    for (int i = 0; i < array.length; i++)
	      array[i] = swap (array[i]);
	  }


	  
	  /**
	   * Byte swap an array of longs. The result of the swapping
	   * is put back into the specified array.
	   * 
	   * @param array  Array of values to swap
	   */
	  public  void swap (long[] array)
	  {
	    for (int i = 0; i < array.length; i++)
	      array[i] = swap (array[i]);
	  }


	  
	  /**
	   * Byte swap an array of floats. The result of the swapping
	   * is put back into the specified array.
	   * 
	   * @param array  Array of values to swap
	   */
	  public  void swap (float[] array)
	  {
	    for (int i = 0; i < array.length; i++)
	      array[i] = swap (array[i]);
	  }


	  
	  /**
	   * Byte swap an array of doubles. The result of the swapping
	   * is put back into the specified array.
	   * 
	   * @param array  Array of values to swap
	   */
	  public  void swap (double[] array)
	  {
	    for (int i = 0; i < array.length; i++)
	      array[i] = swap (array[i]);
	  }
}
