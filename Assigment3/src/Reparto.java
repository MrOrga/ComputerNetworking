import java.util.concurrent.locks.*;
import java.util.*;
public class Reparto implements Runnable
{
    boolean stop=false;
    protected boolean [] medici;
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
    this.medici = new boolean[10];
        for(int i=0;i<10;i++)
            this.medici[i] = true;

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
        try
        {
            while(!stop)
            {
                if(redCode.size() > 0)
                {
                    lock.lock();
                    try
                    {
                        if(mDisp==10)
                        {
                                if (redCode.size() > 0)
                                {
                                    //System.out.println("SVEGLIA :" + redCode.get(0).name);
                                    yourTurn(redCode.get(0));
                                }
                            }
                    }
                        finally
                        {
                            lock.unlock();
                        }
                }
                else if(yellowCode.size() > 0 && redCode.size()==0)
                {
                    lock.lock();
                    try {
                        if (mDisp > 0)
                        {
                            if (yellowCode.size() > 0)
                            {
                               int medFree=medYellowFree();
                               if(medFree >= 0)
                                    yourTurn(yellowCode.get(0));
                                else
                                {
                                    if(whiteCode.size() > 0)
                                    yourTurn(whiteCode.get(0));
                                }

                            }
                            else if(whiteCode.size() > 0)
                                {
                                    yourTurn(whiteCode.get(0));
                                }
                        }
                    }
                    finally{lock.unlock();}
                }
                else if(whiteCode.size()>0)
                {
                    lock.lock();
                    try {
                            if (mDisp > 0) {

                                if (whiteCode.size() > 0)
                                    yourTurn(whiteCode.get(0));
                            }
                        }
                        finally{lock.unlock();}
                }
                Thread.sleep(100);
            }

        }catch (InterruptedException ex){stop=true;}
    }
    private int medYellowFree()
    {
        for (int i=0;i<yellowCode.size();i++)
        {
            if(medici[yellowCode.get(i).getMedico()])
                return yellowCode.get(i).getMedico();
        }
        return -1;
    }
    protected void yourTurn(Paziente p)
    {
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
        {
            int medico = (int) (Math.random() * 10);
            p.setMedico(medico);
            yellowCode.add(p);
        }
        if(p.getCode().equals("white"))
            whiteCode.add(p);
        lock.unlock();

    }
    public boolean redCanGo()
    {
        for(int i=0;i < 10;i++)
            if(!medici[i])
                return false;
        return true;
    }
    public boolean yellowCanGo()
    {
        for(int i=0;i < 10;i++)
            if(!medici[i])
                return false;
        return true;
    }
    public boolean whiteCanGo()
    {
        for(int i=0;i < 10;i++)
            if(!medici[i])
                return false;
        return true;
    }
    // nWhite,nRed,nYellow are parameters of the main and are the respective number of patient with white, yellow and red code
    //Main class nWhite nYellow nRed
    public static void main (String[] args)
    {
        int nWhite = Integer.parseInt(args[0]);
        int nYellow = Integer.parseInt(args[1]);
        int nRed = Integer.parseInt(args[2]);
        ArrayList<Thread> pt =new ArrayList<>();
        Reparto hosp = new Reparto();
        Thread rep = new Thread(hosp);
        rep.start();
        for(int i=0; i < (nRed+nYellow+nWhite); i++)
        {
            Paziente p =null;
            int k=(int)(Math.random()*3);
            if(i < nRed)
            {
                p = new Paziente("Paziente "+i,"red",hosp,k);
            }
            if(nRed <= i && i < (nRed + nYellow))
            {
                p = new Paziente("Paziente "+i,"yellow",hosp,k);
            }
            if((nRed + nYellow) <= i && i < (nRed + nYellow + nWhite))
            {
                p = new Paziente("Paziente "+i,"white",hosp,k);
            }
            if(p!=null)
            {
                Thread paziente = new Thread(p);
                pt.add(paziente);
                paziente.start();
            }
            else throw new NullPointerException("check again the arguments");
        }
        try
        {
            for(int j=0;j < 10;j++)
            {
                if(pt.get(j).isAlive())
                    pt.get(j).join();
            }
        }catch (InterruptedException ex){ex.printStackTrace();}
        rep.interrupt();
    }
}
