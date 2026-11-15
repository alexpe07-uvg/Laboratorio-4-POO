import java.time.LocalDate;

public class CafOrganico extends CafPergamino { // Comparte mucha información con café pergamino, por lo que la hereda de esa clase
    private String entCertificadora;
    private String fechaValidezSello;
    
    public CafOrganico(String ID, String nombreProductor, LocalDate fechaOrden, double pesoRecibido, Calidad calidad, double porcentajeHumedad, String entCertificadora, LocalDate fechaValidezSello) {
        super(ID, nombreProductor, fechaOrden, pesoRecibido, calidad, porcentajeHumedad);
        this.entCertificadora = entCertificadora;
        this.fechaValidezSello = fechaValidezSello.toString();
    }
    
}
