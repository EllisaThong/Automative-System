# APU Automotive System (AssignmentDegree)

## Description
APU Automotive System is a Java-based desktop application designed for managing an automotive service center. The system provides a centralized platform to manage daily operations such as customer appointments, service records, payments, and staff management. It relies on a graphical user interface (GUI) built with Java Swing and uses file-based data storage (`.txt` files) instead of a traditional relational database.

## Default Logins
**Manager:** Wesley Wong (Email: `Abc@gmail.com` | Password: `Abc123123`)  
**Counter Staff:** Mason Tan (Email: `unique@gmail.com` | Password: `test123123`)  
**Technician:** Sin Lee Soh (Email: `5@gmail.com` | Password: `Abc123123`)  
**Customer:** Penny Lim (Email: `penny@gmail.com` | Password: `john1234`)  

## Features by Role
### Manager
- Manage system users (Add, Edit, View users)
- Add and manage automobile services
- Generate Manager reports and System Logs
- Handle announcements for all users
- View feedback/comments

### Counter Staff
- Approve or Reject appointment requests
- Manage automobile services
- Supervise ongoing service requests

### Technician
- View assigned appointments
- Update service statuses

### Customer
- Secure login, registration, and password recovery
- Create, view, and manage appointments
- Make and track payments (view receipts)
- Leave comments or feedback

## Architecture
- **Language**: Java SE
- **UI Framework**: Java Swing
- **Build Tool**: Apache Ant (`build.xml` provided)
- **Data Storage**: Text-based persistence system inside the root directory (`appointments.txt`, `customers.txt`, `payments.txt`, `services.txt`, etc.). Data is read into memory at startup using `FileHandler`.

## Project Structure
- `src/assignmentdegree/`: Contains all Java source files (Controllers, Models, and Views).
- `build.xml` / `nbproject/`: Configuration files for NetBeans / Apache Ant build environment.
- `*.txt`: Flat file databases used to persist objects locally.

## Prerequisite
- **Java Development Kit (JDK)**: Version 8 or higher.
- (Optional) **NetBeans IDE**, or any Java IDE supporting Apache Ant.

## How to Run
1. **Using an IDE (NetBeans)**:
   - Import the directory as a Java application (or Ant-based NetBeans project).
   - Set `assignmentdegree.Main` as the Main class.
   - Run the application.
2. **Using Apache Ant (CLI)**:
   - Navigate to the project root directory where `build.xml` is located.
   - Run the following command:
     ```bash
     ant run
     ```
3. **Manual Compilation**:
   - Compile all the `.java` files inside the `src` directory and execute `assignmentdegree.Main`.

## Initial Setup / Usage Notes
- Make sure not to change the location of the `.txt` files, as the `FileHandler` expects them in the project's root or designated working directory.
- Start the application to access the `LoginPage`. If there are no predefined users, you can use the register option or inspect `customers.txt`, `managers.txt`, etc. for login credentials.
