package com.github.aq0706.support.json;

import junit.framework.TestCase;

import java.util.*;

public class JSONTest extends TestCase {

    public void testParse() {
        Student expectedStudent = new Student();
        expectedStudent.name = "tom";
        expectedStudent.age = 10;
        expectedStudent.isGirl = true;
        expectedStudent.teacher = new Teacher("bob", 40);

        Student student1 = new Student();
        student1.name = "student1";
        student1.teacher = new Teacher("bob", 40);
        Student student2 = new Student();
        student2.name = "student2";

        List<Student> students = new ArrayList<>();
        students.add(student1);
        students.add(student2);
        expectedStudent.students = students;

        String json = String.format("{\"name\": \"%s\", \"age\": %d, \"isGirl\": %s, \"teacher\":{\"name\":\"%s\", \"age\": %d}, " +
                        "\"students\":[{\"name\": \"%s\", \"teacher\":{\"name\":\"%s\", \"age\": %d}}, {\"name\": \"%s\"}]}",
                expectedStudent.name, expectedStudent.age, expectedStudent.isGirl, expectedStudent.teacher.name, expectedStudent.teacher.age,
                expectedStudent.students.get(0).name, expectedStudent.students.get(0).teacher.name, expectedStudent.students.get(0).teacher.age,
                expectedStudent.students.get(1).name);
        Student student = JSON.parse(json, Student.class);
        assertEquals(expectedStudent, student);
    }

    public void testToString() {
        Student student = new Student();
        student.name = "tom";
        student.age = 10;
        student.isGirl = true;
        student.teacher = new Teacher("bob", 40);

        Student student1 = new Student();
        student1.name = "student1";
        student1.teacher = new Teacher("bob", 40);
        Student student2 = new Student();
        student2.name = "student2";

        List<Student> students = new ArrayList<>();
        students.add(student1);
        students.add(student2);
        student.students = students;

        String json = JSON.toJSONString(student);
        Student parsedStudent = JSON.parse(json, Student.class);
        assertEquals(student, parsedStudent);
    }

    static class Student {
        private String name;
        Integer age;
        boolean isGirl;

        List<Student> students;

        Teacher teacher;

        public Student() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Student student = (Student) o;
            return age == student.age &&
                    isGirl == student.isGirl &&
                    Objects.equals(name, student.name) &&
                    Objects.equals(students, student.students) &&
                    Objects.equals(teacher, student.teacher);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, isGirl, students, teacher);
        }

        @Override
        public String toString() {
            return "Student{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", isGirl=" + isGirl +
                    ", students=" + students +
                    ", teacher=" + teacher +
                    '}';
        }
    }

    static class Teacher {
        String name;
        int age;

        public Teacher() {
        }

        public Teacher(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Teacher teacher = (Teacher) o;
            return age == teacher.age &&
                    Objects.equals(name, teacher.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public String toString() {
            return "Teacher{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
