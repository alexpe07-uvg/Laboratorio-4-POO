import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Bodega implements Iterable<Lote> {

    private String ID;
    private double capacidadMax;
    private boolean esCamaraFria;
    private List<Lote> lotes;

    public Bodega(String ID, double capacidadMax, boolean esCamaraFria) {
        if (ID == null || ID.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la bodega no puede estar vacío.");
        }
        if (capacidadMax <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a cero.");
        }

        this.ID = ID.trim();
        this.capacidadMax = capacidadMax;
        this.esCamaraFria = esCamaraFria;
        this.lotes = new ArrayList<>(); //se inicializa lista en constructor
    }

    //getters y setters
    public String getID() {
        return ID;
    }

    public double getCapacidadMax() {
        return capacidadMax;
    }

    public boolean isEsCamaraFria() {
        return esCamaraFria;
    }

    //lógica para definir los pesos de los lotes y la capacidad disponible de la bodega
    public double getPesoOcupado() {
        double total = 0;
        for (Lote lote : lotes) {
            total += lote.getPesoDisponible();
        }
        return total;
    }


    public double getCapacidadDisponible() {
        return capacidadMax - getPesoOcupado();
    }

    //verificación que ID's repetidos no se agreguen a la bodega
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bodega)) return false;
        Bodega otra = (Bodega) o;
        return this.ID.equalsIgnoreCase(otra.ID);
    }

    // método para separar productos según la condición que necesiten en diferentes bodegas
    public boolean esCompatible(Lote lote) {
        // Cámara fría solo acepta refrigerables; bodega seca solo no refrigerables
        return lote.esRefrigerable() == this.esCamaraFria;
    }

    public boolean tieneEspacio(Lote lote) {
        return getPesoOcupado() + lote.getPesoRecibido() <= capacidadMax; //verifica que aún haya espacio disponible para lo solicitado
    }

    public void agregarLote(Lote lote) {
        //verificaciones antes de agregar el lote a la bodega
        if (lote == null) {
            throw new IllegalArgumentException("El lote no puede ser nulo.");
        }
        if (!esCompatible(lote)) {
            String tipoBodega;
            if (esCamaraFria) {
                tipoBodega = "cámara fría";
            } else {
                tipoBodega = "bodega seca";
            }
            throw new IllegalArgumentException("El producto " + lote.getCategoriaProd() + " no es compatible con una " + tipoBodega + ".");
        }
        if (!tieneEspacio(lote)) {
            throw new IllegalArgumentException("La bodega " + ID + " no tiene suficiente capacidad disponible.");
        }
        lotes.add(lote);
    }

    public boolean estaVacia() {
        return lotes.isEmpty();
    }

    // Interfaz Iterable que trabaja junto a Comparable se encarga de siempre entregar los lotes en el orden de salida establecido
    @Override
    public Iterator<Lote> iterator() {
        Collections.sort(lotes);
        return lotes.iterator();
    }
  
}