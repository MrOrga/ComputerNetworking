
public class Paziente implements Runnable
{
    private int k;
    protected String name;
    private String codeType;
    private Reparto hosp;
    private int medico;
    public Paziente(String name , String codeType ,Reparto hosp,int k)
    {
        this.name = name;
        this.codeType = codeType;
        this.hosp = hosp;
        this.k = k;
    }

    public void setMedico(int medico)
    {
        this.medico = medico;
    }

    public int getMedico()
    {
        return medico;
    }

    private void waitTurn()
    {
        hosp.lock.lock();
        try
        {
        if(codeType.equals("red"))
        {
            while(!hosp.redCanGo())
            {
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

        try
        {
            if (this.getCode().equals("red"))
            {
                for (int i = 0; i < 10; i++)
                {
                    hosp.medici[i]=false;
                    hosp.mDisp--;
                }
                return 0;
            }
            if (this.getCode().equals("yellow"))
            {
                while(!hosp.medici[medico])
                {
                    try
                    {
                        hosp.myTurnYellow.await();
                    }catch (InterruptedException ex) {ex.printStackTrace();}
                }

                hosp.medici[medico]=false;
                hosp.mDisp--;
                return medico;

            }
            if (this.getCode().equals("white"))
            {
                int i;
                do {
                    for (i = 0; i < 10; i++)
                    {
                        if (hosp.medici[i])
                        {
                            hosp.medici[i] = false;
                            hosp.mDisp--;
                            return i;
                        }
                    }
                }while(i==10);
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
        int med=inizioVisita();
        simulateVisit();
        fineVisita(med);
        k--;
        if(k > 0)
        {
            int att=(int)(Math.random()*5)*1000;
            try
            {
                Thread.sleep(att);
            }
            catch (InterruptedException ex){ex.printStackTrace();}
            this.run();
        }
    }
}

