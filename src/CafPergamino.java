import java.time.LocalDate;

public class CafPergamino extends Lote {
    private double porcentajeHumedad;

    public CafPergamino(String ID, String nombreProductor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad, double porcentajeHumedad) {
        super(ID, nombreProductor, fechaIngreso, pesoRecibido, calidad);
        if (porcentajeHumedad < 8 || porcentajeHumedad > 14) {
            throw new IllegalArgumentException("El porcentaje de humedad del café debe estar entre 8% y 14%");
        }
        this.porcentajeHumedad = porcentajeHumedad;
    }

    //getters y setters
    public double getPorcentajeHumedad() {
        return porcentajeHumedad;
    }

    @Override
    public double calcularPago() {
        double tarifa = 0; //se inicia con cero antes de verificar la calidad y asignar la tarifa correspondiente
        switch (getCalidad()) {
            case A:
                tarifa = 12.0;
                break;
            case B:
                tarifa = 9.0;
                break;
            case C:
                tarifa = 6.0;
                break;
        }
        double pagoBase = getPesoRecibido() * tarifa;

        if (porcentajeHumedad > 12.0) {
            double puntosExtraPorc = Math.ceil(porcentajeHumedad - 12.0);
            double descuento = pagoBase * (0.03 * puntosExtraPorc); // se descuenta 3% por cada punto de humedad por encima del 12%
            return pagoBase - descuento;      
    }
    return pagoBase;
    }

    @Override
    public String getCategoriaProd() {
        return "Café en pergamino";
    }

    @Override 
    public LocalDate getFechaOrden() {
        return getFechaIngreso(); // producto no perecedero
    }

    
}
