package com.massivecraft.factions.buildings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.massivecraft.factions.P;

public class BuildingFile {
	private String name; public String getName() {return name;} public void setName(String name) {this.name = name;}
	
	public short width,length, height;
	
	public short nullpoint;
	
	private int[][][][] blocks;
	
	public BuildingFile(){
		
	}
	
	public void setDimensions(short width,short height, short length){
		this.width=width;
		this.height=height;
		this.length=length;
		blocks = new int[2][width][height][length];
	}
	
	public int getBlock(int x,int y, int z, int rotate){
		if(rotate==180){
			x=(short) (this.width-1-x);
			z=(short) (this.length-1-z);
		}
		
		return this.blocks[0][x][y][z];
	}
	
	public int getSeeBlock(short X,short Y, short Z, int rotate){
		if(rotate==180){
			X=(short) (this.width-1-X);
			Z=(short) (this.length-1-Z);
		}
		
		if(X==this.width-1 || X==0 || Y==this.height-1 || Y==0 || Z==this.length-1 || Z==0 ){
			return this.blocks[0][X][Y][Z];
		}
		if(
			BuildingType.isIgnoredBlock((byte) this.blocks[0][X+1][Y][Z]) 
			||BuildingType.isIgnoredBlock((byte) this.blocks[0][X-1][Y][Z]) 
			||BuildingType.isIgnoredBlock((byte) this.blocks[0][X][Y+1][Z]) 
			||BuildingType.isIgnoredBlock((byte) this.blocks[0][X][Y-1][Z]) 
			||BuildingType.isIgnoredBlock((byte) this.blocks[0][X][Y][Z+1]) 
			||BuildingType.isIgnoredBlock((byte) this.blocks[0][X][Y][Z-1]) 
		)
		{
			return this.blocks[0][X][Y][Z];
		}
		
		
		return 0;
	}
	
	public int getBlockData(int X,int Y, int Z, int rotate){
		if(rotate==180){
			X=(short) (this.width-1-X);
			Z=(short) (this.length-1-Z);
		}
		
		return this.blocks[1][X][Y][Z];
	}
	
	@SuppressWarnings("unused")
	private byte BlockDataRotate(int blockid, byte blockdata){
		
		/*if(blockid)
		case : //Dispenser
			
		
		}
		
		
		*/
		return 0;
	}
	
	//Load and Create
	
	/*Disabled
	public static BuildingFile loadFromShematics(String path){
		BuildingFile bfile=new BuildingFile();
		
		File file = new File(path);
		CuboidClipboard clipboard;
		try {
			clipboard = CuboidClipboard.loadSchematic(file);
			
			bfile.height=(short)clipboard.getHeight();
			bfile.width=(short)clipboard.getWidth();
			bfile.length=(short)clipboard.getLength();
			bfile.setDimensions(bfile.width,bfile.height,bfile.length);
			
			for(int X=0;X<bfile.width;X++){
				for(int Y=0;Y<bfile.height;Y++){
					for(int Z=0;Z<bfile.length;Z++){
						//P.p.log(X+","+Y+","+Z+","+width+","+height+","+length);
						bfile.blocks[0][X][Y][Z] = clipboard.getPoint(new Vector(X,Y,Z)).getType();
						bfile.blocks[1][X][Y][Z] = clipboard.getPoint(new Vector(X,Y,Z)).getData();
					}
				}
			}
			
			return bfile;
			
		} catch (DataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}*/
	
	public static BuildingFile load(String path){
		BuildingFile bfile=new BuildingFile();
		
		FileInputStream filestream;
		try {
			filestream = new FileInputStream(path);
			
			DataInputStream stream=new DataInputStream(filestream);
			
			//Höhe Breite Länge
			bfile.width=stream.readShort();
			bfile.height=stream.readShort();
			bfile.length=stream.readShort();
			
			bfile.setDimensions(bfile.width,bfile.height,bfile.length);
			
			//Nullpoint
			bfile.nullpoint=stream.readShort();
			
			//Blöcke
			
			for (int x = 0; x < bfile.width; ++x) {
	            for (int y = 0; y < bfile.height; ++y) {
	                for (int z = 0; z < bfile.length; ++z) {
	                	bfile.blocks[0][x][y][z]=stream.readByte();
	                	bfile.blocks[1][x][y][z]=stream.readByte();
	                }
	            }
	        }
			
			stream.close();
	        filestream.close();
	        
	        return bfile;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        return null;
	}
	
	public static BuildingFile loadFromTwoBlocks(Block block1, Block block2){
		BuildingFile bfile=new BuildingFile();
		
		//Dimensions
		short width=(short)(Math.abs(block1.getX()-block2.getX())+1);
		short height=(short)(Math.abs(block1.getY()-block2.getY())+1);
		short length=(short)(Math.abs(block1.getZ()-block2.getZ())+1);
		
		bfile.setDimensions(width,height,length);
		
		//Blocks
		int pX,pY,pZ;
		
		if(block1.getX()>block2.getX()) pX=block2.getX();else pX=block1.getX();
		if(block1.getY()>block2.getY()) pY=block2.getY();else pY=block1.getY();
		if(block1.getZ()>block2.getZ()) pZ=block2.getZ();else pZ=block1.getZ();
		
		for(int x=pX;x<pX+width;x++){
			for(int y=pY;y<pY+height;y++){
				for(int z=pZ;z<pZ+length;z++){
					Location tmplocation=new Location(block1.getWorld(),x,y,z);
					
					bfile.blocks[0][x-pX][y-pY][z-pZ]=tmplocation.getBlock().getTypeId();
					bfile.blocks[1][x-pX][y-pY][z-pZ]=tmplocation.getBlock().getData();
				}
			}
		}
		
		return bfile;
	}
	
	//Save
	public void save(String path){
		FileOutputStream filestream;
		try {
			filestream = new FileOutputStream(path);
			DataOutputStream stream = new DataOutputStream(filestream);
			
			//Höhe Breite Länge
			stream.writeShort(this.width);stream.writeShort(this.height);stream.writeShort(this.length);
			
			//Nullpoint
			stream.writeShort(this.nullpoint);

	        for (int x = 0; x < this.width; x++) {
	            for (int y = 0; y < this.height; y++) {
	                for (int z = 0; z < this.length; z++) {
	                    stream.writeByte((byte) this.getBlock(x, y, z, 0));
	                    stream.writeByte((byte) this.getBlockData(x, y, z, 0));
	                }
	            }
	        }
	        
	        stream.close();
	        filestream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	
	public void paste(int gX,int gY,int gZ, World world){
		for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                for (int z = 0; z < this.length; ++z) {
                	int blocktype=this.blocks[0][x][y][z];
                	byte blockdata=(byte) this.blocks[1][x][y][z];
                	Block block = world.getBlockAt(gX+x,gY+y,gZ+z);
                	block.setTypeId(blocktype);
                	block.setData(blockdata);
                }
            }
		}
	}
	
}
