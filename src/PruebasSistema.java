import java.time.LocalDate;

public class PruebasSistema {

    public static void main(String[] args) {
        System.out.println("=== EJECUCIÓN DE CASOS DE PRUEBA - ACOPIOGT ===\n");

        probarReglasDePago();
        probarVencimientosYPerdidas();
        probarRestriccionesBodegas();
        probarOrdenSalidaYDespachos();
        probarOperacionesSinEfecto();

        System.out.println("\n=== FIN DE LAS PRUEBAS ===");
    }

    private static void verificar(String caso, String entrada, String esperado, String obtenido, boolean paso) {
        System.out.println("CASO: " + caso);
        System.out.println("  Entrada:   " + entrada);
        System.out.println("  Esperado:  " + esperado);
        System.out.println("  Obtenido:  " + obtenido);
        System.out.println("  Estado:    " + (paso ? "[CORRECTO]" : "[INCORRECTO]"));
        System.out.println("--------------------------------------------------");
    }

    private static void probarReglasDePago() {
        LocalDate hoy = LocalDate.of(2026, 3, 1);

        // 1. Café con humedad por fracción (500 kg, Calidad A, 13.4% -> descuenta 6% = Q5,640.00)
        Lote cafe = new CafPergamino("C-01", "Juan", hoy, 500, Calidad.A, 13.4);
        double pagoCafe = cafe.calcularPago();
        verificar("Pago Café en pergamino con fracción de humedad", "500 kg, Calidad A, 13.4% humedad", "Q5640.00", String.format("Q%.2f", pagoCafe), Math.abs(pagoCafe - 5640.00) < 0.01);

        // 2. Café orgánico (mismos datos + Q0.75/kg = Q6,015.00)
        Lote organico = new CafOrganico("CO-01", "Ana", hoy, 500, Calidad.A, 13.4, "Mayacert", hoy.plusMonths(6));
        double pagoOrg = organico.calcularPago();
        verificar("Pago Café orgánico con bonificación", "500 kg, Calidad A, 13.4% humedad, sello vigente", "Q6015.00", String.format("Q%.2f", pagoOrg), Math.abs(pagoOrg - 6015.00) < 0.01);

        // 3. Cardamomo con bono grano verde >= 85% (80 kg, Calidad B, 90% -> Q4,224.00)
        Lote cardamomo = new Cardamomo("CAR-01", "Luis", hoy, 80, Calidad.B, 90.0);
        double pagoCard = cardamomo.calcularPago();
        verificar("Pago Cardamomo con bono de grano verde", "80 kg, Calidad B, 90% grano verde", "Q4224.00", String.format("Q%.2f", pagoCard), Math.abs(pagoCard - 4224.00) < 0.01);

        // 4. Hortaliza con merma del 5% (200 kg, Calidad A -> se pagan 190 kg = Q760.00)
        Lote hortaliza = new Hortaliza("HOR-01", "Rosa", hoy, 200, Calidad.A, 7);
        double pagoHort = hortaliza.calcularPago();
        verificar("Pago Hortaliza con merma del 5%", "200 kg, Calidad A, 7 días vida útil", "Q760.00", String.format("Q%.2f", pagoHort), Math.abs(pagoHort - 760.00) < 0.01);

        // 5. Queso fresco con descuento de empaque (40 kg, Calidad A -> Q1,460.00)
        Lote queso = new QuesoFresco("QUE-01", "Pedro", hoy, 40, Calidad.A, 25.0);
        double pagoQueso = queso.calcularPago();
        verificar("Pago Queso fresco con descuento de empaque", "40 kg, Calidad A, 25% grasa", "Q1460.00", String.format("Q%.2f", pagoQueso), Math.abs(pagoQueso - 1460.00) < 0.01);
    }

    private static void probarVencimientosYPerdidas() {
        LocalDate inicio = LocalDate.of(2026, 3, 5);
        Cooperativa coop = new Cooperativa(inicio);
        coop.registrarBodega(new Bodega("FRIA-1", 1000, true));

        // Hortaliza 200 kg Calidad A, vida útil 7 días -> vence 12 marzo, caduca 13 marzo (Costo = Q3.80/kg)
        Lote hort = new Hortaliza("HOR-10", "Rosa", inicio, 200, Calidad.A, 7);
        coop.recibirLote(hort, "FRIA-1", true);
        coop.despacharPedido("Hortaliza", 150); // Quedan 50 kg

        int descartadosDiaVence = coop.avanzarFecha(7);
        verificar("Lote en su día exacto de vencimiento sigue disponible", "Ingreso 5-mar, vida útil 7 días, actual 12-mar", "0 descartados, estado DISPONIBLE", descartadosDiaVence + " descartados, estado " + hort.getEstado(), descartadosDiaVence == 0 && hort.getEstado() == Estado.DISPONIBLE);

        int descartadosDiaSig = coop.avanzarFecha(1);
        double perdida = coop.getTotalPerdidas("Hortaliza");
        verificar("Descarte al día siguiente del vencimiento y cálculo de pérdida", "Avanzar a 13-mar con 50 kg restantes (Q3.80/kg)", "1 descartado, estado DESCARTADO, pérdida Q190.00", String.format("%d descartado, estado %s, pérdida Q%.2f", descartadosDiaSig, hort.getEstado(), perdida), descartadosDiaSig == 1 && hort.getEstado() == Estado.DESCARTADO && Math.abs(perdida - 190.00) < 0.01);
    }

