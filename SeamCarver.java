//Philip Vasseur
//June 9th, 2016
//Seam Carver, a content aware iamge resizing technique. Reduces the image in size by a pixel of height or width
//based on the 'energy' of the pixel, it basically finds the lowest energy path from top to bottom or side to side
//and removes that, lowest energy path basically being the path of pixels that will have the least effect on the 
//photo. Meant to only remove useless background stuff and keep main parts of the image intact.
import java.awt.Color;

public class SeamCarver {
	private int height,width;
	private Picture picture;
	private Color[][] colorArray;
	
	//Sets the picture, height, and width and then creates a colorArray with the colors of every pixel.
	public SeamCarver(Picture picture){
		this.picture = picture;
		height=this.picture.height();
		width=this.picture.width();
		colorArray = new Color[width()][height()];
		for(int i=0; i<width();i++){
			for(int j=0; j<height();j++){
				colorArray[i][j]=picture().get(i, j);
			}
		}
	}
	
	//Used after removing a seam, makes it so the currently set main picture is then set for the height and such.
	//Basically just does the constructor over again as I didn't know how to do that off the top of my head
	private void reset(){
		height=picture.height();
		width=picture.width();
		colorArray = new Color[width()][height()];
		for(int i=0; i<width();i++){
			for(int j=0; j<height();j++){
				colorArray[i][j]=picture().get(i, j);
			}
		}
	}
	
	public Picture picture(){
		return picture;
	}
	public int width(){
		return width;
	}
	public int height(){
		return height;
	}
	
	//Simply does exactly what instructions said to to calculate the energy of any pixel
	public double energy(int x, int y){
		if(x>=0&&x<width() && y>=0&&y<height()){
			//checks if it's on an edge and makes proper adjustments
			int leftX=x-1,rightX=x+1, topY=y-1,bottomY=y+1;
			if(leftX<0)
				leftX=width()-1;
			if(rightX>=width())
				rightX=0;
			if(topY<0)
				topY=height()-1;
			if(bottomY>=height())
				bottomY=0;
			
			//this will calculate the X differences
			int redX = colorArray[leftX][y].getRed()-colorArray[rightX][y].getRed();
			int greenX = colorArray[leftX][y].getGreen()-colorArray[rightX][y].getGreen();
			int blueX = colorArray[leftX][y].getBlue()-colorArray[rightX][y].getBlue();
			int xDiff = redX*redX + greenX*greenX + blueX*blueX;
			//this will calculate the Y differences
			int redY = colorArray[x][bottomY].getRed()-colorArray[x][topY].getRed();
			int greenY = colorArray[x][bottomY].getGreen()-colorArray[x][topY].getGreen();
			int blueY = colorArray[x][bottomY].getBlue()-colorArray[x][topY].getBlue();
			int yDiff = redY*redY + greenY*greenY + blueY*blueY;
			//returns the calculated energy
			return Math.sqrt(xDiff + yDiff);
		}else{
			throw new IndexOutOfBoundsException();
		}
		
	}
	
	//Rotates the image 90 degrees and then runs the findVerticalSeam to and then returns an int[] which
	//represents the y coordinate in each column of the pixel to be removed
	public int[] findHorizontalSeam(){
		Color[][] tempColorArray = colorArray;
		int temp = width();
		width=height();
		height=temp;
		colorArray = new Color[width()][height()];
		for(int i=0; i<height();i++){
			for(int j=0; j<width();j++){
				//System.out.println((tempHeight-1-i)+","+j);
				colorArray[j][i]=picture().get(height()-1-i, j);
			}
		}
		int[] yPath = findVerticalSeam();
		int[] tempYPath=yPath.clone();
		for(int i=0;i<yPath.length;i++){
			yPath[i]=tempYPath[yPath.length-1-i];
		}
		colorArray = tempColorArray;
		height=width();
		width=temp;
		return yPath;
		
	}
	
