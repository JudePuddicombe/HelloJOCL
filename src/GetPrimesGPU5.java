import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.ArrayList;
import java.util.Arrays;

public class GetPrimesGPU5 {

    public static ArrayList<Integer> getPrimesTo(int max){

        ArrayList<Integer> myPrimes = new ArrayList<>();

        myPrimes.add(2);
        myPrimes.add(3);
        myPrimes.add(5);
        myPrimes.add(7);

        int startNum = 10;
        int endNum = 100;

        Integer[] primes = myPrimes.toArray(new Integer[myPrimes.size()]);

        while (endNum < max) {
            myPrimes.addAll(getPrimes(primes, startNum, endNum));
            primes = myPrimes.toArray(new Integer[myPrimes.size()]);
            startNum = endNum;
            endNum = startNum * startNum;
        }

        myPrimes.addAll(getPrimes(primes, startNum, max));

        return myPrimes;
    }

    /*public static ArrayList<Integer> getPrimesTo(int max){

        ArrayList<Integer> myPrimes = new ArrayList<>();

        if(max < 2){
            myPrimes.add(2);
            return myPrimes;
        }

        int rootMax = (int) Math.sqrt(max);

        myPrimes = getPrimesTo(rootMax);

        Integer[] myIntPrimes = myPrimes.toArray(new Integer[myPrimes.size()]);

        return getPrimes(myIntPrimes,rootMax, max);
    }*/

    public static void testGetPrimes() {

        for (int x = 1; x <= 8; x++) {

            int maxPrime = (int) Math.pow(10, x);

            long startTime = System.currentTimeMillis();

            getPrimesTo(maxPrime);

            long endTime = System.currentTimeMillis();

            long duration = (endTime - startTime);

            System.out.println("[" + maxPrime + "] Time take to execute the script is " + duration + " milliseconds");

        }

    }

    public static void main(String[] args) {

        System.out.println(getPrimesTo(10000));

    }

    public static ArrayList<Integer> getPrimes(final Integer[] primes, final int startNum, final int endNum) {

        //final Integer[] primes = {2,3,5,7};
        //final int startNum = 10;
        //final int endNum = 100;

        int length = endNum - startNum;

        final int[] output = new int[length];

        for (int i = 0; i < output.length; i++) {
            output[i] = 1;
        }

        Kernel testPrimes = new Kernel() { // make kernel

            @Override
            public void run() {

                int gid = getGlobalId();
                int num = gid + startNum;

                for (int i = 0; i < primes.length; i++) {
                    output[gid] = output[gid] * (num % primes[i]);
                }

            }
        };

        testPrimes.setExecutionMode(Kernel.EXECUTION_MODE.GPU);

        testPrimes.execute(Range.create(length));

        System.out.println(testPrimes.getExecutionMode());

        testPrimes.dispose();

        ArrayList<Integer> x = new ArrayList<>();

        for(int i = 0; i < length; i++){
            if(output[i] != 0){
                x.add(i + startNum);
            }
        }

        return x;
    }
}
