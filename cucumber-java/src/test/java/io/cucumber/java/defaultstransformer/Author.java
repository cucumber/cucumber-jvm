package io.cucumber.java.defaultstransformer;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class Author {

    private @Nullable String firstName;
    private @Nullable String lastName;
    private @Nullable String birthDate;

    public Author() {
        /* no-op */
    }

    public Author(String firstName, String lastName, String birthDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public @Nullable String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public @Nullable String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public @Nullable String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Author author))
            return false;
        return Objects.equals(firstName, author.firstName) && Objects.equals(lastName, author.lastName)
                && Objects.equals(birthDate, author.birthDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, birthDate);
    }

    @Override
    public String toString() {
        return "Author{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate='" + birthDate + '\'' +
                '}';
    }

}
