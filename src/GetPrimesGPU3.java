import com.aparapi.Kernel;
import com.aparapi.Range;

public class GetPrimesGPU3 {

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


        final int[] numberOfPrimesKnown = {1};
        final boolean[] maybeIsPrime = {true};
        final boolean[] flip = {true};
        final int[] val = {3};

        Kernel kernel = new Kernel(){ // make kernel

            public void addPrime(int gid){

                if(gid == 0) {

                    if (maybeIsPrime[0]) {
                        primes[numberOfPrimesKnown[0]] = val[0];
                        differences[numberOfPrimesKnown[0]] = 0;
                        numberOfPrimesKnown[0]++;
                    }

                    maybeIsPrime[0] = true;
                    val[0]++;

                }
            }

            public void testPrime(int gid){

                differences[gid]++;

                if(differences[gid] == primes[gid]){
                    differences[gid] = 0;
                    maybeIsPrime[0] = false;
                }

            }

            @Override public void run() {

                int gid = getGlobalId();

                if(flip[0]){
                    testPrime(gid);
                    flip[0] = false;
                } else {
                    addPrime(gid);
                    flip[0] = true;
                }

            }
        };

        primes[0] = 2;
        differences[0] = 0;

        kernel.setExplicit(true);
        kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);

        kernel.put(primes);
        kernel.put(differences);
        kernel.put(val);
        kernel.put(maybeIsPrime);
        kernel.put(numberOfPrimesKnown);

        while(val[0] <= max){

            kernel.execute(Range.create(numberOfPrimesKnown[0])); //start up a GPU code for each prime we already know, and
            kernel.execute(1); //addPrime
            kernel.get(numberOfPrimesKnown);

            val[0] ++;

        }

        kernel.get(primes);

        kernel.dispose();

        int x = primes[numberOfPrimesKnown[0]];

        int[] output = new int[numberOfPrimesKnown[0]];

        for(int i = 0; i < numberOfPrimesKnown[0]; i++){
            int temp = primes[i];
            output[i] = temp;
        }

        return output;

    }
}
