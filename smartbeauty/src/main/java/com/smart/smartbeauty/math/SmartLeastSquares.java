package com.smart.smartbeauty.math;

import android.util.Log;

/**
 * Created by deepglint on 2019/3/28.
 */

public class SmartLeastSquares {

    private static final String TAG = "SmartLeastSquares";
    private static double[] mCoefficients = null;

    private static double[] MultiLine(double[] arrX, double[] arrY, int length, int dimension) {
        int i,j,k,n,N;

//        Log.e(TAG, "Enter the no. of data pairs to be entered:");        //To find the size of arrays that will store x,y, and z values
        N = length;
        double[] x = new double[N];
        double[] y = new double[N];
//        Log.e(TAG, "Enter the x-axis values:");                //Input x-values
        for (i = 0; i < N; i++) {
            x[i] = arrX[i];
        }

//        Log.e(TAG,"Enter the y-axis values:");                //Input y-values
        for (i=0;i<N;i++) {
            y[i] = arrY[i];
        }

//        Log.e(TAG,"What degree of Polynomial do you want to use for the fit?");
        n = dimension;                                // n is the degree of Polynomial

        double[] X = new double[2*n+1];                        //Array that will store the values of sigma(xi),sigma(xi^2),sigma(xi^3)....sigma(xi^2n)
        for (i=0;i<2*n+1;i++)
        {
            X[i]=0;
            for (j=0;j<N;j++)
                X[i]=X[i]+Math.pow(x[j],i);        //consecutive positions of the array will store N,sigma(xi),sigma(xi^2),sigma(xi^3)....sigma(xi^2n)
        }
        double[][] B = new double[n+1][n+2];
        double[] a = new double[n+1];            //B is the Normal matrix(augmented) that will store the equations, 'a' is for value of the final coefficients
        for (i=0;i<=n;i++)
            for (j=0;j<=n;j++)
                B[i][j]=X[i+j];            //Build the Normal matrix by storing the corresponding coefficients at the right positions except the last column of the matrix
        double[] Y = new double[n+1];                    //Array to store the values of sigma(yi),sigma(xi*yi),sigma(xi^2*yi)...sigma(xi^n*yi)
        for (i=0;i<n+1;i++)
        {
            Y[i]=0;
            for (j=0;j<N;j++)
                Y[i]=Y[i]+Math.pow(x[j],i)*y[j];        //consecutive positions will store sigma(yi),sigma(xi*yi),sigma(xi^2*yi)...sigma(xi^n*yi)
        }
        for (i=0;i<=n;i++)
            B[i][n+1]=Y[i];                //load the values of Y as the last column of B(Normal Matrix but augmented)
        n=n+1;                //n is made n+1 because the Gaussian Elimination part below was for n equations, but here n is the degree of polynomial and for n degree we get n+1 equations

//        Log.e(TAG,"The Normal(Augmented Matrix) is as follows:");
        for (i=0;i<n;i++)            //print the Normal-augmented matrix
        {
            for (j=0;j<=n;j++) {
//                Log.e(TAG, B[i][j] + " ");
            }
        }
        for (i=0;i<n;i++)                    //From now Gaussian Elimination starts(can be ignored) to solve the set of linear equations (Pivotisation)
            for (k=i+1;k<n;k++)
                if (B[i][i]<B[k][i])
                    for (j=0;j<=n;j++)
                    {
                        double temp=B[i][j];
                        B[i][j]=B[k][j];
                        B[k][j]=temp;
                    }

        for (i=0;i<n-1;i++)            //loop to perform the gauss elimination
            for (k=i+1;k<n;k++)
            {
                double t=B[k][i]/B[i][i];
                for (j=0;j<=n;j++)
                    B[k][j]=B[k][j]-t*B[i][j];    //make the elements below the pivot elements equal to zero or elimnate the variables
            }
        for (i=n-1;i>=0;i--)                //back-substitution
        {                        //x is an array whose values correspond to the values of x,y,z..
            a[i]=B[i][n];                //make the variable to be calculated equal to the rhs of the last equation
            for (j=0;j<n;j++)
                if (j!=i)            //then subtract all the lhs values except the coefficient of the variable whose value                                   is being calculated
                    a[i]=a[i]-B[i][j]*a[j];
            a[i]=a[i]/B[i][i];            //now finally divide the rhs by the coefficient of the variable to be calculated
        }
//        Log.e(TAG,"The values of the coefficients are as follows:");
        for (i=0;i<n;i++)
//            Log.e(TAG, "x^" + i + "=" + a[i]);            // Print the values of x^0,x^1,x^2,x^3,....

//        Log.e(TAG, "Hence the fitted Polynomial is given by: y=");
        for (i=0;i<n;i++) {
//            Log.e(TAG, " + (" + a[i] + ")" + "x^" + i);
        }

        return a;
    }

    public static void initCoefficients(double[] x, double[] y, int length) {
        mCoefficients = MultiLine(x, y, length, 2);

    }

    public static double getY(double x) {
        double sum = 0;

        sum =
//                (mCurveParam[7] * x * x * x * x * x * x * x)
//                + (mCurveParam[6] * x * x * x * x * x * x )
//                + (mCurveParam[4] * x * x * x * x * x)
//                + (mCoefficients[4] * x * x * x * x)
//                + (mCoefficients[3] * x * x * x)
                + (mCoefficients[2] * x * x)
                        + (mCoefficients[1] * x) + mCoefficients[0];

        return sum;
    }


    public static  void test()  {
        double[] x = {1, 2, 3};
        double[] y = {4, 5, 6};
        double[] a = MultiLine(x, y, 3, 4);

        for(int i =0; i <a.length;i++){
            Log.i(TAG, " " +  a[i]);
        }
    }

}
