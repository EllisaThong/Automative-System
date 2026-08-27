package assignmentdegree;

public class Service implements TextRecord {
    private String serviceID;
    private String serviceName;
    private String serviceType;
    private double price;
    private int duration;

    public Service(String serviceID, String serviceName, String serviceType, double price) {
        this.serviceID = serviceID;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.price = price;
        
        setDurationBasedOnType();
    }

    private void setDurationBasedOnType() {
        if (serviceType.equalsIgnoreCase("Normal")) {
            this.duration = 60;
        } else if (serviceType.equalsIgnoreCase("Major")) {
            this.duration = 180;
        } else {
            this.duration = 0;
        }
    }

    public String getServiceID() {
        return serviceID;
    }
    
    public void setServiceID(String newServiceID) {
        this.serviceID = newServiceID;
    }

    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String newServiceName) {
        this.serviceName = newServiceName;
    }

    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String newServiceType) {
        this.serviceType = newServiceType;
        setDurationBasedOnType();
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double newPrice) {
        this.price = newPrice;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isMajor() {
        return "Major".equalsIgnoreCase(serviceType);
    }

    public String getFormattedPrice() {
        return String.format("RM %.2f", price);
    }

    public String getEstimatedDuration() {
        return duration + " mins";
    }

    public String getServiceInfo() {
        return String.format("%s - %s (%s)", serviceID, serviceName, serviceType);
    }

    @Override
    public String toRecord() {
        return getServiceID() + "," + getServiceName() + "," + getServiceType() + "," + getPrice();
    }
}