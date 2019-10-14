import java.util.concurrent.locks.*;
import java.util.*;
public class Reparto implements Runnable
{
    protected final Lock [] medici;
    protected Condition myTurnRed;
    protected Condition myTurnYellow;
    protected Condition myTurnWhite;
    protected int mDisp;
    protected Lock lock;
    protected ArrayList<Paziente> redCode ;
    protected ArrayList<Paziente> yellowCode ;
    protected ArrayList<Paziente> whiteCode ;
    public Reparto()
    {
        this.medici = new ReentrantLock[10];
        for(int i=0;i<10;i++)
            this.medici[i] = new ReentrantLock();
        this.redCode = new ArrayList<>();
        this.yellowCode = new ArrayList<>();
        this.whiteCode = new ArrayList<>();
        this.mDisp=10;
        this.lock=new ReentrantLock();
        this.myTurnRed = lock.newCondition();
        this.myTurnWhite = lock.newCondition();
        this.myTurnYellow = lock.newCondition();

    }
    @Override
    public void run()
    {
        while(true)
        {
            System.out.println(redCode.size());
            if(redCode.size() > 0)
            {
                System.out.println("TEST");
                //System.out.println(redCode.size()+"test");
                if(mDisp==10)
                {
                    System.out.println("TEST");
                    try {
                        lock.lock();
                        if (redCode.size() > 0) {
                            System.out.println("SVEGLIA :" + redCode.get(0).name);
                            yourTurn(redCode.get(0));
                        }
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
                //redCode.remove(1);
            }
            else if(yellowCode.size() > 0)
            {
                if(mDisp > 0)
                {
                    lock.lock();
                    if(yellowCode.size() > 0)
                        yourTurn(yellowCode.get(0));
                    lock.unlock();
                }
                //yellowCode.remove(1);
            }
            else if(whiteCode.size()>0)
            {
                if(mDisp > 0)
                {
                    lock.lock();
                    if(whiteCode.size()>0)
                        yourTurn(whiteCode.get(0));
                    lock.unlock();
                }
                //whiteCode.remove(1);
            }

        }
    }
    protected void yourTurn(Paziente p)
    {
        //lock.lock();
        if(p.getCode().equals("red"))
        {
            myTurnRed.signal();
        }
        if(p.getCode().equals("yellow"))
        {
            myTurnYellow.signal();

        }
        if(p.getCode().equals("white"))
        {
            myTurnWhite.signal();
        }
        //lock.unlock();
    }
    protected void richiestaVisita(Paziente p)
    {
        lock.lock();
        if(p.getCode().equals("red"))
        {
            System.out.println(p.name +"richiesta visita");
            redCode.add(p);
        }
        if(p.getCode().equals("yellow"))
            yellowCode.add(p);
        if(p.getCode().equals("white"))
            whiteCode.add(p);
        lock.unlock();

    }
    // nWhite,nRed,nYellow are parameters of the main and are the respective number of patient with white, yellow and red code
    public static void main (String[] args)
    {
        int nWhite = Integer.parseInt(args[0]);
        int nYellow = Integer.parseInt(args[1]);
        int nRed = Integer.parseInt(args[2]);

        Reparto hosp = new Reparto();
        Thread rep = new Thread(hosp);
        rep.start();
        for(int i=0; i < (nRed+nYellow+nWhite); i++)
        {
            Paziente p =null;
            if(i < nRed)
            {
                p = new Paziente("Paziente "+i,"red",hosp);
            }
            if(nRed <= i && i < (nRed + nYellow))
            {
                p = new Paziente("Paziente "+i,"yellow",hosp);
            }
            if((nRed + nYellow) <= i && i < (nRed + nYellow + nWhite))
            {
                p = new Paziente("Paziente "+i,"white",hosp);
            }
            if(p!=null)
            {
                Thread paziente = new Thread(p);
                paziente.start();
                //System.out.println("paziente start");

            }
            else throw new NullPointerException("check again the arguments");

        }
    }
}
