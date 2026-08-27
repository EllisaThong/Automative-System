/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package assignmentdegree;

/**
 *
 * @author ellisathong
 */
public class Student extends Person implements Performace{
   private String tpNumber;
   private double cgpa;
   public Student (String name, String tpNumber, double cgpa){
       super(name);
       this.tpNumber = tpNumber;
       this.cgpa = cgpa;
   }
   public String getTpNumber(){
       return tpNumber; }
   public double getCgpa(){
       return cgpa;}
   public String checkPerformance() {
        if (cgpa > 3.5) {
            return "Excellent";
        } else if (cgpa >= 3.0) {
            return "Good";
        } else if (cgpa >= 2.5) {
            return "Satisfactory";
        } else {
            return "Need Improvement";
        }
   }
   public String toString(){
        return "Name: " + getName()
             + " TP Number: " + getTpNumber()
             + " CGPA: " + getCgpa();
   }

}
