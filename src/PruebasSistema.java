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
        System.out.println("  Estado:    " + (paso ? "[OK - PASA]" : "[ERROR - FALLA]"));
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
        // Aquí instancias una Cooperativa limpia, recibes la hortaliza de 200 kg (Q760 -> Q3.80/kg),
        // despachas 150 kg (quedan 50 kg) y avanzas la fecha 8 días (1 día después de vencer).
        // Verificas que el lote pase a DESCARTADO, salga de bodega y la pérdida sea Q190.00.
    }

    private static void probarRestriccionesBodegas() {
        // Intentas meter una Hortaliza en bodega seca -> capturas la excepción (try-catch) y verificas rechazo.
        // Intentas meter un lote que supere la capacidad máxima de la bodega -> capturas excepción y verificas rechazo.
    }

    private static void probarOrdenSalidaYDespachos() {
        // Creas 2 bodegas secas y metes:
        // - Un café (no perecedero)
        // - Dos cardamomos que venzan el mismo día, uno Calidad B y otro Calidad A
        // Haces un pedido que jale peso de ambos cardamomos en dos bodegas distintas.
        // Verificas que salga primero el cardamomo Calidad A antes que el B, y antes que el café,
        // y que el cobro sea costoPorKg * 1.35.
    }

    private static void probarOperacionesSinEfecto() {
        // 1. Pides una pre-liquidación y verificas que totalPagado y existencias sigan en 0.
        // 2. Haces un pedido mayor al stock disponible -> capturas rechazo y verificas que el stock y totalCobrado no cambien.
        // 3. Intentas crear un QuesoFresco calidad C o retroceder la fecha -> capturas excepción y verificas estado intacto.
    }
} 
    

