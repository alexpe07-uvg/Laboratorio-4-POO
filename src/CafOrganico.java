import java.time.LocalDate;

public class CafOrganico extends CafPergamino { // Comparte mucha información con café pergamino, por lo que hereda de esa clase
    private String entCertificadora;
    private LocalDate fechaValidezSello;
    
    public CafOrganico(String ID, String nombreProductor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad, double porcentajeHumedad, String entCertificadora, LocalDate fechaValidezSello) {
        super(ID, nombreProductor, fechaIngreso, pesoRecibido, calidad, porcentajeHumedad);
        if(entCertificadora == null || entCertificadora.trim().isEmpty()) {
            throw new IllegalArgumentException("La entidad certificadora no puede estar vacía");
        }
        if(fechaValidezSello == null) {
            throw new IllegalArgumentException("La fecha de validez del sello es un campo obligatorio");
        }

        //se verifica que la fecha de validez del sello no sea anterior a la fecha de ingreso del lote y que este vencida
        if(fechaValidezSello.isBefore(fechaIngreso)) {
            throw new IllegalArgumentException("El sello de certificación orgánica está vencido");
        }
        this.entCertificadora = entCertificadora.trim();
        this.fechaValidezSello = fechaValidezSello;
    }

    //getters y setters
    public String getEntCertificadora() {
        return entCertificadora;
    }

    public LocalDate getFechaValidezSello() {
        return fechaValidezSello;
    }

    //métodos abstractos implementados
    @Override
    public double calcularPago() {
        return super.calcularPago() + (0.75*getPesoRecibido()); //se le suma 0.75 por cada kilo de café orgánico y se hereda el calculo de pago del otro café

    }

    @Override
    public String getCategoriaProd() {
        return "Café orgánico";
    }

    //fechaOrden() ya queda heredada de la clase CafPergamino sin tener que volver a escribirlo


    
}
