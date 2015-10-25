import java.io.File;
import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;


public class MultiDimSearch {
	HashMap<Long,CustomerInfo> UserHash=new HashMap<>();//hashmap based on user id 
	TreeSet<CustomerInfo> UserTree=new TreeSet<>(new cmp());// treeset ordered on Revenue of the CustomerInfo object
	// a hash map of catagories and treeset of its customers the treeset is ordered as per the total revenue of the customers
	HashMap<Integer,TreeSet<CustomerInfo>> CategoryHash=new HashMap<>(); 
	static int[] categories;
    static final int NUM_CATEGORIES = 1000, MOD_NUMBER = 997;
	static int DEBUG = 9;
    private int phase = 0;
    private long startTime, endTime, elapsedTime;
	
 /* comparator used to compare customer objects based on revenue i have built two comparators 
    as i found by testing that comparing in different styles for the two treemaps for the hash map for 
    top 3 and range gave better results in terms of running times
  */
      private static class cmp_cat implements Comparator<CustomerInfo> // this is used for the treemaps in the treemaps in CategoryHash hashmap
    {
    	public int compare(CustomerInfo c1, CustomerInfo c2) {
    	   	
    	   if(c1.UserID==c2.UserID)
    	   {
    		   return 0;
    	   }
    	   else
    	   {
    		  return Double.compare(c1.Revenue, c2.Revenue);
    	   }
    	   }

    }
    
    private static class cmp implements Comparator<CustomerInfo> // this is used in the treemap UserTree used in range operation
    {
    public int compare(CustomerInfo c1, CustomerInfo c2) {
       	
       	int result=Double.compare(c1.Revenue, c2.Revenue);
       	if(result==0)
       	{
       		if(c1.UserID>c2.UserID)
       		{
       			return 1;
       		}
       		else if(c1.UserID<c2.UserID)
       		{
       			return -1;
       		}
       		else
       		{
       			return 0;
       		}
       	}
       	return result;// compare revenues
    }
    }
    
    /*
     *  Inserts the data and makes changes to the Userhash does not touch other data structures as revenue is 0
     */
    
   int insert(Long id,int[] categories) 
	{
		if(UserHash.containsKey(id))
		{
			return -1;
		}
		else
		{
		BitSet cat_bits=new BitSet(1000);
		for(int i=0;i<categories.length;i++)
		{
			if(categories[i]==0)
			{
				break;
			}
			cat_bits.set(categories[i]);
		}
		CustomerInfo c =new CustomerInfo(id,cat_bits);
		//System.out.println(c.Catagories);
		UserHash.put(id, c);
		return 1;
		}
		
	}
	/*
	 * Finds element from the user hash
	 */
	int  find(Long id) 
	{
		if(UserHash.containsKey(id))
		{
			CustomerInfo c=UserHash.get(id);
			return (int) c.Revenue;
		}
		else
		{
			return -1;
		}
		
	}
	/*
	 *Deletes elements from all the data structures
	 */
	public  int delete(Long id)
	{
		if(UserHash.containsKey(id))
		{
			CustomerInfo c=UserHash.get(id);
			UserHash.remove(id);
			UserTree.remove(c);
			
			if(c.Revenue>0)// as we insert into the category map only when there is some revenue
			{
				int index= c.Catagories.nextSetBit(0);
				while(index!=-1)  // for all the categories that are set to 1 go to the corresponding tree map in the category hash and remove the element
				{
					categoryRemove(c, index);
					index=c.Catagories.nextSetBit(index+1);
				}
			}
			
			
			return (int)c.Revenue;
			
		}
		else
		{
			return -1;
		}
		
	}
	/*
	 * Finds the top three customers for a given category returns their sum
	 */
	int topthree(int cat) 
	{ 
		double res=0;
		int i=0;
		TreeSet<CustomerInfo> t=CategoryHash.get(cat);
		if(t!=null)
		{
		
		for (CustomerInfo c:t.descendingSet())// to order the treemap in decending order
		{
			if(i<3) // take top 3 and then break
			{
				res=res+c.Revenue;
				i++;
			}
			else
			{
				break;
			}
		}

		return (int)res;	
		}
		else
			return 0;
	}
	/* Adds all new interests to the customer object and returns count of new intrests that were added
	 *  although we don't really need to remove the objects from the tree map 
	 * as the amount is not changing for this operation we did it as a general practice 
	 */
	int addinterests(long id, int[] categories) 
	{ 
		int new_cat=0;
		if(UserHash.containsKey(id))
		{
			
			CustomerInfo c=UserHash.get(id);
			UserTree.remove(c);
			for(int i =0;i<categories.length;i++)
			{
				if(categories[i]!=0)
				{
					
					if(c.Catagories.get(categories[i])!=true)
					{
					c.Catagories.set(categories[i]);
					categoryAdd(c, categories[i]);
					new_cat++;
					}
				}
				else
				{
					break;
				}
				
				
			}
			UserTree.add(c);
			return new_cat;
		}
		else
		{
			return -1; 
		}
		
	}
	/*Removes interests from  the customer object  and returns the number if intrests left with the customer
	 * although we don't really need to remove the objects from the tree map as the amount is not changing for this operation 
	 * we did it as a general practice  
	 */	
	
