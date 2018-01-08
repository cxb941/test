package cglib.demo;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.asm.Type;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.InterfaceMaker;

import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;
import net.sf.cglib.beans.BulkBean;
import net.sf.cglib.beans.ImmutableBean;
import net.sf.cglib.core.Converter;
import net.sf.cglib.core.KeyFactory;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.ConstructorDelegate;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import net.sf.cglib.reflect.MethodDelegate;
import net.sf.cglib.reflect.MulticastDelegate;
import net.sf.cglib.util.ParallelSorter;
import net.sf.cglib.util.StringSwitcher;
/*
 * 由于CGLIB的大部分类是直接对Java字节码进行操作，
 * 这样生成的类会在Java的永久堆中。
 * 如果动态代理操作过多，
 * 容易造成永久堆满，
 * 触发OutOfMemory异常。
 */
public class TestFixedValue {
	/*
	 * 替换全部方法
	 */
	public void enhancerTest(){
		Enhancer enhancer=new Enhancer();
		enhancer.setSuperclass(SampleClass.class);
		enhancer.setCallback(new FixedValue() {
			
			@Override
			public Object loadObject() throws Exception {
				// TODO Auto-generated method stub
				return "Hello cglib1";
			}
		});
		SampleClass proxy = (SampleClass) enhancer.create();
	    System.out.println(proxy.test(null)); //拦截test，输出Hello cglib
	    System.out.println(proxy.toString()); 
	    System.out.println(proxy.getClass());
//	    System.out.println(proxy.hashCode());
	}
	/*
	 * 过滤指定方法
	 * @throws Exception
	 */
	public void testCallbackFilter() throws Exception{
	    Enhancer enhancer = new Enhancer();
	    CallbackHelper callbackHelper = new CallbackHelper(SampleClass.class, new Class[0]) {
	        @Override
	        protected Object getCallback(Method method) {
	            if(true){
	                return new FixedValue() {
	                    @Override
	                    public Object loadObject() throws Exception {
	                    	System.out.println("hello cglib");
	                        return "Hello cglib";
	                    }
	                };
	            }else{
	                return NoOp.INSTANCE;//这个NoOp表示no operator，即什么操作也不做，代理类直接调用被代理的方法不进行拦截
	            }
	        }
	    };
	    enhancer.setSuperclass(SampleClass.class);
	    enhancer.setCallbackFilter(callbackHelper);
	    enhancer.setCallbacks(callbackHelper.getCallbacks());
	    SampleClass proxy = (SampleClass) enhancer.create();
	    Assert.assertEquals("Hello cglib", proxy.test(null));
	    Assert.assertEquals("Hello cglib",proxy.toString());
//	    System.out.println(proxy.hashCode());
	}
	/*
	 * 创建不可变bean
	 * @throws Exception
	 */
	public void testImmutableBean() throws Exception{
	    SampleBean bean = new SampleBean();
	    bean.setValue("Hello world");
	    SampleBean immutableBean = (SampleBean) ImmutableBean.create(bean); //创建不可变类
	    Assert.assertEquals("Hello world",immutableBean.getValue()); 
	    bean.setValue("Hello world, again"); //可以通过底层对象来进行修改
	    System.out.println(immutableBean.getValue());
	    Assert.assertEquals("Hello world, again", immutableBean.getValue());
	    immutableBean.setValue("Hello cglib"); //直接修改将throw exception
	}
	/**
	 * 运行时动态创建bean
	 * @throws Exception
	 */
	public void testBeanGenerator() throws Exception{
	    BeanGenerator beanGenerator = new BeanGenerator();
	    beanGenerator.addProperty("value",String.class);
	    Object myBean = beanGenerator.create();
	    Method setter = myBean.getClass().getMethod("setValue",String.class);
	    System.out.println(myBean.getClass().getName());
	    setter.invoke(myBean,"Hello cglib");

	    Method getter = myBean.getClass().getMethod("getValue");
	    Assert.assertEquals("Hello cglib",getter.invoke(myBean));
	}
	/**
	 * cglib提供的能够从一个bean复制到另一个bean中，
	 * 而且其还提供了一个转换器，用来在转换的时候对bean的属性进行操作。
	 * @throws Exception
	 */
	public void testBeanCopier() throws Exception{
	    BeanCopier copier = BeanCopier.create(SampleBean.class, OtherSampleBean.class, false);//设置为true，则使用converter
	    SampleBean myBean = new SampleBean();
	    myBean.setValue("Hello cglib");
	    OtherSampleBean otherBean = new OtherSampleBean();
	    copier.copy(myBean, otherBean, null); //设置为true，则传入converter指明怎么进行转换
	    System.out.println(otherBean.toString());
	}
	/**
	 * 相比于BeanCopier，
	 * BulkBean将copy的动作拆分为getPropertyValues和setPropertyValues两个方法，
	 * 允许自定义处理属性
	 * 
	 * 一般适用通过xml配置注入和注出的属性，运行时才确定处理的Source,Target类，只需要关注属性名即可
	 * @throws Exception
	 */
	public void testBulkBean() throws Exception{
	    BulkBean bulkBean = BulkBean.create(SampleBean.class,
	            new String[]{"getValue"},
	            new String[]{"setValue"},
	            new Class[]{String.class});
	    SampleBean bean = new SampleBean();
	    bean.setValue("Hello world");
	    Object[] propertyValues = bulkBean.getPropertyValues(bean);//参数列表
	    SampleBean bean2 = new SampleBean();
	    bulkBean.setPropertyValues(bean2,propertyValues);
	    System.out.println(bean2.toString());
	    
	}
	/*
	 * BeanMap类实现了java Map，将一个bean对象中的所有属性转换为一个String-to-Obejct的Java Map
	 * @throws Exception
	 */
	public void testBeanMap() throws Exception{
	    BeanGenerator generator = new BeanGenerator();
	    generator.addProperty("username",String.class);
	    generator.addProperty("password",String.class);
	    Object bean = generator.create();
	    Method setUserName = bean.getClass().getMethod("setUsername", String.class);
	    Method setPassword = bean.getClass().getMethod("setPassword", String.class);
	    setUserName.invoke(bean, "admin");
	    setPassword.invoke(bean,"password");
	    BeanMap map = BeanMap.create(bean);
	    for (String name : (Set<String>)map.keySet()) {
			System.out.println(name+":"+map.get(name));
		}
	}
	/*
	 * keyFactory类用来动态生成接口的实例
	 * @throws Exception
	 */
	public void testKeyFactory() throws Exception{
	    SampleKeyFactory keyFactory = (SampleKeyFactory) KeyFactory.create(SampleKeyFactory.class);
	    Object key = keyFactory.newInstance("foo", 42);
	    Object key1 = keyFactory.newInstance("foo", 42);
	    System.out.println(key.getClass());
	    System.out.println(key1);
	}
	/*
	 * 用来模拟一个String到int类型的Map类型。如果在Java7以后的版本中，类似一个switch语句。
	 * @throws Exception
	 */
	public void testStringSwitcher() throws Exception{
	    String[] strings = new String[]{"one", "two"};
	    int[] values = new int[]{10,20,30};
	    StringSwitcher stringSwitcher = StringSwitcher.create(strings,values,true);
	    System.out.println(stringSwitcher.intValue("one"));
	    System.out.println(stringSwitcher.intValue("two"));
	    System.out.println(stringSwitcher.intValue("three"));
	}
	/*
	 * 正如名字所言，Interface Maker用来创建一个新的Interface
	 * 上述的Interface Maker创建的接口中只含有一个方法，签名为double foo(int)。
	 * Interface Maker与上面介绍的其他类不同，它依赖ASM中的Type类型。由于接口仅仅只用做在编译时期进行类型检查，
	 * 因此在一个运行的应用中动态的创建接口没有什么作用。
	 * 但是InterfaceMaker可以用来自动生成代码，为以后的开发做准备。
	 * @throws Exception
	 */
	public void testInterfaceMarker() throws Exception{
	    Signature signature = new Signature("foo", Type.DOUBLE_TYPE, new Type[]{Type.INT_TYPE});
	    InterfaceMaker interfaceMaker = new InterfaceMaker();
	    interfaceMaker.add(signature, new Type[0]);
	    Class iface = interfaceMaker.create();
	    System.out.println(iface.getMethods()[0].getName());
	    System.out.println(iface.getMethods()[0].getName());
	    System.out.println(iface.getMethods()[0].getReturnType());
	}
	interface BeanDelegate{
	    String getValueFromDelegate();
	}
	class BeanDelegateClass implements BeanDelegate{

