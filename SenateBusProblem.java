import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SenateBusProblem{
    static int waiting_riders = 0; //To store the number of riders waiting in the boarding area
    static Semaphore mutex = new Semaphore(1); // To protect waiting_riders
    static Semaphore semaphore_boarded = new Semaphore(0);  //Signals that a bus has boarded
    static Semaphore semaphore_bus = new Semaphore(0);  //Signals that a bus has boarded

    class Rider implements Runnable{

        private int rider_id;

        Rider(int index){
            this.rider_id = index;
        }

        @Override
        public void run(){
            try{
                //when a riders arives, it needs to incriment the waiting_riders by one.
                //Since it is a shared variable, it was protected by a mutex
                mutex.acquire();
                waiting_riders += 1;
                System.out.println("Rider " + rider_id + " is waiting !!");
                mutex.release();

                semaphore_bus.acquire(); // Rider acquires the bus semaphore to get on board
                System.out.println("Rider " + rider_id + "got onboard.");
                semaphore_boarded.release(); //once boarded, release the boarded semaphore

            }
            catch(InterruptedException interruptedException){
                Logger.getLogger(SenateBusProblem.class.getName()).log(Level.SEVERE, "Rider" + rider_id + "'s thread got interrupted !!", interruptedException);
            }
        }
    }

   
    class Bus implements Runnable{
        private int riders_to_board; //A variable to hold the number of riders available for the bus
        private int bus_id;       

        Bus(int index){
            this.bus_id= index;
        }

        @Override
        public void run(){
            try{
                mutex.acquire(); //bus locks the mutex. Riders cant access the waiting_riders
                System.out.println("Bus " + bus_id + " locked the bus stop !!");

                riders_to_board = Math.min(waiting_riders, 50);

                System.out.println("waiting  : " + waiting_riders + " To board : "+riders_to_board);

                //A loop to get all the available riders  on board 
                for(int i = 0; i < riders_to_board; i++){
                    System.out.println("Bus " + bus_id + " released for "+i+"th rider");
                    semaphore_bus.release();  //Bus signals that it has arrived and can take a passenger on board
                    semaphore_boarded.acquire();  //Allows only one rider to get on board at a time
                    System.out.println("Bus " + bus_id + " acquired boarded !");
                }

                //If all riders are get onboard, waiting riders are 0.
                //If waited riders are more than 50, waiting riders at the moment is equal to (waiting_riders - 50)
                waiting_riders = Math.max((waiting_riders-50), 0);
                mutex.release();   
            }
            catch(InterruptedException interruptedException){
                Logger.getLogger(SenateBusProblem.class.getName()).log(Level.SEVERE, "Bus" + bus_id + "'s thread got interrupted !!", interruptedException);
            }
            System.out.println("Bus " + bus_id + " departed with " + riders_to_board + " riders on board!");  
        }
    }


    public static void main(String[] args){
        SenateBusProblem senateBusProblem = new SenateBusProblem();
        int bus_id = 0;
        int rider_id = 0;
        long diff_bus=0,diff_rider=0,time_curr=0;
        long time_prev_bus=System.currentTimeMillis();
        long time_prev_rider=System.currentTimeMillis();

        double mean_rider=30000,mean_bus=1200000;           //Declaring the mean of the exponential distributions of inter-arrival times of riders and buses in milli seconds
        double rand_bus  = 0.0, rand_rider=0, wait_time_rider=0, wait_time_bus=0;

        rand_bus = new Random().nextDouble();
        wait_time_rider = Math.round(Math.log10(rand_bus)*-1*mean_rider);  //Calculating the time before the next bus arrives

        rand_rider = new Random().nextDouble();
        wait_time_bus = Math.round(Math.log10(rand_rider)*-1*mean_bus);     //Calculating the time before the next bus arrives

        while(Boolean.TRUE){
            time_curr = System.currentTimeMillis();
            diff_rider = time_curr - time_prev_rider;
            diff_bus = time_curr - time_prev_bus;

            if(diff_rider == wait_time_rider){
                Rider new_rider = senateBusProblem.new Rider(rider_id++);
                new Thread(new_rider).start();
                time_prev_rider = time_curr;

                rand_rider = new Random().nextDouble();
                wait_time_rider = Math.round(Math.log10(rand_rider)*-1*mean_rider);  //Calculating the time before the next rider arrives
            }

            if(diff_bus == wait_time_bus){
                Bus new_bus = senateBusProblem.new Bus(bus_id++);
                new Thread(new_bus).start();
                time_prev_bus = time_curr;

                rand_bus = new Random().nextDouble(); 
                wait_time_bus = Math.round(Math.log10(rand_bus)*-1*mean_bus);  //Calculating the time before the next bus arrives
            }

        }

    }

}