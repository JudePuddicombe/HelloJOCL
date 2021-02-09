import com.aparapi.Kernel;
import com.aparapi.Range;

public class GetPrimesGPU {

    public static void printIntArray(int[] array){
        for(int i = 0; i < 100; i++){
            System.out.println(array[i]);
        }
    }

    public static void testGetPrimes(){

        for(int x = 1; x <= 6; x++){

            int maxPrime = (int) Math.pow(10,x);

            long startTime = System.currentTimeMillis();

            getPrimes(maxPrime);

            long endTime = System.currentTimeMillis();

            long duration = (endTime - startTime);

            System.out.println("[" + maxPrime + "] Time take to execute the script is " + duration + " milliseconds");

        }

    }

    public static void main(String[] args) throws Exception {

        testGetPrimes();

    }

    public static int[] getPrimes(int max){

        final int[] primes = new int[max];
        final int[] differences = new int[max];
        final boolean[] maybeIsPrime = {true};

        Kernel kernel = new Kernel(){ // make kernel

            @Override public void run() {

                int gid = getGlobalId();

                differences[gid]++;

                if(differences[gid] == primes[gid]){
                    differences[gid] = 0;
                    maybeIsPrime[0] = false;
                }

            }
        };


        int pointer = 1; //length of primes/differences array or index of next available spot

        primes[0] = 2;
        differences[0] = 0;

        for(int val = 3; val <= max; val++){

            kernel.execute(Range.create(max));

            if(maybeIsPrime[0]){
                primes[pointer] = val;
                differences[pointer] = 0;
                pointer++;
            }

            maybeIsPrime[0] = true;

        }

        kernel.dispose();

        int[] output = new int[pointer];

        for(int i = 0; i < pointer; i++){
            output[i] = primes[i];
        }

        return output;

    }

}
