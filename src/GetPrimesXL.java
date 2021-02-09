import com.aparapi.Kernel;

import java.util.ArrayList;

public class GetPrimesXL {

    public static int[] getPrimesTo(int max){

        int[] primes = {2,3,5,7};

        if(max > 100000000){ //10^8

            primes = getPrimes(primes,100,10);
            primes = getPrimes(primes, 10000, 100);
            primes = getPrimes(primes,100000000,10000);
            primes = getPrimes(primes, max, 100000);
            return primes;

        }

        if(max > 10000){ //10^4

            primes = getPrimes(primes,100,10);
            primes = getPrimes(primes, 10000, 100);
            primes = getPrimes(primes,max,10000);
            return primes;

        }

        if(max > 100){ //10^2

            primes = getPrimes(primes,100,10);
            primes = getPrimes(primes, max, 5000);
            return primes;

        }

        if(max > 10){ //10^1

            primes = getPrimes(primes,max,5);
            return primes;

        }

        return primes;

    }

    public static void valueTestGetPrimes() {

        int[] primes= {1};

        for (int x = 1; x <= 9; x++) { // we start breaking down at 431000

            int max = (int) Math.pow(10,x);

            long startTime = System.currentTimeMillis();

            primes = getPrimesTo(max);

            long endTime = System.currentTimeMillis();

            long duration = (endTime - startTime);

            System.out.println("[" + max + "] Time take to execute the whole script is " + duration + " milliseconds");



        }

        for (int i = primes.length - 10; i < primes.length; i++) {
            System.out.println(primes[i]);
        }

    }

    public static void main(String[] args) {

        //valueTestGetPrimes();

        long startTime = System.currentTimeMillis();

        getPrimesTo(2000000000);

        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        System.out.println("Time take to execute the whole script is " + duration + " milliseconds");


    }



    public static void speedTestGetPrimes(){

        double total = 0;

        getPrimesTo(1000000000); //warmup kernel

        for (int i = 0; i < 10; i++) {
            long startTime = System.currentTimeMillis();
            getPrimesTo(1000000000);
            long endTime = System.currentTimeMillis();
            total += (endTime - startTime);
        }

        System.out.println("Average run time is " + (total / 10) + " (Excluding warmup)");

    }

    static int[] globalPrimesMap;

    static int[] globalThreadIdMap;


    public static int[] getPrimes(final int[] primes, final int max, final int x){

        final boolean[] isNotPrime = new boolean[max];

        final int[] miniThreadSize = {x}; //Has to be an array for silly reasons

        //----------------------------------------

        int totalNumberOfMiniThreads = 0;

        int[] primeNumberOfMiniThreads = new int[primes.length]; //The number of miniThreads for each prime

        for (int i = 0; i < primes.length; i++) {
            int numberOfMiniThreads = calculateNumberOfMiniThreads(primes[i], max, miniThreadSize[0]);

            if(numberOfMiniThreads < 0){ //This detects any kind of overflow error (For testing purposes)
                System.out.println(numberOfMiniThreads + " for " + primes[i]);
            }

            primeNumberOfMiniThreads[i] = numberOfMiniThreads;
            totalNumberOfMiniThreads += numberOfMiniThreads;
        }

        //------------------------------------------

        generatePrimesMapAndThreadIdMap(primes, primeNumberOfMiniThreads, totalNumberOfMiniThreads);

        final int[] threadIdMap = globalThreadIdMap;

        final int[] primesMap = globalPrimesMap;

        Kernel kernel = new Kernel(){

            @Override public void run() {

                int gid = getGlobalId(); //get global id (number between 0 and totalNumberOfMiniThreads), this is unique to each thread

                int prime = primesMap[gid]; // converts gid to prime

                int miniThreadId = threadIdMap[gid]; // converts gid to threadId

                int x = prime * (prime + miniThreadSize[0] * miniThreadId); // calculate starting value

                for (int i = 0; i <= miniThreadSize[0]; i++) { //loop through and set all multiples in miniThread of the prime we are implementing to be not prime

                    if(x >= isNotPrime.length){return;} //stops the thread if we get to the end of the array (I really didn't think this would work... but it does) PS. What if this doesn't do anything and i'm writing to random parts of the GPU memory?

                    isNotPrime[x] = true;
                    x += prime;

                }

            }
        };

        //kernel.setExplicit(true);
        //kernel.put(primesMap);
        //kernel.put(threadIdMap);
        //kernel.put(miniThreadSize);
        //kernel.put(isNotPrime);

        kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);

        long startTime = System.currentTimeMillis(); //start stopwatch

        kernel.execute(totalNumberOfMiniThreads);

        System.out.println("Kernel executed via " + kernel.getExecutionMode());

        long endTime = System.currentTimeMillis(); //stop stopwatch

        long duration = (endTime - startTime);

        System.out.println("Time take to execute the kernel is " + duration + " milliseconds");

        //kernel.get(isNotPrime);
        kernel.dispose();



        int count = 0;

        for (int i = 2; i < isNotPrime.length; i++) {
            if(!isNotPrime[i]){
                count++;
            }
        }

        int[] output = new int[count];
        int index = 0;

        for (int i = 2; i < isNotPrime.length; i++) {
            if(!isNotPrime[i]){
                output[index] = i;
                index++;
            }
        }



        return output;
    }

    public static void generatePrimesMapAndThreadIdMap(int[] primes, int[] primeNumberOfMiniThreads, int totalNumberOfMiniThreads){

        globalPrimesMap = new int[totalNumberOfMiniThreads];

        globalThreadIdMap = new int[totalNumberOfMiniThreads];

        int index = 0;

        for (int i = 0; i < primes.length; i++) {
            for (int j = 0; j < primeNumberOfMiniThreads[i]; j++) {
                globalPrimesMap[index] = primes[i];
                globalThreadIdMap[index] = j;
                index++;
            }
        }

        // return primesMap;
    }

    public static int calculateNumberOfMiniThreads(long prime, long max, long miniThreadSize){ //fyi this does not work if the miniThreadSize is too large ¯\_(ツ)_/¯

        long num = (max / prime) - prime; //This is a reordering of max - prime*prime, if this is negative or zero the prime doesn't need to be checked

        if(num < 1){
            return 0;
        }

        long lengthOfOneThread = prime * miniThreadSize;

        long primeSquared = prime * prime;

        return (int) ((max - primeSquared) / lengthOfOneThread) + 1; //rounded up
    }
}
