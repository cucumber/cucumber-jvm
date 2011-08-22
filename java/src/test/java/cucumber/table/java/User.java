package cucumber.table.java;

import java.util.Date;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.birthDate == null) ? 0 : this.birthDate.hashCode());
        result = prime * result + ((this.credits == null) ? 0 : this.credits.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (this.birthDate == null) {
            if (other.birthDate != null)
                return false;
        } else if (!this.birthDate.equals(other.birthDate))
            return false;
        if (this.credits == null) {
            if (other.credits != null)
                return false;
        } else if (!this.credits.equals(other.credits))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        } else if (!this.name.equals(other.name))
            return false;
        return true;
    }

}
