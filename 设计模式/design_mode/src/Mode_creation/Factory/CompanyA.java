package Mode_creation.Factory;

/**
 * @author Kevin
 * @date 2020/3/9 0:11
 */
public class CompanyA extends AbstractCompany {
    Person person = null;

//    @Override
//    Person accept(String type) {
//        if ("JAVA".equals(type)) {
//            person = new JavaPerson();
//        } else if("Python".equals(type)) {
//            person = new PythonPerson();
//        }
//
//        return person;
//    }

    public static void main(String[] args) {
        AbstractCompany company = new CompanyA();
        Person person = company.accept("JAVA");
        person.code();
    }
    //招人的工厂
    PersonFactory factory = new PersonFactory();
    @Override
    Person accept(String type) {
        return factory.accept(type);
    }
}
