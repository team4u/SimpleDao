package org.team4u.test;

/**
 * @author Jay Wu
 */
public class TestBean {

    private String clientId;

    private String name;

    public TestBean() {
    }

    public TestBean(TestEntity entity) {
        this.clientId = entity.getId();
        this.name = entity.getName();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TestBean{" +
                "clientId='" + clientId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestBean testBean = (TestBean) o;

        if (!clientId.equals(testBean.clientId)) return false;
        return name != null ? name.equals(testBean.name) : testBean.name == null;

    }

    @Override
    public int hashCode() {
        int result = clientId.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}