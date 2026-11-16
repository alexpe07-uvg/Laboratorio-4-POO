import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Cooperativa {

    private LocalDate fechaOperacion;
    private List<Bodega> bodegas;
    private List<Lote> historialLotes; // guarda todos los lotes ingresados para evitar IDs repetidos

    // listas auxiliares para llevar control financiero por categoría y mayor orden
    private List<String> categoriasRegistradas;
    private List<Double> totalesPagados;
    private List<Double> totalesCobrados;
    private List<Double> totalesDePerdidas;

    public Cooperativa(LocalDate fechaInicial) {
        if (fechaInicial == null) {
            throw new IllegalArgumentException("La fecha inicial no puede ser nula.");
        }
        this.fechaOperacion = fechaInicial;
        this.bodegas = new ArrayList<>(); // se inicializan las listas en constructor
        this.historialLotes = new ArrayList<>();
        this.categoriasRegistradas = new ArrayList<>();
        this.totalesPagados = new ArrayList<>();
        this.totalesCobrados = new ArrayList<>();
        this.totalesDePerdidas = new ArrayList<>();

        // se registran las categorías iniciales en cero
        registrarCategoria("Café en pergamino");
        registrarCategoria("Café orgánico");
        registrarCategoria("Cardamomo");
        registrarCategoria("Hortaliza");
        registrarCategoria("Queso fresco");
    }

    // método auxiliar para agregar una categoría si aún no existe en las listas financieras (por si se quiere expandir el negocio a otros productos)
    private int obtenerIndiceCategoria(String categoria) {
        for (int i = 0; i < categoriasRegistradas.size(); i++) {
            if (categoriasRegistradas.get(i).equalsIgnoreCase(categoria.trim())) {
                return i;
            }
        }
        registrarCategoria(categoria.trim());
        return categoriasRegistradas.size() - 1;
    }

    private void registrarCategoria(String categoria) {
        categoriasRegistradas.add(categoria);
        totalesPagados.add(0.0); //todo comienza en ceros en un inicio
        totalesCobrados.add(0.0);
        totalesDePerdidas.add(0.0);
    }

    // getters y setters
    public LocalDate getFechaOperacion() {
        return fechaOperacion;
    }

    public List<Bodega> getBodegas() {
        return Collections.unmodifiableList(bodegas); // se devuelve lista protegida (recomendación de IA)
    }

    public List<String> getCategoriasRegistradas() {
        return Collections.unmodifiableList(categoriasRegistradas);
    }

    // lógica para registrar y buscar bodegas
    public void registrarBodega(Bodega bodega) {
        if (bodega == null) {
            throw new IllegalArgumentException("La bodega no puede ser nula.");
        }
        if (bodegas.contains(bodega)) {
            throw new IllegalArgumentException("Ya existe una bodega con el ID: " + bodega.getID());
        }
        bodegas.add(bodega);
    }

    public Bodega buscarBodega(String IDBodega) {
        if (IDBodega == null || IDBodega.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la bodega no puede estar vacío.");
        }
        for (Bodega b : bodegas) {
            if (b.getID().equalsIgnoreCase(IDBodega.trim())) {
                return b;
            }
        }
        throw new IllegalArgumentException("No existe ninguna bodega con el ID: " + IDBodega);
    }

    // lógica pre-liquidación sin modificar pagos ni inventarios
    public String preliquidar(Lote lote) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote no puede ser nulo.");
        }
        if (historialLotes.contains(lote)) {
            throw new IllegalArgumentException("Ya existe un lote registrado con el ID: " + lote.getID());
        }

        String infoVencimiento;
        if (lote.esCaducable()) {
            infoVencimiento = (lote.getFechaCaducidad().toString()); //se pasa fecha a string para mostrarla en el reporte
        } else {
            infoVencimiento = "No vence";
        }

        return String.format("Pre-liquidación [%s - %s] | Pago estimado: Q%.2f | Vencimiento: %s", //sintaxis porcentajes y formato de cantidades a 2 decimales
                lote.getID(), lote.getCategoriaProd(), lote.calcularPago(), infoVencimiento);
    }

    // lógica recepción y almacenamiento de lotes en bodega
    public void recibirLote(Lote lote, String idBodega, boolean registrarPago) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote no puede ser nulo.");
        }
        if (historialLotes.contains(lote)) {
            throw new IllegalArgumentException("Ya existe un lote registrado con el ID: " + lote.getID());
        }

        Bodega bodega = buscarBodega(idBodega);
        bodega.agregarLote(lote); // valida compatibilidad y capacidad antes de guardar
        historialLotes.add(lote);

        if (registrarPago) {
            int indice = obtenerIndiceCategoria(lote.getCategoriaProd());
            double nuevoTotal = totalesPagados.get(indice) + lote.calcularPago();
            totalesPagados.set(indice, nuevoTotal);
        }
    }

    // lógica para pedidos y despachos en orden de salida
    public double despacharPedido(String categoria, double kgPedidos) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar una categoría válida.");
        }
        if (kgPedidos <= 0) {
            throw new IllegalArgumentException("Los kilogramos pedidos deben ser mayores a cero.");
        }

        // se reúnen todos los lotes disponibles de esa categoría en todas las bodegas
        List<Lote> candidatos = new ArrayList<>();
        double stockTotal = 0; //se inicia en cero antes de recorrer todas las bodegas y sumar el stock disponible  

        for (Bodega b : bodegas) {
            for (Lote lote : b) { // recorre bodega usando Iterable
                if (lote.getCategoriaProd().equalsIgnoreCase(categoria.trim())
                        && lote.getEstado() == Estado.DISPONIBLE) {
                    candidatos.add(lote);
                    stockTotal += lote.getPesoDisponible();
                }
            }
        }

        // si no alcanza el stock entre todas las bodegas, se rechaza sin descontar nada
        if (candidatos.isEmpty() || stockTotal < kgPedidos) {
            throw new IllegalArgumentException(String.format(
                "Pedido rechazado: se solicitaron %.2f kg de %s, pero solo hay %.2f kg disponibles.",
                kgPedidos, categoria, stockTotal));
        }

        Collections.sort(candidatos); // se ordenan globalmente según lo establecido en compareTo

        double restante = kgPedidos;
        double totalCobrado = 0;
        String nombreCategoria = candidatos.get(0).getCategoriaProd();

        for (Lote lote : candidatos) {
            if (restante <= 0.0001) { // margen de error para evitar problemas de precisión con double
                restante = 0; 
                break;
            }
            double aTomar = Math.min(lote.getPesoDisponible(), restante);
            totalCobrado += lote.descontarPesoXLote(aTomar);
            restante -= aTomar;
        }

        // se retiran de las bodegas los lotes que quedaron agotados para liberar espacio
        for (Bodega b : bodegas) {
            Iterator<Lote> it = b.iterator();
            while (it.hasNext()) {
                if (it.next().getEstado() == Estado.AGOTADO) {
                    it.remove();
                }
            }
        }

        int indice = obtenerIndiceCategoria(nombreCategoria);
        totalesCobrados.set(indice, totalesCobrados.get(indice) + totalCobrado);

        return totalCobrado;
    }

    // avanzar fecha de operación y descartar vencidos mientras se recorre cada bodega
    public int avanzarFecha(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("Solo se puede avanzar la fecha uno o más días hacia adelante.");
        }
        fechaOperacion = fechaOperacion.plusDays(dias);
        int descartados = 0;

        for (Bodega b : bodegas) {
            Iterator<Lote> it = b.iterator();
            while (it.hasNext()) {
                Lote lote = it.next();
                if (lote.esCaducable() && lote.estaCaducado(fechaOperacion)) {
                    double perdida = lote.descartar();
                    int indice = obtenerIndiceCategoria(lote.getCategoriaProd());
                    totalesDePerdidas.set(indice, totalesDePerdidas.get(indice) + perdida);
                    it.remove(); // elimina el lote vencido de la bodega mientras se recorre
                    descartados++;
                }
            }
        }
        return descartados;
    }

    // consultas y reportes
    public List<Lote> getProximosAVencer(int diasMargen) {
        List<Lote> proximos = new ArrayList<>();
        LocalDate limite = fechaOperacion.plusDays(diasMargen);

        for (Bodega b : bodegas) {
            for (Lote lote : b) {
                if (lote.esCaducable()) {
                    LocalDate vence = (lote.getFechaCaducidad());
                    if (!vence.isBefore(fechaOperacion) && !vence.isAfter(limite)) {
                        proximos.add(lote);
                    }
                }
            }
        }
        Collections.sort(proximos); // del que vence primero al que vence después
        return proximos;
    }

    public double getTotalPagado(String categoria) {
        int indice = obtenerIndiceCategoria(categoria);
        return totalesPagados.get(indice);
    }

    public double getTotalCobrado(String categoria) {
        int indice = obtenerIndiceCategoria(categoria);
        return totalesCobrados.get(indice);
    }

    public double getTotalPerdidas(String categoria) {
        int indice = obtenerIndiceCategoria(categoria);
        return totalesDePerdidas.get(indice);
    }


    // Carga inicial pedida de 3 bodegas y 10 lotes (2 por categoría) sin afectar finanzas y con datos variados para pruebas con el programa
    public void cargarDatosIniciales() {
        LocalDate hoy = this.fechaOperacion;

        // 3 bodegas iniciales (2 secas y 1 cámara fría)
        registrarBodega(new Bodega("BOD-SECA-1", 2000.0, false));
        registrarBodega(new Bodega("BOD-SECA-2", 1500.0, false));
        registrarBodega(new Bodega("CAM-FRIA-1", 1000.0, true));

        // 2 lotes de café en pergamino
        recibirLote(new CafPergamino("CP-001", "Carlos Méndez", hoy.minusDays(5), 500.0, Calidad.A, 13.4), "BOD-SECA-1", false);
        recibirLote(new CafPergamino("CP-002", "Nora Ramos", hoy.minusDays(2), 300.0, Calidad.B, 11.0), "BOD-SECA-2", false);

        // 2 lotes de café orgánico
        recibirLote(new CafOrganico("CO-001", "Finca El Mirador", hoy.minusDays(4), 400.0, Calidad.A, 12.5, "Mayacert", hoy.plusMonths(8)), "BOD-SECA-1", false);
        recibirLote(new CafOrganico("CO-002", "Finca El Chortoy", hoy.minusDays(1), 250.0, Calidad.B, 10.5, "BioLatino", hoy.plusMonths(5)), "BOD-SECA-2", false);

        // 2 lotes de cardamomo (misma fecha de vencimiento para desempate por calidad)
        recibirLote(new Cardamomo("CA-001", "Pedro Xol", hoy.minusDays(10), 100.0, Calidad.B, 82.0), "BOD-SECA-1", false);
        recibirLote(new Cardamomo("CA-002", "María Caal", hoy.minusDays(10), 80.0, Calidad.A, 90.0), "BOD-SECA-2", false);

        // 2 lotes de hortaliza (HO-001 vence en 2 días)
        recibirLote(new Hortaliza("HO-001", "José Ajú", hoy.minusDays(3), 200.0, Calidad.A, 5), "CAM-FRIA-1", false);
        recibirLote(new Hortaliza("HO-002", "Elena Tzotz", hoy, 150.0, Calidad.B, 10), "CAM-FRIA-1", false);

        // 2 lotes de queso fresco
        recibirLote(new QuesoFresco("QF-001", "Lácteos del Valle", hoy.minusDays(5), 40.0, Calidad.A, 28.0), "CAM-FRIA-1", false);
        recibirLote(new QuesoFresco("QF-002", "Granja San Pedro", hoy.minusDays(1), 60.0, Calidad.B, 22.0), "CAM-FRIA-1", false);
    }
}