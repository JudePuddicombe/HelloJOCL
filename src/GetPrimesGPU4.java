import com.aparapi.Kernel;
import com.aparapi.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GetPrimesGPU4 {

    public static void testGetPrimes(){

        for(int x = 46340; x <= 100000; x += 1){

           // int maxPrime = (int) Math.pow(10,x);

            System.out.println("Finding all primes up to " + x + " ");

            long startTime = System.currentTimeMillis();

            getPrimes(x);

            long endTime = System.currentTimeMillis();

            long duration = (endTime - startTime);

            System.out.println("[" + x + "] Time take to execute the script was " + duration + " milliseconds");

        }

    }

    public static void main(String[] args){
        testGetPrimes();
    }

    public static ArrayList getPrimes(int maxNumberToTest){

        final boolean[] numbersToTest = new boolean[maxNumberToTest];
        Arrays.fill(numbersToTest, Boolean.TRUE); // false = definitely not a prime, true = either yet to be tested, or confirmed as prime
        numbersToTest[0] = false;
        numbersToTest[1] = false;

        final int[] primeBuffer = new int[maxNumberToTest]; // list of primes that we will use to apply the sieve
        int primeBufferLength; // because the array is static, we have to record it's length 'manually'

        Kernel kernel = new Kernel(){

            @Override public void run() {

                int gid = getGlobalId(); // unique number representing the specific GPU core on which this code is being executed, always between 0 and the number of cores being run
                int prime = primeBuffer[gid]; //get a prime off the prime buffer, based on the id of the core (so we know each prime is only being pulled out once)

                int i = prime*prime; //all non primes before prime^2 have already been marked

                while(i < numbersToTest.length){

                    numbersToTest[i] = false;
                    i += prime;
                }
            }
        };

        int i = 0;
        int highestPrime = 2;
        int highestPrimePlusOne;
        int maxStableNumber = 4; //I can find primes up to the square of the smallest prime I have not filtered.

        while(i < numbersToTest.length){

            primeBufferLength = 0;

            while(i < maxStableNumber && i < numbersToTest.length){ //put a bunch of threads into the prime buffer

                if(numbersToTest[i]){  // true iif i is a prime
                    primeBuffer[primeBufferLength] = i; // add 'i' into the list of primes we're going to use to sieve
                    primeBufferLength++;
                    highestPrime = i;
                }

                i++;

            }

            kernel.put(primeBuffer);

            kernel.execute(Range.create(primeBufferLength)); //filter all of the primes in the buffer

            kernel.get(numbersToTest);

            highestPrimePlusOne = highestPrime + 1; //This is the smallest potential prime I have not tested

            maxStableNumber = highestPrimePlusOne*highestPrimePlusOne; //update max prime I can go up to

        }

        kernel.dispose();

        ArrayList primes = new ArrayList();

        for(int j = 0; j < maxNumberToTest; j++){
            if(numbersToTest[j]){
                primes.add(j);
            }
        }

        return primes;
    }

}

