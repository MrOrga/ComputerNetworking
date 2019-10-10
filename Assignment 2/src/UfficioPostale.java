import java.sql.Time;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UfficioPostale implements Runnable
{
    private static final int nSportelli = 4;
    private static final int timeout = 10;
    private ThreadPoolExecutor sportelli;
    private LinkedBlockingQueue<Persona> salaAttesa;

    //k è  il parametro per la massima lunghezza della coda e deve essere >0
    public UfficioPostale(int k)
    {
        sportelli = new ThreadPoolExecutor(nSportelli, nSportelli, timeout, TimeUnit.SECONDS, new LinkedBlockingQueue<>(k));
        salaAttesa = new LinkedBlockingQueue<>();
    }

    public void arrivoPersona(Persona p)
    {
        if (p == null) throw new NullPointerException("Persona non può essere null");
        else {
            salaAttesa.add(p);
            System.out.println(p + " è entrato nella sala d'attesa");
        }
    }

    @Override
    public void run()
    {
        try {
            while (true)
            {
                if (sportelli.getQueue().remainingCapacity() > 0)
                {
                    Persona curr = salaAttesa.take();
                    sportelli.submit(curr);
                }
            }
        } catch (InterruptedException ex)
        {
            sportelli.shutdown();
            System.out.println("Chiusura ufficio");
        }

    }
    //k int è richiesto >0 ed è la dimensione massima della seconda sala di attesa
    public static void main(String[] args)
    {
        int k = Integer.parseInt(args[0]);
        if (k <= 0) throw new IllegalArgumentException("k deve essere maggiore di 0");
        UfficioPostale uff = new UfficioPostale(k);
        Thread ufficio = new Thread(uff);
        ufficio.start();
        //tempo di apertura ufficio generato casualmente
        long timeUff = 30+(long)(Math.random()*60);
        long time1=System.currentTimeMillis();
        int i;
        //generazione prima 30 persone all apertura dell ufficio
        for (i = 0; i < 30; i++)
            uff.arrivoPersona(new Persona("Persona" + i));
        //generazione flusso continuo fino alla chiusura dell ufficio
        try {
            while ((System.currentTimeMillis()-time1) / 1000 < timeUff )
            {
                long sleeptime = (long) (Math.random() * 10);
                Thread.sleep(sleeptime*1000);
                uff.arrivoPersona(new Persona("Persona" + i));
                i++;
            }


        } catch (InterruptedException e)
        {
        }
        ufficio.interrupt();
    }
}
