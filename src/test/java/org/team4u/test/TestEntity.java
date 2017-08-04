package org.team4u.test;


import org.team4u.sql.builder.entity.annotation.*;
import org.team4u.sql.builder.entity.invoker.UUIDForPkInvoker;

/**
 * @author Jay Wu
 */
@Table(name = "client")
@Actions({@Action(key = UUIDForPkInvoker.KEY, actionType = ActionType.BEFORE_INSERT)})
public class TestEntity {

    @Id
    @Column(name = "client_id")
    private String id;

    @Column
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestEntity entity = (TestEntity) o;

        if (!id.equals(entity.id)) return false;
        return name != null ? name.equals(entity.name) : entity.name == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}