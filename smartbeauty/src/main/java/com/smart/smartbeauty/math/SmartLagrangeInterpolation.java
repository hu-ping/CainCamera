package com.smart.smartbeauty.math;

/**
 * Created by deepglint on 2019/3/27.
 */

public class SmartLagrangeInterpolation {
    /*拉格朗日插值法*/
    public static double[] lagMethod(double X[], double Y[], double X0[]){
        int m=X.length;
        int n=X0.length;
        double Y0[]=new double[n];
        for(int i1=0;i1<n;i1++){//遍历X0
            double t=0;
            for(int i2=0;i2<m;i2++){//遍历Y
                double u=1;
                for(int i3=0;i3<m;i3++){//遍历X
                    if(i2!=i3){
                        u=u*(X0[i1]-X[i3])/(X[i2]-X[i3]);
                    }
                }
                u=u*Y[i2];
                t=t+u;
            }
            Y0[i1]=t;
        }

        return Y0;

    }
}
