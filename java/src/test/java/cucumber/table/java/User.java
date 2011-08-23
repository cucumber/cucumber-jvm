package cucumber.table.java;

import java.util.Date;

public class User {
    private String name;
    private Date birthDate;
    private Integer credits;

    public User() {
    }

    public User(String name, Date birthDate, Integer credits) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (birthDate != null ? !birthDate.equals(user.birthDate) : user.birthDate != null) return false;
        if (credits != null ? !credits.equals(user.credits) : user.credits != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (birthDate != null ? birthDate.hashCode() : 0);
        result = 31 * result + (credits != null ? credits.hashCode() : 0);
        return result;
    }
}
