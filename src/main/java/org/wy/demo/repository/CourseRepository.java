package org.wy.demo.repository;

import org.wy.demo.entity.Course;
import org.wy.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository  extends JpaRepository<Course, Integer> {
    // 保留你原有方法
    List<Course> findByTeacher(User teacher);
    List<Course> findByNameContaining(String keyword);
    List<Course> findByMajorAndSemester(String major, String semester);
    List<Course> findByMajor(String major);

    // 新增：按教师ID查询（比传整个User对象更实用）
    List<Course> findByTeacherId(Integer teacherId);
}