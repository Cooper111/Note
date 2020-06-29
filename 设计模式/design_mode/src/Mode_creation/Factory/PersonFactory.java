package Mode_creation.Factory;

/**
 * @author Kevin
 * @date 2020/3/9 0:26
 */
public class PersonFactory extends AbstractPersonFactory {

    @Override
    Person accept(String type) {
        if ("JAVA".equals(type)) {
            return new JavaPerson();
        } else if("Python".equals(type)) {
            return new PythonPerson();
        } else {
            return null;
        }
    }
}
