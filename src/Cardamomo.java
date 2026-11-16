import java.time.LocalDate;

public class Cardamomo extends Lote implements Caducable {
    private double porcentajeGranoVerde;

    public Cardamomo(String ID, String nombreProductor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad, double porcentajeGranoVerde) {
        super(ID, nombreProductor, fechaIngreso, pesoRecibido, calidad);
        if(porcentajeGranoVerde < 0 || porcentajeGranoVerde > 100) {
            throw new IllegalArgumentException("El porcentaje de grano verde debe estar entre 0% y 100%");   
        }
        this.porcentajeGranoVerde = porcentajeGranoVerde;
    }

    //getters y setters
    public double getPorcentajeGranoVerde() {
        return porcentajeGranoVerde;
    }

    //métodos abstractos implementados
    @Override 
    public double calcularPago() {
        double tarifa = 0; //se inicia con cero antes de verificar la calidad y asignar la tarifa correspondiente
        switch (getCalidad()) {
            case A: tarifa = 60.0; break;
            case B: tarifa = 48.0; break;
            default: tarifa = 35.0; break;
        }
        double pago = getPesoRecibido() * tarifa;

        //condición si el grano verde es al menos del 85%
        if(porcentajeGranoVerde >= 85.0) {
            pago = pago * 1.10; // el pago se incrementa unn 10% si se cumple la condicion
        }
        return pago;       
}

    @Override public String getCategoriaProd() {
        return "Cardamomo";
    }

    @Override
    public boolean esCaducable() {
        return true;
    }

    @Override
    public LocalDate getFechaCaducidad() {
        return getFechaIngreso().plusDays(180);
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
