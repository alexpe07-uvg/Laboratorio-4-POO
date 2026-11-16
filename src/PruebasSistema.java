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

    // Método auxiliar para imprimir siempre el formato que pide el enunciado
    private static void verificar(String caso, String entrada, String esperado, String obtenido, boolean paso) {
        System.out.println("CASO: " + caso);
        System.out.println("  Entrada:   " + entrada);
        System.out.println("  Esperado:  " + esperado);
        System.out.println("  Obtenido:  " + obtenido);
        // Cambiamos el mensaje para que suene más a tu estilo
        System.out.println("  Estado:    " + (paso ? "[CORRECTO]" : "[INCORRECTO]"));
        System.out.println("--------------------------------------------------");
    }

    private static void probarReglasDePago() {
        LocalDate hoy = LocalDate.of(2026, 3, 1);

        // 1. Café con humedad por fracción (500 kg, Calidad A, 13.4% -> descuenta 6% = Q5,640.00)
        Lote cafe = new CafPergamino("C-01", "Juan", hoy, 500, Calidad.A, 13.4);
        double pagoCafe = cafe.calcularPago();
        verificar(
            "Pago Café en pergamino con fracción de humedad",
            "500 kg, Calidad A, 13.4% humedad",
            "Q5640.00",
            String.format("Q%.2f", pagoCafe),
            Math.abs(pagoCafe - 5640.00) < 0.01
        );

        // 2. Café orgánico (mismos datos + Q0.75/kg = Q6,015.00)
        Lote organico = new CafOrganico("CO-01", "Ana", hoy, 500, Calidad.A, 13.4, "Mayacert", hoy.plusMonths(6));
        double pagoOrg = organico.calcularPago();
        verificar(
            "Pago Café orgánico con bonificación",
            "500 kg, Calidad A, 13.4% humedad, sello vigente",
            "Q6015.00",
            String.format("Q%.2f", pagoOrg),
            Math.abs(pagoOrg - 6015.00) < 0.01
        );

        // 3. Cardamomo con bono grano verde >= 85% (80 kg, Calidad B, 90% -> Q4,224.00)
        Lote cardamomo = new Cardamomo("CAR-01", "Luis", hoy, 80, Calidad.B, 90.0);
        double pagoCard = cardamomo.calcularPago();
        verificar(
            "Pago Cardamomo con bono de grano verde",
            "80 kg, Calidad B, 90% grano verde",
            "Q4224.00",
            String.format("Q%.2f", pagoCard),
            Math.abs(pagoCard - 4224.00) < 0.01
        );

        // 4. Hortaliza con merma del 5% (200 kg, Calidad A -> se pagan 190 kg = Q760.00)
        Lote hortaliza = new Hortaliza("HOR-01", "Rosa", hoy, 200, Calidad.A, 7);
        double pagoHort = hortaliza.calcularPago();
        verificar(
            "Pago Hortaliza con merma del 5%",
            "200 kg, Calidad A, 7 días vida útil",
            "Q760.00",
            String.format("Q%.2f", pagoHort),
            Math.abs(pagoHort - 760.00) < 0.01
        );

        // 5. Queso fresco con descuento de empaque (40 kg, Calidad A -> Q1,460.00)
        Lote queso = new QuesoFresco("QUE-01", "Pedro", hoy, 40, Calidad.A, 25.0);
        double pagoQueso = queso.calcularPago();
        verificar(
            "Pago Queso fresco con descuento de empaque",
            "40 kg, Calidad A, 25% grasa",
            "Q1460.00",
            String.format("Q%.2f", pagoQueso),
            Math.abs(pagoQueso - 1460.00) < 0.01
        );
    }

    private static void probarVencimientosYPerdidas() {
        LocalDate inicio = LocalDate.of(2026, 3, 5);
        Cooperativa coop = new Cooperativa(inicio);
        coop.registrarBodega(new Bodega("FRIA-1", 1000, true));

        // Hortaliza 200 kg Calidad A, vida útil 7 días -> vence 12 marzo, caduca 13 marzo
        // Pago = Q760.00 -> costo/kg = Q3.80
        Lote hort = new Hortaliza("HOR-10", "Rosa", inicio, 200, Calidad.A, 7);
        coop.recibirLote(hort, "FRIA-1", true);

        // Despachamos 150 kg -> quedan 50 kg disponibles
        coop.despacharPedido("Hortaliza", 150);

        // Avanzamos 7 días (llegamos al 12 de marzo: el día de vencimiento AÚN NO está caducado)
        int descartadosDiaVence = coop.avanzarFecha(7);
        verificar(
            "Lote en su día exacto de vencimiento aún sigue disponible",
            "Ingreso 5-mar, vida útil 7 días, fecha actual 12-mar",
            "0 descartados, estado DISPONIBLE",
            descartadosDiaVence + " descartados, estado " + hort.getEstado(),
            descartadosDiaVence == 0 && hort.getEstado() == Estado.DISPONIBLE
        );

        // Avanzamos 1 día más (13 de marzo -> ya caducó; pérdida = 50 kg * Q3.80 = Q190.00)
        int descartadosDiaSig = coop.avanzarFecha(1);
        
        // CORRECCIÓN: Aquí se usa el nuevo método para obtener la pérdida por categoría
        double perdida = coop.getTotalPerdidas("Hortaliza");
        
        verificar(
            "Descarte al día siguiente del vencimiento y cálculo de pérdida",
            "Avanzar a 13-mar con 50 kg restantes (Q3.80/kg)",
            "1 descartado, estado DESCARTADO, pérdida Q190.00",
            String.format("%d descartado, estado %s, pérdida Q%.2f", descartadosDiaSig, hort.getEstado(), perdida),
            descartadosDiaSig == 1 && hort.getEstado() == Estado.DESCARTADO && Math.abs(perdida - 190.00) < 0.01
        );
    }

    private static void probarRestriccionesBodegas() {
        LocalDate hoy = LocalDate.of(2026, 3, 1);
        Cooperativa coop = new Cooperativa(hoy);
        coop.registrarBodega(new Bodega("SECA-1", 100, false));
        coop.registrarBodega(new Bodega("FRIA-1", 100, true));

        // 1. Producto refrigerable en bodega seca
        boolean rechazoTipo = false;
        try {
            Lote queso = new QuesoFresco("QUE-99", "Mario", hoy, 20, Calidad.A, 20);
            coop.recibirLote(queso, "SECA-1", true);
        } catch (IllegalArgumentException e) {
            rechazoTipo = true;
        }
        verificar(
            "Rechazo por incompatibilidad de bodega (Queso en bodega seca)",
            "Queso fresco 20 kg -> Bodega SECA-1",
            "Operación rechazada",
            rechazoTipo ? "Operación rechazada" : "Se permitió (ERROR)",
            rechazoTipo
        );

        // 2. Exceso de capacidad
        boolean rechazoCapacidad = false;
        try {
            Lote cafeGrande = new CafPergamino("CAF-99", "Luis", hoy, 150, Calidad.A, 11.0);
            coop.recibirLote(cafeGrande, "SECA-1", true);
        } catch (IllegalArgumentException e) {
            rechazoCapacidad = true;
        }
        verificar(
            "Rechazo por exceso de capacidad de bodega",
            "Café 150 kg -> Bodega SECA-1 (Capacidad 100 kg)",
            "Operación rechazada",
            rechazoCapacidad ? "Operación rechazada" : "Se permitió (ERROR)",
            rechazoCapacidad
        );
    }

    private static void probarOrdenSalidaYDespachos() {
        LocalDate hoy = LocalDate.of(2026, 3, 1);
        Cooperativa coop = new Cooperativa(hoy);
        coop.registrarBodega(new Bodega("SECA-1", 500, false));
        coop.registrarBodega(new Bodega("SECA-2", 500, false));

        // Ingresamos primero un café (no perecedero), luego cardamomo B en SECA-1 y cardamomo A en SECA-2 (ambos vencen igual)
        Lote cafe = new CafPergamino("CAF-01", "Pedro", hoy.minusDays(5), 100, Calidad.A, 10.0);
        Lote cardB = new Cardamomo("CAR-B", "Ana", hoy, 50, Calidad.B, 80.0); // Q48/kg
        Lote cardA = new Cardamomo("CAR-A", "Juan", hoy, 50, Calidad.A, 80.0); // Q60/kg

        coop.recibirLote(cafe, "SECA-1", true);
        coop.recibirLote(cardB, "SECA-1", true);
        coop.recibirLote(cardA, "SECA-2", true);

        // Pedido de 70 kg de Cardamomo: debe tomar 50 kg de CAR-A (SECA-2) y 20 kg de CAR-B (SECA-1)
        // Cobro esperado: (50 * 60 * 1.35) + (20 * 48 * 1.35) = 4050 + 1296 = Q5,346.00
        double cobro = coop.despacharPedido("Cardamomo", 70);
        boolean ordenCorrecto = (cardA.getEstado() == Estado.AGOTADO) && (Math.abs(cardB.getPesoDisponible() - 30.0) < 0.01);

        verificar(
            "Desempate por calidad entre bodegas y despacho combinado",
            "Pedido 70 kg Cardamomo (CAR-A 50kg en SECA-2 vs CAR-B 50kg en SECA-1)",
            "CAR-A agotado, CAR-B con 30.00 kg, cobro Q5346.00",
            String.format("CAR-A %s, CAR-B con %.2f kg, cobro Q%.2f", cardA.getEstado(), cardB.getPesoDisponible(), cobro),
            ordenCorrecto && Math.abs(cobro - 5346.00) < 0.01
        );
    }

    private static void probarOperacionesSinEfecto() {
        LocalDate hoy = LocalDate.of(2026, 3, 1);
        Cooperativa coop = new Cooperativa(hoy);
        coop.registrarBodega(new Bodega("SECA-1", 500, false));

        // 1. Pre-liquidación no altera pagos ni existencias
        Lote prospecto = new CafPergamino("CAF-PRE", "Carlos", hoy, 100, Calidad.A, 11.0);
        coop.preliquidar(prospecto);
        
        // CORRECCIÓN: Aquí se usa el nuevo método
        double pagadoTrasPre = coop.getTotalPagado("Café en pergamino");
        boolean bodegaSigueVacia = coop.getBodegas().get(0).estaVacia();

        verificar(
            "Pre-liquidación no modifica pagos ni inventarios",
            "Pre-liquidar CAF-PRE de 100 kg",
            "Pagado Q0.00, bodega vacía",
            String.format("Pagado Q%.2f, bodega %s", pagadoTrasPre, bodegaSigueVacia ? "vacía" : "con datos"),
            pagadoTrasPre == 0.0 && bodegaSigueVacia
        );

        // 2. Pedido rechazado no altera stock ni cobros
        Lote real = new CafPergamino("CAF-REAL", "Carlos", hoy, 100, Calidad.A, 11.0);
        coop.recibirLote(real, "SECA-1", true);

        boolean pedidoRechazado = false;
        try {
            coop.despacharPedido("Café en pergamino", 250); // Pide más de los 100 disponibles
        } catch (IllegalArgumentException e) {
            pedidoRechazado = true;
        }
        
        // CORRECCIÓN: Aquí se usa el nuevo método
        double cobrado = coop.getTotalCobrado("Café en pergamino");
        
        verificar(
            "Pedido insuficiente rechazado sin modificar stock ni cobros",
            "Solicitar 250 kg cuando solo hay 100 kg",
            "Rechazado, stock 100.00 kg, cobrado Q0.00",
            String.format("%s, stock %.2f kg, cobrado Q%.2f",
                pedidoRechazado ? "Rechazado" : "Aceptado", real.getPesoDisponible(), cobrado),
            pedidoRechazado && Math.abs(real.getPesoDisponible() - 100.0) < 0.01 && cobrado == 0.0
        );

        // 3. Queso calidad C y retroceso de fecha rechazados
        boolean quesoCRechazado = false;
        try {
            new QuesoFresco("QUE-C", "Marta", hoy, 10, Calidad.C, 20);
        } catch (IllegalArgumentException e) {
            quesoCRechazado = true;
        }
        boolean fechaRetrocesoRechazada = false;
        try {
            coop.avanzarFecha(-1);
        } catch (IllegalArgumentException e) {
            fechaRetrocesoRechazada = true;
        }
        verificar(
            "Rechazo de Queso calidad C y retroceso de fecha",
            "Crear Queso calidad C y avanzarFecha(-1)",
            "Ambas operaciones rechazadas",
            (quesoCRechazado && fechaRetrocesoRechazada) ? "Ambas operaciones rechazadas" : "Fallo en validación",
            quesoCRechazado && fechaRetrocesoRechazada
        );
    }
}