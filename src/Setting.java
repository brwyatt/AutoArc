public class Setting implements Comparable<Setting>{
	private String n;
	private String v;
	
	public Setting(String name,String value){
		n=name;
		v=value;
	}
	public String getName(){
		return n;
	}
	public String getValue(){
		return v;
	}
	public void setValue(String value){
		v=value;
	}
	public int compareTo(Setting S){
		return getName().compareTo(S.getName());
	}
	public String toString(){
		return getValue();
	}
}