	int removeinterests(long id, int[] categories)
	{ 
		if(UserHash.containsKey(id))
		{
			CustomerInfo c=UserHash.get(id);
			UserTree.remove(c);
			for(int i =0;i<categories.length;i++)
			{
				if(categories[i]!=0)
				{
					if(c.Catagories.get(categories[i]))
					{
						
						if(c.Revenue>0)
						{
							categoryRemove(c,categories[i]);
						}
					}
				}
				else
				{
					break;
				}
				
			}
			
			for(int i =0;i<categories.length;i++)
			{
				if(categories[i]!=0)
				{
					if(c.Catagories.get(categories[i]))
					{
						c.Catagories.set(categories[i],false);
					}
				}
				else
				{
					break;
				}
				
			}
			
			
			UserTree.add(c);
			return c.Catagories.cardinality();
		}
		else
		{
			return -1; 
		}
		 
	}
	/*
	 * Helper method that removes the given customer object from the category hash for the given category
	 */
	void categoryRemove(CustomerInfo c,int i) 
	{
		
			TreeSet<CustomerInfo> t=CategoryHash.get(i);
			if(t!=null)
			{
				if(t.contains(c))
				{
				t.remove(c);
				}
			}
		
	}
	/*
	 * Helper method that adds the given customer object to the category hash for the given category
	 * if there is no entry in the Category hash for a given category, it creates a new treemap and adds it to the category hash
	 */
	 void categoryAdd(CustomerInfo c,int i)  
	{
		
		if(CategoryHash.containsKey(i))
		{
			TreeSet<CustomerInfo> t=CategoryHash.get(i);
			t.add(c);

		}
		else
		{
			TreeSet<CustomerInfo> t=new TreeSet<>(new cmp_cat());
			t.add(c);
			CategoryHash.put(i, t);
		}
	}
/* This method adds revenue to the customer object it also removes and adds back the customer objects from the treemaps as they are based 
 * based on the revenue field it returns the total revenue of the customer 
 */
	int addrevenue(long id, double purchase) 
	{ 
		
		if(UserHash.containsKey(id))
		{
			
			CustomerInfo c=UserHash.get(id);
			
			int index= c.Catagories.nextSetBit(0);
			while(index!=-1)
			{
				categoryRemove(c, index);
				index=c.Catagories.nextSetBit(index+1);
			}
			UserTree.remove(c);
			c.Revenue=c.Revenue+purchase;
			c.purchaces++;
			
			index= c.Catagories.nextSetBit(0);
			while(index!=-1)
			{
				categoryAdd(c, index);
				index=c.Catagories.nextSetBit(index+1);
			}
			UserTree.add(c);
			
			return (int) c.Revenue;
			
		}
		else
		{
			return -1;
		}
		
		
	}
/*This method returns the number of customers in the given range it uses the UserTree Treemap for this operation
 * 
 */
	int range(double low, double high) 
	{ 
		BitSet temp=new BitSet();
		CustomerInfo c1=new CustomerInfo((long) -1,temp );
		CustomerInfo c2=new CustomerInfo((long) -2,temp );
		c1.Revenue=low;
		c2.Revenue=high+0.001;
		SortedSet<CustomerInfo> range=UserTree.subSet(c1,true, c2,true);
		
		return range.size();
		}
	int numberpurchases(long id)
	{ 
		if(UserHash.containsKey(id))
		{
			CustomerInfo c=UserHash.get(id);
			return c.purchaces;
		}
		else
		{
			return -1;
		}
		
	}
	
