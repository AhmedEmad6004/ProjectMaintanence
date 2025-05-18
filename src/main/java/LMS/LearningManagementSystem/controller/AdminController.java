package LMS.LearningManagementSystem.controller;

import LMS.LearningManagementSystem.model.AssignmentLog;
import LMS.LearningManagementSystem.model.Course;
import LMS.LearningManagementSystem.model.Student;
import LMS.LearningManagementSystem.repository.AssignmentLogRepository;
import LMS.LearningManagementSystem.repository.CourseRepository;
import LMS.LearningManagementSystem.repository.StudentRepository;
import LMS.LearningManagementSystem.service.AdminService;
import LMS.LearningManagementSystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AssignmentLogRepository assignmentLogRepository;

    @Autowired
    AdminController(AdminService adminService, ReportService reportService){this.adminService = adminService;
        this.reportService = reportService;
    }

    @PostMapping("/add-student")
    public String addStudent(@RequestParam int id, @RequestParam String name, @RequestParam String email, @RequestParam String password) {
        adminService.addStudent(name, email, password);
        return "Student added successfully!";
    }
    @PostMapping("/add-course-to-student")
    public ResponseEntity<String> addCourseToStudent(@RequestParam int studentId, @RequestParam int courseId) {
        try {
            // Find the student
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with ID " + studentId + " not found"));
        
            // Find the course
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course with ID " + courseId + " not found"));
        
            // Add course to student
            if (student.getCourses() == null) {
                student.setCourses(new ArrayList<>());
            }
            student.addCourse(course);
        
            // Add student to course (maintain both sides of the relationship)
            course.addStudent(student);
        
            // Save both entities
            studentRepository.save(student);
            courseRepository.save(course);
        
            return ResponseEntity.ok("Course added successfully to the student!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding course to student: " + e.getMessage());
        }
    }

    @PostMapping("/remove-course-from-student")
    public ResponseEntity<String> removeCourseFromStudent(@RequestParam int studentId, @RequestParam int courseId) {
        try {
            // Find the student
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student with ID " + studentId + " not found"));
        
        // Find the course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course with ID " + courseId + " not found"));
        
        // Remove course from student
        student.removeCourse(course);
        
        // Remove student from course (maintain both sides of the relationship)
        course.getEnrolledStudents().remove(student);
        
        // Save both entities
        studentRepository.save(student);
        courseRepository.save(course);
        
        return ResponseEntity.ok("Course removed successfully from the student!");
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error removing course from student: " + e.getMessage());
    }
}
    @GetMapping("/generate-report")
    public ResponseEntity<byte[]> generateReport(@RequestParam int courseId) {
        try {
            byte[] report = reportService.generateExcelReport(courseId);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=student_performance_report.xlsx")
                    .body(report);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/generate-performance-chart")
    public ResponseEntity<byte[]> generatePerformanceChart(@RequestParam int courseId) {
        try {
            byte[] chart = reportService.generatePerformanceChart(courseId);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=student_performance_chart.png")
                    .body(chart);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteStudent(@RequestParam int studentId) {
        try {
            Optional<Student> studentOptional = studentRepository.findById(studentId);

            if (studentOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Student with ID " + studentId + " not found.");
            }

            Student student = studentOptional.get();

            // Remove student from all enrolled courses
            for (Course course : student.getCourses()) {
                course.getEnrolledStudents().remove(student);
                courseRepository.save(course);
            }

            // Delete all assignment logs for this student
            List<AssignmentLog> assignmentLogs = assignmentLogRepository
                    .findAllByStudentIdAndAssignmentIdIn(studentId, Collections.emptyList());
            assignmentLogRepository.deleteAll(assignmentLogs);

            // Delete the student
            studentRepository.deleteById(studentId);

            return ResponseEntity.ok("Student deleted successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting student: " + e.getMessage());
        }
    }
}