		@Override
		public String getValueFromDelegate() {
			// TODO Auto-generated method stub
			System.out.println("BeanDelegateClass");
			return "BeanDelegateClass";
		}
	}
	/*
	 * MethodDelegate主要用来对方法进行代理
	 * 1. 第二个参数为即将被代理的方法
	 * 2. 第一个参数必须是一个无参数构造的bean。因此MethodDelegate.create并不是你想象的那么有用
	 * 3. 第三个参数为只含有一个方法的接口。当这个接口中的方法被调用的时候，将会调用第一个参数所指向bean的第二个参数方法
	 * 缺点：
	 *1. 为每一个代理类创建了一个新的类，这样可能会占用大量的永久代堆内存
	 *2. 你不能代理需要参数的方法
	 *3. 如果你定义的接口中的方法需要参数，那么代理将不会工作，并且也不会抛出异常；
	 *如果你的接口中方法需要其他的返回类型，那么将抛出IllegalArgumentException
	 * @throws Exception
	 */
	public void testMethodDelegate()  throws Exception{
	    SampleBean bean = new SampleBean();
	    bean.setValue("Hello cglib");
	    BeanDelegate delegate = (BeanDelegate) MethodDelegate.create(bean,"getValue", BeanDelegate.class);
	    System.out.println(delegate.getValueFromDelegate());
	}
	interface DelegatationProvider {
	    void setValue(String value);
	}
	class SimpleMulticastBean implements DelegatationProvider {
	    private String value;
	    @Override
	    public void setValue(String value) {
	        this.value = value;
	    }

