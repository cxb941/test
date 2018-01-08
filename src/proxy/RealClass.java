package proxy;

public class RealClass implements RealClassIfc{
    public void method1(String myName){
        System.out.println(this.getClass().getName() + " method1Name:" + myName);
    }
}