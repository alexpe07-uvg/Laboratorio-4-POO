import java.time.LocalDate;

public class QuesoFresco extends Lote implements Refrigerable, Caducable {
    private double porcentajeGrasa;

    public QuesoFresco(String ID, String nombreProductor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad, double porcentajeGrasa) {
        super(ID, nombreProductor, fechaIngreso, pesoRecibido, calidad);
        if(getCalidad() == Calidad.C) {
            throw new IllegalArgumentException("El queso fresco de calidad C no es aceptado en la cooperativa");
        }
        if(porcentajeGrasa < 10 || porcentajeGrasa > 40) {
            throw new IllegalArgumentException("El porcentaje de grasa debe estar entre 10% y 40%");
        }
        this.porcentajeGrasa = porcentajeGrasa;
    }

    //getters y setters
    public double getPorcentajeGrasa() {
        return porcentajeGrasa;
    }

    //métodos abstractos implementados
    @Override
    public double calcularPago() {
        double tarifa = 0; //se inicia con cero antes de verificar la calidad y asignar la tarifa correspondiente
        switch (getCalidad()) {
            case A: tarifa = 38.0; break;
            case B: tarifa = 30.0; break;
            default: tarifa = 0.0; break; //no debería llegar a este punto porque ya se verifica en el constructor que no sea calidad C
        }
        return getPesoRecibido() * (tarifa - 1.50); //se descuenta 1.50 por cada kilo de queso fresco por concepto de empaque
     } 

    @Override
    public String getCategoriaProd() {
        return "Queso fresco";
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
        return getFechaIngreso().plusDays(21);
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
    

