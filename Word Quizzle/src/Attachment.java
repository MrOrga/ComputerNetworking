public class Attachment
{
	private String res;
	private int len;
	private int left;
	
	public Attachment(String res, int len, int left)
	{
		this.res = res;
		this.len = len;
		this.left = left;
	}
	
	public String getRes()
	{
		return res;
	}
	
	public void setRes(String res)
	{
		this.res = res;
	}
	
	public int getLen()
	{
		return len;
	}
	
	public void setLen(int len)
	{
		this.len = len;
	}
	
	public int getLeft()
	{
		return left;
	}
	
	public void setLeft(int left)
	{
		this.left = left;
	}
}
