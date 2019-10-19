import java.io.File;

public class Consumatore implements Runnable
{
    LinkedListSync myQueue;
    boolean running =true;
    Consumatore(LinkedListSync myQueue)
    {
        this.myQueue = myQueue;
    }
    @Override
    public void run()
    {
        while(running)
        {
            myQueue.waitElements();

            if(myQueue.finished().equals("END"))
            {
                System.out.println(Thread.currentThread().getName()+" chiusura thread");
                running= false;
                return;
            }
            String path=myQueue.getFromQueue();
            File dir = new File(path);
            String[] files = dir.list();

            if (files == null)
            {
                System.out.println("Directory " + dir.getName() + " is empty");
            }
            else
                {
                    System.out.println("Directory " + dir.getName() + " contains " + files.length + " files or directory:");
                    for (String file : files)
                        System.out.println(file);
                }
        }
    }
}
