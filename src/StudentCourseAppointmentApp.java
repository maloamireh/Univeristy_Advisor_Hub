import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StudentCourseAppointmentApp extends Application {

    private Connection connection;
    private Scene mainScene;

    Pane addStudentPanel;
    Pane addCoursePanel;
    Pane addAppointmentPanel;
    Pane appointmentPanel;

    VBox mainGrid;

    ComboBox<Course> courseComboBox;

    ComboBox<Student> studentComboBox;

    private ObservableList<Course> coursesList = FXCollections.observableArrayList();
    private ObservableList<Student> studentsList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws SQLException {
        // Initialize the database connection
        connection = DatabaseUtil.getConnection();

        primaryStage.setTitle("Student, Course, and Appointment Management");

        courseComboBox = new ComboBox<>(coursesList);
        studentComboBox = new ComboBox<>(studentsList);
        updateCoursesList();

        // Create UI panels
        addStudentPanel = createAddStudentPanel();
        addCoursePanel = createAddCoursePanel();
        addAppointmentPanel = createAddAppointmentPanel();
        appointmentPanel = viewAppointmentsPanel();

        Button addStudentMenuItem = new Button("Add Student");
        Button addCourseMenuItem = new Button("Add Course");
        Button addAppointmentMenuItem = new Button("Add Appointment");
        Button viewAppointmentsMenuItem = new Button("View Appointments");

        VBox menuContainer = new VBox();
        menuContainer.setSpacing(5);
        menuContainer.getChildren().addAll(addStudentMenuItem, addCourseMenuItem, addAppointmentMenuItem, viewAppointmentsMenuItem);

        // Add the panels to the main grid
        mainGrid = new VBox();
        mainGrid.getChildren().addAll(addStudentPanel);

        HBox mainPanel = new HBox();
        mainPanel.setSpacing(5);
        mainPanel.getChildren().addAll(menuContainer, mainGrid);
        // Create the main scene
        mainScene = new Scene(mainPanel, 400, 400);
        primaryStage.setScene(mainScene);


        addStudentMenuItem.setOnAction(e -> switchScene(addStudentPanel));
        addCourseMenuItem.setOnAction(e -> switchScene(addCoursePanel));
        addAppointmentMenuItem.setOnAction(e -> switchScene(addAppointmentPanel));
        viewAppointmentsMenuItem.setOnAction(e -> switchScene(appointmentPanel));

        primaryStage.show();
    }

    private void switchScene(Pane pane) {
        if (pane == appointmentPanel) {
            pane = viewAppointmentsPanel();
        }

        mainGrid.getChildren().clear();
        mainGrid.getChildren().add(pane);

        updateCoursesList();


    }

    private Pane viewAppointmentsPanel() {
        VBox panel = new VBox();
        panel.setSpacing(10);

        TableView<Appointment> appointmentTable = new TableView<>();
        appointmentTable.setEditable(true);

        // Define table columns (appointment_id, student name, course code, appointment_time, duration_minutes)
        TableColumn<Appointment, Integer> appointmentIdColumn = new TableColumn<>("ID");
        appointmentIdColumn.setCellValueFactory(cellData -> cellData.getValue().appointmentIdProperty().asObject());

        TableColumn<Appointment, String> studentNameColumn = new TableColumn<>("Student Name");
        studentNameColumn.setCellValueFactory(cellData -> {
            String studentName = null;
            try {
                studentName = cellData.getValue().getStudentName();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return new SimpleStringProperty(studentName);
        });

        TableColumn<Appointment, String> courseCodeColumn = new TableColumn<>("Course Code");
        courseCodeColumn.setCellValueFactory(cellData -> {
            String courseCode = null;
            try {
                courseCode = cellData.getValue().getCourseCode();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return new SimpleStringProperty(courseCode);
        });

        TableColumn<Appointment, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().appointmentTimeProperty());

        TableColumn<Appointment, Integer> durationColumn = new TableColumn<>("Duration (minutes)");
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationMinutesProperty().asObject());

        appointmentTable.getColumns().addAll(appointmentIdColumn, studentNameColumn, courseCodeColumn, timeColumn, durationColumn);

        Button editAppointmentButton = new Button("Edit Appointment");
        Button deleteAppointmentButton = new Button("Delete Appointment");

        // Add components to the panel
        panel.getChildren().addAll(appointmentTable, editAppointmentButton, deleteAppointmentButton);

        // Fetch appointments from the database and populate the table
        List<Appointment> appointments = fetchAppointmentsFromDatabase();
        appointmentTable.getItems().addAll(appointments);

        // Handle edit and delete actions
        editAppointmentButton.setOnAction(e -> {
            Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
            if (selectedAppointment != null) {
                openEditAppointmentDialog(selectedAppointment);
            } else {
                showAlert("Please select an appointment to edit.");
            }
        });

        deleteAppointmentButton.setOnAction(e -> {
            Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
            if (selectedAppointment != null) {

                // Get the appointment ID of the selected appointment
                int appointmentId = selectedAppointment.getAppointmentId();

                // Delete the appointment from the database
                String deleteSQL = "DELETE FROM Appointments WHERE appointment_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
                    preparedStatement.setInt(1, appointmentId);
                    preparedStatement.executeUpdate();

                    // Optionally, refresh the appointments table after deletion
                    appointmentTable.getItems().clear();
                    appointmentTable.getItems().addAll(fetchAppointmentsFromDatabase());

                    showAlert("Appointment deleted successfully!");
                } catch (SQLException ex) {
                    showAlert("Error deleting appointment: " + ex.getMessage());
                }

            } else {
                showAlert("Please select an appointment to delete.");
            }
        });


        return panel;
    }

    // Method to fetch appointments from the database
    private List<Appointment> fetchAppointmentsFromDatabase() {
        List<Appointment> appointments = new ArrayList<>();

        try {
            String query = "SELECT * FROM Appointments";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    int appointmentId = resultSet.getInt("appointment_id");
                    int studentId = resultSet.getInt("student_id");
                    int courseId = resultSet.getInt("course_id");
                    String appointmentTime = resultSet.getString("appointment_time");
                    int durationMinutes = resultSet.getInt("duration_minutes");

                    Appointment appointment = new Appointment(appointmentId, studentId, courseId, appointmentTime, durationMinutes);
                    appointments.add(appointment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }


    private Pane createAddStudentPanel() {
        VBox panel = new VBox();
        panel.setSpacing(10);

        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();
        Label regNumberLabel = new Label("Registration Number:");
        TextField regNumberField = new TextField();
        Button addStudentButton = new Button("Add Student");

        // Add components to the panel
        panel.getChildren().addAll(firstNameLabel, firstNameField, lastNameLabel, lastNameField, regNumberLabel, regNumberField, addStudentButton);

        // Add action for the "Add Student" button
        addStudentButton.setOnAction(e -> {
            try {
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String regNumber = regNumberField.getText();

                // Insert student into the database
                String insertSQL = "INSERT INTO Students (first_name, last_name, registration_number) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, regNumber);
                preparedStatement.executeUpdate();

                // Clear the fields after adding
                firstNameField.clear();
                lastNameField.clear();
                regNumberField.clear();

                // Optionally, provide a success message
                showAlert("Student added successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Error adding student: " + ex.getMessage());
            }
        });
        return panel;
    }

    private Pane createAddCoursePanel() {
        VBox panel = new VBox();
        panel.setSpacing(10);

        Label courseCodeLabel = new Label("Course Code:");
        TextField courseCodeField = new TextField();
        Label courseNameLabel = new Label("Course Name:");
        TextField courseNameField = new TextField();
        Button addCourseButton = new Button("Add Course");

        // Add components to the panel
        panel.getChildren().addAll(courseCodeLabel, courseCodeField, courseNameLabel, courseNameField, addCourseButton);

        // Add action for the "Add Course" button
        addCourseButton.setOnAction(e -> {
            try {
                String courseCode = courseCodeField.getText();
                String courseName = courseNameField.getText();

                // Insert course into the database
                String insertSQL = "INSERT INTO Courses (course_code, course_name) VALUES (?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                preparedStatement.setString(1, courseCode);
                preparedStatement.setString(2, courseName);
                preparedStatement.executeUpdate();

                // Clear the fields after adding
                courseCodeField.clear();
                courseNameField.clear();

                // Optionally, provide a success message
                showAlert("Course added successfully!");

                updateCoursesList();
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Error adding course: " + ex.getMessage());
            }
        });

        return panel;
    }

    private Pane createAddAppointmentPanel() {
        VBox panel = new VBox();
        panel.setSpacing(10);

        Label studentNameLabel = new Label("Student Name:");
        Label courseLabel = new Label("Course:");

        Label timeLabel = new Label("Time:");
        TextField timeField = new TextField();
        Label durationLabel = new Label("Duration (minutes):");
        TextField durationField = new TextField();
        Button addAppointmentButton = new Button("Add Appointment");

        // Add components to the panel
        panel.getChildren().addAll(studentNameLabel, studentComboBox, courseLabel, courseComboBox, timeLabel, timeField, durationLabel, durationField, addAppointmentButton);

        // Add action for the "Add Appointment" button
        addAppointmentButton.setOnAction(e -> {
            try {
                String studentName = studentComboBox.getValue().toString();
                String course = courseComboBox.getValue().getCourseName();
                String time = timeField.getText();
                int duration = Integer.parseInt(durationField.getText());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime localDateTime = LocalDateTime.now();
                String formattedDateTime = localDateTime.format(formatter);

                // Retrieve student_id and course_id based on names
                int studentId = getStudentIdByName(studentName);
                int courseId = getCourseIdByName(course);

                // Insert appointment into the database
                String insertSQL = "INSERT INTO Appointments (student_id, course_id, appointment_time, duration_minutes) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
                preparedStatement.setInt(1, studentId);
                preparedStatement.setInt(2, courseId);
                preparedStatement.setString(3, formattedDateTime);
                preparedStatement.setInt(4, duration);
                preparedStatement.executeUpdate();

                // Clear the fields after adding
                courseComboBox.getSelectionModel().clearSelection();
                studentComboBox.getSelectionModel().clearSelection();
                timeField.clear();
                durationField.clear();

                // Optionally, provide a success message
                showAlert("Appointment added successfully!");
            } catch (SQLException | NumberFormatException ex) {
                ex.printStackTrace();
                showAlert("Error adding appointment: " + ex.getMessage());
            }
        });

        return panel;
    }

    private int getStudentIdByName(String studentName) throws SQLException {
        String[] nameParts = studentName.split(" ");
        if (nameParts.length != 2) {
            return -1; // Invalid input
        }

        String firstName = nameParts[0];
        String lastName = nameParts[1];

        String query = "SELECT student_id FROM Students WHERE first_name = ? AND last_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("student_id");
            }
        }
        return -1; // Not found
    }

    private int getCourseIdByName(String courseName) throws SQLException {
        String query = "SELECT course_id FROM Courses WHERE course_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, courseName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("course_id");
            }
        }
        return -1; // Not found
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void openEditAppointmentDialog(Appointment appointment) {
        Dialog<Void> editDialog = new Dialog<>();
        editDialog.setTitle("Edit Appointment");

        // Set the button types (e.g., Save and Cancel)
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        editDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Create a GridPane to layout the dialog's content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // Add input fields for editing
        TextField appointmentTimeField = new TextField(appointment.getAppointmentTime());
        TextField durationField = new TextField(Integer.toString(appointment.getDurationMinutes()));

        grid.add(new Label("Appointment Time:"), 0, 0);
        grid.add(appointmentTimeField, 1, 0);
        grid.add(new Label("Duration (minutes):"), 0, 1);
        grid.add(durationField, 1, 1);

        editDialog.getDialogPane().setContent(grid);

        // Set the result converter to handle the Save button
        editDialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                try {
                    // Get the updated values from the input fields
                    String updatedTime = appointmentTimeField.getText();
                    int updatedDuration = Integer.parseInt(durationField.getText());

                    // Update the appointment's time and duration in the database
                    updateAppointmentDetails(appointment, updatedTime, updatedDuration);

                    // Optionally, refresh the appointments table
                    // appointmentTable.setItems(getAppointmentsFromDatabase());

                    showAlert("Appointment updated successfully!");
                } catch (NumberFormatException e) {
                    showAlert("Invalid duration value. Please enter a valid number.");
                } catch (SQLException e) {
                    showAlert("Error updating appointment: " + e.getMessage());
                }
            }
            return null;
        });

        editDialog.showAndWait();
    }

    private void updateAppointmentDetails(Appointment appointment, String updatedTime, int updatedDuration) throws SQLException {
        // Implement the logic to update the appointment's time and duration in the database
        String updateQuery = "UPDATE Appointments SET appointment_time = ?, duration_minutes = ? WHERE appointment_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
        preparedStatement.setString(1, updatedTime);
        preparedStatement.setInt(2, updatedDuration);
        preparedStatement.setInt(3, appointment.getAppointmentId());
        preparedStatement.executeUpdate();

        // Update the appointment object with the new values
        appointment.setAppointmentTime(updatedTime);
        appointment.setDurationMinutes(updatedDuration);
    }


    private void updateCoursesList() {
        coursesList.clear();
        coursesList.addAll(fetchCoursesFromDatabase());
        courseComboBox.setItems(coursesList);

        studentsList.clear();
        studentsList.addAll(fetchStudentsFromDatabase());
        studentComboBox.setItems(studentsList);
    }

    private List<Course> fetchCoursesFromDatabase() {
        List<Course> courses = new ArrayList<>();

        try {
            String query = "SELECT * FROM Courses";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    int courseId = resultSet.getInt("course_id");
                    String courseCode = resultSet.getString("course_code");
                    String courseName = resultSet.getString("course_name");

                    Course course = new Course(courseId, courseCode, courseName);
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    private List<Student> fetchStudentsFromDatabase() {
        List<Student> students = new ArrayList<>();

        try {
            String query = "SELECT * FROM Students";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    int studentId = resultSet.getInt("student_id");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");
                    String registrationNumber = resultSet.getString("registration_number");

                    Student student = new Student(studentId, firstName, lastName, registrationNumber);
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }


    public static void main(String[] args) {
        launch(args);
    }

}
