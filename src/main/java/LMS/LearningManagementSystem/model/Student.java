package LMS.LearningManagementSystem.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Null;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Table(name = "students")
public class Student extends User {
    @ManyToMany
    @JoinTable(
            name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses;
    @Enumerated(EnumType.STRING)
    private Role role ;

    public Student(String name, String email, String password,Role role) {
        super(name, email, password);
        this.role = Role.STUDENT;
    }

    public void addCourse(Course course) {
    if (this.courses == null) {
        this.courses = new ArrayList<>();
    }
    this.courses.add(course);
}
    public void removeCourse(Course course){                    // يجب التعديل بعد إضافة كلاس الكورس
        this.courses.remove(course);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of();
    }
   

}
