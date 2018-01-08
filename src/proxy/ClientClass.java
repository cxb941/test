package proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/*
 * 这个是客户端类，在hibernate中，如果延迟加载被设置了，我们获取的对象只是代理对象，就是对应这个类的
 * 该类会通过jdk 的Proxy类的getInstance方法获取一个代理类，该代理类会自动帮你实现真实类的所有接口对应方法
 */
class ClientClass {
    public static void main(String[] args) {
    	 RealClassIfc realClass =(RealClassIfc) new ProxyClass(new RealClass()).bind();
    	 realClass.method1("动态代理的方法");
 		 System.out.println(realClass.getClass().getName());
	}
}