	    public String getValue() {
	        return value;
	    }
	}
	/*
	 * 
	 * 多重代理和方法代理差不多，都是将代理类方法的调用委托给被代理类。使用前提是需要一个接口，以及一个类实现了该接口
	 * 通过这种interface的继承关系，我们能够将接口上方法的调用分散给各个实现类上面去。
	 * 多重代理的缺点是接口只能含有一个方法，如果被代理的方法拥有返回值，
	 * 那么调用代理类的返回值为最后一个添加的被代理类的方法返回值
	 */
	public void testMulticastDelegate() throws Exception{
	    MulticastDelegate multicastDelegate = MulticastDelegate.create(DelegatationProvider.class);
	    SimpleMulticastBean first = new SimpleMulticastBean();
	    SimpleMulticastBean second = new SimpleMulticastBean();
	    multicastDelegate = multicastDelegate.add(first);
	    multicastDelegate  = multicastDelegate.add(second);

	    DelegatationProvider provider = (DelegatationProvider) multicastDelegate;
	    provider.setValue("Hello world");
	    System.out.println(first.getValue());
	    System.out.println(second.getValue());
	}
	interface SampleBeanConstructorDelegate{
	    Object newInstance(String name);
	}
	/*
	 *  对构造函数进行代理
	 */
	public void testConstructorDelegate() throws Exception{
	    SampleBeanConstructorDelegate constructorDelegate = (SampleBeanConstructorDelegate) ConstructorDelegate.create(
	            SampleBean.class, SampleBeanConstructorDelegate.class);
	    SampleBean bean = (SampleBean) constructorDelegate.newInstance("Hello world");
	    Assert.assertTrue(SampleBean.class.isAssignableFrom(bean.getClass()));
	    System.out.println(bean.getValue());
	}
	/*
	 * 能够对多个数组同时进行排序，目前实现的算法有归并排序和快速排序
	 */
	public void testParallelSorter() throws Exception{
	    Integer[][] value = {
	            {4, 3, 9, 0},
	            {2, 1, 6, 0},{2, 1, 6, 0}
	    };
	    ParallelSorter.create(value).mergeSort(0);
	    for(Integer[] row : value){
	        for(int val : row){
	            System.out.println(val);
	        }
	    }
	}
	/*
	 * 顾明思义，FastClass就是对Class对象进行特定的处理，
	 * 比如通过数组保存method引用，因此FastClass引出了一个index下标的新概念，
	 * 比如getIndex(String name, Class[] parameterTypes)就是以前的获取method的方法。
	 * 通过数组存储method,constructor等class信息，从而将原先的反射调用，
	 * 转化为class.index的直接调用，从而体现所谓的FastClass。
	 */
	public void testFastClass() throws Exception{
	    FastClass fastClass = FastClass.create(SampleBean.class);
	    FastMethod fastMethod = fastClass.getMethod("getValue",new Class[0]);
	    SampleBean bean = new SampleBean();
	    bean.setValue("Hello world");
	    System.out.println(fastMethod.invoke(bean, new Object[0]));
	}
	public static void main(String[] args) {
		try {
			new TestFixedValue().testCallbackFilter();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
