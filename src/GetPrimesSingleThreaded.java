public class GetPrimesSingleThreaded {

    public static void printIntArray(int[] array){
        for(int i = 0; i < array.length; i++){
            System.out.println(array[i]);
        }
    }

    public static void main(String[] args) throws Exception {

        for(int x = 1; x <= 6; x++){

            int maxPrime = (int) Math.pow(10,x);

            long startTime = System.currentTimeMillis();

            getPrimes(maxPrime);

            long endTime = System.currentTimeMillis();

            long duration = (endTime - startTime);

            System.out.println("[" + maxPrime + "] Time take to execute the script is " + duration + " milliseconds");

        }

    }

    public static int[] getPrimes(int max){

        int[] primes = new int[max];
        int[] differences = new int[max];
        int pointer = 1; //length of primes/differences array or index of next available spot

        primes[0] = 2;
        differences[0] = 0;

        for(int val = 3; val <= max; val++){

            boolean maybeIsPrime = true;

            for(int i = 0; i < differences.length; i++){ //

                differences[i]++;

                if(differences[i] == primes[i]){

                    differences[i] = 0;
                    maybeIsPrime = false;

                }

            }

            if(maybeIsPrime){
                primes[pointer] = val;
                differences[pointer] = 0;
                pointer++;
            }

        }

        int[] output = new int[pointer];

        for(int i = 0; i < pointer; i++){
            output[i] = primes[i];
        }

        return output;

    }

}
