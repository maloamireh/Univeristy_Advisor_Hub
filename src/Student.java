public class Student {
    private int studentId;
    private String firstName;
    private String lastName;
    private String registrationNumber;
    private int courseId;

    // Constructor, getters, and setters


    public Student() {
    }

    public Student(int studentId, String firstName, String lastName, String registrationNumber, int courseId) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registrationNumber = registrationNumber;
        this.courseId = courseId;
    }

    public Student(int studentId, String firstName, String lastName, String registrationNumber) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registrationNumber = registrationNumber;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return  firstName + ' ' +lastName ;
    }
}
