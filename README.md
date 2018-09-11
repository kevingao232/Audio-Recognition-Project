# Audio-Recognition-Project

Description Of Project Components:

*LineOpen* - Opens computer's microphone and records 10 seconds of audio, uses an OutputStream to convert byte data to time-domain data which sets up for FFT.

*FFT* - Based on the Princeton Implementation, with changes.

*Triplets* - eliminates majority of outlier frequency domain data, preserving only peaks and troughs using derivative calculation and dot product analysis.

*Correlation* - statistical analysis of triplets, checking how similar the relative peaks and troughs of the respective experimental and theoretical data are.