	/*
	 * This operation finds all the customers that have the same set of interests as itself , it will only consider the users who have more 
	 * than 5 interests
	 */
	int samesame()
	{ // build a reverse hash of category bitset  and user counts for all category combinations that have 5 or more categories 
		HashMap<BitSet,Long> revhash=new HashMap<>();
		Long val=(long)0;
		for(Long k:UserHash.keySet())
		{
			BitSet b=UserHash.get(k).Catagories;
			if(b.cardinality()>=5)
				if(revhash.containsKey(b))
				{
					val=revhash.get(b)+1;
					revhash.put(b, val);
				}
				else
				revhash.put(b, (long)1);
		}
		int ans=0;// loop this reverse hashmap and sum the number of users to get the answer for same same 
		for(Long bb:revhash.values())
		{
			if(bb>1) // ignore the category groups that have only one user as he will not be same with anyone
			ans=(int) (ans+bb);
		}
		
		return ans;
		
	
	}
	
	
    public static void main(String[] args)  throws FileNotFoundException {
	categories = new int[NUM_CATEGORIES];
	Scanner in;
	if(args.length > 0) {
	    in = new Scanner(new File(args[0]));
        } else {
	    in = new Scanner(System.in);
	}
	MultiDimSearch x = new MultiDimSearch();
	x.timer();
	long rv = x.driver(in);
	System.out.println(rv);
	x.timer();
    }

    /** Read categories from in until a 0 appears.
     *  Values are copied into static array categories.  Zero marks end.
     * @param in : Scanner from which inputs are read
     * @return : Number of categories scanned
     */
    public static int readCategories(Scanner in) {
	int cat = in.nextInt();
	int index = 0;
	while(cat != 0) {
	    categories[index++] = cat;
	    cat = in.nextInt();
	}
	categories[index] = 0;
	return index;
    }

    public long driver(Scanner in) {
      String s;
      long rv = 0, id;
      int cat;
      double purchase;

      while(in.hasNext()) {
	  s = in.next();
	  if(s.charAt(0) == '#') {
	      s = in.nextLine();
	      continue;
	  }
	  if(s.equals("Insert")) {
	      id = in.nextLong();
	      readCategories(in);
	      rv += insert(id, categories);
	  } else if(s.equals("Find")) {
	      id = in.nextLong();
	      rv += find(id);
	  } else if(s.equals("Delete")) {
	      id = in.nextLong();
	      rv += delete(id);
	  } else if(s.equals("TopThree")) {
	      cat = in.nextInt();
	      rv += topthree(cat);
	  } else if(s.equals("AddInterests")) {
	      id = in.nextLong();
	      readCategories(in);
	      rv += addinterests(id, categories);
	  } else if(s.equals("RemoveInterests")) {
	      id = in.nextLong();
	      readCategories(in);
	      rv += removeinterests(id, categories);
	  } else if(s.equals("AddRevenue")) {
	      id = in.nextLong();
	      purchase = in.nextDouble();
	      rv += addrevenue(id, purchase);
	  } else if(s.equals("Range")) {
	      double low = in.nextDouble();
	      double high = in.nextDouble();
	      rv += range(low, high);
	      //rv=8422233;
	  } else if(s.equals("SameSame")) {
	      rv += samesame();
	  } else if(s.equals("NumberPurchases")) {
	      id = in.nextLong();
	      rv += numberpurchases(id);
	  } else if(s.equals("End")) {
	      return rv % 997;
		  //return rv;
	  } else {
	      System.out.println("Houston, we have a problem.\nUnexpected line in input: "+ s);
	      System.exit(0);
	  }
      }
      // This can be inside the loop, if overflow is a problem
      rv = rv % MOD_NUMBER;

      return rv;
    }

    public void timer()
    {
        if(phase == 0) {
	    startTime = System.currentTimeMillis();
	    phase = 1;
	} else {
	    endTime = System.currentTimeMillis();
            elapsedTime = endTime-startTime;
            System.out.println("Time: " + elapsedTime + " msec.");
            memory();
            phase = 0;
        }
    }

    public void memory() {
        long memAvailable = Runtime.getRuntime().totalMemory();
        long memUsed = memAvailable - Runtime.getRuntime().freeMemory();
        System.out.println("Memory: " + memUsed/1000000 + " MB / " + memAvailable/1000000 + " MB.");
    }

}
