import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.*;


class GetPrimesMultiThreaded {

    static int[] primes;

    static int max = 4;

    static boolean[] maybeIsPrimes;

    public static void printIntArray(int[] array){
        for(int i = 0; i < array.length; i++){
            System.out.println(array[i]);
        }
    }

    static class Eliminate implements Runnable{ //defining my thread

        private int prime;

        Eliminate(int prime){
            this.prime = prime;
        }

        public void run(){

            try{

                int i = prime*prime;

                while(i < maybeIsPrimes.length){

                    maybeIsPrimes[i] = false;
                    i += prime;
                }

            } catch(Exception RuntimeException) {}

        }

        public void start (){
            new Thread(this).start();
        }
    }

    public static void main(String[] args) throws InterruptedException {



        //for(int x = 1; x <= 12; x++){

            int maxPrime = (int) Math.pow(10,8);

            long startTime = System.currentTimeMillis();

            getPrimes(maxPrime);

            long endTime = System.currentTimeMillis();

            long duration = (endTime - startTime);

            System.out.println("[" + maxPrime + "] Time take to execute the script is " + duration + " milliseconds");

        //}



    }

    public static ArrayList getPrimes(int x) throws InterruptedException {

        maybeIsPrimes = new boolean[x];
        Arrays.fill(maybeIsPrimes, Boolean.TRUE);
        maybeIsPrimes[0] = false;
        maybeIsPrimes[1] = false;

        int i = 0;
        int highestPrime = 2;
        int highestPrimePlusOne;
        int max = 4; //I can find primes up to the square of the smallest prime I have not implmented.

        while(i < maybeIsPrimes.length){

            ExecutorService es = Executors.newCachedThreadPool();

            while(i < max && i < maybeIsPrimes.length){ //spool up a bunch of threads up to max

                if(maybeIsPrimes[i]){
                    es.execute(new Eliminate(i));
                    highestPrime = i;
                }

                i++;

            }

            es.shutdown();

            es.awaitTermination(1000, TimeUnit.MILLISECONDS); //wait for all the threads to complete

            highestPrimePlusOne = highestPrime + 1; //This is the smallest prime I have not implemented

            max = highestPrimePlusOne*highestPrimePlusOne; //update max prime I can go up to

        }

        ArrayList primes = new ArrayList();

        for(int j = 0; j < x; j++){
            if(maybeIsPrimes[j]){
                primes.add(j);
            }
        }

        return primes;
    }
}