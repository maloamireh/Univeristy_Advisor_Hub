import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Appointment {
    private final SimpleIntegerProperty appointmentId;
    private final SimpleIntegerProperty studentId;
    private final SimpleIntegerProperty courseId;
    private final SimpleStringProperty appointmentTime;
    private final SimpleIntegerProperty durationMinutes;

    public Appointment(int appointmentId, int studentId, int courseId, String appointmentTime, int durationMinutes) {
        this.appointmentId = new SimpleIntegerProperty(appointmentId);
        this.studentId = new SimpleIntegerProperty(studentId);
        this.courseId = new SimpleIntegerProperty(courseId);
        this.appointmentTime = new SimpleStringProperty(appointmentTime);
        this.durationMinutes = new SimpleIntegerProperty(durationMinutes);
    }

    public int getAppointmentId() {
        return appointmentId.get();
    }

    public int getStudentId() {
        return studentId.get();
    }

    public int getCourseId() {
        return courseId.get();
    }

    public String getAppointmentTime() {
        return appointmentTime.get();
    }

    public int getDurationMinutes() {
        return durationMinutes.get();
    }

    // Define getter and setter methods for other attributes


    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes.set(durationMinutes);
    }

    public String getStudentName() throws SQLException {
        Connection connection = DatabaseUtil.getConnection();
        try {
            String query = "SELECT first_name, last_name FROM Students WHERE student_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, getStudentId());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                return firstName + " " + lastName;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return "Unknown Student";
    }

    public String getCourseCode() throws SQLException {
        Connection connection = DatabaseUtil.getConnection();
        // Implement logic to retrieve the course code based on courseId from the database
        try {
            String query = "SELECT course_code FROM Courses WHERE course_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, getCourseId());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("course_code");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return "Unknown Course";
    }


    public void setAppointmentTime(String time) {
        // Implement setter logic
        appointmentTime.set(time);
    }

    // Define other setter methods for editing attributes

    public SimpleIntegerProperty appointmentIdProperty() {
        return appointmentId;
    }

    public SimpleStringProperty appointmentTimeProperty() {
        return appointmentTime;
    }

    public SimpleIntegerProperty durationMinutesProperty() {
        return durationMinutes;
    }
}