    private static void probarRestriccionesBodegas() {
        LocalDate hoy = LocalDate.of(2026, 3, 1);
        Cooperativa coop = new Cooperativa(hoy);
        coop.registrarBodega(new Bodega("SECA-1", 100, false));
        coop.registrarBodega(new Bodega("FRIA-1", 100, true));

        double pagoInicialQueso = coop.getTotalPagado("Queso fresco");
        double pagoInicialCafe = coop.getTotalPagado("Café en pergamino");

        // 1. Bodegas cruzadas: Queso (frío) en seca y Café (seco) en fría
        boolean rechazoFrioEnSeco = false;
        try { coop.recibirLote(new QuesoFresco("QUE-99", "Mario", hoy, 20, Calidad.A, 20), "SECA-1", true); } 
        catch (IllegalArgumentException e) { rechazoFrioEnSeco = true; }

        boolean rechazoSecoEnFrio = false;
        try { coop.recibirLote(new CafPergamino("CAF-99", "Mario", hoy, 20, Calidad.A, 12.0), "FRIA-1", true); } 
        catch (IllegalArgumentException e) { rechazoSecoEnFrio = true; }

        boolean finanzasIntactas = (coop.getTotalPagado("Queso fresco") == pagoInicialQueso) && (coop.getTotalPagado("Café en pergamino") == pagoInicialCafe);

        verificar("Rechazo cruzado de tipos de bodega y estado financiero", "Queso en seca y Café en fría", "Operaciones rechazadas, pagos inalterados", (rechazoFrioEnSeco && rechazoSecoEnFrio) ? "Rechazadas y pagos inalterados" : "Fallo en validación", rechazoFrioEnSeco && rechazoSecoEnFrio && finanzasIntactas);

        // 2. Capacidad acumulada
        coop.recibirLote(new CafPergamino("CAF-OK", "Luis", hoy, 60, Calidad.A, 11.0), "SECA-1", true);
        boolean rechazoAcumulado = false;
        try {
            // Intentar meter 50kg extra en bodega de 100kg que ya tiene 60kg
            coop.recibirLote(new CafPergamino("CAF-EXCESO", "Luis", hoy, 50, Calidad.A, 11.0), "SECA-1", true);
        } catch (IllegalArgumentException e) {
            rechazoAcumulado = true;
        }

        Bodega seca1 = coop.buscarBodega("SECA-1");
        verificar("Rechazo por capacidad acumulada de bodega", "Meter 50kg en bodega de 100kg con 60kg ocupados", "Rechazado, ocupación mantenida en 60kg", "Rechazado: " + rechazoAcumulado + ", Ocupación: " + seca1.getPesoOcupado() + "kg", rechazoAcumulado && Math.abs(seca1.getPesoOcupado() - 60.0) < 0.01);
    }

