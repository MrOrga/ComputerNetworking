
public class Paziente implements Runnable
{
    private String name;
    private String codeType;
    private Reparto hosp;
    public Paziente(String name , String codeType ,Reparto hosp)
    {
        this.name = name;
        this.codeType = codeType;
        this.hosp = hosp;
    }
    private void waitTurn()
    {
        hosp.lock.lock();
        try
        {
        if(codeType.equals("red"))
        {
            //while(hosp.mDisp < 10)
                hosp.myTurnRed.await();
        }
        if(codeType.equals("yellow"))
        {
            hosp.myTurnYellow.await();
        }
        if(codeType.equals("white"))
        {
            hosp.myTurnWhite.await();
        }

        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
        hosp.lock.unlock();

    }
    public String getCode()
    {
        return this.codeType;
    }
    private int inizioVisita()
    {
        hosp.lock.lock();
        try {
            if (this.getCode().equals("red")) {
                for (int i = 0; i < 10; i++) {
                    hosp.medici[i].lock();
                    hosp.mDisp--;
                }
                return 0;
            }
            if (this.getCode().equals("yellow")) {
                int i = (int) (Math.random() * 10);
                hosp.medici[i].lock();
                hosp.mDisp--;
                return i;

            }
            if (this.getCode().equals("white")) {
                int i;
                for (i = 0; i < 10; i++) {
                    if (hosp.medici[i].tryLock())
                        hosp.mDisp--;
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
        hosp.lock.lock();
        try
        {
            if(this.getCode().equals("red"))
            {
                for(i=0;i < 10; i++)
                {
                    hosp.medici[i].unlock();
                    hosp.mDisp++;
                }
                hosp.redCode.remove(this);
            }
            if(this.getCode().equals("yellow"))
            {
                hosp.medici[i].unlock();
                hosp.mDisp++;
                hosp.yellowCode.remove(this);
            }
            if(this.getCode().equals("white"))
            {
                hosp.medici[i].unlock();
                hosp.whiteCode.remove(this);
                hosp.mDisp++;
            }
        }
        finally
        {
            hosp.lock.unlock();
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
        int medico=inizioVisita();
        simulateVisit();
        fineVisita(medico);

    }
}

