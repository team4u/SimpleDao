package org.team4u.test;

import org.team4u.sql.builder.entity.annotation.Column;
import org.team4u.sql.builder.entity.annotation.Id;
import org.team4u.sql.builder.entity.annotation.Table;

/**
 * @author Jay Wu
 */
@Table(name = "token")
public class TestTokenEntity {

    @Id(auto = true)
    @Column
    private Long id;

    @Column
    private String name;

    public Long getId() {
        return id;
    }

    public TestTokenEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TestTokenEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestTokenEntity that = (TestTokenEntity) o;

        if (!id.equals(that.id)) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}