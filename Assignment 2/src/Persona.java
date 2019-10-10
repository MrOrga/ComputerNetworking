public class Persona implements Runnable
{
    private String name;
    public Persona(String name)
    {
        this.name = name;
    }
    @Override
    public void run()
    {
        System.out.println(Thread.currentThread().getName() + ": Task " + name);
        try
        {
            Long duration = (long)(Math.random()*10);
            System.out.println(Thread.currentThread().getName() + ": Task " + name + " is doing a task during " + duration + " seconds") ;
            Thread.sleep(duration*1000);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + ": Task Finished " + name);
    }
}
