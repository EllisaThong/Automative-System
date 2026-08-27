package assignmentdegree;

public class Main {
    public static void main(String[] args) {
        FileHandler.readAllFiles();
        LoginPage loginpage = new LoginPage();
        loginpage.openLoginPage();
    }
    
}
