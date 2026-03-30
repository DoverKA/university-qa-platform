package org.wy.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wy.demo.entity.Course;
import org.wy.demo.entity.User;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByTeacher(User teacher);

    List<Course> findByNameContaining(String keyword);

    List<Course> findByMajorAndSemester(String major, String semester);

    List<Course> findByMajor(String major);

    List<Course> findByTeacherId(Integer teacherId);

    List<Course> findByNameContainingIgnoreCaseOrMajorContainingIgnoreCaseOrSemesterContainingIgnoreCase(
            String nameKeyword,
            String majorKeyword,
            String semesterKeyword
    );

    boolean existsByNameAndMajorAndSemesterAndTeacherId(
            String name,
            String major,
            String semester,
            Integer teacherId
    );
}
