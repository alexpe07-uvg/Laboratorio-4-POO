import java.time.LocalDate;

public class Hortaliza extends Lote implements Refrigerable, Caducable {
    private int vidaUtilDias;

    public Hortaliza(String ID, String nombreProductor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad, int vidaUtilDias) {
        super(ID, nombreProductor, fechaIngreso, pesoRecibido, calidad);
        if(vidaUtilDias < 3 || vidaUtilDias > 14) {
            throw new IllegalArgumentException("La vida útil en días debe estar entre 3 y 14 días");
        }
        this.vidaUtilDias = vidaUtilDias;
    }

    //getters y setters
    public int getVidaUtilDias() {
        return vidaUtilDias;
    }

    //métodos abstractos implementados
    @Override
    public double calcularPago() {
        double tarifa = 0; //se inicia con cero antes de verificar la calidad y asignar la tarifa correspondiente
        switch (getCalidad()) {
            case A: tarifa = 4.0; break;
            case B: tarifa = 3.0; break;
            default: tarifa = 2.0; break;
        }
        return (getPesoRecibido() * 0.95) * tarifa; //se descuenta un 5% del peso recibido porque el resto se considera merma de transporte
    }

    @Override
    public String getCategoriaProd() {
        return "Hortaliza";
    }

    @Override
    public boolean esRefrigerable() {
        return true;
    }

    @Override 
    public boolean esCaducable() {
        return true;
    }

    @Override
    public LocalDate getFechaCaducidad() {
        return getFechaIngreso().plusDays((long) vidaUtilDias);
    }

    @Override 
    public boolean estaCaducado(LocalDate fechaOperacion) {
        // Está vencido a partir del día siguiente a su fecha de vencimiento
        return fechaOperacion.isAfter(getFechaCaducidad());
    }

    @Override
    public LocalDate getFechaOrden() {
        return getFechaCaducidad(); // producto perecedero
    }
    
}
