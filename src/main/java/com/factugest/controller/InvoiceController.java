package com.factugest.controller;

import com.factugest.entity.Customer;
import com.factugest.entity.Empresa;
import com.factugest.entity.MetodoPago;
import com.factugest.entity.PagoFactura;
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

@Controller
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired private InvoiceService invoiceService;
    @Autowired private CustomerService customerService;
    @Autowired private EmpresaService empresaService;
    @Autowired private MetodoPagoService metodoPagoService;
    @Autowired private PagoFacturaService pagoFacturaService;
    @Autowired private ProductoService productoService;
    @Autowired private PdfService pdfService;

    @GetMapping
    public String listInvoices(Model model) {
        model.addAttribute("all_invoices", invoiceService.getAllInvoicesDetailed());
        return "invoice/index";
    }

    @GetMapping("/new")
    public String newInvoiceForm(Model model) {
        model.addAttribute("customers", customerService.getAll());
        model.addAttribute("empresas", empresaService.getAll());
        model.addAttribute("metodos_pago", metodoPagoService.getAll());
        model.addAttribute("pagos_factura", pagoFacturaService.getAll());
        model.addAttribute("productos", productoService.getAllDetailed());
        return "invoice/form";
    }

    @PostMapping("/new")
    public String createInvoice(
            @RequestParam Integer cod_cliente,
            @RequestParam(required = false) Integer cod_usuario,
            @RequestParam Integer cod_empresa,
            @RequestParam Integer cod_metodo_pago,
            @RequestParam Integer cod_pago,
            @RequestParam String fecha,
            @RequestParam(required = false) String fecha_vencimiento,
            @RequestParam(defaultValue = "FV") String tipo_factura,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) BigDecimal total,
            @RequestParam("cod_producto") List<Integer> codProductos,
            @RequestParam("precio_unitario") List<BigDecimal> precios,
            @RequestParam("cantidad") List<Integer> cantidades,
            @RequestParam("descuento_porcentaje") List<BigDecimal> descuentos) {

        // Use user 1 as default if not provided
        int usuarioId = cod_usuario != null ? cod_usuario : 1;

        // Calculate totals server-side
        BigDecimal subtotalBruto = BigDecimal.ZERO;
        BigDecimal totalDescuentos = BigDecimal.ZERO;
        BigDecimal totalImpuestos = BigDecimal.ZERO;

        // Pre-compute lines
        java.util.List<BigDecimal[]> lines = new java.util.ArrayList<>();
        for (int i = 0; i < codProductos.size(); i++) {
            Integer codProd = codProductos.get(i);
            BigDecimal precio = precios.size() > i ? precios.get(i) : BigDecimal.ZERO;
            int cant = cantidades.size() > i ? cantidades.get(i) : 1;
            BigDecimal descPct = descuentos.size() > i ? descuentos.get(i) : BigDecimal.ZERO;

            // Get tax info
            Map<String, Object> taxInfo = invoiceService.getProductTaxInfo(codProd);
            BigDecimal taxPct = BigDecimal.ZERO;
            if (taxInfo != null && taxInfo.get("tax_pct") != null) {
                Object tp = taxInfo.get("tax_pct");
                if (tp instanceof BigDecimal bd) taxPct = bd;
                else taxPct = new BigDecimal(tp.toString());
            }

            BigDecimal bruto = precio.multiply(new BigDecimal(cant));
            BigDecimal descVal = bruto.multiply(descPct).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            BigDecimal base = bruto.subtract(descVal);
            BigDecimal ivaVal = base.multiply(taxPct).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

            subtotalBruto = subtotalBruto.add(bruto);
            totalDescuentos = totalDescuentos.add(descVal);
            totalImpuestos = totalImpuestos.add(ivaVal);

            lines.add(new BigDecimal[]{
                new BigDecimal(codProd), precio, new BigDecimal(cant),
                descPct, descVal, base, taxPct, ivaVal
            });
        }

        BigDecimal subtotal = subtotalBruto.subtract(totalDescuentos);
        BigDecimal totalFinal = total != null ? total : subtotal.add(totalImpuestos);

        Integer invoiceId = invoiceService.createInvoice(
            cod_cliente, usuarioId, cod_empresa, cod_metodo_pago, cod_pago,
            fecha, totalFinal, subtotal, totalDescuentos, totalImpuestos,
            tipo_factura, observaciones, fecha_vencimiento
        );

        // Create detail lines
        for (int i = 0; i < lines.size(); i++) {
            BigDecimal[] line = lines.get(i);
            invoiceService.createInvoiceDetail(
                invoiceId,
                line[0].intValue(),    // cod_producto
                line[2].intValue(),    // cantidad
                line[1],               // precio_unitario
                line[5],               // subtotal (base gravable)
                line[3],               // descuento_porcentaje
                line[4],               // descuento_valor
                line[6],               // impuesto_porcentaje
                line[7]                // impuesto_valor
            );
        }

        return "redirect:/invoice/" + invoiceId;
    }

    @GetMapping("/{id}")
    public String viewInvoice(@PathVariable Integer id, Model model) {
        Map<String, Object> invoice = invoiceService.getInvoiceById(id);
        if (invoice == null) return "redirect:/invoice";
        model.addAttribute("invoice", invoice);
        model.addAttribute("details", invoiceService.getInvoiceDetails(id));
        model.addAttribute("pagos_factura", pagoFacturaService.getAll());
        return "invoice/view";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Integer id, @RequestParam Integer cod_pago) {
        invoiceService.updateInvoiceStatus(id, cod_pago);
        return "redirect:/invoice/" + id;
    }

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

    @GetMapping("/delete/{id}")
    public String deleteInvoice(@PathVariable Integer id) {
        invoiceService.deleteInvoice(id);
        return "redirect:/invoice";
    }
}
