package createmodel;

//将锁于get分离
public class single {
	private static single sing;
	private single(){}
	private static void synInit(){
		synchronized (single.class) {
			sing=new single();
		}
	}
	public static single getSingle(){
		if(sing==null){
			synInit();
		}
		return sing;
	}
	
}
