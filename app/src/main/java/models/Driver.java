package models;

public class Driver {
    String id;
    String name;
    String email;
    String vehicleBrand;
    String vehiclePlate;
    String remisera;
    String aptoNV;
    String image;

    public Driver()
    {

    }


    public Driver(String id, String name, String email, String vehicleBrand, String vehiclePlate, String remisera, String aptoNV) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.vehicleBrand = vehicleBrand;
        this.vehiclePlate = vehiclePlate;
        this.remisera = remisera;
        this.aptoNV = aptoNV;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public String getRemisera() {
        return remisera;
    }

    public String getAptoNV() {
        return aptoNV;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public void setRemisera(String remisera) {
        this.remisera = remisera;
    }

    public void setAptoNV(String aptoNV) {
        this.aptoNV = aptoNV;
    }
}
