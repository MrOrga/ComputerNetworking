/*
*@author Daniele Orgallo 501516

 */
import java.util.concurrent.*;
import static java.lang.Math.abs;

public class PICalc implements Runnable
{
    double accuracy;
    double value=0;

    PICalc(double accuracy)
    {
        this.accuracy = accuracy;
    }

    @Override
    public void run()
    {
        double i=1;
        while(abs(value - Math.PI) > accuracy)
        {
            value += 4/i;
            i=-(abs(i)+2);
        }

    }

    public static void main(String[] args)
    {
        PICalc calc = new PICalc(Double.parseDouble(args[0]));
        Thread worker = new Thread(calc);
        worker.start();
        try
        {
            Thread.sleep(Long.parseLong(args[1]));
            worker.interrupt();
            System.out.println("valore di calcolato fino all interruzione PI = " + calc.value);
        }
        catch (InterruptedException intex)
        {
            
        }
    }
}