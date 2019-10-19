import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LinkedListSync
{
     private LinkedList <String> list;
     private Lock lock;
     private Condition emptyQueue;
    LinkedListSync()
    {
        this.list = new LinkedList<>();
        this.lock = new ReentrantLock();
        emptyQueue = lock.newCondition();
    }
    public void addToQueue(String path)
    {
        lock.lock();
        try
        {
         list.add(path);
         emptyQueue.signal();
        }
        finally
        {
            lock.unlock();
        }
    }
    public String getFromQueue()
    {
        lock.lock();
        try
        {
            return list.removeFirst();
        }
        finally
        {
            lock.unlock();
        }
    }
    public String finished()
    {
        lock.lock();
        try
        {
            return list.getFirst();
        }
        finally
        {
            lock.unlock();
        }
    }
    public boolean isEmpty()
    {
        lock.lock();
        try
        {
            return list.isEmpty();
        }
        finally
        {
            lock.unlock();
        }
    }
    public void waitElements()
    {
        lock.lock();
        while (this.isEmpty())
        {
            try
            {
                emptyQueue.await();
            }
            catch (InterruptedException ex){ex.printStackTrace();}
        }
        lock.unlock();
    }
    public void wakeUpConsumers()
    {
        lock.lock();
        try
        {
            emptyQueue.signalAll();
        }finally{lock.unlock();}

    }



}
