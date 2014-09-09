package com.hexairbot.hexmini.ipc.view;

public class MediaFile implements Comparable{
    public int type = -1;
    public String name = null;
    public long modifyDate;
    public boolean isRemote = false;
    public boolean isDownloaded = false;
    public long id = -1;
    public String localPath;
    public String remotePath;
    public long size = 0;
	
    public MediaFile(int type,String name,int size){
        this.type = type;
        this.name = name;
        this.size = size;
//        this.modifyDate = modifyDate;
    }
    
    public MediaFile(long id,int type,String name,String path,long size,boolean isRemote) {
	this.id = id;
	this.type = type;
	this.name = name;
//	this.modifyDate = modifyDate;
	this.localPath = path;
	this.size = size;
	this.isRemote = isRemote;
    }
    
    public boolean differentTo(MediaFile mediafile) {
	if(!name.equals(mediafile.name) ||id != mediafile.id ||
		modifyDate != mediafile.modifyDate || size != mediafile.size) {
	    return true;
	}
	return false;
    }

	@Override
	public int compareTo(Object another) {
		// TODO Auto-generated method stub
		
		MediaFile m = (MediaFile) another; 
		String this_name = this.name.substring(0, this.name.length() - 4);
		String another_name = m.name.substring(0, m.name.length() - 4);
		
	    return another_name.compareTo(this_name);
    }        
		
}
