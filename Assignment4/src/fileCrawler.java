import java.util.ArrayList;

public class fileCrawler
{

    public static void main(String[] args)
    {
        //fileCrawler dirPath k
        //dirPath (String) path delle directory
        //k(int) numero di consumatori
        String dirPath = args[0];
        int k = Integer.parseInt(args[1]);
        LinkedListSync queue = new LinkedListSync();
        Produttore prod =new Produttore(dirPath,queue);
        Thread producer = new Thread(prod);
        producer.start();
        ArrayList<Thread> consThreads = new ArrayList<>();
        for(int i=0;i < k;i++)
        {
            Consumatore cons = new Consumatore(queue);
            Thread consumer = new Thread(cons);
            consThreads.add(consumer);
            consumer.start();
        }
        //attesa produttore
        try
        {
            if(producer.isAlive())
                producer.join();
            //attese consumatori
            for(int i=0;i < k;i++)
            {
                if (consThreads.get(i).isAlive())
                    consThreads.get(i).join();
            }
        }catch (InterruptedException ex){ex.printStackTrace();}



    }
}
