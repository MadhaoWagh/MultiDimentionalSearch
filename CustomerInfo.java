import java.util.BitSet;


public class CustomerInfo 
{
	 public long UserID;
	 public BitSet Catagories;
	 public double Revenue;
	 public int purchaces;


CustomerInfo(long UID,BitSet Cat)
	{
		UserID=UID;
		Catagories=Cat;
		Revenue=0.0;
		purchaces=0;
	}
}
