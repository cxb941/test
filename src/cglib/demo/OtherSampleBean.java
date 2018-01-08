package cglib.demo;

public class OtherSampleBean {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
	@Override
	public String toString() {
		return "OtherSampleBean [value=" + value + "]";
	}
}