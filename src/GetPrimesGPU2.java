import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.ArrayList;

public class GetPrimesGPU2 {

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

        testGetPrimes();

    }

    public static ArrayList<Integer> getPrimes(final Integer[] primes, final int startNum, final int endNum) {

        final int[] count = {0};

        int length = endNum - startNum;

        final boolean[] isNotPrime = new boolean[length];

        Kernel testPrimes = new Kernel() { // make kernel

            @Override
            public void run() {

                int gid = getGlobalId();

                int num = gid + startNum;

                for(int i = 0; i < primes.length; i++) {
                    if((num % primes[i]) == 0){
                        isNotPrime[gid] = true;
                    }
                }

            }
        };

        testPrimes.setExecutionMode(Kernel.EXECUTION_MODE.GPU);

        testPrimes.execute(Range.create(length));

        System.out.println("Execution mode = " + testPrimes.getExecutionMode());

        ArrayList<Integer> output = new ArrayList<>();

        for(int i = 0; i < length; i++){
            if(!isNotPrime[i]){
                output.add(i + startNum);
            }
        }

        return output;
    }
}
