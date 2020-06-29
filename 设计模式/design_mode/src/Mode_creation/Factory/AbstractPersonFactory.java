package Mode_creation.Factory;

/**
 * @author Kevin
 * @date 2020/3/9 0:24
 */
abstract class AbstractPersonFactory {
    //招人
    abstract Person accept(String type);
}
