public class Node {
  
    private Node leftchild, rightchild;
    private String key;
    private int id;

    Node(String key) {
    	this.key = key;
    }
    
    public int getID() {
    	return id;
    }
    
    public String getKey() {
		return key;
    }

    public void setKey(String key) {
		this.key = key;
    }
    
    public boolean toBool() {
    	if (key.equals("TRUE"))
    		return true;
    	return false;
	}	
}
