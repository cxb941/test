package createmodel;

import createmodel.model.Factory;

/**
 * 抽象工厂 待验证性能
 * @author MrChen
 *
 */
public interface ProviderFactory<T> {
	Factory getFactory();
}
