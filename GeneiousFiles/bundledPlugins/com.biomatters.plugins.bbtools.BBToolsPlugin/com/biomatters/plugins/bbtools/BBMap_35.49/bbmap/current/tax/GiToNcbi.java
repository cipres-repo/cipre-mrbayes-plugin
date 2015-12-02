package tax;

import java.util.ArrayList;
import java.util.Arrays;

import align2.Tools;
import fileIO.ByteFile;
import fileIO.ReadWrite;

/**
 * @author Brian Bushnell
 * @date Mar 10, 2015
 *
 */
public class GiToNcbi {
	
	public static void main(String[] args){
		ReadWrite.USE_UNPIGZ=true;
		ReadWrite.USE_PIGZ=true;
		ReadWrite.ZIPLEVEL=8;
		initialize(args[0]);
		if(args.length>2){//Write array
			test(args);
		}else if(args.length==2){//Write array
			ReadWrite.write(array, args[1], true);
		}
	}
	
	public static void test(String[] args){
		System.err.println(get(1000));
		System.err.println(get(10000));
		System.err.println(get(10001));
		System.err.println(get(10002));
		System.err.println(get(10003));
		System.err.println(get(10004));
		System.err.println(get(10005));
		System.err.println(get(100000));
		System.err.println(get(1000000));
		System.err.println(get(10000000));
		
		TaxTree tree=null;
		if(args.length>1){
			tree=new TaxTree(args[1], args[2]);
		}
		
		System.err.println("Strings:");
		int x;
		x=get("gi|18104025|emb|AJ427095.1| Ceratitis capitata centromeric or pericentromeric satellite DNA, clone 44");
		System.err.println(x);
		if(tree!=null){
			System.err.println(tree.getNode(x));
			tree.incrementRaw(x, 30);
		}
		x=get("gi|15982920|gb|AY057568.1| Arabidopsis thaliana AT5g43500/MWF20_22 mRNA, complete cds");
		System.err.println(x);
		if(tree!=null){
			System.err.println(tree.getNode(x));
			tree.incrementRaw(x, 40);
		}
		x=get("gi|481043749|gb|KC494054.1| Plesiochorus cymbiformis isolate ST05-58 internal transcribed spacer 2, partial sequence");
		System.err.println(x);
		if(tree!=null){
			System.err.println(tree.getNode(x));
			tree.incrementRaw(x, 20);
		}
		
		if(tree!=null){
			tree.percolateUp();
			ArrayList<TaxNode> nodes=tree.gatherNodesAtLeastLimit(35);
			for(TaxNode n : nodes){
				System.err.println(n);
			}
		}
	}
	
	public static int get(String s){
		assert(s!=null && s.length()>3);
		final int initial=s.indexOf('|');
		assert(initial>1);
		boolean giMode=s.startsWith("gi|");
		assert(giMode || s.startsWith("ncbi|"));
		
		int number=0;
		for(int i=initial+1; i<s.length(); i++){
			char c=s.charAt(i);
			if(c=='|'){break;}
			assert(Character.isDigit(c));
			number=(number*10)+(c-'0');
		}
		if(giMode){return array[number];}
		return number;
	}
	
	public static int getCarrot(String s){
		assert(s!=null && s.length()>3);
		final int initial=s.indexOf('|');
		assert(initial>1);
		boolean giMode=s.startsWith(">gi|");
		assert(giMode || s.startsWith(">ncbi|"));
		
		int number=0;
		for(int i=initial+1; i<s.length(); i++){
			char c=s.charAt(i);
			if(c=='|'){break;}
			assert(Character.isDigit(c));
			number=(number*10)+(c-'0');
		}
		if(giMode){return array[number];}
		return number;
	}
	
	public static int get(byte[] s){
		assert(s!=null && s.length>3);
		final int initial=Tools.indexOf(s, (byte)'|');
		assert(initial>1);
		boolean giMode=(s[0]=='g' && s[1]=='i' && s[2]=='|');
		assert(giMode || (s[0]=='n' && s[1]=='c' && s[2]=='b'));
		
		int number=0;
		for(int i=initial+1; i<s.length; i++){
			byte c=s[i];
			if(c=='|'){break;}
			assert(Character.isDigit(c));
			number=(number*10)+(c-'0');
		}
		if(giMode){return array[number];}
		return number;
	}
	
	public static int getCarrot(byte[] s){
		assert(s!=null && s.length>3);
		final int initial=Tools.indexOf(s, (byte)'|');
		assert(initial>1);
		boolean giMode=(s[0]=='>' && s[1]=='g' && s[2]=='i' && s[3]=='|');
		assert(giMode || (s[0]=='>' && s[1]=='n' && s[2]=='c' && s[3]=='b'));
		
		int number=0;
		for(int i=initial+1; i<s.length; i++){
			byte c=s[i];
			if(c=='|'){break;}
			assert(Character.isDigit(c));
			number=(number*10)+(c-'0');
		}
		if(giMode){return array[number];}
		return number;
	}
	
	public static int get(int gi){
		assert(gi>=0) : gi;
		assert(gi<array.length) : gi+", "+array.length;
		return array[gi];
	}
	
	public static void initialize(String fname){
		assert(fname!=null);
		if(file==null || !file.equals(fname)){
			synchronized(GiToNcbi.class){
				if(file==null || !file.equals(fname)){
					file=fname;
					if(fname.contains(".int1d")){
						array=ReadWrite.read(int[].class, fname, true);
					}else{
						array=makeArray(fname);
					}
				}
				initialized=true;
			}
		}
	}
	
	public static boolean isInitialized(){return initialized;}
	
	public static synchronized void unload(){
		array=null;
		file=null;
		initialized=false;
	}
	
	private static int[] makeArray(String fname){
		ByteFile bf=ByteFile.makeByteFile(fname, false, true);
		long count=0, max=0;
		byte[] line=bf.nextLine();
		while(line!=null){
			count++;
			int tab=Tools.indexOf(line, (byte)'\t');
			long gi=Tools.parseLong(line, 0, tab);
			max=Tools.max(max, gi);
			line=bf.nextLine();
		}
		assert(max<Integer.MAX_VALUE) : "Overflow.";
		int[] ret=new int[(int)max+1];
		Arrays.fill(ret, -1);
//		bf.close();
//		bf=ByteFile.makeByteFile(fname, false, true);
		bf.reset();
		line=bf.nextLine();
		long count2=0;
		while(line!=null){
			count2++;
			int tab=Tools.indexOf(line, (byte)'\t');
			int gi=Tools.parseInt(line, 0, tab);
			int ncbi=Tools.parseInt(line, tab+1, line.length);
			ret[gi]=ncbi;
			line=bf.nextLine();
		}
		if(verbose){System.err.println("Count: "+count+", "+count2);}
		bf.close();
		return ret;
	}
	
	private static int[] array;
	private static String file;
	
	public static boolean verbose=false;
	private static boolean initialized=false;
}
