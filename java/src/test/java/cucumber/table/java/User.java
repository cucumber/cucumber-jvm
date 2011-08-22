package cucumber.table.java;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class User {
    private String name;
    private Date birthDate;
    private Integer credits;

    public User() {
        super();
    }

    public User(String name, Date birthDate, Integer credits) {
        super();
        this.name = name;
        this.birthDate = birthDate;
        this.credits = credits;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getCredits() {
        return this.credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        User rhs = (User) obj;
        return new EqualsBuilder().append(name, rhs.name).append(birthDate, rhs.birthDate).append(credits, rhs.credits)
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).append(birthDate).append(credits).toHashCode();
    }

}
