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
		double j=i;
		while(abs(value - Math.PI) > accuracy && !Thread.currentThread().isInterrupted())
		{
			value += 4/(j*(i));
			i=(i+2);
			j=-1*j;
		}
		if(Thread.currentThread().isInterrupted())
			System.out.println("Worker interrotto");
	}
	public static void main(String[] args)
	{

		PICalc calc = new PICalc(Double.parseDouble(args[0]));
		Thread worker = new Thread(calc);
		worker.start();
		try
		{
			worker.join(Long.parseLong(args[1]));
			worker.interrupt();
			System.out.println("Il valore calcolo fino a quel momento è PI = " + calc.value);
		}
		catch (InterruptedException intEx)
		{
			intEx.printStackTrace();
			System.out.println("valore di calcolato fino all interruzione PI = " + calc.value);

		}
	}
}
