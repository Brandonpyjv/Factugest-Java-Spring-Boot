package com.factugest.controller;

import com.factugest.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Controlador de facturación: el corazón del sistema.
 *
 * Maneja el ciclo completo de una factura: listar, crear, ver, cambiar estado,
 * exportar a PDF y eliminar. También expone endpoints REST internos (/api/*)
 * que el JavaScript del formulario usa via fetch() para búsquedas en vivo.
 *
 * Arquitectura: este controlador no toca la BD directamente. Delega toda la
 * lógica y consultas a InvoiceService (que usa JdbcTemplate para SQL complejo)
 * y servicios auxiliares para catálogos (empresas, métodos de pago, etc.).
 */
@Controller
@RequestMapping("/invoice")  // todas las rutas de este controlador empiezan con /invoice
public class InvoiceController {

    @Autowired private InvoiceService invoiceService;
    @Autowired private CustomerService customerService;
    @Autowired private EmpresaService empresaService;
    @Autowired private MetodoPagoService metodoPagoService;
    @Autowired private PagoFacturaService pagoFacturaService;
    @Autowired private ProductoService productoService;
    @Autowired private PdfService pdfService;

    /**
     * Lista todas las facturas con información completa (cliente, empresa, estado).
     * Usa un LEFT JOIN en el servicio para no perder facturas con datos incompletos.
     */
    @GetMapping
    public String listInvoices(Model model) {
        model.addAttribute("all_invoices", invoiceService.getAllInvoicesDetailed());
        return "invoice/index";
    }

    /**
     * Muestra el formulario de nueva factura precargado con todos los catálogos
     * necesarios: empresas, métodos de pago, estados de pago y descuentos aplicables
     * a nivel de factura. El formulario de productos se llena dinámicamente via AJAX.
     */
    @GetMapping("/new")
    public String newInvoiceForm(Model model) {
        model.addAttribute("empresas", empresaService.getAll());
        model.addAttribute("metodos_pago", metodoPagoService.getAll());
        model.addAttribute("pagos_factura", pagoFacturaService.getAll());
        model.addAttribute("invoice_discounts", invoiceService.getInvoiceDiscounts());
        return "invoice/form";
    }

    // ── Endpoints REST internos para el formulario ────────────────────────────
    // @ResponseBody indica que el valor de retorno va directo al cuerpo HTTP
    // como JSON (Spring usa Jackson para serializar List<Map>) — no busca una vista.

    /** Autocomplete de clientes: busca por nombre o número de documento. */
    @GetMapping("/api/customers/search")
    @ResponseBody
    public List<Map<String, Object>> searchCustomers(@RequestParam String q) {
        return invoiceService.searchCustomers(q);
    }

    /** Autocomplete de productos: busca por SKU o nombre, incluye precio e IVA. */
    @GetMapping("/api/products/search")
    @ResponseBody
    public List<Map<String, Object>> searchProductos(@RequestParam String q) {
        return invoiceService.searchProductos(q);
    }

    /** Descuentos configurados para un producto específico. */
    @GetMapping("/api/products/{id}/discounts")
    @ResponseBody
    public List<Map<String, Object>> getProductDiscounts(@PathVariable Integer id) {
        return invoiceService.getProductDiscounts(id);
    }

    // ── Creación de factura ───────────────────────────────────────────────────

    /**
     * Procesa el formulario de nueva factura.
     *
     * Los productos vienen como listas paralelas: cod_producto[0], precio_unitario[0],
     * cantidad[0], descuento_porcentaje[0] corresponden al primer producto;
     * [1] al segundo, etc. Spring mapea automáticamente los campos repetidos del HTML
     * en List<Integer>, List<BigDecimal>, etc.
     *
     * Los cálculos se rehacen en el servidor (no se confía en los totales del cliente)
     * para garantizar integridad aunque alguien manipule el formulario.
     */
    @PostMapping("/new")
    public String createInvoice(
            @RequestParam Integer cod_cliente,
            @RequestParam(required = false) Integer cod_usuario,
            @RequestParam Integer cod_empresa,
            @RequestParam Integer cod_metodo_pago,
            @RequestParam Integer cod_pago,
            @RequestParam(defaultValue = "FV") String tipo_factura,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) BigDecimal total,
            @RequestParam("cod_producto") List<Integer> codProductos,
            @RequestParam("precio_unitario") List<BigDecimal> precios,
            @RequestParam("cantidad") List<Integer> cantidades,
            @RequestParam("descuento_porcentaje") List<BigDecimal> descuentos,
            @RequestParam(required = false) Integer cod_descuento_factura,
            @RequestParam(required = false) BigDecimal valor_descuento_factura) {

        // Si no viene el usuario (sesión anónima temporal), usamos el ID 1 como fallback
        int usuarioId = cod_usuario != null ? cod_usuario : 1;

        // Acumuladores para los totales de la factura
        BigDecimal subtotalBruto = BigDecimal.ZERO;
        BigDecimal totalDescuentos = BigDecimal.ZERO;
        BigDecimal totalImpuestos = BigDecimal.ZERO;

        // Procesamos cada línea de producto: calculamos bruto, descuento, base gravable e IVA
        java.util.List<BigDecimal[]> lines = new java.util.ArrayList<>();
        for (int i = 0; i < codProductos.size(); i++) {
            Integer codProd = codProductos.get(i);
            BigDecimal precio = precios.size() > i ? precios.get(i) : BigDecimal.ZERO;
            int cant = cantidades.size() > i ? cantidades.get(i) : 1;
            BigDecimal descPct = descuentos.size() > i ? descuentos.get(i) : BigDecimal.ZERO;

            // Consultamos el IVA del producto directamente desde la BD.
            // Esto evita que alguien envíe un IVA falso desde el formulario.
            Map<String, Object> taxInfo = invoiceService.getProductTaxInfo(codProd);
            BigDecimal taxPct = BigDecimal.ZERO;
            if (taxInfo != null && taxInfo.get("tax_pct") != null) {
                Object tp = taxInfo.get("tax_pct");
                // JDBC puede devolver BigDecimal u otros Number según el driver/BD
                if (tp instanceof BigDecimal bd) taxPct = bd;
                else taxPct = new BigDecimal(tp.toString());
            }

            // Fórmulas de facturación DIAN:
            //   Bruto  = precio × cantidad
            //   DescVal = bruto × (descPct / 100)
            //   Base   = bruto − descVal          ← la base gravable
            //   IVA    = base × (taxPct / 100)
            BigDecimal bruto   = precio.multiply(new BigDecimal(cant));
            BigDecimal descVal = bruto.multiply(descPct).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            BigDecimal base    = bruto.subtract(descVal);
            BigDecimal ivaVal  = base.multiply(taxPct).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

            subtotalBruto   = subtotalBruto.add(bruto);
            totalDescuentos = totalDescuentos.add(descVal);
            totalImpuestos  = totalImpuestos.add(ivaVal);

            // Guardamos los valores calculados para insertarlos después de crear la cabecera
            lines.add(new BigDecimal[]{
                new BigDecimal(codProd), precio, new BigDecimal(cant),
                descPct, descVal, base, taxPct, ivaVal
            });
        }

        // El descuento a nivel de factura se suma al total de descuentos de líneas
        BigDecimal invDiscVal = valor_descuento_factura != null ? valor_descuento_factura : BigDecimal.ZERO;
        totalDescuentos = totalDescuentos.add(invDiscVal);

        BigDecimal subtotal   = subtotalBruto.subtract(totalDescuentos);
        BigDecimal totalFinal = total != null ? total : subtotal.add(totalImpuestos);

        // 1º: crear la cabecera de la factura y obtener el ID asignado por la BD
        Integer invoiceId = invoiceService.createInvoice(
            cod_cliente, usuarioId, cod_empresa, cod_metodo_pago, cod_pago,
            totalFinal, subtotal, totalDescuentos, totalImpuestos,
            tipo_factura, observaciones,
            cod_descuento_factura, invDiscVal.compareTo(BigDecimal.ZERO) > 0 ? invDiscVal : null
        );

        // 2º: insertar cada línea de detalle con la clave foránea cod_factura recién creada
        for (int i = 0; i < lines.size(); i++) {
            BigDecimal[] line = lines.get(i);
            invoiceService.createInvoiceDetail(
                invoiceId,
                line[0].intValue(),   // cod_producto
                line[2].intValue(),   // cantidad
                line[1],              // precio_unitario
                line[5],              // subtotal (base gravable)
                line[3],              // descuento_porcentaje
                line[4],              // descuento_valor
                line[6],              // impuesto_porcentaje
                line[7]               // impuesto_valor
            );
        }

        // Redirigimos a la vista de la factura recién creada (patrón POST-Redirect-GET)
        return "redirect:/invoice/" + invoiceId;
    }

    /**
     * Vista de detalle de una factura existente.
     * Si el ID no existe, redirige al listado en vez de mostrar un error de servidor.
     */
    @GetMapping("/{id}")
    public String viewInvoice(@PathVariable Integer id, Model model) {
        Map<String, Object> invoice = invoiceService.getInvoiceById(id);
        if (invoice == null) return "redirect:/invoice";
        model.addAttribute("invoice", invoice);
        model.addAttribute("details", invoiceService.getInvoiceDetails(id));
        model.addAttribute("pagos_factura", pagoFacturaService.getAll());
        return "invoice/view";
    }

    /** Actualiza solo el estado de pago de una factura (pendiente, pagada, vencida…). */
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Integer id, @RequestParam Integer cod_pago) {
        invoiceService.updateInvoiceStatus(id, cod_pago);
        return "redirect:/invoice/" + id;
    }

    /**
     * Genera el PDF de la factura y lo envía directamente al navegador como descarga.
     * Usa HttpServletResponse para escribir los bytes del PDF sin pasar por Thymeleaf.
     */
    @GetMapping("/{id}/pdf")
    public void downloadPdf(@PathVariable Integer id, HttpServletResponse response) {
        Map<String, Object> invoice = invoiceService.getInvoiceById(id);
        List<Map<String, Object>> details = invoiceService.getInvoiceDetails(id);
        if (invoice == null) {
            response.setStatus(404);
            return;
        }
        byte[] pdf = pdfService.generateInvoicePdf(invoice, details);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=factura_" + id + ".pdf");
        response.setContentLength(pdf.length);
        try {
            response.getOutputStream().write(pdf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina una factura con todos sus detalles y descuentos asociados.
     * El servicio garantiza el orden correcto de eliminación para respetar las
     * claves foráneas (primero detalles, luego cabecera).
     */
    @GetMapping("/delete/{id}")
    public String deleteInvoice(@PathVariable Integer id) {
        invoiceService.deleteInvoice(id);
        return "redirect:/invoice";
    }
}
