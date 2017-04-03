import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

class Tree{
	int data[][], left_data[][], right_data[][], column[], info_gain, label;
	Tree left,right;
	Tree(int data[][], int column[], int label){
		this.data = data;
		this.column = column;
		this.label = label;
	}
}

public class ID3 {
	static String colnames[];
	static int number_nodes,leaf;
	static int prune_count, prune_factor;
	
	// Check features
	static boolean feature(int column[]){
		int i=0;
		while(i<column.length){
			if(column[i]==0)
				return true;
			i++;
		}
		return false;
	}
	
	static int f_class(int data[][]){
		int count0=0, count1=0;
		int i=0;
		while(i<data.length)
		{
			
			if(data[i][colnames.length-1]==0)
				count0++;
			else
				count1++;
			i++;
		}

		return count0>=count1?0:1;
	}
	
	//Feature selection
	static void select_feature(Tree node, String line){
		
		if(!feature(node.column))
		{
			node.label = f_class(node.data);
			leaf++;
			
			System.out.print(" "+node.label);
			return;
		}
		
		float count0=0.0f,count1=0.0f;
		
		
		for(int i=0;i<node.data.length;i++)
		{
			if(node.data[i][colnames.length-1]==0)
				count0++;
			else
				count1++;
		}
		
		
		if(new Float(count0).equals(0.0f))
		{
			node.label = 1;
			leaf++;
			
			System.out.print(" "+node.label);
			return;
		}
		
		if(new Float(count1).equals(0.0f))
		{
			node.label = 0;
			leaf++;
			
			System.out.print(" "+node.label);
			return;
		}
		
		count0/=(float)node.data.length;
		count1/=(float)node.data.length;
		
		float parent_entropy = (float) (-count0*(Math.log(count0)/Math.log(2))-count1*(Math.log(count1)/Math.log(2)));
		
		float max_infogain=Float.MIN_VALUE;
		int info_gain = -1;
		
		for(int i=0;i<node.column.length;i++)
		{
			if(node.column[i]==0){
				float temp_infogain = parent_entropy - entropy(node.data,i);
				if(temp_infogain > max_infogain){
					max_infogain = temp_infogain;
					info_gain = i;
				}else{
					if(temp_infogain == 0.0){
						max_infogain = temp_infogain;
						info_gain = i;
					}
				}
			}
		}
		
		if(info_gain==-1)
			return;
		
		node.column[info_gain]=1;
		node.info_gain = info_gain;
		

		
		int p=-1,q=-1,newcount0=0,newcount1=0;
		for(int i=0;i<node.data.length;i++){
			if(node.data[i][info_gain]==0)
				newcount0++;
			else
				newcount1++;
		}
		
		if(newcount0 == 0)
			newcount0 = 1;
		if(newcount1 == 0)
			newcount1= 1;
		
		int data_new0[][]=new int[newcount0][colnames.length], data_new1[][]=new int[newcount1][colnames.length] ;
		for(int i=0;i<node.data.length;i++){
			if(node.data[i][info_gain]==0)
				data_new0[++p] = node.data[i];
			else
				data_new1[++q] = node.data[i];
		}
		
		int column_temp[] = Arrays.copyOf(node.column, node.column.length);
		
		Tree node_left = new Tree(data_new0,column_temp,-1);
		number_nodes++;
		Tree node_right = new Tree(data_new1,column_temp,-1);
		number_nodes++;
		node.left = node_left;
		node.right = node_right;
		
		node.left_data = data_new0;
		node.right_data = data_new1;
		
			System.out.print("\n"+line+" "+colnames[node.info_gain]+" = 0 : ");
			select_feature(node_left,line+" |");
		
			System.out.print("\n"+line+" "+colnames[node.info_gain]+" = 1 : ");
			select_feature(node_right,line+" |");
	
	}
	
