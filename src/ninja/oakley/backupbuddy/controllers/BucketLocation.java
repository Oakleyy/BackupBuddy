package ninja.oakley.backupbuddy.controllers;

import java.util.ArrayList;
import java.util.List;

public enum BucketLocation {
	US,
	EU,
	ASIA;
	
	public static List<String> getStrings(){
		List<String> rt = new ArrayList<>();

		for(BucketLocation clazz : BucketLocation.values()){
			rt.add(clazz.toString());
		}
		
		return rt;
	}
}