	//Creates an 'edge' between every node and the 3 below it with weights as the energy of that node and it uses
	//new imaginary nodes at the top and bottom which are connected to all the top row and all bottom row respectively
	//Then it uses basically uses dijkstras to find the shortest path between very top and very bottom node and records 
	//the x position of each pixel to create an int[] showing where in each row the pixel is that should be removed.
	//It traces back with a parallel array called EdgeFrom to find the shortest path.
	public int[] findVerticalSeam(){
		int n=width()*height()+2;
		double[] weightValues = new double[n];
		int[] edgeFrom = new int[n];
		weightValues[0]=0;
		for(int i=1;i<n;i++){
			weightValues[i]=Double.MAX_VALUE;
		}
		for(int i=1;i<=width();i++){ //Sets all the top row to come from the imaginary node 0.
			weightValues[i]=energy(i-1,0);
			edgeFrom[i]=0;
		}
		int x=0,y=0;
		for(int i=1;i<n;i++){ //In all the following, it checks for whatever will be the lowest sum of weights
							  //Works by setting temp energy to be equal to the lower node + current nodes energy
							  //And then checks if current energy to get to that node is higher, if so then energy
							  //to get there becomes the temp energy. Must understand weightValues is weights to GET TO
			if(y<height()-1){
				if(x!=0){ //Checks the weights of lower left
					double newLeftEnergy = weightValues[i]+energy(x-1,y+1);
					if(newLeftEnergy<weightValues[i+width()-1]){
						weightValues[i+width()-1]=newLeftEnergy;
						edgeFrom[i+width()-1]=i;
					}
				}//Checks the weights of directly below
				double newBotEnergy = weightValues[i]+energy(x,y+1);
				if(newBotEnergy<weightValues[i+width()]){
					weightValues[i+width()]=newBotEnergy;
					edgeFrom[i+width()]=i;
				}
				if(x!=width()-1){//Checks the weights of lower right
					double newRightEnergy = weightValues[i]+energy(x+1,y+1);
					if(newRightEnergy<weightValues[i+width()+1]){
						weightValues[i+width()+1]=newRightEnergy;
						edgeFrom[i+width()+1]=i;
					}
				}
			}
			x++;
			if(x>=width()){
				x=0;
				y++;
			}
		}
		for(int i=n-1-width();i<n-1;i++){ //checks which of vertices on the bottom row is the smallest and sets the imaginary
			if(weightValues[i]<weightValues[n-1]){ //bottom node to be from that vertex
				weightValues[n-1]=weightValues[i];
				edgeFrom[n-1]=i;
			}
		}
		
		int [] xPath = new int[height()];
		int lastChecked=n-1;
		for(int i=0;i<height();i++){ //Looks back from the bottom node to find what the lowest weight path is
			lastChecked=edgeFrom[lastChecked];
			xPath[height()-1-i]=lastChecked;
		}
		for(int i=0;i<xPath.length;i++){ //converts it from a format of index of total array to the X coordinate
			int num=xPath[i]-1;
			while(num>=width()){
				num-=width();
			}
			xPath[i]=num;
		}
		return xPath;
	}
	
	//Creates a new picture with one less height, and then skips over every pixel which was removed
	//which is just 1 per column, and then sets the colors of this new pic, and sets the current picture as the new pic
	public void removeHorizontalSeam(int[] seam){
		if(seam==null){
			throw new NullPointerException();
		}else if(seam.length!=width() || seam.length==1){
			throw new IllegalArgumentException();
		}else{
			Picture newPic = new Picture(width(),height()-1);
			for(int i=0;i<width();i++){
				int w=0;
				int badPixel = seam[i];
				for(int j=0;j<height()-1;j++){
					Color cPixelColor;
					if(j==badPixel){
						w++;
					}
					cPixelColor=colorArray[i][w];
					newPic.set(i, j, cPixelColor);
					w++;
				}
			}
			picture = newPic;
			reset();
		}
		
	}
	
	//Basically just creates a new picture with one less width, and then skips over every pixel which was removed
	//which is just 1 per row, and then sets the colors of this new pic, and sets the current picture as the new pic
	public void removeVerticalSeam(int[] seam){
		if(seam==null){
			throw new NullPointerException();
		}else if(seam.length!=height() || seam.length==1){
			throw new IllegalArgumentException();
		}else{
			Picture newPic = new Picture(width()-1,height());
			for(int i=0;i<height();i++){
				int w=0;
				int badPixel = seam[i];
				for(int j=0;j<width()-1;j++){
					Color cPixelColor;
					if(j==badPixel){
						w++;
					}
					cPixelColor=colorArray[w][i];
					newPic.set(j, i, cPixelColor);
					w++;
				}
			}
			picture = newPic;
			reset();
		}
	}
	
	
	public void showPicture(){
		picture.show();
	}
	//Takes in the number of lines to remove testing the program. Shows the original first and then the final.
	
	public static void main(String[] args){
		int verticalLinesToRemove = Integer.parseInt(args[0]);
		int horizontalLinesToRemove = Integer.parseInt(args[1]);
		SeamCarver s = new SeamCarver(new Picture("/Users/Admin/Downloads/tester.png"));
		s.showPicture(); //shows the before picture
		for(int i=0;i<verticalLinesToRemove;i++){
			int[] vSeam = s.findVerticalSeam();
			s.removeVerticalSeam(vSeam);
		}
		for(int i=0;i<horizontalLinesToRemove;i++){
			int[] hSeam = s.findHorizontalSeam();
			s.removeHorizontalSeam(hSeam);
		}
		s.showPicture(); //shows the after picture

		
	}
}
