import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UfficioPostale implements Runnable
{
    private static final int nSportelli=4;
    private static final int timeout=10;
    private ThreadPoolExecutor sportelli;
    LinkedBlockingQueue salaAttesa;
    public UfficioPostale(int k)
    {
        sportelli = new ThreadPoolExecutor(nSportelli,nSportelli,timeout, TimeUnit.SECONDS,new LinkedBlockingQueue<>(k));
        salaAttesa= new LinkedBlockingQueue();
    }
    @Override
    public void run()
    {

    }
}