	static float entropy(int data[][], int feature_no){
		int n=data.length;
		int class_index=colnames.length-1;
		int c_count0=0,c_count1=0,c_count2=0,c_count3=0,feature0_count=0,feature1_count=0;
		float entropy0=0.0f,entropy1=0.0f;
		
		for(int i=0;i<n;i++){
			if(data[i][feature_no]==0){
				feature0_count++;
				if(data[i][class_index]==0)
					c_count0++;
				else
					c_count1++;
			}else{
				feature1_count++;
				if(data[i][class_index]==0)
					c_count2++;
				else
					c_count3++;
			}
		}
		
		if(feature0_count==0)
			feature0_count = 1;
		
		float temp0=(float)c_count0/feature0_count;
		float temp1=(float)c_count1/feature0_count;
		
	
		if(temp0==0)
			temp0 = 1;
		if(temp1==0)
			temp1 = 1;
		
		entropy0=-(temp0*(float)(Math.log(temp0)/Math.log(2))) - (temp1*(float)(Math.log(temp1)/Math.log(2)));
		
		if(feature1_count==0)
			feature1_count = 1;
		
		temp0=(float)c_count2/feature1_count;
		temp1=(float)c_count3/feature1_count;
		
		//compensate for log(0)=NaN
		if(temp0==0)
			temp0=1;
		if(temp1==0)
			temp1=1;
				
		entropy1=-(temp0*(float)(Math.log(temp0)/Math.log(2))) - (temp1*(float)(Math.log(temp1)/Math.log(2)));
		
		return 	(feature0_count/(float)n)*entropy0 + (feature1_count/(float)n)*entropy1;
	}
	
	static int f_class(Tree node,int row[]){
		if(node.label!=-1){
			return node.label;
		}
		else{
			if(row[node.info_gain]==0){
				if(node.left!=null)
					return f_class(node.left,row);
				else{
					return node.label;
				}	
			}
			else{
				if(node.right!=null)
					return f_class(node.right,row);
				else{
					return node.label;
				}
			}
		}
		//return -1;
	}
	
	
	
	
	static void values_cal(Tree root,int data[][], String name){
		int miss_class_count=0;
		for(int i=0;i<data.length;i++){
			int label1=f_class(root,data[i]);
			if(label1!=data[i][colnames.length-1])
				miss_class_count++;
			
		}
		System.out.print("Accuracy of the model on the "+ name +" data = ");
		System.out.println((((data.length-miss_class_count)/(float)data.length)*100)+"%");
		
	}
	

	
	
	
	static int[][] read(String file) throws IOException{
		
		int data[][] = null, data_size=0; 
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
		    String line = br.readLine();
		    
		    boolean colflag=false; 
		    
		    //read the data size
		    while (line != null) {
		    	++data_size;
		        line = br.readLine();
		    }
		    br.close();
		    
		    //read the data
		    br = new BufferedReader(new FileReader(file));
		    line = br.readLine();
		    int i=0;
		    while (line != null) {
		    	if(!colflag){	
		    		colnames = line.split(",");
		    		data = new int[data_size-1][colnames.length];
		    		colflag=!colflag;
		    	}
		    	else{
		    		String temp[] = line.split(",");
		    		for(int j=0;j<colnames.length;j++)
		    			data[i][j] = Integer.parseInt(temp[j]);
		    		i++;
		    	}
		        line = br.readLine();
		    }
		}
		finally {
		    br.close();
		}
		
		System.out.println("Number of testing instances = "+data.length);
		System.out.println("Number of testing attributes = "+colnames.length);
		