   private static void probarOrdenSalidaYDespachos() {
        LocalDate hoy = LocalDate.of(2026, 3, 1);
        Cooperativa coop = new Cooperativa(hoy);
        coop.registrarBodega(new Bodega("FRIA-1", 500, true));

        // DEMOSTRACIÓN DE DESEMPATE EN CASCADA (Reglas de compareTo):
        // H1: Ingresa hoy, vida 10 días -> Vence en 10 días, Calidad A
        Lote h1 = new Hortaliza("HOR-A10", "A", hoy, 50, Calidad.A, 10);
        
        // H2: Ingresó hace 1 día, vida 3 días -> Vence en 2 días, Calidad B
        Lote h2 = new Hortaliza("HOR-B2", "B", hoy.minusDays(1), 50, Calidad.B, 3);
        
        // H3: Ingresó hace 2 días, vida 3 días -> Vence en 1 día, Calidad B
        Lote h3 = new Hortaliza("HOR-B1", "C", hoy.minusDays(2), 50, Calidad.B, 3);

        coop.recibirLote(h1, "FRIA-1", false);
        coop.recibirLote(h2, "FRIA-1", false);
        coop.recibirLote(h3, "FRIA-1", false);

        // Despachar 20kg: Debe salir de H3 (Vence en 1 día, le gana a todos)
        coop.despacharPedido("Hortaliza", 20);
        verificar("Desempate de salida: Vencimiento próximo vence a Calidad", 
            "H3(vence mañana), H2(vence en 2d), H1(vence en 10d Cal.A)", 
            "H3 pierde 20kg, H2 y H1 intactos", 
            String.format("H3 disp: %.2fkg, H2 disp: %.2fkg", h3.getPesoDisponible(), h2.getPesoDisponible()), 
            Math.abs(h3.getPesoDisponible() - 30.0) < 0.01 && h2.getPesoDisponible() == 50.0);

        // Despachar 40kg más: Debe agotar H3 (30kg) y tomar 10kg de H2 (vence antes que H1)
        coop.despacharPedido("Hortaliza", 40);
        verificar("Desempate continuo en despacho combinado", 
            "Despachar 40kg restantes", 
            "H3 agotado, H2 pierde 10kg, H1 intacto", 
            String.format("H3 %s, H2 disp: %.2fkg", h3.getEstado(), h2.getPesoDisponible()), 
            h3.getEstado() == Estado.AGOTADO && Math.abs(h2.getPesoDisponible() - 40.0) < 0.01 && Math.abs(h1.getPesoDisponible() - 50.0) < 0.01);

        // EXTRA: Comprobación directa de Perecedero vs No Perecedero
        Lote cafe = new CafPergamino("CAF-Z", "X", hoy, 50, Calidad.A, 12); // No perecedero
        boolean perecederoGana = h1.compareTo(cafe) < 0; // H1 (Hortaliza) debe ordenarse antes que Café
        verificar("Desempate: Perecedero siempre va antes que No Perecedero", 
            "Comparar Hortaliza vs Café", 
            "Hortaliza gana (retorna negativo)", 
            perecederoGana ? "Hortaliza gana" : "Fallo en orden", 
            perecederoGana);
    }
    
    private static void probarOperacionesSinEfecto() {
        LocalDate hoy = LocalDate.of(2026, 3, 1);
        Cooperativa coop = new Cooperativa(hoy);
        coop.registrarBodega(new Bodega("SECA-1", 500, false));

        // 1. Pre-liquidación
        Lote prospecto = new CafPergamino("CAF-PRE", "Carlos", hoy, 100, Calidad.A, 11.0);
        coop.preliquidar(prospecto);
        verificar("Pre-liquidación no modifica pagos ni inventarios", "Pre-liquidar CAF-PRE de 100 kg", "Pagado Q0.00, bodega vacía", String.format("Pagado Q%.2f, bodega vacía: %b", coop.getTotalPagado("Café en pergamino"), coop.getBodegas().get(0).estaVacia()), coop.getTotalPagado("Café en pergamino") == 0.0 && coop.getBodegas().get(0).estaVacia());

        // 2. Pedido rechazado
        Lote real = new CafPergamino("CAF-REAL", "Carlos", hoy, 100, Calidad.A, 11.0);
        coop.recibirLote(real, "SECA-1", true);
        double cobrosAntes = coop.getTotalCobrado("Café en pergamino");
        boolean pedidoRechazado = false;
        try { coop.despacharPedido("Café en pergamino", 250); } 
        catch (IllegalArgumentException e) { pedidoRechazado = true; }
        
        verificar("Pedido insuficiente rechazado sin modificar cobros", "Solicitar 250 kg de 100 kg", "Rechazado, stock 100kg, cobros inalterados", String.format("Rechazado: %b, Stock: %.2fkg", pedidoRechazado, real.getPesoDisponible()), pedidoRechazado && Math.abs(real.getPesoDisponible() - 100.0) < 0.01 && coop.getTotalCobrado("Café en pergamino") == cobrosAntes);

        // 3. Queso calidad C y retroceso de fecha
        double pagosAntes = coop.getTotalPagado("Queso fresco");
        boolean quesoCRechazado = false;
        try { new QuesoFresco("QUE-C", "Marta", hoy, 10, Calidad.C, 20); } 
        catch (IllegalArgumentException e) { quesoCRechazado = true; }
        
        boolean fechaRetrocesoRechazada = false;
        try { coop.avanzarFecha(-1); } 
        catch (IllegalArgumentException e) { fechaRetrocesoRechazada = true; }

        verificar("Rechazo de validaciones críticas explícitas", "Crear Queso C y avanzarFecha(-1)", "Ambas rechazadas, pagos Q0.00", String.format("Rechazos: %b, Pagos intactos: %b", (quesoCRechazado && fechaRetrocesoRechazada), (coop.getTotalPagado("Queso fresco") == pagosAntes)), quesoCRechazado && fechaRetrocesoRechazada && coop.getTotalPagado("Queso fresco") == pagosAntes);
    }
}
