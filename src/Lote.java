import java.time.LocalDate;

public abstract class Lote implements Comparable<Lote>{
    private String ID;
    private String nombreProductor;
    private LocalDate fechaIngreso;
    private double pesoRecibido;
    private Calidad calidad;
    private double pesoDisponible;
    private Estado estado;
    

    public Lote(String ID, String nombreProductor, LocalDate fechaIngreso, double pesoRecibido, Calidad calidad) {
       //verificaciones y lanzamiento de excepciones
        if (ID == null || ID.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del lote no debe estar vacío");
       }
       if (nombreProductor == null || nombreProductor.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del productor no debe estar vacío");
       } 
       if(fechaIngreso == null) {
            throw new IllegalArgumentException("La fecha de ingreso es un campo obligatorio");
       }
       if(pesoRecibido <= 0) {
            throw new IllegalArgumentException("El peso recibido debe ser mayor a cero");
       }
       if(calidad == null) {
            throw new IllegalArgumentException("La calidad del lote es un campo obligatorio");
       }
       
       this.ID = ID.trim();
        this.nombreProductor = nombreProductor.trim();
        this.fechaIngreso = fechaIngreso;
        this.pesoRecibido = pesoRecibido;
        this.calidad = calidad;
        this.pesoDisponible = pesoRecibido; //al inicio ambas variables son iguales
        this.estado = Estado.DISPONIBLE; //por defecto los productos inician disponibles
    }

    //getters y setters
    public String getID() {return ID;}
    public String getNombreProductor() {return nombreProductor;}
    public LocalDate getFechaIngreso() {return fechaIngreso;}
    public double getPesoRecibido() {return pesoRecibido;}
    public Calidad getCalidad() {return calidad;}
    public double getPesoDisponible() {return pesoDisponible;}
    public Estado getEstado() {return estado;}

    //verficacion que dos codigos no se repitan
    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (!(o instanceof Lote)) {return false;}
        Lote otro = (Lote) o;
        return this.ID.equalsIgnoreCase(otro.ID);
}

    //métodos abstractos para clases hijas
    public abstract double calcularPago();
    public abstract String getCategoriaProd();
    public abstract LocalDate getFechaOrden();

    //metodos de caducable y refrigerable iniciados con false por defecto para evitar que los cafes repitan está lógica innecesaria para ellos
    public boolean esCaducable() {
        return false;
    }
    public boolean esRefrigerable() {
        return false;
    }

    public LocalDate getFechaCaducidad() {
        return null;
    }

    public boolean estaCaducado(LocalDate fechaOperacion) {
        return false;
    }

    public double getCostoXKg() {
        return calcularPago() / pesoRecibido;
    }

    public double descontarPesoXLote(double kgSolicitados) {
        if(estado != Estado.DISPONIBLE) {
            throw new IllegalStateException("El lote no está disponible para despacho");
        }
        if(kgSolicitados <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor a cero");
        }
        if(kgSolicitados > pesoDisponible) {
            throw new IllegalArgumentException("No hay suficiente peso disponible dentro del lote.");
        }
        pesoDisponible -= kgSolicitados;
        if(pesoDisponible <= 0.0001) { //en lugar de llegar a cero exacto, se usa un margen de error para evitar problemas de precisión con double
            pesoDisponible = 0;
            estado = Estado.AGOTADO;
        }
        return kgSolicitados * getCostoXKg() * 1.35; // se aplica un 35% de ganancia sobre el costo por kg
    }

    public double descartar() {
        if(estado != Estado.DISPONIBLE) {
            return 0.00;  // SOLAMENTE SE PUEDE DESCARTAR SI SE RETIRA POR VENCIMIENTO
        }
        double perdida = this.pesoDisponible * getCostoXKg();
        estado = Estado.DESCARTADO;
        return perdida;
    }

    // Lógica orden de salida de los lotes con Comparable
    @Override
    public int compareTo(Lote otro) {
        //1. Primero perecederos y después no perecederos
        if (this.esCaducable() != otro.esCaducable()){
            return this.esCaducable() ? -1 : 1; //forma resumida de if-else
        }

        //2. Fecha de vencimiento (perecedero) o de ingreso (no perecedero)
        int fechaComparacion = this.getFechaOrden().compareTo(otro.getFechaOrden());
        if (fechaComparacion != 0) { // si no hay empate, se devuelve el resultado de la comparación
            return fechaComparacion;
        }

        //3. Si hay empate, sigue saliendo primero de mejor calidad (A > B > C) 
        int calidadComparacion = this.calidad.compareTo(otro.calidad);
        if (calidadComparacion != 0) { // si no hay empate, se devuelve el resultado de la comparación
            return calidadComparacion;
        }

        //4. Si todavía hay empate, se toma ID menor en orden alfabético
        return this.ID.compareTo(otro.ID);
    }

}
