import java.util.Arrays;
import java.util.Vector;

public class Sort
{
	
	
	private Vector<Integer> scores;
	private Vector<String> users;
	
	public Sort(Vector<Integer> scores, Vector<String> users)
	{
		if (scores == null || scores.size() == 0)
		{
			return;
		}
		this.users = users;
		this.scores = scores;
		
	}
	
	public void sort()
	{
		
	/*	if (arr == null || arr.size() == 0)
		{
			return;
		}
		users = users;
		scores = arr;*/
		quickSort(0, scores.size() - 1);
	}
	
	private void quickSort(int left, int right)
	{
		
		int i = left;
		int j = right;
		// find pivot number, we will take it as mid
		int pivot = scores.get(left + (right - left) / 2);
		
		while (i <= j)
		{
			/**
			 * In each iteration, we will increment left until we find element greater than pivot
			 * We will decrement right until we find element less than pivot
			 */
			while (scores.get(i) < pivot)
			{
				i++;
			}
			while (scores.get(j) > pivot)
			{
				j--;
			}
			if (i <= j)
			{
				exchange(i, j);
				//move index to next position on both sides
				i++;
				j--;
			}
		}
		// call quickSort() method recursively
		if (left < j)
			quickSort(left, j);
		if (i < right)
			quickSort(i, right);
	}
	
	public Vector<Integer> getScores()
	{
		return scores;
	}
	
	public Vector<String> getUsers()
	{
		return users;
	}
	
	private void exchange(int i, int j)
	{
		String tString = users.get(i);
		int temp = scores.get(i);
		
		users.set(i, users.get(j));
		scores.set(i, scores.get(j));
		
		users.set(j, tString);
		scores.set(j, temp);
	}
}