		return data;
	}
	// Prune tree
	static void prune(Tree node){
		if(prune_count >= prune_factor)
			return;
		
		if(node.left!=null && node.right!=null){
			if(node.left.label!=-1 && node.right.label!=-1){
				node.left = null;
				node.right = null;
				node.label = prune_class(node);
				prune_count+=2;
			}else{
				if(node.left.label==-1)
					prune(node.left);
				if(node.left.label==-1)
					prune(node.right);
			}	
		}
		return;
	}
	// check prune class
	static int prune_class(Tree node){
		int data[][] = node.data;
		int class_index = colnames.length-1, feature0_count = 0, feature1_count = 0;
		
		for(int i=0; i<data.length; i++){
			if(data[i][class_index] == 0)
				feature0_count++;
			else
				feature1_count++;
		}
		return feature0_count >= feature1_count ? 0:1;
	}
	
	//Traverse Tree
	public static void traverse(Tree node, String line){
		if(node.label != -1){
			System.out.print(" "+node.label);
			number_nodes++;
			leaf++;
		}
		else{
			number_nodes++;
			System.out.print("\n" + line + colnames[node.info_gain] + " = 0 :");
			if(node.left != null)
				traverse(node.left, line+"| ");
			System.out.print("\n" + line + colnames[node.info_gain] + " = 1 :");
			if(node.right != null)
				traverse(node.right, line+"| ");
		}
	}
	
	public static void main(String[] args) throws IOException {
		
			int data[][] = null, data_size=0, column[] = null;
		
			
		String file=args[0];

		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
		    String line = br.readLine();
		    
		    boolean colflag=false; 
		    
		    //read the data size
		    while (line != null) {
		    	++data_size;
		        line = br.readLine();
		    }
		    br.close();
		    
		    //read the data
		    br = new BufferedReader(new FileReader(file));
		    line = br.readLine();
		    int i=0;
		    while (line != null) {
		    	if(!colflag){	
		    		colnames = line.split(",");
		    		data = new int[data_size-1][colnames.length];
		    		column = new int[colnames.length-1];	
		    		colflag=!colflag;
		    	}
		    	else{
		    		String temp[] = line.split(",");
		    		for(int j=0;j<colnames.length;j++)
		    			data[i][j] = Integer.parseInt(temp[j]);
		    		i++;
		    	}
		        line = br.readLine();
		    }
		}
		finally {
		    br.close();
		}
		

		Tree root = new Tree(data,column,-1);
		number_nodes++;
		
		String line="";
		select_feature(root,line);
		System.out.println("\n\nPre-Pruned Accuracy");
		System.out.println("                                         ");
		System.out.println("Number of training instances = " + data.length);
		System.out.println("Number of training attributes = " + colnames.length);
		System.out.println("Total number of nodes in the tree = " + number_nodes);
		System.out.println("Number of leaf nodes in the tree = " + leaf);
				
		values_cal(root, root.data, "training");
		
		String vfilename = args[1];
		
		//read validation data
		System.out.println();
		values_cal(root, read(vfilename), "validation");
		
		file=args[2];
		//read testing data
		System.out.println();
		values_cal(root, read(file),"testing");
		
		
		//Take prune factor as input
		prune_factor = (int)(Float.parseFloat(args[3]) * number_nodes);
		prune_factor = (prune_factor % 2) == 0 ? prune_factor : prune_factor+1;
		
		
		number_nodes = 0;
		leaf = 0;
		
		
		//Prune Tree using validation data
		while(prune_count < prune_factor){
			int past_prune_count = prune_count;
			prune(root);
			if(root.left == null && root.right == null)
				break;
			
			if(past_prune_count == prune_count)
				break;
			}
		
		traverse(root,"");
		
		

		
		
		
		//Post prune starts here
		System.out.println("\n\nPost-Pruned Accuracy");
		System.out.println("                                     ");
		System.out.println("Number of training instances = " + data.length);
		System.out.println("Number of training attributes = " + colnames.length);
		System.out.println("Total number of nodes in the tree = " + number_nodes);
		System.out.println("Number of leaf nodes in the tree = " + leaf);
		
		//Post pruned training accuracy
		values_cal(root, root.data,"training");
		
		System.out.println();
		
		//Post pruned validation accuracy
		values_cal(root, read(vfilename),"validation");
		
		System.out.println();
		
		//Post pruned testing accuracy
		values_cal(root, read(file),"testing");
		
	}
}
