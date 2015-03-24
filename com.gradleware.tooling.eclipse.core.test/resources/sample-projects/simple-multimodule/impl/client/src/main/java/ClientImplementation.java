import org.apache.commons.lang3.StringUtils;


public class ClientImplementation implements ApiInterface {

    @Override
    public void foo() {
        System.out.println(StringUtils.abbreviate("Deoxyribonucleic", 6));
    }

}
