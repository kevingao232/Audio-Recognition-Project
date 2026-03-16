package com.audiofp.audio;

/**
 * Immutable complex number. Arithmetic operations return new instances.
 */
public final class Complex {

    public final double re;
    public final double im;

    public static final Complex ZERO = new Complex(0, 0);

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public Complex add(Complex b) {
        return new Complex(re + b.re, im + b.im);
    }

    public Complex sub(Complex b) {
        return new Complex(re - b.re, im - b.im);
    }

    public Complex mult(Complex b) {
        return new Complex(re * b.re - im * b.im, re * b.im + im * b.re);
    }

    /** Magnitude (absolute value). */
    public double abs() {
        return Math.hypot(re, im);
    }

    /** Phase angle in radians. */
    public double phase() {
        return Math.atan2(im, re);
    }

    @Override
    public String toString() {
        return String.format("(%.4f + %.4fi)", re, im);
    }
}
