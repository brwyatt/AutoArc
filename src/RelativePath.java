import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;


public class RelativePath {
	public static String getRelativePath(URI from, URI to){
		String path="";
		ArrayList<String> f=new ArrayList<String>(Arrays.asList(from.getPath().split("/")));
		ArrayList<String> t=new ArrayList<String>(Arrays.asList(to.getPath().split("/")));
		
		int removed=0;
		while((0<f.size()&&0<t.size())&&f.get(0).equals(t.get(0))){
			f.remove(0);
			t.remove(0);
			removed++;
		}
		while(0<t.size()){
			path+="/"+t.remove(0);
		}
		if(f.size()>0){
			path=path.substring(1);
			if(removed>1){
				while(f.size()>0){
					path="../"+path;
					f.remove(0);
				}
			}
		}else{
			path=path.substring(1);
		}
		return path;
	}
}