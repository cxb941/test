package cglib.demo;

import static org.junit.Assert.*;
import net.sf.cglib.proxy.Mixin;

import org.junit.Test;
/**
 * Mixin类比较尴尬，因为他要求Minix的类（例如MixinInterface）实现一些接口。
 * 既然被Minix的类已经实现了相应的接口，那么我就直接可以通过纯Java的方式实现，没有必要使用Minix类。
 * @author MrChen
 *
 */
public class MixinInterfaceTest {
	interface Interface1{
		String first();
	}
	interface Interface2{
		String second();
	}

	class Class1 implements Interface1{
		@Override
		public String first() {
			return "first";
		}
	}

	class Class2 implements Interface2{
		@Override
		public String second() {
			return "second";
		}
	}

	interface MixinInterface extends Interface1, Interface2{

	}

	public static void main(String[] args){
		Mixin mixin = Mixin.create(new Class[]{Interface1.class, Interface2.class,
				MixinInterface.class}, new Object[]{new MixinInterfaceTest().new Class1(),
				new MixinInterfaceTest().new Class2()});
		MixinInterface mixinDelegate = (MixinInterface) mixin;
		System.out.println(mixinDelegate.first());
		System.out.println(mixinDelegate.second());
	}
}
