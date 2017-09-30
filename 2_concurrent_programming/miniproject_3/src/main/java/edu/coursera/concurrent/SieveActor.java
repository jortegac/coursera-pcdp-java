package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import edu.rice.pcdp.PCDP;

import java.util.ArrayList;
import java.util.List;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {

        final SieveActorActor sieveActorActor = new SieveActorActor();
        PCDP.finish(() ->{
             for (int i = 3; i <= limit; i += 2) {
                 sieveActorActor.send(i);
             }
        });

        SieveActorActor tmp = sieveActorActor;
        int numPrimes = 1;
        while (tmp != null) {
            numPrimes += tmp.getNumberOfPrimes();
            tmp = tmp.nextActor;
        }

        return numPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {

        private final static int MAX_LOCAL_PRIMES = 50;
        SieveActorActor nextActor;
        private List<Integer> localPrimes;

        SieveActorActor() {
            super();
            localPrimes = new ArrayList<>(MAX_LOCAL_PRIMES);
            this.nextActor = null;
        }

        /**
         * Process a single message sent to this actor.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            final int candidate = (int) msg;
            final boolean isPrime = isPrime(candidate);

            if (isPrime) {
                if (getNumberOfPrimes() < MAX_LOCAL_PRIMES) {
                    localPrimes.add(candidate);
                } else if (nextActor == null) {
                    nextActor = new SieveActorActor();
                    nextActor.send(candidate);
                } else {
                    nextActor.send(msg);
                }
            }
        }

        public boolean isPrime(int n)
        {
            for(Integer prime : localPrimes){
                if(n % prime == 0){
                    return false;
                }
            }
            return true;
        }

        public int getNumberOfPrimes() {
            return localPrimes.size();
        }
    }
}
