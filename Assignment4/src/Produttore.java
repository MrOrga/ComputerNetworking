import java.io.File;

public class Produttore implements Runnable
{
    private String path;
    private LinkedListSync myQueue;
    Produttore(String path,LinkedListSync myQueue)
    {
        this.path = path;
        this.myQueue =myQueue;
    }
    @Override
    public void run()
    {
        myQueue.addToQueue(path);
        recursiveDir(path);
        //inserimento null alla fine della coda per chiusura consumatori
        myQueue.addToQueue(null);
        myQueue.wakeUpConsumers();


    }
    private void recursiveDir(String path)
    {
        File currDir = new File(path);
        if(currDir.isDirectory())
        {
            File [] files = currDir.listFiles();
            if (files != null)
            for(File file : files)
            {
                if(file.isDirectory())
                {
                    System.out.println("ADDING "+ file.getName()+" TO THE QUEUE");
                    myQueue.addToQueue(file.getAbsolutePath());
                    recursiveDir(file.getAbsolutePath());
                }
            }
        }
    }
}
