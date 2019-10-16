
public class Paziente implements Runnable
{
    private int k;
    protected String name;
    private String codeType;
    private Reparto hosp;
    public Paziente(String name , String codeType ,Reparto hosp,int k)
    {
        this.name = name;
        this.codeType = codeType;
        this.hosp = hosp;
        this.k = k;
    }
    private void waitTurn()
    {

        try
        { hosp.lock.lock();
        if(codeType.equals("red"))
        {
            while(!hosp.redCanGo())
            {
                System.out.println("SONO IN ATTESA :"+name);
                hosp.myTurnRed.await();
            }
        }
        if(codeType.equals("yellow"))
        {
            while(hosp.redCode.size()>0)
                hosp.myTurnYellow.await();
        }
        if(codeType.equals("white"))
        {
            while(hosp.redCode.size()>0)
                hosp.myTurnWhite.await();
        }

        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }

    }
    public String getCode()
    {
        return this.codeType;
    }
    private int inizioVisita()
    {

        try {
            //hosp.lock.lock();
            System.out.println("lock presa"+name);
            if (this.getCode().equals("red"))
            {
                for (int i = 0; i < 10; i++)
                {
                    hosp.medici[i]=false;
                    //System.out.println("lock presa medico"+name);
                    hosp.mDisp--;
                }
                return 0;
            }
            if (this.getCode().equals("yellow"))
            {
                int i = (int) (Math.random() * 10);
                while(!hosp.medici[i])
                {
                    try
                    {
                        hosp.myTurnYellow.await();
                    }catch (InterruptedException ex) {ex.printStackTrace();}
                }

                hosp.medici[i]=false;
                hosp.mDisp--;
                return i;

            }
            if (this.getCode().equals("white"))
            {
                int i;
                for (i = 0; i < 10; i++)
                {
                    if (hosp.medici[i])
                    {
                        hosp.medici[i]=false;
                        hosp.mDisp--;
                        return i;
                    }
                }
                return i;
            }
        }
        finally
        {
            hosp.lock.unlock();
        }
        return -1;
    }
    private void fineVisita(int i)
    {

        try
        {
            hosp.lock.lock();
            if(this.getCode().equals("red"))
            {
                for(i=0;i < 10; i++)
                {
                    hosp.medici[i]=true;
                    hosp.mDisp++;
                }
                hosp.redCode.remove(this);
            }
            if(this.getCode().equals("yellow"))
            {
                hosp.medici[i]=true;
                hosp.mDisp++;
                hosp.yellowCode.remove(this);
            }
            if(this.getCode().equals("white"))
            {
                hosp.medici[i]=true;
                hosp.whiteCode.remove(this);
                hosp.mDisp++;
            }
        }
        finally
        {
            hosp.lock.unlock();
            //System.out.println(hosp.redCode.size());
        }
    }
    private void simulateVisit()
    {
        System.out.println(Thread.currentThread().getName() + ": " + name);
        try
        {
            Long duration = (long) (Math.random() * 10);
            System.out.println(Thread.currentThread().getName() + ": " + name + " is doing a visit during " + duration + " seconds.");
            Thread.sleep(duration * 1000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + ": " + name + " Visit Finished .");
    }
    @Override
    public void run()
    {
        hosp.richiestaVisita(this);
        waitTurn();
        System.out.println("inizio visita"+name);
        int medico=inizioVisita();
        simulateVisit();
        fineVisita(medico);
        System.out.println(hosp.mDisp);
        System.out.println(hosp.redCode.size());
        System.out.println(hosp.yellowCode.size());
        System.out.println(hosp.whiteCode.size());
        k--;
        if(k > 0)
        {
            this.run();
        }
    }
}

