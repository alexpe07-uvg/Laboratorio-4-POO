import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static void printMenu(LocalDate fecha) {
        System.out.println("\nBienvenido al sistema de gestión de AcopioGT");
        System.out.println("\n==========================================");
        System.out.println("  ACOPIOGT - Fecha de operación: " + fecha);
        System.out.println("==========================================");
        System.out.println("1. Pre-liquidar lote");
        System.out.println("2. Recibir y almacenar lote");
        System.out.println("3. Registrar nueva bodega");
        System.out.println("4. Registrar pedido y despachar");
        System.out.println("5. Avanzar fecha de operación");
        System.out.println("6. Consultar bodegas y lotes (en orden de salida)");
        System.out.println("7. Generar reportes gerenciales");
        System.out.println("8. Salir");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Cooperativa cooperativa = new Cooperativa(LocalDate.now()); //fecha actual
        cooperativa.cargarDatosIniciales(); // carga las 3 bodegas y 10 lotes iniciales estabecidos en clase Cooperativa

        boolean salir = false;
        int opcion;

        while (!salir) {
            printMenu(cooperativa.getFechaOperacion());
            System.out.println("Por favor, seleccione una opción del menú:");

            try {
                opcion = sc.nextInt();
                sc.nextLine(); // limpiar buffer

                switch (opcion) {
                    case 1:
                    case 2:
                        // opciones 1 y 2 piden los mismos datos del lote
                        System.out.println("\nCategorías disponibles:");
                        System.out.println("1. Café en pergamino");
                        System.out.println("2. Café orgánico");
                        System.out.println("3. Cardamomo");
                        System.out.println("4. Hortaliza");
                        System.out.println("5. Queso fresco");
                        System.out.print("Seleccione categoría (1-5): ");
                        int cat = sc.nextInt();
                        sc.nextLine();

                        if (cat < 1 || cat > 5) {
                            System.out.println("Categoría inválida.");
                            break;
                        }

                        System.out.print("ID del lote: ");
                        String idLote = sc.nextLine();

                        System.out.print("Nombre del productor: ");
                        String productor = sc.nextLine();

                        System.out.print("Peso recibido (kg): ");
                        double peso = sc.nextDouble();
                        sc.nextLine();

                        System.out.print("Calidad (A, B o C): ");
                        String calTexto = sc.nextLine().trim().toUpperCase();
                        Calidad calidad = Calidad.valueOf(calTexto);

                        LocalDate hoy = cooperativa.getFechaOperacion();
                        Lote lote = null;

                        if (cat == 1) {
                            System.out.print("Porcentaje de humedad (8 - 14): ");
                            double hum = sc.nextDouble();
                            sc.nextLine();
                            lote = new CafPergamino(idLote, productor, hoy, peso, calidad, hum);
                        } else if (cat == 2) {
                            System.out.print("Porcentaje de humedad (8 - 14): ");
                            double humOrg = sc.nextDouble();
                            sc.nextLine();
                            System.out.print("Entidad certificadora: ");
                            String entidad = sc.nextLine();
                            System.out.print("Fecha de validez del sello (AAAA-MM-DD): ");
                            LocalDate validez = LocalDate.parse(sc.nextLine().trim());
                            lote = new CafOrganico(idLote, productor, hoy, peso, calidad, humOrg, entidad, validez);
                        } else if (cat == 3) {
                            System.out.print("Porcentaje de grano verde (0 - 100): ");
                            double granoVerde = sc.nextDouble();
                            sc.nextLine();
                            lote = new Cardamomo(idLote, productor, hoy, peso, calidad, granoVerde);
                        } else if (cat == 4) {
                            System.out.print("Vida útil en días (3 - 14): ");
                            int vidaUtil = sc.nextInt();
                            sc.nextLine();
                            lote = new Hortaliza(idLote, productor, hoy, peso, calidad, vidaUtil);
                        } else if (cat == 5) {
                            System.out.print("Porcentaje de grasa (10 - 40): ");
                            double grasa = sc.nextDouble();
                            sc.nextLine();
                            lote = new QuesoFresco(idLote, productor, hoy, peso, calidad, grasa);
                        }

                        if (opcion == 1) {
                            // solo pre-liquidación
                            System.out.println("\n" + cooperativa.preliquidar(lote));
                        } else {
                            // recibir y almacenar en bodega
                            System.out.print("ID de la bodega destino: ");
                            String idBodega = sc.nextLine();
                            cooperativa.recibirLote(lote, idBodega, true);
                            System.out.printf("\nLote %s almacenado en %s. Pago al productor: Q%.2f%n",
                                    lote.getID(), idBodega, lote.calcularPago());
                        }
                        break;

                    case 3:
                        System.out.print("ID de la nueva bodega: ");
                        String idNuevaBodega = sc.nextLine();

                        System.out.print("Capacidad máxima (kg): ");
                        double capMax = sc.nextDouble();
                        sc.nextLine();

                        System.out.print("¿Es cámara fría? (S/N): ");
                        String respFria = sc.nextLine().trim().toUpperCase();
                        if (!respFria.equals("S") && !respFria.equals("N")) {
                            System.out.println("Debe responder S o N.");
                            break;
                        }

                        boolean esFria = respFria.equals("S");
                        cooperativa.registrarBodega(new Bodega(idNuevaBodega, capMax, esFria));
                        System.out.println("Bodega registrada correctamente.");
                        break;

                    case 4:
                        System.out.println("\nCategorías: Café en pergamino | Café orgánico | Cardamomo | Hortaliza | Queso fresco");
                        System.out.print("Escriba la categoría solicitada: ");
                        String catPedido = sc.nextLine();

                        System.out.print("Kilogramos solicitados: ");
                        double kgSolicitados = sc.nextDouble();
                        sc.nextLine();

                        double totalCobrado = cooperativa.despacharPedido(catPedido, kgSolicitados);
                        System.out.printf("Pedido despachado con éxito. Total cobrado: Q%.2f%n", totalCobrado);
                        break;

                    case 5:
                        System.out.print("¿Cuántos días desea avanzar?: ");
                        int dias = sc.nextInt();
                        sc.nextLine();

                        int descartados = cooperativa.avanzarFecha(dias);
                        System.out.println("Nueva fecha de operación: " + cooperativa.getFechaOperacion());
                        System.out.println("Lotes vencidos descartados: " + descartados);
                        break;

                    case 6:
                        System.out.println("\n--- CONTENIDO DE BODEGAS (EN ORDEN DE SALIDA) ---");
                        for (Bodega b : cooperativa.getBodegas()) {
                            String tipoBodega;
                            if (b.isEsCamaraFria()) {
                                tipoBodega = "Cámara Fría";
                            } else {
                                tipoBodega = "Bodega Seca";
                            }

                            System.out.printf("\nBodega: %s (%s) | Ocupado: %.2f / %.2f kg (Disponible: %.2f kg)%n",
                                    b.getID(), tipoBodega, b.getPesoOcupado(), b.getCapacidadMax(), b.getCapacidadDisponible());

                            if (b.estaVacia()) {
                                System.out.println("   (Sin lotes almacenados)");
                            } else {
                                for (Lote l : b) { // usa el Iterable de Bodega
                                    String infoVence;
                                    if (l.esCaducable()) {
                                        infoVence = " | Vence: " + ((Caducable) l).getFechaCaducidad();
                                    } else {
                                        infoVence = " | No vence";
                                    }

                                    System.out.printf("   -> [%s] %s | Calidad %s | Disp: %.2f kg | Ingreso: %s%s%n",
                                            l.getID(), l.getCategoriaProd(), l.getCalidad(), l.getPesoDisponible(), l.getFechaIngreso(), infoVence);
                                }
                            }
                        }
                        break;

                    case 7:
                        System.out.println("\n================ REPORTES GERENCIALES ================");

                        // 1. existencias por categoría
                        System.out.println("\n1. EXISTENCIAS POR CATEGORÍA:");
                        for (String c : cooperativa.getCategoriasRegistradas()) {
                            double totalKg = 0;
                            System.out.println("  * " + c + ":");
                            for (Bodega b : cooperativa.getBodegas()) {
                                for (Lote l : b) {
                                    if (l.getCategoriaProd().equalsIgnoreCase(c)) {
                                        System.out.printf("      - Lote %s (Bodega %s): %.2f kg disponibles%n",
                                                l.getID(), b.getID(), l.getPesoDisponible());
                                        totalKg += l.getPesoDisponible();
                                    }
                                }
                            }
                            System.out.printf("      Total %s: %.2f kg%n", c, totalKg);
                        }

                        // 2. perecederos que vencen en los próximos 3 días
                        System.out.println("\n2. LOTES PERECEDEROS QUE VENCEN EN LOS PRÓXIMOS 3 DÍAS:");
                        List<Lote> proximos = cooperativa.getProximosAVencer(3);
                        if (proximos.isEmpty()) {
                            System.out.println("  No hay lotes próximos a vencer en los siguientes 3 días.");
                        } else {
                            for (Lote l : proximos) {
                                System.out.printf("  - [%s] %s | Vence: %s | Disp: %.2f kg%n",
                                        l.getID(), l.getCategoriaProd(), ((Caducable) l).getFechaCaducidad(), l.getPesoDisponible());
                            }
                        }

                        // 3. resumen financiero por categoría
                        System.out.println("\n3. RESUMEN FINANCIERO POR CATEGORÍA:");
                        for (String c : cooperativa.getCategoriasRegistradas()) {
                            System.out.printf("  - %-18s | Pagado: Q%8.2f | Cobrado: Q%8.2f | Pérdidas: Q%8.2f%n",
                                    c, cooperativa.getTotalPagado(c), cooperativa.getTotalCobrado(c), cooperativa.getTotalPerdidas(c));
                        }
                        break;

                    case 8:
                        System.out.println("Saliendo del sistema AcopioGT...");
                        salir = true;
                        break;

                    default:
                        System.out.println("Opción no válida. Intente de nuevo.");
                        break;
                }

            } catch (InputMismatchException e) {
                System.out.println("\nERROR DE ENTRADA: Debe ingresar un número válido.");
                sc.nextLine(); // limpia lo que quedó mal escrito en el buffer
            } catch (DateTimeParseException e) {
                System.out.println("\nERROR DE FECHA: Use el formato AAAA-MM-DD (ejemplo: 2026-08-15).");
            } catch (IllegalArgumentException e) {
                System.out.println("\nOPERACIÓN RECHAZADA: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("\nERROR: " + e.getMessage());
            }
        }

        sc.close();
    }
}